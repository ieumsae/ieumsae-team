class ChatClient {
    constructor() {
        this.stompClient = null;
        this.chatIdx = null;
        this.userIdx = null;
        this.chatType = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
    }

    connect(chatIdx, userIdx, chatType) {
        if (chatIdx == null || userIdx == null) {
            console.error("Invalid chatIdx or userIdx", {chatIdx, userIdx, chatType});
            alert("채팅 연결에 필요한 정보가 올바르지 않습니다. 페이지를 새로고침하거나 다시 접속해주세요.");
            return;
        }

        this.chatIdx = chatIdx;
        this.userIdx = userIdx;
        this.chatType = chatType;

        const socket = new SockJS('/ws-endpoint');
        this.stompClient = Stomp.over(socket);

        this.stompClient.connect({},
            (frame) => {
                console.log('Connected: ' + frame);
                this.reconnectAttempts = 0;
                this.subscribeToChat();
                this.joinChat();
                this.loadPreviousMessages();
            },
            (error) => {
                console.error('Connection error:', error);
                this.handleReconnect();
            }
        );
    }

    handleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            const timeout = Math.min(30000, (Math.pow(2, this.reconnectAttempts) - 1) * 1000);
            console.log(`Attempting to reconnect in ${timeout/1000} seconds...`);
            setTimeout(() => this.connect(this.chatIdx, this.userIdx, this.chatType), timeout);
        } else {
            console.error('Max reconnection attempts reached');
            alert("연결을 다시 수립할 수 없습니다. 페이지를 새로고침해주세요.");
        }
    }

    subscribeToChat() {
        const topic = this.chatType === 'PERSONAL' ? `/topic/chat/${this.chatIdx}` : `/topic/groupChat/${this.chatIdx}`;
        this.stompClient.subscribe(topic, (message) => {
            const chatMessage = JSON.parse(message.body);
            this.displayMessage(chatMessage);
        });
    }

    joinChat() {
        const destination = this.chatType === 'PERSONAL' ? `/app/chat.addUser/${this.chatIdx}` : `/app/groupChat.addUser/${this.chatIdx}`;
        const joinMessage = {
            userIdx: this.userIdx,
            content: '',
            sendDateTime: new Date(),
            chatType: this.chatType
        };
        this.stompClient.send(destination, {}, JSON.stringify(joinMessage));
        this.displaySystemMessage(`사용자(${this.userIdx})님이 입장하셨습니다.`);
    }

    leaveChat() {
        const leaveMessage = {
            userIdx: this.userIdx,
            content: '',
            sendDateTime: new Date(),
            chatType: this.chatType
        };
        const destination = this.chatType === 'PERSONAL' ? `/app/chat.leaveUser/${this.chatIdx}` : `/app/groupChat.leaveUser/${this.chatIdx}`;
        this.stompClient.send(destination, {}, JSON.stringify(leaveMessage));
        this.stompClient.disconnect();
    }

    sendMessage(content) {
        const chatMessage = {
            chatIdx: this.chatIdx,
            userIdx: this.userIdx,
            content: content,
            sendDateTime: new Date(),
            chatType: this.chatType
        };
        const destination = this.chatType === 'PERSONAL' ? `/app/chat.sendMessage/${this.chatIdx}` : `/app/groupChat.sendMessage/${this.chatIdx}`;
        this.stompClient.send(destination, {}, JSON.stringify(chatMessage));
    }

    loadPreviousMessages(previousMessages) {
        if (previousMessages && Array.isArray(previousMessages)) {
            previousMessages.forEach(message => {
                this.displayMessage(message);
            });
        } else {
            console.warn("No previous messages or invalid format");
        }
    }

    displayMessage(message) {
        const chatMessages = document.getElementById('chat-messages');
        const messageElement = document.createElement('div');

        if (message.chatType === "ENTRANCE") {
            messageElement.classList.add('entrance-message');
            messageElement.textContent = message.content;
        } else {
            messageElement.classList.add('message');
            messageElement.classList.add(message.userIdx == this.userIdx ? 'own-message' : 'other-message');

            const userIdxSpan = document.createElement('span');
            userIdxSpan.classList.add('user-idx');
            userIdxSpan.textContent = `User ${message.userIdx}: `;

            const contentSpan = document.createElement('span');
            contentSpan.classList.add('content');
            contentSpan.textContent = message.content;

            const timeSpan = document.createElement('span');
            timeSpan.classList.add('time');
            timeSpan.textContent = new Date(message.sendDateTime).toLocaleTimeString();

            messageElement.appendChild(userIdxSpan);
            messageElement.appendChild(contentSpan);
            messageElement.appendChild(timeSpan);
        }

        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    displaySystemMessage(content) {
        const chatMessages = document.getElementById('chat-messages');
        const messageElement = document.createElement('div');
        messageElement.classList.add('system-message');
        messageElement.textContent = content;
        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    handleError(error) {
        console.error("Chat error:", error);
        alert("채팅 중 오류가 발생했습니다. 페이지를 새로고침해주세요.");
    }
}