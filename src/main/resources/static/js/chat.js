// 2024.07.10

class ChatClient {
    constructor() {
        this.stompClient = null;
        this.roomId = null;
        this.userId = null;
        this.nickname = null;
        this.roomType = null;
    }

    connect(roomId, userId, nickname, roomType) {
        this.roomId = roomId;
        this.userId = userId;
        this.nickname = nickname;
        this.roomType = roomType;

        // 웹소켓: 양방향 통신 시스템을 구축(서버)
        // STOMP: 웹소켓이라는 시스템 위에 얹는 느낌 (프로토콜)

        const socket = new SockJS('/ws-endpoint');
        this.stompClient = Stomp.over(socket);
        // over메소드는 STOMP 클라이언트를 생성하는 기능을 한다.
        // 주어진 웹소켓 객체(SockJS로 생성된 socket)을 STOMP 프로토콜로 래핑한다.
        // 래핑: 어떤 객체나 데이터를 다른 객체로 감싸는 것 => 기능을 확장하거나 변경하기 위해 새로운 레이어를 추가하는 것
        // 클라이언트 객체를 통해 STOMP 명령어를 사용할 수 있다. (CONNECT, SUBSCRIBE, SEND 등)

        this.stompClient.connect({}, (frame) => {
            console.log('Connected: ' + frame); // 연결 성공 시 프레임 정보를 콘솔에 출력
            this.subscribeToRoom(); // 채팅방 주제를 구독하는 메소드 호출
            this.joinRoom(); // 채팅방 입장 메시지를 보내는 메소드 호출
            this.loadPreviousMessages(); // 이전 채팅 메시지를 불러오는 메소드를 호출
        });
    }

    // connect(): 첫 번쨰 인자{}는 연결 시 사용할 추가 헤더로, 여기서는 비어있음 / 두 번째 인자는 연결 성공 시 실행될 콜백함수를 람다로 표현
    // frame은 연결 성공 시 서버로부터 받는 응답객체
    // 함수 내부의 this는 외부 스코프의 this와 동일하다. (lexical this)

    // frame: STOMP 프로토콜에서 클라이언트와 서버 간에 주고받는 메시지의 기본단위, 연결이 성공했을 때 서버가 보내는 응답도 하나의 frame
    // frame의 예시
    /*
    {
        command: "CONNECTED",
            headers: {
        version: "1.1",
            "heart-beat": "0,0",
            server: "Apache/2.3.45"
    },
        body: ""
    }
     */

    subscribeToRoom() {
        this.stompClient.subscribe(`/topic/chat/${this.roomId}`, (message) => {
            const chatMessage = JSON.parse(message.body);
            // `/topic/chat/${this.roomId}` 주소에서 받은 message를 매개변수로 하여 message의 본문을 JSON 형태로 변환(파싱)
            this.displayMessage(chatMessage);
            // displayMessage 메소드를 호출하여 화면에 표시
        });
    }

    // 채팅방 입장
    joinRoom() {
        this.sendMessage("", ChatMessage.MessageType.JOIN);
    }

    // 채팅방 탈퇴
    leaveRoom() {
        this.sendMessage("", ChatMessage.MessageType.LEAVE);
        this.stompClient.disconnect();
    }

    // 서버로 메시지를 전송 (/app 주소로 발신)
    sendMessage (content, type = ChatMessage.MessageType.CHAT) {
        const chatMessage = {
            senderId: this.userId, // 메시지를 보낸 사용자 Id
            senderNickname: this.nickname, // 메시지 보낸 사용자 nickname
            content: content, // 메시지 내용 (매개변수로 받은 값)
            type: type, // 메시지 타입 (매개변수로 받은 값) => ChatMessage.MessageType.CHAT
            roomType: this.roomType // 채팅방 타입 (개인/그룹)
        };
        this.stompClient.send(`/app/chat.sendMessage/${this.roomId}`, {}, JSON.stringify(chatMessage));
        // stompClient.send 메소드를 사용하여 메시지를 전송
        // 첫 번째 인자: 메시지를 보낼 목적지 주소
        // 두 번째 인자: 추가적인 헤더, 여기서는 빈 객체를 사용
        // 세 번째 인자: 메시지 본문 (객체를 JSON 문자열로 반환)
    }

    // 이전 채팅 메시지를 불러오기
    loadPreviousMessages () {
        this.stompClient.send(`/app/chat.getMessages/${this.roomId}`, {}, this.userId);
    }


}