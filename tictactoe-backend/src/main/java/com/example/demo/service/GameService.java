package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
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

    private final Map<String, GameMatch> games = new ConcurrentHashMap<>();

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

    public GameMatch resetGame(String roomId) {
        GameMatch match = games.get(roomId);
        if (match != null) {
            match.setBoard(new int[5][5]);
            match.setWinner(null);
            match.setWinningCells(null); // Reset winning cells
            match.setCurrentTurn(match.getPlayer1());
        }
        return match;
    }

    public GameMatch makeMove(String roomId, String player, int x, int y) {
        GameMatch match = games.get(roomId);

        if (match == null || match.getWinner() != null)
            return match;

        if (!player.equals(match.getCurrentTurn()))
            return match;

        int[][] board = match.getBoard();
        if (board[x][y] != 0)
            return match;

        int playerVal = player.equals(match.getPlayer1()) ? 1 : 2;
        board[x][y] = playerVal;

        boolean gameEnded = false;

        // Kiểm tra thắng và lấy các ô thắng
        List<int[]> winCells = getWinningCells(board, x, y, playerVal);
        if (winCells != null) {
            match.setWinner(player);
            match.setWinningCells(winCells);
            gameEnded = true;
        } else if (isBoardFull(board)) {
            match.setWinner("DRAW");
            gameEnded = true;
        } else {
            String next = player.equals(match.getPlayer1()) ? match.getPlayer2() : match.getPlayer1();
            match.setCurrentTurn(next);
        }

        if (gameEnded) {
            saveHistory(match);
        }

        return match;
    }

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
        System.out.println("✅ Đã lưu lịch sử đấu cho phòng: " + roomName);
    }

    private boolean isBoardFull(int[][] board) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (board[i][j] == 0)
                    return false;
            }
        }
        return true;
    }

    // Trả về danh sách các ô thắng, hoặc null nếu chưa thắng
    private List<int[]> getWinningCells(int[][] board, int r, int c, int val) {
        List<int[]> cells;

        cells = checkDirectionCells(board, r, c, val, 0, 1); // Ngang
        if (cells != null)
            return cells;

        cells = checkDirectionCells(board, r, c, val, 1, 0); // Dọc
        if (cells != null)
            return cells;

        cells = checkDirectionCells(board, r, c, val, 1, 1); // Chéo \
        if (cells != null)
            return cells;

        cells = checkDirectionCells(board, r, c, val, 1, -1); // Chéo /
        if (cells != null)
            return cells;

        return null;
    }

    private List<int[]> checkDirectionCells(int[][] board, int r, int c, int val, int dRow, int dCol) {
        List<int[]> cells = new ArrayList<>();
        cells.add(new int[] { r, c });

        // Duyệt về phía dương (+)
        int nextR = r + dRow;
        int nextC = c + dCol;
        while (isValid(nextR, nextC) && board[nextR][nextC] == val) {
            cells.add(new int[] { nextR, nextC });
            nextR += dRow;
            nextC += dCol;
        }
        int headVal = isValid(nextR, nextC) ? board[nextR][nextC] : -1;

        // Duyệt về phía âm (-)
        int prevR = r - dRow;
        int prevC = c - dCol;
        while (isValid(prevR, prevC) && board[prevR][prevC] == val) {
            cells.add(new int[] { prevR, prevC });
            prevR -= dRow;
            prevC -= dCol;
        }
        int tailVal = isValid(prevR, prevC) ? board[prevR][prevC] : -1;

        int count = cells.size();

        // TH1: 4 con trở lên -> Thắng luôn
        if (count >= 4)
            return cells;

        // TH2: Đúng 3 con -> Phải không bị chặn 2 đầu
        if (count == 3) {
            boolean blockedHead = (headVal != 0);
            boolean blockedTail = (tailVal != 0);
            if (!blockedHead && !blockedTail) {
                return cells;
            }
        }

        return null;
    }

    private boolean isValid(int r, int c) {
        return r >= 0 && r < 5 && c >= 0 && c < 5;
    }
}
