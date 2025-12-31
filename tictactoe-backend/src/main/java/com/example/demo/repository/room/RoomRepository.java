package com.example.demo.repository.room;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.room.RoomEntity;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, Long> {

    // Kiểm tra xem mã code đã tồn tại chưa (để tránh trùng)
    boolean existsByRoomCode(String roomCode);

    // Tìm phòng theo mã Code HOẶC tên phòng
    // Lưu ý: Chỉ tìm phòng đang chờ (waiting) để vào
    @Query("SELECT r FROM RoomEntity r WHERE (r.roomCode = :input OR r.roomName = :input) AND r.status = 'waiting'")
    Optional<RoomEntity> findRoomToJoin(String input);
}