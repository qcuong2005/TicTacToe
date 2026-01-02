package com.example.demo.controller.matchHistory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.matchHistory.MatchHistory;
import com.example.demo.repository.matchHistory.MatchHistoryRepository;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private MatchHistoryRepository matchHistoryRepository;

    // API cũ: Xem lịch sử 1 phòng
    @GetMapping("/{roomCode}")
    public ResponseEntity<?> getRoomHistory(@PathVariable String roomCode) {
        return ResponseEntity.ok(matchHistoryRepository.findByRoomCodeOrderByPlayedAtDesc(roomCode));
    }

    // ✅ API MỚI: Lấy TẤT CẢ và CHIA THEO PHÒNG
    // GET /api/history/all-grouped
    @GetMapping("/all-grouped")
    public ResponseEntity<?> getAllHistoryGrouped() {
        // 1. Lấy tất cả dữ liệu từ DB
        List<MatchHistory> allMatches = matchHistoryRepository.findAllByOrderByPlayedAtDesc();

        // 2. Dùng Java Stream để gom nhóm theo Mã Phòng (roomCode)
        // Kết quả trả về là một Map:
        // Key: Mã phòng (String)
        // Value: Danh sách các trận đấu của phòng đó (List<MatchHistory>)
        Map<String, List<MatchHistory>> groupedHistory = allMatches.stream()
                .collect(Collectors.groupingBy(MatchHistory::getRoomCode));

        return ResponseEntity.ok(groupedHistory);
    }
}