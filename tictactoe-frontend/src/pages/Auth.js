import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const Auth = () => {
    const [isLogin, setIsLogin] = useState(true);
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        const endpoint = isLogin ? '/login' : '/register';
        const payload = { username, password };

        try {
            const response = await api.post(endpoint, payload);

            if (isLogin) {
                localStorage.setItem('token', response.data.token);
                localStorage.setItem('username', response.data.username);
                navigate('/lobby');
            } else {
                setIsLogin(true);
                setSuccess('Đăng ký thành công! Vui lòng đăng nhập.');
                setUsername('');
                setPassword('');
            }
        } catch (err) {
            if (err.response && err.response.data) {
                setError(err.response.data.error || 'Có lỗi xảy ra');
            } else {
                setError('Không thể kết nối đến server');
            }
        }
    };

    return (
        <div className="glass-container">
            {/* Logo/Title */}
            <h1>🎮 Caro Online</h1>
            <p style={{ marginBottom: '2rem', color: 'var(--text-secondary)' }}>
                {isLogin ? 'Đăng nhập để bắt đầu chơi' : 'Tạo tài khoản mới'}
            </p>

            {/* Messages */}
            {error && <div className="error-message">{error}</div>}
            {success && <div className="success-message">{success}</div>}

            {/* Form */}
            <form onSubmit={handleSubmit}>
                <input
                    type="text"
                    className="glass-input"
                    placeholder="Tên đăng nhập"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                    autoComplete="username"
                />
                <input
                    type="password"
                    className="glass-input"
                    placeholder="Mật khẩu"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    autoComplete="current-password"
                />
                <button type="submit" className="glass-button" style={{ marginTop: '0.5rem' }}>
                    {isLogin ? '🚀 Vào Game' : '✨ Đăng Ký Ngay'}
                </button>
            </form>

            {/* Toggle */}
            <div className="divider"></div>
            <p style={{ color: 'var(--text-muted)' }}>
                {isLogin ? 'Chưa có tài khoản? ' : 'Đã có tài khoản? '}
                <a href="#" onClick={(e) => {
                    e.preventDefault();
                    setIsLogin(!isLogin);
                    setError('');
                    setSuccess('');
                }}>
                    {isLogin ? 'Đăng ký' : 'Đăng nhập'}
                </a>
            </p>
        </div>
    );
};

export default Auth;

