class ChatClient {
    constructor() {
        this.stompClient = null;
        this.chatIdx = null;
        this.userIdx = null;
        this.nickname = null;
        this.chatType = null;
    }

    connect(chatIdx, userIdx, nickname, chatType) {
        this.chatIdx = chatIdx;
        this.userIdx = userIdx;
        this.nickname = nickname;
        this.chatType = chatType;

        const socket = new SockJS('/ws-endpoint');
        this.stompClient = Stomp.over(socket);

        this.stompClient.connect({}, (frame) => {
            console.log('Connected: ' + frame);
            this.subscribeToChat();
            this.joinChat();
            this.loadPreviousMessages();
        });
    }

    subscribeToChat() {
        const topic = this.chatType === 'PERSONAL' ? `/topic/chat/${this.chatIdx}` : `/topic/groupChat/${this.chatIdx}`;
        this.stompClient.subscribe(topic, (message) => {
            const chatMessage = JSON.parse(message.body);
            if (Array.isArray(chatMessage)) {
                this.handlePreviousMessages(chatMessage);
            } else {
                this.displayMessage(chatMessage);
            }
        });
    }

    joinChat() {
        const destination = this.chatType === 'PERSONAL' ? `/app/chat.addUser/${this.chatIdx}` : `/app/groupChat.addUser/${this.chatIdx}`;
        const joinMessage = {
            userIdx: this.userIdx,
            nickname: this.nickname,
            content: '',
            sendDateTime: new Date(),
            chatType: this.chatType
        };
        this.stompClient.send(destination, {}, JSON.stringify(joinMessage));
        this.displaySystemMessage(`${this.nickname}님이 입장하셨습니다.`);
    }

    leaveChat() {
        const leaveMessage = {
            userIdx: this.userIdx,
            nickname: this.nickname,
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
            nickname: this.nickname,
            content: content,
            sendDateTime: new Date(),
            chatType: this.chatType
        };
        const destination = this.chatType === 'PERSONAL' ? `/app/chat.sendMessage/${this.chatIdx}` : `/app/groupChat.sendMessage/${this.chatIdx}`;
        this.stompClient.send(destination, {}, JSON.stringify(chatMessage));
    }

    loadPreviousMessages() {
        const destination = this.chatType === 'PERSONAL'
            ? `/app/chat.getMessages/${this.chatIdx}`
            : `/app/groupChat.getMessages/${this.chatIdx}`;
        this.stompClient.send(destination, {}, JSON.stringify({
            userIdx: this.userIdx,
            chatType: this.chatType
        }));
    }

    handlePreviousMessages(messages) {
        console.log("Handling previous messages:", messages);
        const chatMessages = document.getElementById('chat-messages');
        // 기존 메시지를 임시로 저장
        const existingMessages = chatMessages.innerHTML;
        // 채팅창을 비웁니다
        chatMessages.innerHTML = '';

        // 이전 메시지들을 추가합니다
        messages.forEach(message => this.displayMessage(message));

        // 기존 메시지를 다시 추가합니다
        chatMessages.innerHTML += existingMessages;

        chatMessages.scrollTop = chatMessages.scrollHeight;
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

            const nicknameSpan = document.createElement('span');
            nicknameSpan.classList.add('nickname');
            nicknameSpan.textContent = message.nickname;

            const contentSpan = document.createElement('span');
            contentSpan.classList.add('content');
            contentSpan.textContent = message.content;

            const timeSpan = document.createElement('span');
            timeSpan.classList.add('time');
            timeSpan.textContent = this.formatTime(message.sendDateTime);

            messageElement.appendChild(nicknameSpan);
            messageElement.appendChild(contentSpan);
            messageElement.appendChild(timeSpan);
        }

        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    displaySystemMessage(content) {
        const chatMessages = document.getElementById('chat-messages');
        const messageElement = document.createElement('div');
        messageElement.classList.add('entrance-message');
        messageElement.textContent = content;
        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    formatTime(dateTime) {
        const date = new Date(dateTime);
        const hours = date.getHours().toString().padStart(2, '0');
        const minutes = date.getMinutes().toString().padStart(2, '0');
        return `${hours}:${minutes}`;
    }
}