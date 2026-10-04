# Thiết kế chi tiết hệ thống ZTNA

## 1. Thiết kế mật mã

| Mục đích | Thuật toán | Khóa | Nơi dùng |
|---|---|---|---|
| Mã hóa payload gói tin | AES-256-GCM (IV 96-bit ngẫu nhiên mỗi gói) | `session_key` 256-bit, sinh khi đăng nhập, 1 phiên = 1 khóa | client agent ↔ ztna-server |
| Chứng minh danh tính mỗi lượt truy cập | JWT HS256 | `JWT_SECRET` (server-side) | client → ztna-server (mọi gói tin) |
| Xác thực kênh nội bộ | HMAC-SHA256(`timestamp.body`) | `CONNECTOR_SECRET` | ztna-server → connector |
| Lưu mật khẩu | bcrypt (cost 10) | — | ztna-server |
| Định danh phiên / request | UUID v4 | — | `sessions.id` (jti), `requestId` |

**Quy ước ciphertext:** dữ liệu mã hóa + **auth tag 16 byte nối vào cuối**, toàn bộ mã hóa base64. WebCrypto (trình duyệt) tự nối tag; Node.js tự nối khi mã hóa và tách 16 byte cuối khi giải mã — nhờ đó agent trình duyệt và agent CLI tương thích hoàn toàn.

**Vì sao GCM?** Vừa bảo mật vừa toàn vẹn: nếu gói tin bị sửa 1 bit, `setAuthTag`/decrypt sẽ fail → server phát hiện giả mạo (không chỉ là “mã hóa”).

**Chống replay:** 3 lớp — (1) `timestamp` trong phần mã hóa, lệch quá 5 phút bị từ chối; (2) `nonce` ngẫu nhiên mỗi gói; (3) kênh nội bộ dùng timestamp skew 2 phút trong chữ ký HMAC.

## 2. ztna-server (cổng 443)

```
ztna-server/
├── package.json
├── db/ztna.db              # SQLite, tự tạo khi chạy lần đầu
└── src/
    ├── config.js           # Cổng, secrets, TTL phiên, ngưỡng replay (đọc từ env)
    ├── crypto.js           # AES-256-GCM encrypt/decrypt, sinh session key, ký HMAC
    ├── db.js               # Khởi tạo schema + seed (users/services/policies)
    ├── logger.js           # logLogin / logAccess -> SQLite
    ├── auth.js             # login(), verifyToken(), isAllowed(), middleware requireAuth/requireAdmin
    ├── connectorClient.js  # POST /forward sang connector kèm chữ ký HMAC
    └── index.js            # HTTP API + WebSocket gateway /ws
```

### HTTP API

| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| GET | `/health` | — | Kiểm tra sống |
| POST | `/api/auth/login` | — | Body `{username, password}` → `{token, sessionKey, expiresIn, user, services[]}` |
| GET | `/api/services` | Bearer JWT | Danh sách service role hiện tại được phép |
| GET | `/api/logs/access` | Bearer JWT + **admin** | 200 log truy cập mới nhất (JOIN username) |
| GET | `/api/logs/login` | Bearer JWT + **admin** | 200 log đăng nhập mới nhất |

### WebSocket `/ws` — pipeline xử lý 1 gói tin

```
parse JSON → verify JWT → kiểm tra session → giải mã AES-GCM
→ kiểm tra timestamp → kiểm tra policy (policies)
→ ghi access_logs → forward connector (HMAC)
→ nhận response → ghi status/latency → mã hóa response → trả về cùng socket
```

- Mọi lỗi sau khi giải mã được trả về **dạng mã hóa** `{status, data:{error}}` trong `ztna-response`.
- Lỗi trước khi xác thực được danh tính (JWT hỏng, sai định dạng) trả về `{type:'error', message}` dạng thường.

## 3. connector (cổng 5000)

```
connector/
├── package.json
├── config.json     # port + routes: mã service -> URL nội bộ backend app
└── src/index.js    # POST /forward (verify HMAC, định tuyến, proxy request)
```

