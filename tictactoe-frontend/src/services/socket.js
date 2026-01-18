import { Client } from '@stomp/stompjs';

class SocketService {
    constructor() {
        this.client = null;
        this.connected = false;
    }

    connect(onConnected, onError) {
        if (this.client && this.client.active) {
            if (onConnected) onConnected();
            return;
        }

        this.client = new Client({
            brokerURL: 'ws://localhost:8080/ws/websocket', // WebSocket endpoint
            reconnectDelay: 5000,
            onConnect: () => {
                this.connected = true;
                console.log('Connected to WebSocket');
                if (onConnected) onConnected();
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
                if (onError) onError(frame);
            },
            onWebSocketClose: () => {
                this.connected = false;
                console.log('WebSocket Connection Closed');
            }
        });

        this.client.activate();
    }

    subscribeToRoom(roomId, callback) {
        if (!this.client || !this.client.active) {
            console.error('Socket not connected');
            return null;
        }
        console.log(`Subscribing to /topic/room/${roomId}`);
        return this.client.subscribe(`/topic/room/${roomId}`, (message) => {
            const body = JSON.parse(message.body);
            callback(body);
        });
    }

    sendMove(moveData) {
        if (!this.client || !this.client.active) {
            console.error('Socket not connected');
            return;
        }
        this.client.publish({
            destination: '/app/move',
            body: JSON.stringify(moveData),
        });
    }

    sendJoin(roomId) {
        if (!this.client || !this.client.active) return;
        this.client.publish({
            destination: '/app/join',
            body: JSON.stringify({ roomId }),
        });
    }

    sendRestart(roomId) {
        if (!this.client || !this.client.active) return;
        this.client.publish({
            destination: '/app/restart',
            body: JSON.stringify({ roomId }),
        });
    }

    disconnect() {
        if (this.client) {
            this.client.deactivate();
        }
    }
}

const socketService = new SocketService();
export default socketService;
