package com.example.demo.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.gameMatch.GameMatch;
import com.example.demo.model.matchHistory.MatchHistory;
import com.example.demo.model.room.RoomEntity;
import com.example.demo.repository.matchHistory.MatchHistoryRepository;
import com.example.demo.repository.room.RoomRepository;

@Service
public class GameService {

    @Autowired
    private MatchHistoryRepository matchHistoryRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BotService botService; // Inject Bot Service

    // Lưu trạng thái game trong RAM
    private final Map<String, GameMatch> games = new ConcurrentHashMap<>();

    // 1. Tạo hoặc lấy game
    public GameMatch createOrGetGame(String roomId, String p1) {
        games.computeIfAbsent(roomId, k -> {
            GameMatch match = new GameMatch();
            match.setRoomId(roomId);
            match.setPlayer1(p1);
            match.setCurrentTurn(p1);
            return match;
        });
        return games.get(roomId);
    }

    // 2. Reset Game
    public GameMatch resetGame(String roomId) {
        GameMatch match = games.get(roomId);
        if (match != null) {
            match.setBoard(new int[5][5]);
            match.setWinner(null);
            match.setCurrentTurn(match.getPlayer1());
        }
        return match;
    }

    // 3. XỬ LÝ NƯỚC ĐI (Gộp cả Người và Bot)
    public GameMatch makeMove(String roomId, String player, int x, int y) {
        GameMatch match = games.get(roomId);

        // Validations cơ bản
        if (match == null || match.getWinner() != null)
            return match;
        if (!player.equals(match.getCurrentTurn()))
            return match;

        int[][] board = match.getBoard();
        if (board[x][y] != 0)
            return match;

        // --- BƯỚC 1: NGƯỜI CHƠI ĐÁNH ---
        int playerVal = player.equals(match.getPlayer1()) ? 1 : 2;
        board[x][y] = playerVal;

        boolean gameEnded = false;

        // Check Người thắng
        if (checkWinDeep(board, x, y, playerVal)) {
            match.setWinner(player);
            gameEnded = true;
        } else if (isBoardFull(board)) {
            match.setWinner("DRAW");
            gameEnded = true;
        } else {
            // Đổi lượt tạm thời
            String next = player.equals(match.getPlayer1()) ? match.getPlayer2() : match.getPlayer1();
            match.setCurrentTurn(next);
        }

        // Lưu lịch sử nếu Người thắng/Hòa
        if (gameEnded) {
            saveHistory(match);
            return match; // Kết thúc hàm luôn
        }

        // --- BƯỚC 2: BOT ĐÁNH (Nếu đối thủ là BOT và game chưa kết thúc) ---
if ("BOT".equals(match.getPlayer2())) {

            // Bot tính toán (Bot là Player 2 -> val=2, Người là Player 1 -> val=1)
            int[] botMove = botService.findBestMove(match.getBoard(), 2, 1);
            int bx = botMove[0];
            int by = botMove[1];

            if (bx != -1) {
                // Bot đánh
                board[bx][by] = 2;

                // Check Bot thắng
                if (checkWinDeep(board, bx, by, 2)) {
                    match.setWinner("BOT");
                    saveHistory(match);
                } else if (isBoardFull(board)) {
                    match.setWinner("DRAW");
                    saveHistory(match);
                } else {
                    // Trả lượt lại cho Người chơi 1
                    match.setCurrentTurn(match.getPlayer1());
                }
            }
        }

        return match;
    }

    // 4. Lưu lịch sử
    private void saveHistory(GameMatch match) {
        String roomName = "Unknown";
        Optional<RoomEntity> roomOpt = roomRepository.findByRoomCode(match.getRoomId());
        if (roomOpt.isPresent()) {
            roomName = roomOpt.get().getRoomName();
        }

        MatchHistory history = new MatchHistory(
                match.getRoomId(),
                roomName,
                match.getPlayer1(),
                match.getPlayer2(),
                match.getWinner());
        matchHistoryRepository.save(history);
        System.out.println("✅ Đã lưu lịch sử: " + roomName + " - Winner: " + match.getWinner());
    }

    // --- CÁC HÀM LOGIC GAME ---

    private boolean isBoardFull(int[][] board) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (board[i][j] == 0)
                    return false;
            }
        }
        return true;
    }

    private boolean checkWinDeep(int[][] board, int r, int c, int val) {
        return checkDirection(board, r, c, val, 0, 1) ||
                checkDirection(board, r, c, val, 1, 0) ||
                checkDirection(board, r, c, val, 1, 1) ||
                checkDirection(board, r, c, val, 1, -1);
    }

    private boolean checkDirection(int[][] board, int r, int c, int val, int dRow, int dCol) {
        int count = 1;
        int nextR = r + dRow;
        int nextC = c + dCol;
        while (isValid(nextR, nextC) && board[nextR][nextC] == val) {
            count++;
            nextR += dRow;
            nextC += dCol;
        }
        int headVal = isValid(nextR, nextC) ? board[nextR][nextC] : -1;

        int prevR = r - dRow;
        int prevC = c - dCol;
        while (isValid(prevR, prevC) && board[prevR][prevC] == val) {
            count++;
            prevR -= dRow;
            prevC -= dCol;
        }
        int tailVal = isValid(prevR, prevC) ? board[prevR][prevC] : -1;

        if (count >= 4)
            return true;
        if (count == 3) {
            boolean blockedHead = (headVal != 0);
boolean blockedTail = (tailVal != 0);
            if (!blockedHead && !blockedTail)
                return true;
        }
        return false;
    }

    private boolean isValid(int r, int c) {
        return r >= 0 && r < 5 && c >= 0 && c < 5;
    }
}
