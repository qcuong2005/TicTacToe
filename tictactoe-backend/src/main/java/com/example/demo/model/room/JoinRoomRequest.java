package com.example.demo.model.room;

public class JoinRoomRequest {
    private String identifier; // Có thể nhập ID (3 số) hoặc Tên phòng

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}