# ZTNA Project – PBL4

**Zero Trust Network Access (ZTNA)** – Đồ án PBL4 môn Hệ điều hành và Mạng máy tính.

Hệ thống xây dựng mô hình kiểm soát truy cập tài nguyên nội bộ dựa trên nguyên tắc Zero Trust. Mọi yêu cầu truy cập đều phải đi qua ZTNA Server để xác thực danh tính, kiểm tra quyền và thiết lập kết nối tới tài nguyên được cấp phép.

## 1. Tổng quan hệ thống

Hệ thống gồm ba thành phần chính:

| Thành phần | Thư mục | Vai trò |
|---|---|---|
| Agent | `agent/` | Chạy phía máy người dùng, gửi yêu cầu truy cập và chuyển tiếp dữ liệu TCP |
| ZTNA Server | `ztna-server/` | Xác thực, phân quyền, quản lý Connector, routing, session và relay dữ liệu |
| Connector | `connector/` | Kết nối tới Server và mở kết nối TCP tới tài nguyên nội bộ |

**Lưu ý:** Root `pom.xml` khai báo ba Maven module `agent`, `ztna-server` và `connector`. Giao diện web nằm ở `ztna-portal-ui/` (React + Vite).

## 2. Kiến trúc tổng quan

```text
       MÁY NGƯỜI DÙNG                         MẠNG NỘI BỘ

+---------------------+
|      Application    |
+----------+----------+
           |
           | TCP
           |
+----------v----------+
|        Agent        |
+----------+----------+
           |
           | WebSocket
           |
+----------v--------------------------------+
|               ZTNA Server                 |
|                                           |
|  +----------------+  +----------------+   |
|  | Authentication |  | Authorization  |   |
|  +----------------+  +----------------+   |
|                                           |
|  +----------------+  +----------------+   |
|  | Routing        |  | SessionManager |   |
|  +----------------+  +----------------+   |
|                                           |
|             RelayEngine                   |
+---------------------+---------------------+
                      ^
                      |
                      | WebSocket
                      |
             +--------+---------+
             |     Connector   |
             +--------+---------+
                      |
                      | TCP
                      |
             +--------v---------+
             | Internal Resource|
             +------------------+
```

Agent và Connector đều kết nối tới ZTNA Server thông qua WebSocket.

Connector chủ động thiết lập kết nối outbound tới Server. Khi có yêu cầu truy cập hợp lệ, Server yêu cầu Connector mở kết nối TCP tới tài nguyên nội bộ.

Agent không cần kết nối trực tiếp tới mạng nội bộ.

## 3. Nguyên lý hoạt động

Quy trình truy cập tài nguyên gồm các bước:

1. Agent thiết lập kết nối WebSocket tới ZTNA Server.
2. Connector thiết lập kết nối WebSocket tới ZTNA Server.
3. Agent gửi yêu cầu truy cập một resource.
4. Server xác thực danh tính người dùng và kiểm tra quyền truy cập.
5. Server xác định Connector có khả năng phục vụ resource.
6. Server tạo `streamId` và gửi frame `CONNECT` tới Connector.
7. Connector mở kết nối TCP tới ứng dụng nội bộ.
8. Connector phản hồi bằng `CONNECT_ACK`.
9. Server tạo session và thông báo kết quả cho Agent.
10. Agent và Connector truyền dữ liệu hai chiều thông qua các frame `DATA`.
11. Khi kết thúc, frame `CLOSE` được sử dụng để đóng stream và giải phóng tài nguyên.

## 4. Protocol

Hệ thống sử dụng binary frame để giao tiếp giữa Agent, Server và Connector.

Các frame chính:

| Frame | Chức năng |
|---|---|
| `HTTP_ACCESS` | Agent gửi yêu cầu xác thực/truy cập HTTP |
| `HTTP_RESPONSE` | Server trả kết quả xử lý |
| `RESOURCE_ACCESS` | Agent yêu cầu mở resource TCP |
| `RESOURCE_RESPONSE` | Server trả kết quả mở resource |
| `CONNECT` | Server yêu cầu Connector mở TCP |
| `CONNECT_ACK` | Connector phản hồi kết quả kết nối |
| `DATA` | Truyền dữ liệu hai chiều |
| `CLOSE` | Đóng stream |
| `ROUTING_REQUEST` | Yêu cầu đồng bộ routing |
| `ROUTING_RESPONSE` | Connector phản hồi routing |

Chi tiết protocol được định nghĩa trong các class thuộc package `protocol` của từng module.

## 5. Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 21 |
| Build | Maven |
| Backend | Spring Boot |
| Security | Spring Security, JWT |
| Network I/O | Netty |
| Communication | WebSocket, TCP |
| ORM | Spring Data JPA |
| Database | MySQL |

## 6. Cấu trúc dự án

```text
ztnapbl4/
│
├── agent/
│   └── src/
│
├── ztna-server/
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   └── resources/
│       └── test/
│
├── connector/
│   └── src/
│
├── ztna-portal-ui/
│   └── Frontend (React + Vite)
│
├── database/
│   └── schema.sql      (cấu trúc bảng MySQL)
│
├── docker-compose.yml  (MySQL + Server + giao diện cho máy Server)
├── .env.example        (mẫu biến môi trường)
├── README.md
└── pom.xml
```

## 7. Cổng và biến môi trường

