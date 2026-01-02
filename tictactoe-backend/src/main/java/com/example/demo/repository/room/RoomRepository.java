package com.example.demo.repository.room;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.room.RoomEntity;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, Long> {

    // 1. Kiểm tra xem mã code đã tồn tại chưa (để tránh trùng khi tạo random)
    boolean existsByRoomCode(String roomCode);

    // 2. Tìm phòng chính xác theo mã (Dùng để lấy tên phòng lưu vào lịch sử)
    Optional<RoomEntity> findByRoomCode(String roomCode);

    // 3. Tìm phòng để tham gia (Tìm theo Tên hoặc Mã)
    // Logic: Chỉ tìm những phòng chưa đầy (status khác 'full')
    @Query("SELECT r FROM RoomEntity r WHERE (r.roomCode = :input OR r.roomName = :input) AND r.status <> 'full'")
    Optional<RoomEntity> findRoomToJoin(String input);
}