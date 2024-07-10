class ChatClient {
    constructor() {
        this.stompClient = null;
        this.chatIdx = null;
        this.userIdx = null;
        this.nickname = null;
        this.chatType = null; // 'personal' or 'group'
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
        const topic = this.chatType === 'personal' ? `/topic/chat/${this.chatIdx}` : `/topic/groupChat/${this.chatIdx}`;
        this.stompClient.subscribe(topic, (message) => {
            const chatMessage = JSON.parse(message.body);
            this.displayMessage(chatMessage);
        });
    }

    joinChat() {
        const destination = this.chatType === 'personal' ? `/app/chat.addUser/${this.chatIdx}` : `/app/groupChat.addUser/${this.chatIdx}`;
        const joinMessage = {
            userIdx: this.userIdx,
            nickname: this.nickname,
            content: '',
            sendDateTime: new Date()
        };
        this.stompClient.send(destination, {}, JSON.stringify(joinMessage));
    }

    leaveChat() {
        const leaveMessage = {
            userIdx: this.userIdx,
            nickname: this.nickname,
            content: '',
            sendDateTime: new Date()
        };
        const destination = this.chatType === 'personal' ? `/app/chat.leaveUser/${this.chatIdx}` : `/app/groupChat.leaveUser/${this.chatIdx}`;
        this.stompClient.send(destination, {}, JSON.stringify(leaveMessage));
        this.stompClient.disconnect();
    }

    sendMessage(content) {
        const chatMessage = {
            userIdx: this.userIdx,
            nickname: this.nickname,
            content: content,
            sendDateTime: new Date()
        };
        const destination = this.chatType === 'personal' ? `/app/chat.sendMessage/${this.chatIdx}` : `/app/groupChat.sendMessage/${this.chatIdx}`;
        this.stompClient.send(destination, {}, JSON.stringify(chatMessage));
    }

    loadPreviousMessages() {
        const destination = this.chatType === 'personal' ? `/app/chat.getMessages/${this.chatIdx}` : `/app/groupChat.getMessages/${this.chatIdx}`;
        this.stompClient.send(destination, {}, this.userIdx);
    }

    displayMessage(message) {
        // 이 메서드는 실제 UI에 메시지를 표시하는 로직을 구현해야 합니다.
        // 예를 들어, DOM 조작을 통해 메시지를 화면에 추가하는 등의 작업을 수행합니다.
        console.log('Received message:', message);
        // 실제 구현에서는 이 부분을 채팅 UI에 메시지를 추가하는 코드로 대체해야 합니다.
    }
}