| Thành phần | Cổng | Ghi chú |
|---|---|---|
| ZTNA Server – REST API | 443 (HTTP thường) | Giao diện web gọi `http://<hostname>:443` |
| ZTNA Server – WS cho Connector | 8443, đường dẫn `/connector` | |
| ZTNA Server – WS cho Agent | 9443, đường dẫn `/app` | |
| Giao diện web | 3000 | `npm run dev` hoặc container `web` |
| Agent – HTTP nội bộ | 127.0.0.1:9999 | Portal gọi `/open?appId=&type=` để mở ứng dụng |

Server đọc cấu hình từ biến môi trường, mỗi biến có giá trị mặc định cho máy dev (xem `.env.example`):

| Biến | Mặc định | Ý nghĩa |
|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME` | `localhost`, `3306`, `ztna_db` | MySQL. Database tự tạo nếu chưa có |
| `DB_USERNAME`, `DB_PASSWORD` | `root`, giá trị trong `application.yaml` | XAMPP mặc định: `DB_PASSWORD` để trống |
| `JWT_SECRET` | (trống) | Khoá ký JWT, ≥ 32 ký tự. Trống thì server tự sinh ngẫu nhiên mỗi lần khởi động |
| `SERVER_PORT` | `443` | Cổng REST API |

Agent: địa chỉ Server lấy theo thứ tự tham số dòng lệnh → biến `SERVER_URL` → `ws://localhost:9443/app`.
Connector: biến `SERVER_URL` (vd `ws://<IP-Server>:8443/connector`) và `CONNECTOR_TOKEN` (Connection String của connector, xem trên trang Connectors).

Tài khoản và connector mẫu được tạo lần chạy đầu trong `ztna-server/src/main/java/com/example/ztna_server/config/DataInit.java`.

## 8. Chạy demo từng bước (LAN)

Mô hình: 1 máy Server (MySQL + Server + giao diện web), các máy khác chạy Agent hoặc Connector. Thay `<IP-Server>` bằng IP LAN của máy Server (`ipconfig`). Mở firewall Windows cho cổng 443, 8443, 9443, 3000 trên máy Server.

### Cách A – Máy Server chạy bằng Docker

```bash
copy .env.example .env      # sửa DB_PASSWORD và JWT_SECRET
docker compose up -d --build
```

Mở `http://<IP-Server>:3000`. Dữ liệu MySQL nằm trong volume `ztna_mysql`.

### Cách B – Chạy tay (Eclipse + XAMPP + VS Code)

1. Bật MySQL trong XAMPP.
2. Eclipse: Import → Existing Maven Projects → chọn thư mục gốc. Chạy `ZtnaServerApplication` (Run As → Java Application).
   Nếu MySQL của XAMPP không có mật khẩu: Run Configurations → Environment → thêm `DB_PASSWORD` với giá trị rỗng
   (hoặc tab Arguments, ô Program arguments: `--spring.datasource.password=`).
3. Giao diện: `cd ztna-portal-ui && npm ci && npm run dev`, mở `http://<IP-Server>:3000`.

### Máy chạy Connector (mạng nội bộ)

1. Trên giao diện admin, tạo connector (hoặc dùng connector mẫu), lấy Connection String.
2. Tạo các ứng dụng (Resource) trỏ tới `host:port` mà **máy connector** truy cập được.
3. Chạy:

```bash
./ztna-server/mvnw -f pom.xml -DskipTests package
set SERVER_URL=ws://<IP-Server>:8443/connector
set CONNECTOR_TOKEN=<Connection String>
java -jar connector/target/connector-1.0-SNAPSHOT.jar
```

Docker: xem `connector/Dockerfile` và các file `connector/docker-compose-*.yml` (sửa `SERVER_URL`, `CONNECTOR_TOKEN` cho đúng).

### Máy người dùng (Agent)

```bash
java -jar agent/target/agent-1.0-SNAPSHOT.jar ws://<IP-Server>:9443/app
```

Mở `http://<IP-Server>:3000`, đăng nhập bằng tài khoản người dùng, bấm mở ứng dụng. Agent mở một cổng local ngẫu nhiên và chạy trình mở ứng dụng tương ứng (Chrome cho web).

### Kịch bản kiểm tra quyền khi demo

1. Người dùng chưa có rule: mở ứng dụng bị từ chối (403), log truy cập ghi DENY.
2. Admin tạo rule cho người dùng: mở được, log ghi ALLOW, phiên hiện ở dashboard.
3. Trong lúc đang dùng, admin tắt rule hoặc khoá người dùng: phiên bị cắt ngay, token cũ không gọi được API nữa.
4. Admin tắt connector: kết nối của connector bị ngắt, connector không đăng ký lại được cho tới khi bật lại.

Lưu ý: Agent và Server phải cùng phiên bản (giao thức frame đã thay đổi).

## 9. Build

```bash
./ztna-server/mvnw -f pom.xml -DskipTests package     # server, connector, agent (máy không có mvn)
cd ztna-portal-ui && npm run build                   # giao diện
```

Jar tạo ra: `ztna-server/target/ztna-server-0.0.1-SNAPSHOT.jar`, `connector/target/connector-1.0-SNAPSHOT.jar`, `agent/target/agent-1.0-SNAPSHOT.jar` (đều chạy bằng `java -jar`).

## 10. Phạm vi và giới hạn

Đây là hệ thống mô phỏng phục vụ mục đích học tập và nghiên cứu.

Trước khi triển khai thực tế cần tiếp tục hoàn thiện:

- TLS/WSS cho các kết nối.
- Quản lý secret và credential an toàn.
- Xác thực Connector.
- Giới hạn kích thước frame và payload.
- Timeout và cleanup pending request/session.
- Xử lý reconnect khi Agent hoặc Connector mất kết nối.
- Kiểm thử các trường hợp lỗi và truy cập trái phép.