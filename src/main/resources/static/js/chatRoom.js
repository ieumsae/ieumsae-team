document.addEventListener('DOMContentLoaded', function() {
    const chatIdx = /*[[${chatIdx}]]*/ [[${chatIdx}]];
    const userIdx = /*[[${userIdx}]]*/ [[${userIdx}]];
    const chatType = "PERSONAL";
    const previousMessages = /*[[${previousMessages}]]*/ [[${previousMessages}]];

    console.log("chatIdx:", chatIdx, "userIdx:", userIdx);

    if (chatIdx === null || userIdx === null) {
        console.error("chat.js: chatIdx나 userIdx가 null입니다.");
        // 에러 처리 로직
        alert("채팅방 정보를 불러오는데 실패했습니다. 이전 페이지로 돌아갑니다.");
        window.history.back();
    } else {
        const chatClient = new ChatClient();
        // WebSocket 연결 및 이전 메시지 로드
        chatClient.connect(chatIdx, userIdx, chatType);

        // 이전 메시지 표시
        if (previousMessages && previousMessages.length > 0) {
            // previousMessages가 null이나 undefined인지 + 배열의 크기가 0보다 큰지
            chatClient.loadPreviousMessages(previousMessages);
        }

        // 메시지 전송 버튼 이벤트 리스너
        document.getElementById('send-button').addEventListener('click', sendMessage);

        // 엔터 키 입력 시 메시지 전송
        document.getElementById('message-input').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                sendMessage();
            }
        });
    }

    // 메시지 전송 함수
    function sendMessage() {
        const messageInput = document.getElementById('message-input');
        const message = messageInput.value.trim();
        if (message) {
            chatClient.sendMessage(message);
            messageInput.value = '';
        }
    }

    // 페이지 나갈 때 연결 종료
    window.addEventListener('beforeunload', function() {
        chatClient.leaveChat();
    });
});
