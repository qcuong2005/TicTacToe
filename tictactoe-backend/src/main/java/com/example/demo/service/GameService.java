package com.example.demo.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Các import Model và Repository (Đảm bảo đúng package của bạn)
import com.example.demo.model.gameMatch.GameMatch;
import com.example.demo.model.matchHistory.MatchHistory;
import com.example.demo.model.room.RoomEntity;
import com.example.demo.repository.matchHistory.MatchHistoryRepository;
import com.example.demo.repository.room.RoomRepository;

@Service
public class GameService {

    // Inject Repository để lưu và lấy dữ liệu
    @Autowired
    private MatchHistoryRepository matchHistoryRepository;

    @Autowired
    private RoomRepository roomRepository;

    // Lưu trạng thái các bàn cờ đang chơi trong RAM
    private final Map<String, GameMatch> games = new ConcurrentHashMap<>();

    // 1. Tạo hoặc lấy game hiện tại
    public GameMatch createOrGetGame(String roomId, String p1) {
        games.computeIfAbsent(roomId, k -> {
            GameMatch match = new GameMatch();
            match.setRoomId(roomId);
            match.setPlayer1(p1);
            match.setCurrentTurn(p1); // Người tạo phòng đi trước
            return match;
        });
        return games.get(roomId);
    }

    // 2. Reset game (Chơi ván mới)
    public GameMatch resetGame(String roomId) {
        GameMatch match = games.get(roomId);
        if (match != null) {
            match.setBoard(new int[5][5]); // Xóa bàn cờ
            match.setWinner(null); // Xóa người thắng
            match.setCurrentTurn(match.getPlayer1()); // Reset lượt
        }
        return match;
    }

    // 3. Xử lý nước đi (Logic chính)
    public GameMatch makeMove(String roomId, String player, int x, int y) {
        GameMatch match = games.get(roomId);

        // Nếu game không tồn tại hoặc đã kết thúc thì không làm gì
        if (match == null || match.getWinner() != null)
            return match;

        // Kiểm tra đúng lượt đi không
        if (!player.equals(match.getCurrentTurn()))
            return match;

        int[][] board = match.getBoard();
        // Kiểm tra ô đó có trống không
        if (board[x][y] != 0)
            return match;

        // Đánh dấu ô (1 cho Player1, 2 cho Player2)
        int playerVal = player.equals(match.getPlayer1()) ? 1 : 2;
        board[x][y] = playerVal;

        boolean gameEnded = false;

        // A. Kiểm tra thắng (Logic 3 ô không chặn hoặc 4 ô)
        if (checkWinDeep(board, x, y, playerVal)) {
            match.setWinner(player);
            gameEnded = true;
        }
        // B. Kiểm tra hòa (Bàn cờ đầy)
        else if (isBoardFull(board)) {
            match.setWinner("DRAW");
            gameEnded = true;
        }
        // C. Nếu chưa xong thì đổi lượt
        else {
            String next = player.equals(match.getPlayer1()) ? match.getPlayer2() : match.getPlayer1();
            match.setCurrentTurn(next);
        }

        // --- LƯU LỊCH SỬ NẾU GAME KẾT THÚC ---
        if (gameEnded) {
            saveHistory(match);
        }
        // --------------------------------------

        return match;
    }

    // 4. Hàm lưu lịch sử vào Database
    private void saveHistory(GameMatch match) {
        String roomName = "Unknown";

        // Tìm tên phòng trong DB để lưu cho đẹp
        // Lưu ý: Đảm bảo RoomRepository của bạn có hàm findByRoomCode
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

    // --- CÁC HÀM LOGIC BỔ TRỢ ---

    // Kiểm tra bàn cờ đầy (Hòa)
    private boolean isBoardFull(int[][] board) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (board[i][j] == 0)
                    return false; // Vẫn còn ô trống
            }
        }
        return true;
    }

    // Kiểm tra thắng nâng cao
    private boolean checkWinDeep(int[][] board, int r, int c, int val) {
        return checkDirection(board, r, c, val, 0, 1) || // Ngang
                checkDirection(board, r, c, val, 1, 0) || // Dọc
                checkDirection(board, r, c, val, 1, 1) || // Chéo \
                checkDirection(board, r, c, val, 1, -1); // Chéo /
    }

    private boolean checkDirection(int[][] board, int r, int c, int val, int dRow, int dCol) {
        int count = 1;

        // Duyệt về phía dương (+)
        int nextR = r + dRow;
        int nextC = c + dCol;
        while (isValid(nextR, nextC) && board[nextR][nextC] == val) {
            count++;
            nextR += dRow;
            nextC += dCol;
        }
        int headVal = isValid(nextR, nextC) ? board[nextR][nextC] : -1; // -1 là ra ngoài bàn cờ

        // Duyệt về phía âm (-)
        int prevR = r - dRow;
        int prevC = c - dCol;
        while (isValid(prevR, prevC) && board[prevR][prevC] == val) {
            count++;
            prevR -= dRow;
            prevC -= dCol;
        }
        int tailVal = isValid(prevR, prevC) ? board[prevR][prevC] : -1;

        // TH1: 4 con trở lên -> Thắng luôn (bất chấp chặn)
        if (count >= 4)
            return true;

        // TH2: Đúng 3 con -> Phải không bị chặn 2 đầu
        if (count == 3) {
            boolean blockedHead = (headVal != 0); // Bị chặn nếu đầu không phải 0
            boolean blockedTail = (tailVal != 0); // Bị chặn nếu đuôi không phải 0

            // Nếu CẢ 2 ĐẦU ĐỀU KHÔNG BỊ CHẶN -> Thắng
            if (!blockedHead && !blockedTail) {
                return true;
            }
        }

        return false;
    }

    private boolean isValid(int r, int c) {
        return r >= 0 && r < 5 && c >= 0 && c < 5;
    }
}