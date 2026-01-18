import React, { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import socketService from '../services/socket';
import './GameRoom.css'; // Will create this for specific board styles

const GameRoom = () => {
    const { roomId } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const currentUser = localStorage.getItem('username');
    const isBotMode = location.state?.botMode || false;

    const [gameState, setGameState] = useState(null);
    const [status, setStatus] = useState('Đang kết nối...');

    // Use ref to prevent double connection in React Strict Mode which might be enabled
    const connectedRef = useRef(false);
    const subscriptionRef = useRef(null);

    useEffect(() => {
        if (connectedRef.current) return;

        const onConnected = () => {
            setStatus('Đã kết nối! Đang đợi dữ liệu...');
            connectedRef.current = true;

            // 1. Subscribe FIRST to avoid missing messages
            subscriptionRef.current = socketService.subscribeToRoom(roomId, (matchData) => {
                console.log('Received match data:', matchData);
                setGameState(matchData);

                // Determine status text
                if (matchData.winner) {
                    if (matchData.winner === 'DRAW') setStatus('Hòa!');
                    else setStatus(`Người thắng: ${matchData.winner}`);
                } else {
                    setStatus(`Lượt của: ${matchData.currentTurn}`);
                }
            });

            // 2. Then Send join message to trigger the backend to send initial state
            socketService.sendJoin(roomId);
        };

        const onError = (err) => {
            setStatus('Mất kết nối máy chủ.');
            console.error(err);
        };

        socketService.connect(onConnected, onError);

        return () => {
            if (subscriptionRef.current) {
                subscriptionRef.current.unsubscribe();
                subscriptionRef.current = null;
            }
            socketService.disconnect();
            connectedRef.current = false;
        };
    }, [roomId]);

    const handleCellClick = (r, c) => {
        if (!gameState) return;
        if (gameState.winner) return;
        if (gameState.currentTurn !== currentUser) return;
        if (gameState.board[r][c] !== 0) return;

        socketService.sendMove({
            roomId: roomId,
            player: currentUser,
            x: r,
            y: c
        });
    };

    const handleBack = () => {
        navigate('/lobby');
    };

    const handleRestart = () => {
        socketService.sendRestart(roomId);
    };

    if (!gameState) {

        return (
            <div className="glass-container" style={{ maxWidth: '400px' }}>
                <h2 style={{ marginBottom: '1rem' }}>{status}</h2>
                {/* Only show Room Code if NOT in Bot Mode */}
                {!isBotMode && (
                    <>
                        <div style={{
                            background: 'var(--glass-bg)',
                            border: '1px dashed var(--accent-neon-blue)',
                            borderRadius: 'var(--radius-md)',
                            padding: '1.5rem',
                            marginBottom: '1.5rem'
                        }}>
                            <p style={{ color: 'var(--text-muted)', marginBottom: '0.5rem', fontSize: '0.9rem' }}>Mã phòng của bạn:</p>
                            <p style={{
                                color: 'var(--accent-neon-blue)',
                                fontSize: '2rem',
                                fontWeight: 700,
                                letterSpacing: '4px',
                                margin: 0
                            }}>{roomId}</p>
                        </div>
                        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
                            📤 Chia sẻ mã này cho bạn bè để cùng chơi!
                        </p>
                    </>
                )}
                <button onClick={handleBack} className="glass-button secondary">← Quay lại Lobby</button>
            </div>
        );
    }

    const isMyTurn = gameState.currentTurn === currentUser;
    const winnerText = gameState.winner === 'DRAW' ? 'Hòa!' : `Người thắng: ${gameState.winner}`;

    return (
        <div className="glass-container game-container">
            <div className="game-header">
                <h3>Phòng: {roomId}</h3>
                <div className="players-info">
                    <span className={gameState.player1 === currentUser ? 'me' : ''}>P1 (X): {gameState.player1}</span>
                    <span className="vs">VS</span>
                    <span className={gameState.player2 === currentUser ? 'me' : ''}>P2 (O): {gameState.player2 || '...'}</span>
                </div>
            </div>

            <div className="game-status">
                {gameState.winner ? (
                    <div className="winner-banner">{winnerText}</div>
                ) : (
                    <div className={`turn-indicator ${isMyTurn ? 'my-turn' : ''}`}>
                        {isMyTurn ? 'Lượt của bạn!' : `Lượt của ${gameState.currentTurn}`}
                    </div>
                )}
            </div>

            <div className="board">
                {gameState.board.map((row, r) => (
                    <div key={r} className="board-row">
                        {row.map((cell, c) => {
                            let cellContent = '';
                            if (cell === 1) cellContent = 'X';
                            if (cell === 2) cellContent = 'O';

                            return (
                                <div
                                    key={c}
                                    className={`cell ${cellContent} ${!cell && isMyTurn && !gameState.winner ? 'clickable' : ''}`}
                                    onClick={() => handleCellClick(r, c)}
                                >
                                    {cellContent}
                                </div>
                            );
                        })}
                    </div>
                ))}
            </div>

            <div className="game-actions">
                <button onClick={handleBack} className="glass-button secondary">Thoát</button>
                {gameState.winner && (
                    <button onClick={handleRestart} className="glass-button">Chơi Lại</button>
                )}
            </div>
        </div>
    );
};

export default GameRoom;
