class ChatClient {
    constructor() {
        this.stompClient = null;
        this.chatIdx = null;
        this.userIdx = null; // 세션을 통해 사용자 ID를 가져오므로 초기화 필요 없음
        this.chatType = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
    }

    // 서버에서 chatIdx를 받아오는 함수
    async getChatIdxFromServer() {
        const response = await fetch('/getChatIdx');
        const data = await response.json();
        return data.chatIdx;

    }

    // 서버에서 userIdx를 받아오는 함수
    async getUserIdxFromServer() {
        const response = await fetch('/getUserIdx');
        const data = await response.json();
        return data.userIdx;
    }

    // 서버에서 이전 메시지를 받아오는 함수
    async getPreviousMessages() {
        const response = await fetch(`/getPreviousMessages?chatIdx=${this.chatIdx}&userIdx=${this.userIdx}&chatType=${this.chatType}`);
        const data = await response.json();
        return data.previousMessages;
    }

    // 채팅에 연결하는 메소드
    async connect(chatType) {
        this.userIdx = await this.getUserIdxFromServer();
        this.chatIdx = await this.getChatIdxFromServer();
        this.chatType = chatType;

        if (this.chatIdx == null || this.userIdx == null) {
            console.error("chatIdx나 userIdx의 값이 존재하지 않습니다.", { chatIdx: this.chatIdx, userIdx: this.userIdx, chatType: this.chatType });
            return;
        }

        const socket = new SockJS('/ws-endpoint');
        this.stompClient = Stomp.over(socket);

        this.stompClient.connect({}, (frame) => {
            console.log('Connected: ' + frame);
            this.reconnectAttempts = 0;
            this.subscribeToChat();
            this.joinChat();

            // previousMessages가 이미 HTML 페이지에 포함되어 있다면, 이를 사용합니다.
            const previousMessagesElement = document.getElementById('previousMessages');
            if (previousMessagesElement) {
                const previousMessages = JSON.parse(previousMessagesElement.value);
                this.loadPreviousMessages(previousMessages);
            }
        }, (error) => {
            console.error('Connection error:', error);
            this.handleReconnect();
        });
    }

    handleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            const timeout = Math.min(30000, (Math.pow(2, this.reconnectAttempts) - 1) * 1000);
            console.log(`Attempting to reconnect in ${timeout / 1000} seconds...`);
            setTimeout(() => this.connect(this.chatType), timeout);
        } else {
            console.error('Max reconnection attempts reached');
            alert("연결을 다시 수립할 수 없습니다. 페이지를 새로고침해주세요.");
        }
    }

    subscribeToChat() {
        const topic = `/topic/chat/${this.chatIdx}`;
        this.stompClient.subscribe(topic, (message) => {
            const chatMessage = JSON.parse(message.body);
            this.displayMessage(chatMessage);
        });
    }

    joinChat() {
        const destination = `/app/chat.addUser/${this.chatIdx}`;
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
        const destination = `/app/chat.leaveUser/${this.chatIdx}`;
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
        const destination = `/app/chat.sendMessage/${this.chatIdx}`;
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

            const nicknameOrUserIdx = message.nickName || message.userIdx; // 닉네임 사용

            const userIdxSpan = document.createElement('span');
            userIdxSpan.classList.add('user-idx');
            userIdxSpan.textContent = `User ${nicknameOrUserIdx}: `;

            const contentSpan = document.createElement('span');
            contentSpan.classList.add('content');
            contentSpan.textContent = message.content;

            const timeSpan = document.createElement('span');
            timeSpan.classList.add('time');
            timeSpan.textContent = new Date(message.sendDateTime).toLocaleTimeString();

            messageElement.appendChild(userIdxSpan);
            messageElement.appendChild(contentSpan);
            messageElement.appendChild(timeSpan);
        }-

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
}

// // 사용 예시
// const chatClient = new ChatClient();
//
// // 채팅 유형에 따라 PERSONAL 또는 GROUP 값을 전달하여 채팅 유형 설정
// document.getElementById('personalChatButton').addEventListener('click', () => {
//     chatClient.connect('PERSONAL');
// });
//
// document.getElementById('groupChatButton').addEventListener('click', () => {
//     chatClient.connect('GROUP');
// });

// 구독 해제 및 웹소켓 연결 종료 (한 사용자가 여러개의 채팅방을 구독했을 때, 구독한 상태가 중첩되면서 부하가 걸릴 가능성이 있다.
// 둘 다 하는 이유
// 1. 구독 해제: 특정 채널(주제)로부터 메시지를 받지 않고 특정 채팅방에서 나갔음을 서버에 알리는 역할 (웹소켓 연결은 유지되어 있는 상태)
// 2. 웹소켓 연결 종료: 클라이언트와 서버 간의 모든 통신이 종료, 애플리케이션을 완전히 떠나는 것을 의미, 모든 채팅방에 대한 구독을 일괄 해제
// => disconnect()만으로도 충분함. 각 단계별로의 기능을 명확하게 표시하기 위해서 둘 다 사용
// window.addEventListener('beforeunload', () => {
//     if (chatClient.subscription) {
//         chatClient.subscription.unsubscribe();
//     }
//     chatClient.stompClient.disconnect();
// });
