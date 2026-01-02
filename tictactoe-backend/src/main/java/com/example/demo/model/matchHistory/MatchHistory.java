package com.example.demo.model.matchHistory;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "match_history")
public class MatchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomCode; // Mã phòng (để tìm kiếm chính xác)
    private String roomName; // Tên phòng (để hiển thị)
    private String player1;
    private String player2;
    private String winner; // Tên người thắng hoặc "DRAW"
    private LocalDateTime playedAt; // Thời gian kết thúc

    public MatchHistory() {
    }

    public MatchHistory(String roomCode, String roomName, String player1, String player2, String winner) {
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.player1 = player1;
        this.player2 = player2;
        this.winner = winner;
        this.playedAt = LocalDateTime.now();
    }

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public String getWinner() {
        return winner;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }
}