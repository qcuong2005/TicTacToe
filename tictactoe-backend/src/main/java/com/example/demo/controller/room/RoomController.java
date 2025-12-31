package com.example.demo.controller.room;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.room.CreateRoomRequest;
import com.example.demo.model.room.JoinRoomRequest;
import com.example.demo.model.room.RoomEntity;
import com.example.demo.repository.room.RoomRepository;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    // ✅ TẠO PHÒNG (Tên phòng tự đặt, Người tạo là Player 1)
    @PostMapping("/create")
    public ResponseEntity<?> createRoom(@RequestBody CreateRoomRequest req) {

        // 1. Lấy tên người dùng hiện tại từ Token (để làm chủ phòng)
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        RoomEntity room = new RoomEntity();

        // 2. Lấy tên phòng từ người dùng nhập
        // Kiểm tra nếu người dùng không nhập tên thì báo lỗi hoặc đặt mặc định
        if (req.getRoomName() == null || req.getRoomName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tên phòng không được để trống"));
        }
        room.setRoomName(req.getRoomName());

        // 3. Gán người tạo vào ghế Player 1 luôn
        room.setPlayer1(currentUsername);
        room.setPlayer2(null);
        room.setStatus("waiting");

        // 4. Sinh mã 3 số ngẫu nhiên
        String randomCode;
        do {
            int num = new Random().nextInt(900) + 100;
            randomCode = String.valueOf(num);
        } while (roomRepository.existsByRoomCode(randomCode));

        room.setRoomCode(randomCode);
        roomRepository.save(room);

        return ResponseEntity.ok(Map.of(
                "message", "Tạo phòng '" + room.getRoomName() + "' thành công!",
                "roomCode", room.getRoomCode(),
                "roomName", room.getRoomName(),
                "player1", room.getPlayer1(),
                "status", room.getStatus()));
    }

    // ✅ VÀO PHÒNG (Chỉ cần mã phòng, tên user tự lấy từ Token)
    @PostMapping("/join")
    public ResponseEntity<?> joinRoom(@RequestBody JoinRoomRequest req) {

        // 1. Lấy tên người dùng hiện tại từ Token
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Tìm phòng theo Code hoặc Tên (chỉ tìm phòng chưa Full)
        Optional<RoomEntity> roomOpt = roomRepository.findRoomToJoin(req.getIdentifier());

        if (roomOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Không tìm thấy phòng hoặc phòng đã đầy"));
        }

        RoomEntity room = roomOpt.get();

        // 3. Logic xếp chỗ ngồi
        if (room.getPlayer1() == null) {
            // Trường hợp phòng trống trơn (chưa ai vào)
            room.setPlayer1(currentUser);
            room.setStatus("waiting");
            roomRepository.save(room);

            return ResponseEntity.ok(Map.of(
                    "message", "Bạn đã vào phòng (Player 1)",
                    "role", "Player 1",
                    "room", room));

        } else if (room.getPlayer2() == null) {
            // Trường hợp đã có 1 người, mình là người thứ 2

            // Check: Không cho phép tự mình vào phòng của mình 2 lần
            if (room.getPlayer1().equals(currentUser)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bạn đang ở trong phòng này rồi"));
            }

            room.setPlayer2(currentUser);
            room.setStatus("full"); // Đủ người -> Full
            roomRepository.save(room);

            return ResponseEntity.ok(Map.of(
                    "message", "Bạn đã vào phòng (Player 2)",
                    "role", "Player 2",
                    "room", room));

        } else {
            // Phòng đã có đủ 2 người (Dự phòng, dù câu query findRoomToJoin đã lọc rồi)
            return ResponseEntity.badRequest().body(Map.of("error", "Phòng đã đầy"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllRooms() {
        return ResponseEntity.ok(roomRepository.findAll());
    }
}