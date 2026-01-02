import axios from 'axios';

// Create generic axios instance
const api = axios.create({
    baseURL: 'http://localhost:8080/api', // Update if backend runs on different port
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add a request interceptor to inject the token
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Add a response interceptor to handle auth errors
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            // Token expired or invalid
            localStorage.removeItem('token');
            localStorage.removeItem('username');
            // Optional: Redirect to login or trigger an event
            // window.location.href = '/login'; 
        }
        return Promise.reject(error);
    }
);

export default api;
