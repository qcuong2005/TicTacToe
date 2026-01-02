package com.example.demo.controller.room;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate; // 1. Thêm cái này
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.gameMatch.GameMatch;
import com.example.demo.model.room.CreateRoomRequest;
import com.example.demo.model.room.JoinRoomRequest; // 2. Thêm cái này
import com.example.demo.model.room.RoomEntity;
import com.example.demo.repository.room.RoomRepository;
import com.example.demo.service.GameService;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // Dùng để bắn socket

    @Autowired
    private GameService gameService; // Dùng để khởi tạo bàn cờ trong RAM

    // ✅ SỬA HÀM CREATE ROOM
    @PostMapping("/create")
    public ResponseEntity<?> createRoom(@RequestBody CreateRoomRequest req) {

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        RoomEntity room = new RoomEntity();

        // Kiểm tra tên phòng
        String roomName = (req.getRoomName() == null || req.getRoomName().isEmpty())
                ? "Phòng của " + currentUsername
                : req.getRoomName();

        room.setRoomName(roomName);
        room.setPlayer1(currentUsername);
        room.setPlayer2(null);
        room.setStatus("waiting");

        // Sinh mã ngẫu nhiên (Logic cũ của bạn)
        // ... (Bạn tự thêm đoạn do-while randomCode ở đây nhé) ...
        // Ví dụ tạm thời để test nếu lười viết random:
        room.setRoomCode(String.valueOf(System.currentTimeMillis() % 1000));

        roomRepository.save(room);

        // --- KHỞI TẠO GAME TRONG RAM NGAY KHI TẠO PHÒNG ---
        gameService.createOrGetGame(room.getRoomCode(), room.getPlayer1());

        // --- QUAN TRỌNG: PHẢI TRẢ VỀ DỮ LIỆU ĐẦY ĐỦ ---
        return ResponseEntity.ok(Map.of(
                "message", "Tạo phòng thành công!",
                "roomCode", room.getRoomCode(), // Frontend cần cái này
                "roomName", room.getRoomName(), // Frontend cần cái này
                "player1", room.getPlayer1(),
                "status", room.getStatus()));
    }

    // ✅ SỬA HÀM JOIN ROOM: Thêm đoạn bắn thông báo Socket
    @PostMapping("/join")
    public ResponseEntity<?> joinRoom(@RequestBody JoinRoomRequest req) {

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<RoomEntity> roomOpt = roomRepository.findRoomToJoin(req.getIdentifier());

        if (roomOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy phòng hoặc phòng đã đầy"));
        }

        RoomEntity room = roomOpt.get();

        // --- LOGIC XẾP CHỖ CŨ (Giữ nguyên) ---
        if (room.getPlayer1() == null) {
            room.setPlayer1(currentUser);
            room.setStatus("waiting");
            roomRepository.save(room);
        } else if (room.getPlayer2() == null) {
            if (room.getPlayer1().equals(currentUser)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bạn đang ở trong phòng này rồi"));
            }
            room.setPlayer2(currentUser);
            room.setStatus("full"); // Phòng đầy -> Bắt đầu game
            roomRepository.save(room);
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Phòng đã đầy"));
        }

        // --- ĐOẠN MỚI THÊM: KÍCH HOẠT GAME & BẮN SOCKET ---

        // 1. Khởi tạo bàn cờ trong GameService (RAM) nếu chưa có
        GameMatch match = gameService.createOrGetGame(room.getRoomCode(), room.getPlayer1());

        // 2. Cập nhật thông tin Player 2 vào GameMatch trong RAM
        if (room.getPlayer2() != null) {
            match.setPlayer2(room.getPlayer2());
        }

        // 3. BẮN TIN NHẮN SOCKET: "Game đã cập nhật, Player 2 đã vào!"
        // Gửi đến topic mà frontend đang subscribe
        messagingTemplate.convertAndSend("/topic/room/" + room.getRoomCode(), match);

        // ----------------------------------------------------

        return ResponseEntity.ok(Map.of(
                "message", "Tham gia thành công!",
                "roomCode", room.getRoomCode(),
                "roomName", room.getRoomName(),
                "player1", room.getPlayer1(),
                "player2", (room.getPlayer2() == null ? "" : room.getPlayer2())));
    }

    // ... (Giữ nguyên getAllRooms) ...

    // ✅ API MỚI: Lấy danh sách phòng đang chờ
    @org.springframework.web.bind.annotation.GetMapping("/waiting")
    public ResponseEntity<?> getWaitingRooms() {
        java.util.List<RoomEntity> waitingRooms = roomRepository.findByStatus("waiting");
        return ResponseEntity.ok(waitingRooms);
    }
}