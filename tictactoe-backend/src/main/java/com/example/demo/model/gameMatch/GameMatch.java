package com.example.demo.model.gameMatch; // Hoặc package com.example.demo.model.gameMatch; tùy thư mục bạn lưu

import java.util.List;

public class GameMatch {
    private String roomId;
    private int[][] board = new int[5][5]; // 0: Trống, 1: X, 2: O
    private String player1;
    private String player2;
    private String currentTurn; // Username người đang được đi
    private String winner = null;
    private List<int[]> winningCells = null; // Danh sách các ô thắng [[r1,c1], [r2,c2], ...]

    // --- CÁC HÀM GETTER VÀ SETTER (BẮT BUỘC PHẢI CÓ) ---

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int[][] getBoard() {
        return board;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public String getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(String currentTurn) {
        this.currentTurn = currentTurn;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public List<int[]> getWinningCells() {
        return winningCells;
    }

    public void setWinningCells(List<int[]> winningCells) {
        this.winningCells = winningCells;
    }
}
