# 🎮 Tic Tac Toe Online - Backend (Spring Boot)
Lenh Chay: mvn spring-boot:run
## 🧩 Giới thiệu
Backend của dự án **Tic Tac Toe Online**, viết bằng **Java Spring Boot**, cung cấp các API RESTful và WebSocket để:
- Quản lý người dùng (JWT Auth)
- Tạo / Tham gia phòng chơi
- Gửi – Nhận nước đi realtime
- Lưu lịch sử trận đấu vào MySQL

---

## ⚙️ Công nghệ sử dụng
| Thành phần | Công nghệ |
|-------------|------------|
| Ngôn ngữ | Java 17+ |
| Framework | Spring Boot 3.x |
| Realtime | WebSocket (Spring Messaging / STOMP) |
| CSDL | MySQL |
| Bảo mật | Spring Security + JWT |
| Công cụ build | Maven |
| API Docs | Swagger 3 (SpringDoc OpenAPI) |

---

## 🧱 Cấu trúc thư mục
src/
├─ controller/
│ ├─ AuthController.java
│ ├─ RoomController.java
│ └─ GameController.java
├─ model/
│ ├─ User.java
│ ├─ Room.java
│ ├─ GameHistory.java
│ └─ Move.java
├─ repository/
│ ├─ UserRepository.java
│ ├─ RoomRepository.java
│ └─ GameHistoryRepository.java
├─ service/
│ ├─ UserService.java
│ ├─ RoomService.java
│ ├─ GameService.java
├─ security/
│ ├─ JwtService.java
│ ├─ JwtAuthFilter.java
│ └─ SecurityConfig.java
├─ websocket/
│ ├─ WebSocketConfig.java
│ └─ GameSocketHandler.java
└─ DemoApplication.java