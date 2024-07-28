class ChatClient {
    constructor() {
        this.stompClient = null;
        this.chatRoomId = null;
        this.userId = null;
    }

    connect(chatRoomId, userId) {
        if (!chatRoomId || !userId) {
            console.error("Invalid chatRoomId or userId", {chatRoomId, userId});
            alert("채팅 연결에 필요한 정보가 올바르지 않습니다. 페이지를 새로고침하거나 다시 접속해주세요.");
            return;
        }

        this.chatRoomId = chatRoomId;
        this.userId = userId;

        const socket = new SockJS('/ws-endpoint');
        this.stompClient = Stomp.over(socket);

        this.stompClient.connect({}, (frame) => {
            console.log('Connected: ' + frame);
            this.subscribeToChat();
        }, (error) => {
            console.error('Connection error:', error);
        });
    }

    subscribeToChat() {
        this.stompClient.subscribe(`/topic/chat/${this.chatRoomId}`, (message) => {
            const chatMessage = JSON.parse(message.body);
            this.displayMessage(chatMessage);
        });
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
