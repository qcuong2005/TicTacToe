package com.example.demo.repository.matchHistory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.matchHistory.MatchHistory;

@Repository
public interface MatchHistoryRepository extends JpaRepository<MatchHistory, Long> {

    // Tìm lịch sử theo mã phòng, sắp xếp mới nhất lên đầu
    List<MatchHistory> findByRoomCodeOrderByPlayedAtDesc(String roomCode);

    List<MatchHistory> findAllByOrderByPlayedAtDesc();
}