package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class BotService {

    // Hàm chính: Tìm nước đi "Thánh Chặn"
    public int[] findBestMove(int[][] board, int botVal, int humanVal) {

        // 1. ƯU TIÊN TUYỆT ĐỐI: Nếu Bot có cơ hội thắng ngay (4 con hoặc 3 con thoáng),
        // đánh luôn để kết liễu.
        int[] winningMove = findWinningMove(board, botVal);
        if (winningMove != null)
            return winningMove;

        // 2. CHỨC NĂNG CHÍNH: CHẶN NGƯỜI CHƠI
        // Tìm xem nếu người chơi đánh vào đâu thì sẽ tạo thành chuỗi nguy hiểm nhất
        int[] blockingMove = findBlockingMove(board, humanVal);
        if (blockingMove != null)
            return blockingMove;

        // 3. Nếu không có gì nguy hiểm, đánh vào giữa hoặc random
        if (board[2][2] == 0)
            return new int[] { 2, 2 };
        return findRandomMove(board);
    }

    // Tìm nước đi để thắng ngay lập tức
    private int[] findWinningMove(int[][] board, int playerVal) {
        // Duyệt tất cả ô trống, thử đánh vào, nếu thắng thì chọn luôn
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                if (board[r][c] == 0) {
                    board[r][c] = playerVal; // Thử đánh
                    if (checkWinDeep(board, r, c, playerVal)) { // Nếu thắng
                        board[r][c] = 0; // Trả lại như cũ
                        return new int[] { r, c };
                    }
                    board[r][c] = 0; // Trả lại như cũ
                }
            }
        }
        return null;
    }

    private int[] findBlockingMove(int[][] board, int humanVal) {
        int[] bestBlock = null;
        int maxDanger = 0; // Mức độ nguy hiểm

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                if (board[r][c] == 0) {
                    // Giả sử người chơi đánh vào ô này
                    board[r][c] = humanVal;

                    // Tính xem nước này tạo ra chuỗi bao nhiêu con liên tiếp
                    int dangerLevel = getMaxConsecutive(board, r, c, humanVal);

                    board[r][c] = 0; // Trả lại

                    // LOGIC CHẶN CỦA BẠN Ở ĐÂY:
                    // Nếu nước đi này tạo thành 3 con liên tiếp (tức là đã có 2 con rồi) -> Mức
                    // nguy hiểm cao
                    // Nếu nước đi này tạo thành 4 con liên tiếp (tức là đã có 3 con rồi) -> Mức
                    // nguy hiểm cực cao

                    if (dangerLevel > maxDanger) {
                        maxDanger = dangerLevel;
                        bestBlock = new int[] { r, c };
                    }
                }
            }
        }
// Logic của bạn: "Nếu có 2 ô liên tiếp (tức dangerLevel >= 3) là chặn ngay"
        // Ở đây mình để >= 3 vì: 2 con cũ + 1 con mới đặt thử = 3 con.
        if (maxDanger >= 3) {
            return bestBlock;
        }

        return null; // Không có gì quá nguy hiểm (chưa được 2 con liên tiếp)
    }

    // Hàm đếm số quân liên tiếp lớn nhất tại vị trí vừa đánh
    private int getMaxConsecutive(int[][] board, int r, int c, int val) {
        return Math.max(Math.max(
                countDirection(board, r, c, val, 0, 1), // Ngang
                countDirection(board, r, c, val, 1, 0)), // Dọc
                Math.max(
                        countDirection(board, r, c, val, 1, 1), // Chéo chính
                        countDirection(board, r, c, val, 1, -1)) // Chéo phụ
        );
    }

    private int countDirection(int[][] board, int r, int c, int val, int dRow, int dCol) {
        int count = 1; // Tính cả quân vừa đánh
        // Duyệt chiều dương
        int i = 1;
        while (isValid(r + dRow * i, c + dCol * i) && board[r + dRow * i][c + dCol * i] == val) {
            count++;
            i++;
        }
        // Duyệt chiều âm
        i = 1;
        while (isValid(r - dRow * i, c - dCol * i) && board[r - dRow * i][c - dCol * i] == val) {
            count++;
            i++;
        }
        return count;
    }

    private int[] findRandomMove(int[][] board) {
        List<int[]> moves = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (board[i][j] == 0)
                    moves.add(new int[] { i, j });
            }
        }
        if (moves.isEmpty())
            return new int[] { -1, -1 };
        return moves.get(new Random().nextInt(moves.size()));
    }

    // --- LOGIC CHECK WIN (Copy y nguyên từ GameService để Bot biết luật thắng) ---
    private boolean checkWinDeep(int[][] board, int r, int c, int val) {
        // Logic giống hệt GameService để Bot nhận diện được nước thắng
        return checkDir(board, r, c, val, 0, 1) || checkDir(board, r, c, val, 1, 0) ||
                checkDir(board, r, c, val, 1, 1) || checkDir(board, r, c, val, 1, -1);
    }

    private boolean checkDir(int[][] board, int r, int c, int val, int dRow, int dCol) {
        int count = 1;
        int nextR = r + dRow, nextC = c + dCol;
        while (isValid(nextR, nextC) && board[nextR][nextC] == val) {
            count++;
            nextR += dRow;
            nextC += dCol;
        }
        int headVal = isValid(nextR, nextC) ? board[nextR][nextC] : -1;

        int prevR = r - dRow, prevC = c - dCol;
        while (isValid(prevR, prevC) && board[prevR][prevC] == val) {
            count++;
            prevR -= dRow;
            prevC -= dCol;
        }
        int tailVal = isValid(prevR, prevC) ? board[prevR][prevC] : -1;

        if (count >= 4)
return true;
        if (count == 3) {
            if (headVal == 0 && tailVal == 0)
                return true;
        }
        return false;
    }

    private boolean isValid(int r, int c) {
        return r >= 0 && r < 5 && c >= 0 && c < 5;
    }
}
