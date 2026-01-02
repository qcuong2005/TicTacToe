import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const History = () => {
    const navigate = useNavigate();
    const [history, setHistory] = useState({});
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchHistory = async () => {
            try {
                const response = await api.get('/history/all-grouped');
                setHistory(response.data);
            } catch (err) {
                console.error('Failed to fetch history', err);
            } finally {
                setLoading(false);
            }
        };
        fetchHistory();
    }, []);

    return (
        <div className="glass-container" style={{ maxWidth: '700px' }}>
            {/* Header */}
            <div className="flex-between mb-3">
                <h1 style={{ fontSize: '1.75rem', marginBottom: 0 }}>📊 Lịch Sử Đấu</h1>
                <button
                    onClick={() => navigate('/lobby')}
                    className="glass-button secondary"
                    style={{ width: 'auto', padding: '0.5rem 1.5rem' }}
                >
                    ← Quay lại
                </button>
            </div>

            {loading ? (
                <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '2rem' }}>Đang tải...</p>
            ) : Object.keys(history).length === 0 ? (
                <div style={{ textAlign: 'center', padding: '3rem' }}>
                    <p style={{ color: 'var(--text-muted)', fontSize: '1.1rem' }}>🎮 Chưa có trận đấu nào.</p>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>Hãy tạo phòng và chơi thôi!</p>
                </div>
            ) : (
                <div style={{ maxHeight: '500px', overflowY: 'auto' }}>
                    {Object.entries(history).map(([roomCode, matches]) => (
                        <div key={roomCode} className="card" style={{ textAlign: 'left', marginBottom: '1rem' }}>
                            <div className="flex-between mb-2">
                                <h3 style={{ margin: 0, color: 'var(--accent-neon-blue)' }}>
                                    🏠 {matches[0].roomName}
                                </h3>
                                <span className="badge waiting">#{roomCode}</span>
                            </div>

                            {matches.map((match) => (
                                <div key={match.id} style={{
                                    padding: '0.75rem',
                                    background: 'rgba(255,255,255,0.03)',
                                    borderRadius: 'var(--radius-sm)',
                                    marginBottom: '0.5rem'
                                }}>
                                    <div className="flex-between">
                                        <span>
                                            <span style={{ color: 'var(--player-x)' }}>{match.player1}</span>
                                            <span style={{ color: 'var(--text-muted)' }}> vs </span>
                                            <span style={{ color: 'var(--player-o)' }}>{match.player2}</span>
                                        </span>
                                        <span style={{
                                            fontWeight: 700,
                                            color: match.winner === 'DRAW' ? 'var(--text-muted)' : 'var(--accent-gold)'
                                        }}>
                                            {match.winner === 'DRAW' ? '🤝 Hòa' : `🏆 ${match.winner}`}
                                        </span>
                                    </div>
                                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.5rem' }}>
                                        🕐 {new Date(match.playedAt).toLocaleString('vi-VN')}
                                    </div>
                                </div>
                            ))}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default History;

