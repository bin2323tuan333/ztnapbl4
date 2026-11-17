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

**Lưu ý:** Root `pom.xml` khai báo ba Maven module `agent`, `ztna-server` và `connector`. Thư mục `ztna-connector/` là một implementation/prototype khác, không thuộc danh sách module build chính.

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
├── ztna-connector/
│   └── Implementation/prototype khác
│
├── ztna-portal-ui/
│   └── Frontend
│
├── README.md
├── architecture.md
├── design.md
├── flow.md
├── sql.md
└── pom.xml
```

## 7. Build project

Chạy tại thư mục gốc:

```bash
mvn clean package
```

Build riêng từng module:

```bash
mvn -pl ztna-server -am clean package

mvn -pl connector -am clean package

mvn -pl agent -am clean package
```

## 8. Thứ tự khởi chạy

1. Khởi động MySQL và cấu hình database cho ZTNA Server.
2. Khởi động ZTNA Server.
3. Khởi động Connector và cấu hình địa chỉ Server cùng các resource nội bộ.
4. Khởi động Agent và cấu hình địa chỉ Server.
5. Đăng nhập và thực hiện yêu cầu truy cập resource.

Các port và thông tin kết nối cần lấy từ cấu hình thực tế của từng module, không sử dụng port của prototype cũ.

## 9. Tài liệu thiết kế

| File | Nội dung |
|---|---|
| `architecture.md` | Kiến trúc hệ thống, thành phần và trust boundary |
| `design.md` | Thiết kế chi tiết, protocol, session và xử lý dữ liệu |
| `flow.md` | Các luồng xử lý của hệ thống |
| `sql.md` | Mô hình dữ liệu và database |

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