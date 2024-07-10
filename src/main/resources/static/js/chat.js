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
            this.displayMessage(chatMessage);
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
            chatIdx: this.chatIdx,  // 이 부분이 중요합니다
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
        const destination = this.chatType === 'PERSONAL' ? `/app/chat.getMessages/${this.chatIdx}` : `/app/groupChat.getMessages/${this.chatIdx}`;
        this.stompClient.send(destination, {}, this.userIdx);
    }

    displayMessage(message) {
        const chatMessages = document.getElementById('chat-messages');
        const messageElement = document.createElement('div');
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
        timeSpan.textContent = new Date(message.sendDateTime).toLocaleTimeString();

        messageElement.appendChild(nicknameSpan);
        messageElement.appendChild(contentSpan);
        messageElement.appendChild(timeSpan);

        chatMessages.appendChild(messageElement);

        chatMessages.scrollTop = chatMessages.scrollHeight;

        console.log('Displayed message:', message);
    }
}