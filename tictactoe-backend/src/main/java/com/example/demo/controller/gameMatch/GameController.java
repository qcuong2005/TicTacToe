package com.example.demo.controller.gameMatch;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.demo.model.gameMatch.GameMatch;
import com.example.demo.model.gameMatch.MoveMessage;
import com.example.demo.service.GameService;

@Controller
public class GameController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // Dùng để bắn tin nhắn về Client

    @Autowired
    private GameService gameService;

    // Client sẽ gửi đến: /app/move
    @MessageMapping("/move")
    public void processMove(@Payload MoveMessage move) {
        System.out.println("Nhận nước đi: " + move.getX() + ", " + move.getY() + " từ " + move.getPlayer());

        // 1. Cập nhật bàn cờ và check win trong Service
        GameMatch updatedMatch = gameService.makeMove(
                move.getRoomId(),
                move.getPlayer(),
                move.getX(),
                move.getY());

        // 2. Gửi trạng thái mới nhất cho CẢ 2 người chơi trong phòng
        // Client phải subscribe vào: /topic/room/{roomId}
        messagingTemplate.convertAndSend("/topic/room/" + move.getRoomId(), updatedMatch);
    }

    @MessageMapping("/restart")
    public void restartGame(@Payload Map<String, String> payload) {
        String roomId = payload.get("roomId");

        // 1. Gọi Service để reset bàn cờ
        GameMatch newMatch = gameService.resetGame(roomId);

        // 2. Bắn bàn cờ mới tinh về cho CẢ 2 người chơi
        if (newMatch != null) {
            messagingTemplate.convertAndSend("/topic/room/" + roomId, newMatch);
        }
    }
}