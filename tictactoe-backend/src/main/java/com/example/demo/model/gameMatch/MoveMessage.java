package com.example.demo.model.gameMatch;

public class MoveMessage {
    private String roomId;
    private String player; // Tên người đi
    private int x; // Tọa độ dòng (0-4)
    private int y; // Tọa độ cột (0-4)

    // Getter Setter
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getPlayer() {
        return player;
    }

    public String getRoomId() {
        return roomId;
    }
}