# 🎮 Caro Online - Frontend

Giao diện người dùng cho game Caro (Gomoku) đa người chơi thời gian thực.

## ✨ Tính năng

- 🔐 **Đăng ký / Đăng nhập** với JWT Authentication
- 🏠 **Lobby** - Tạo phòng, vào phòng bằng mã, xem danh sách phòng đang chờ
- 🎯 **Chơi game Caro 5x5** với luật thắng 3-4 ô liên tiếp
- ⚡ **Real-time** với WebSocket (STOMP)
- 📊 **Lịch sử đấu** - Xem lại các trận đã chơi
- 🎨 **Giao diện Premium** - Dark Neon Theme với hiệu ứng Glassmorphism

## 🛠️ Công nghệ

- **React 18** - UI Framework
- **React Router v6** - Điều hướng SPA
- **Axios** - HTTP Client
- **STOMP.js** - WebSocket Client
- **CSS3** - Animations & Glassmorphism

## 📁 Cấu trúc thư mục

```
src/
├── pages/           # Các trang chính
│   ├── Auth.js      # Đăng nhập / Đăng ký
│   ├── Lobby.js     # Sảnh chờ
│   ├── GameRoom.js  # Phòng chơi game
│   └── History.js   # Lịch sử đấu
├── services/        # API & WebSocket
│   ├── api.js       # Axios instance + interceptors
│   └── socket.js    # STOMP WebSocket service
├── styles/
│   └── global.css   # Design system & global styles
└── App.js           # Router configuration
```

## 🚀 Cài đặt & Chạy

### Yêu cầu
- Node.js 18+
- Backend đang chạy ở `http://localhost:8080`

### Cài đặt dependencies
```bash
npm install
```

### Chạy development server
```bash
npm start
```

Mở [http://localhost:3000](http://localhost:3000) để xem trong trình duyệt.

### Build production
```bash
npm run build
```

## 🔗 Kết nối Backend

Frontend mặc định kết nối tới:
- **REST API**: `http://localhost:8080/api`
- **WebSocket**: `ws://localhost:8080/ws`

Để thay đổi, sửa file `src/services/api.js` và `src/services/socket.js`.

## 🎨 Design System

| Màu | Mã |
|-----|-----|
| Neon Pink | `#ff2d75` |
| Neon Blue | `#00d4ff` |
| Neon Purple | `#a855f7` |
| Gold | `#ffd700` |
| Background | `#0a0a1a` |

## 📝 License

MIT License
