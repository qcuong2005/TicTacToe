import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const Lobby = () => {
    const navigate = useNavigate();
    const username = localStorage.getItem('username');
    const [roomName, setRoomName] = useState('');
    const [joinCode, setJoinCode] = useState('');
    const [error, setError] = useState('');
    const [waitingRooms, setWaitingRooms] = useState([]);
    const [loadingRooms, setLoadingRooms] = useState(true);

    useEffect(() => {
        const fetchWaitingRooms = async () => {
            try {
                const response = await api.get('/rooms/waiting');
                setWaitingRooms(response.data);
            } catch (err) {
                console.error('Failed to fetch waiting rooms', err);
            } finally {
                setLoadingRooms(false);
            }
        };
        fetchWaitingRooms();
    }, []);

    const handleCreateRoom = async () => {
        try {
            const response = await api.post('/rooms/create', { roomName });
            navigate(`/game/${response.data.roomCode}`);
        } catch (err) {
            setError('Không thể tạo phòng');
        }
    };

    const handleJoinRoom = async (code) => {
        const identifier = code || joinCode;
        if (!identifier) {
            setError('Vui lòng nhập mã phòng');
            return;
        }
        try {
            const response = await api.post('/rooms/join', { identifier });
            navigate(`/game/${response.data.roomCode}`);
        } catch (err) {
            setError(err.response?.data?.error || 'Không thể vào phòng');
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        navigate('/');
    };

    return (
        <div className="glass-container" style={{ maxWidth: '520px' }}>
            {/* Header */}
            <div className="flex-between mb-3">
                <div>
                    <h2 style={{ marginBottom: '0.25rem' }}>👋 Xin chào!</h2>
                    <p style={{ margin: 0, color: 'var(--accent-neon-blue)', fontWeight: 600 }}>{username}</p>
                </div>
                <div className="flex gap-1">
                    <button
                        onClick={() => navigate('/history')}
                        className="glass-button secondary"
                        style={{ width: 'auto', padding: '0.5rem 1rem', fontSize: '0.875rem' }}
                    >
                        📊 Lịch sử
                    </button>
                    <button
                        onClick={handleLogout}
                        className="glass-button secondary"
                        style={{ width: 'auto', padding: '0.5rem 1rem', fontSize: '0.875rem' }}
                    >
                        🚪 Thoát
                    </button>
                </div>
            </div>

            {error && <div className="error-message">{error}</div>}

            {/* Create Room */}
            <div className="mb-3">
                <h3>🎯 Tạo Phòng Mới</h3>
                <input
                    type="text"
                    className="glass-input"
                    placeholder="Tên phòng (Tùy chọn)"
                    value={roomName}
                    onChange={(e) => setRoomName(e.target.value)}
                />
                <button onClick={handleCreateRoom} className="glass-button">
                    ✨ Tạo Phòng
                </button>
            </div>

            <div className="divider"></div>

            {/* Join by Code */}
            <div className="mb-3">
                <h3>🔑 Vào Bằng Mã</h3>
                <div className="flex gap-1">
                    <input
                        type="text"
                        className="glass-input"
                        placeholder="Nhập mã phòng..."
                        value={joinCode}
                        onChange={(e) => setJoinCode(e.target.value)}
                        style={{ marginBottom: 0 }}
                    />
                    <button
                        onClick={() => handleJoinRoom()}
                        className="glass-button secondary"
                        style={{ width: 'auto', padding: '0 1.5rem', whiteSpace: 'nowrap' }}
                    >
                        Vào
                    </button>
                </div>
            </div>

            <div className="divider"></div>

            {/* Waiting Rooms */}
            <div>
                <h3>🎮 Phòng Đang Chờ <span className="badge waiting">{waitingRooms.length}</span></h3>
                {loadingRooms ? (
                    <p style={{ color: 'var(--text-muted)', textAlign: 'center' }}>Đang tải...</p>
                ) : waitingRooms.length === 0 ? (
                    <p style={{ color: 'var(--text-muted)', textAlign: 'center' }}>Không có phòng nào đang chờ</p>
                ) : (
                    <div style={{ maxHeight: '200px', overflowY: 'auto' }}>
                        {waitingRooms.map((room) => (
                            <div key={room.roomCode} className="card flex-between">
                                <div style={{ textAlign: 'left' }}>
                                    <div style={{ fontWeight: 600, marginBottom: '0.25rem' }}>{room.roomName}</div>
                                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                                        👤 {room.player1} · 🏷️ {room.roomCode}
                                    </div>
                                </div>
                                <button
                                    onClick={() => handleJoinRoom(room.roomCode)}
                                    className="glass-button"
                                    style={{ width: 'auto', padding: '0.5rem 1rem', fontSize: '0.875rem' }}
                                >
                                    Vào
                                </button>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Lobby;