- `POST /forward`: bắt buộc header `x-ztna-timestamp` + `x-ztna-signature`. Chữ ký = `HMAC_SHA256(CONNECTOR_SECRET, timestamp + "." + rawBody)`, so khớp bằng `crypto.timingSafeEqual` (chống timing attack).
- Định tuyến theo `config.json`:
  ```json
  { "routes": { "app1": "http://localhost:5001", "app2": "http://localhost:5002" } }
  ```
- Khi gọi backend app, connector gắn:
  - `X-ZTNA-User`: base64(JSON `{id, username, role}`) — định danh **đã được ztna-server xác thực**, backend tin tưởng header này;
  - `X-ZTNA-Request-Id`: mã request để truy vết log.
- Response của backend trả ngược `{status, data}` cho ztna-server → client nhận lại **đúng đường cũ**, được ztna-server mã hóa lại.

## 4. Backend apps (5001, 5002)

Cấu trúc giống nhau; điểm chung:

- **Middleware Zero Trust**: mọi request (trừ `/health`) phải có header `X-ZTNA-User` hợp lệ, nếu không → `403`. Tức là app “ẩn”, gọi trực tiếp không có tác dụng.
- Phân quyền mức nghiệp vụ dựa trên `role` trong `X-ZTNA-User` (ví dụ app1 chỉ admin được POST/DELETE).

| App | Endpoint chính |
|---|---|
| `backend-app1` — Quản lý sinh viên | `GET /api/students`, `GET /api/students/:id`, `POST /api/students` (admin), `DELETE /api/students/:id` (admin) |
| `backend-app2` — Thư viện | `GET /api/books`, `GET /api/books/:id`, `POST /api/borrow`, `GET /api/borrows` |

Dữ liệu mẫu giữ trong RAM (mảng JS) — đủ cho demo; có thể thay bằng DB thật.

## 5. Client agent

Hai hiện thực cùng một giao thức:



## 6. portal-ui (cổng 5173)

- Dashboard liệt kê đúng các service user được phép (lấy từ response login).
- Panel request hiển thị **gói tin đã bọc** (requestId, iv, ciphertext) và **phản hồi đã giải mã** — phục vụ demo/báo cáo.
- Tab **Nhật ký** (chỉ admin): bảng log truy cập + log đăng nhập, dòng DENY/thất bại tô đỏ.

## 7. Bảng mã lỗi

| HTTP/status | Ý nghĩa | Nguồn |
|---|---|---|
| 200/201 | Thành công | mọi tầng |
| 400 | Gói tin sai định dạng / giải mã lỗi / timestamp lạ / thiếu trường | ztna-server |
| 401 | Sai mật khẩu, JWT lỗi/hết hạn, session thu hồi, sai chữ ký HMAC | ztna-server, connector |
| 403 | Không có quyền vào service; gọi thẳng backend bỏ qua ZTNA; nghiệp vụ cấm (vd user POST app1) | ztna-server, backend app |
| 404 | Không có route cho service; không tìm thấy tài nguyên | connector, backend app |
| 409 | Xung đột nghiệp vụ (hết sách) | backend app |
| 502 | Không gọi được connector / backend đích | ztna-server, connector |

## 8. Biến môi trường

| Biến | Mặc định (dev) | Ý nghĩa |
|---|---|---|
| `ZTNA_PORT` | `4000` | Cổng ztna-server |
| `JWT_SECRET` | `ztna-dev-jwt-secret-CHANGE-ME` | Khóa ký JWT — **bắt buộc đổi khi triển khai thật** |
| `JWT_EXPIRES_IN` | `1h` | Hạn JWT |
| `SESSION_TTL_MINUTES` | `60` | Hạn phiên trong DB |
| `CONNECTOR_URL` | `http://localhost:5000` | Địa chỉ connector |
| `CONNECTOR_SECRET` | `ztna-dev-connector-secret-CHANGE-ME` | Khóa HMAC kênh nội bộ (phải giống nhau ở ztna-server và connector) |
| `DB_PATH` | `ztna-server/db/ztna.db` | File SQLite |
| `MAX_PACKET_SKEW_MS` | `300000` | Ngưỡng timestamp chống replay |