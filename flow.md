# Các luồng xử lý của hệ thống (Flows)

## 1. Luồng đăng nhập (cấp JWT + session key)

```mermaid
sequenceDiagram
    autonumber
    actor U as User
    participant AG as Client Agent<br/>(portal-ui / CLI)
    participant GW as ztna-server
    participant DB as SQLite

    U->>AG: Nhập username + password
    AG->>GW: POST /api/auth/login {username, password}
    GW->>DB: Tìm user, so khớp bcrypt(password)
    alt Sai thông tin / tài khoản bị khóa
        GW->>DB: INSERT login_logs (success=0, lý do)
        GW-->>AG: 401/403 {error}
        AG-->>U: Hiển thị lỗi đăng nhập
    else Đúng thông tin
        GW->>GW: Tạo session_id (UUID) + session_key (AES-256 ngẫu nhiên)
        GW->>DB: INSERT sessions (id, user_id, session_key, expires_at)
        GW->>GW: Ký JWT {sub, username, role, jti=session_id} (HS256)
        GW->>DB: INSERT login_logs (success=1)
        GW-->>AG: 200 {token(JWT), sessionKey, user, services[]}
        AG->>AG: Lưu token + sessionKey (localStorage / file .ztna-session.json)
        AG-->>U: Vào dashboard, hiển thị danh mục dịch vụ
    end
```

**Kết quả của bước đăng nhập:** client có 2 thứ — `token` (JWT, chứng minh danh tính cho *mỗi* gói tin) và `sessionKey` (khóa AES-256 dùng mã hóa *toàn bộ dữ liệu* của phiên).

## 2. Luồng truy cập dịch vụ (bọc gói tin → giải mã → xác thực → forward → response theo đường cũ)

```mermaid
sequenceDiagram
    autonumber
    actor U as User
    participant AG as Client Agent
    participant GW as ztna-server
    participant DB as SQLite
    participant CN as connector
    participant AP as backend-app

    U->>AG: Chọn service + method + path (+ body)
    AG->>AG: inner = {service, method, path, body, timestamp, nonce}
    AG->>AG: AES-256-GCM(sessionKey, inner)<br/>(bọc dữ liệu, JWT nằm NGOÀI)
    AG->>GW: WS /ws ← {type:'ztna-request', requestId, token, iv, ciphertext}

    GW->>GW: Verify JWT (chữ ký, hạn dùng)
    GW->>DB: Kiểm tra session (jti) còn hiệu lực, chưa bị thu hồi
    GW->>GW: Giải mã ciphertext bằng session_key của phiên
    GW->>GW: Kiểm tra timestamp (chống replay, skew ≤ 5 phút)
    GW->>DB: Kiểm tra policy: role có quyền vào service?
    GW->>DB: INSERT access_logs (ALLOW)
    GW->>CN: POST /forward {requestId, service, method, path, body, user}<br/>+ ký HMAC-SHA256(timestamp, body)

    CN->>CN: Verify chữ ký HMAC + timestamp (skew ≤ 2 phút)
    CN->>CN: Tra routes: service → upstream URL
    CN->>AP: HTTP {method} {path} {body}<br/>+ header X-ZTNA-User (định danh đã xác thực)
    AP->>AP: Kiểm tra X-ZTNA-User, xử lý nghiệp vụ
    AP-->>CN: Response {status, data}
    CN-->>GW: {status, data}

    GW->>GW: Mã hóa response bằng session_key (AES-256-GCM)
    GW-->>AG: WS ← {type:'ztna-response', requestId, iv, ciphertext}
    AG->>AG: Giải mã bằng sessionKey
    AG-->>U: Hiển thị kết quả
```

## 3. Các luồng từ chối (DENY)

| # | Tình huống | Điểm chặn | Kết quả | Log |
|---|---|---|---|---|
| 1 | Sai username/password | ztna-server `/api/auth/login` | HTTP 401 | `login_logs(success=0)` |
| 2 | JWT giả mạo / hết hạn | WS gateway, bước verify JWT | `{type:'error', status:401}` | — (chưa giải mã được nên chưa có nội dung để log truy cập) |
| 3 | Session bị thu hồi / hết hạn | WS gateway, kiểm tra `sessions` | `{type:'error', status:401}` | — |
| 4 | Gói tin bị sửa đổi / sai session key | WS gateway, giải mã AES-GCM (auth tag lỗi) | HTTP 400 (bọc, mã hóa) | `access_logs(result='DENY', reason='Giải mã thất bại')` |
| 5 | Timestamp ngoài ngưỡng (replay) | WS gateway | HTTP 400 (bọc) | `access_logs(result='DENY', reason='Timestamp ngoài ngưỡng')` |
| 6 | Không có quyền vào service | WS gateway, kiểm tra `policies` | HTTP 403 (bọc) | `access_logs(result='DENY', reason='Không có quyền truy cập service')` |
| 7 | Gọi connector mà không có/sai chữ ký HMAC | connector `/forward` | HTTP 401 | log console của connector |
| 8 | Gọi thẳng backend app bỏ qua ZTNA | middleware của backend app | HTTP 403 | log console của app |

## 4. Luồng admin xem log

```mermaid
sequenceDiagram
    autonumber
    actor A as Admin
    participant UI as portal-ui
    participant GW as ztna-server
    participant DB as SQLite

    A->>UI: Mở tab "Nhật ký"
    UI->>GW: GET /api/logs/access (Bearer JWT)
    GW->>GW: Verify JWT + kiểm tra role = 'admin'
    GW->>DB: SELECT access_logs JOIN users (200 dòng mới nhất)
    GW-->>UI: {logs[]}
    UI-->>A: Bảng log (dòng DENY tô đỏ)
```

## 5. Định dạng gói tin tổng hợp

### Gói tin đi (client → ztna-server, qua WebSocket)

```json
{
  "type": "ztna-request",
  "requestId": "7a8f3a81-690a-4c54-bbf5-f72a7aa56017",
  "token": "<JWT HS256: {sub, username, role, jti}>",
  "iv": "WTJHhuoL583ET66e",
  "ciphertext": "<base64: AES-256-GCM(inner) + auth tag 16 byte nối cuối>"
}
```

### Nội dung bên trong (sau khi giải mã)

```json
{
  "service": "app1",
  "method": "GET",
  "path": "/api/students",
  "body": null,
  "timestamp": 1759576399000,
  "nonce": "a1b2c3d4"
}
```

### Gói tin về (ztna-server → client)

```json
{
  "type": "ztna-response",
  "requestId": "7a8f3a81-690a-4c54-bbf5-f72a7aa56017",
  "iv": "...",
  "ciphertext": "<base64: AES-256-GCM({status, data}) + tag>"
}
```

`requestId` (UUID) dùng ghép cặp request–response trên cùng một kết nối WebSocket, cho phép gửi nhiều request đồng thời.
