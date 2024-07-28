class ChatClient {
    constructor() {
        this.stompClient = null;
        this.chatRoomId = null;
        this.userId = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
    }

    connect(chatRoomId, userId) {
        if (!chatRoomId || chatRoomId === 'defaultChatRoomId' || !userId) {
            console.error("Invalid chatRoomId or userId", {chatRoomId, userId});
            alert("채팅 연결에 필요한 정보가 올바르지 않습니다. 페이지를 새로고침하거나 다시 접속해주세요.");
            return;
        }

        this.chatRoomId = chatRoomId;
        this.userId = userId;

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
            setTimeout(() => this.connect(this.chatRoomId, this.userId), timeout);
        } else {
            console.error('최대 재접속 횟수를 모두 소진하였습니다.');
            alert("연결을 다시 수립할 수 없습니다. 페이지를 새로고침해주세요.");
        }
    }

    subscribeToChat() {
        this.stompClient.subscribe(`/topic/chat/${this.chatRoomId}`, (message) => {
            const chatMessage = JSON.parse(message.body);
            this.displayMessage(chatMessage);
        });
    }

    joinChat() {
        if (!this.chatRoomId || !this.userId) {
            console.error("Cannot join chat: Invalid chatRoomId or userId");
            return;
        }

        const joinMessage = {
            chatRoomId: this.chatRoomId,
            userId: this.userId,
            content: '입장하셨습니다.',
            sentAt: new Date()
        };
        this.stompClient.send(`/app/chat.join/${this.chatRoomId}`, {}, JSON.stringify(joinMessage));
    }

    leaveChat() {
        const leaveMessage = {
            chatRoomId: this.chatRoomId,
            userId: this.userId,
            content: '퇴장하셨습니다.',
            sentAt: new Date()
        };
        this.stompClient.send(`/app/chat.leave/${this.chatRoomId}`, {}, JSON.stringify(leaveMessage));
        this.stompClient.disconnect();
    }

    sendMessage(content) {
        if (!this.stompClient || !this.chatRoomId || !this.userId) {
            console.error('Cannot send message: Connection not established or invalid chatRoomId/userId');
            return;
        }
        const chatMessage = {
            chatRoomId: this.chatRoomId,
            userId: this.userId,
            content: content,
            sentAt: new Date()
        };
        this.stompClient.send(`/app/chat.sendMessage/${this.chatRoomId}`, {}, JSON.stringify(chatMessage));
    }

    loadPreviousMessages() {
        fetch(`/api/chat/${this.chatRoomId}/messages`)
            .then(response => response.json())
            .then(messages => {
                messages.forEach(message => this.displayMessage(message));
            })
            .catch(error => console.error('Error loading previous messages:', error));
    }

    displayMessage(message) {
        const chatMessages = document.getElementById('chat-messages');
        const messageElement = document.createElement('div');
        messageElement.classList.add('message');

        if (message.userId === this.userId) {
            messageElement.classList.add('own-message');
        } else {
            messageElement.classList.add('other-message');
        }

        const userIcon = document.createElement('div');
        userIcon.classList.add('user-icon');
        const avatar = document.createElement('div');
        avatar.classList.add('avatar');
        avatar.textContent = '🙋‍♂️';
        userIcon.appendChild(avatar);

        const messageContent = document.createElement('div');
        messageContent.classList.add('message-content');
        const contentSpan = document.createElement('span');
        contentSpan.classList.add('content');
        contentSpan.textContent = message.content;

        const timeSpan = document.createElement('span');
        timeSpan.classList.add('time');
        timeSpan.textContent = new Date(message.sentAt).toLocaleTimeString('ko-KR', {
            timeZone: 'Asia/Seoul'
        });

        messageContent.appendChild(contentSpan);
        messageContent.appendChild(timeSpan);

        messageElement.appendChild(userIcon);
        messageElement.appendChild(messageContent);

        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
}