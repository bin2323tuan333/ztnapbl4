# ZTNA Project - PBL4

Zero Trust Network Access (ZTNA) - Đồ án PBL4 Hệ điều hành và Mạng máy tính.

## Thành phần

- **ztna-server/** — ZTNA Server (Spring Boot + Netty)
- **ztna-connector/** — ZTNA Connector (Java + Netty)

## Kiến trúc

User ──WSS──> ZTNA Server <──WSS── Connector ──TCP──> App nội bộ

## Cách chạy

### 1. ZTNA Server

### 2. ZTNA Connector

cd ztna-server
./mvnw spring-boot:run

Server listen:
- Port 8080 — REST API
- Port 8443 — WebSocket

### 2. ZTNA Connector

cd ztna-connector
./mvnw clean package
java -jar target/ztna-connector-0.0.1-SNAPSHOT.jar


## Công nghệ

- Java 21
- Spring Boot 4.1.1
- Netty 4.1.115
- JWT

## Tác giả

