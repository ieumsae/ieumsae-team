package com.ieumsae.chat.controller;

import com.ieumsae.chat.domain.ChatMessage;
import com.ieumsae.chat.repository.ChatMessageRepository;
import com.ieumsae.chat.repository.ChatRoomRepository;
import com.ieumsae.chat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;


@Controller
public class ChatController {

    private ChatService chatService;
    private ChatMessageRepository chatMessageRepository;
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    public ChatController(ChatService chatService, ChatMessageRepository chatMessageRepository, ChatRoomRepository chatRoomRepository) {
        this.chatService = chatService;
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

    /*
     * 1. 채팅방 연결: /app/chat.addUser/{roomId}로 메시지 전송
     * 2. 메시지 전송: /app/chat.sendMessage/{roomId}로 메시지 전송
     * 3. 채팅방 퇴장: /app/chat.leaveUser/{roomId}로 메시지 전송
     * 4. 메시지 수신: /topic/chat/{roomId} 주제 구독
     */

    // 메시지에 관한 로직 (MessageType CHAT)
    @MessageMapping("/chat.sendMessage/{roomId}") // 클라이언트 -> 서버 ("/app/chat.sendMessage/{roomId}")
    // 웹소켓을 통해 들어오는 메시지의 경로, /chat.sendMessage/{roomId}는 클라이언트가 메시지를 보낼 때 사용하는 주소
    // 실제 전체 주소는 WebSocketConfig에서 설정한 "/app"과 결합하여 "app/chat.sendMessage/{roomId}"가 될 수 있다.
    // 클라이언트가 이 주소로 메시지를 보내면 이 메소드가 호출된다.
    // config.setApplicationDestinationPrefixes("/app");
    @SendTo("/topic/chat/{roomId}") // 서버 -> 클라이언트 ("/topic/chat/{roomId}")
    // 메소드의 반환값을 어디로 보낼지 지정한다.
    // "/topic"은 메시지를 발행할 주제
    // 이 주제를 구독하고 있는 모든 클라이언트에게 메시지가 브로드캐스트
    // config.enableSimpleBroker("/topic")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage, @DestinationVariable String roomId) {
        chatMessage.setTimestamp(LocalDateTime.now()); // 메시지를 보낸 시간

        if (chatMessage.getRoomType() == ChatMessage.RoomType.PERSONAL) {
            // 1:1채팅 처리
            chatService.formatPersonalChatMessage(chatMessage);
        } else {
            // 그룹채팅 처리
            chatService.formatGroupChatMessage(chatMessage);
        }
        chatMessageRepository.save(chatMessage);
        return chatMessage;
    }


    /* 정리
        1. "/app/chat.sendMessage/{roomId}" 주소로 메시지가 전송되면 클라이언트에서 서버로 메시지가 전달되고 메소드가 호출된다.
        2. sendPersonalMessage 메소드는 메시지 본문과 receiverId를 매개변수로 받아 ChatMessage 객체를 처리하고 반환한다
        3. ChatMessage 타입으로 변환된 메시지는 @SendTo에 적혀있는 "/topic/chat/{roomId}" 주소로 전송된다.
        4. 이는 서버에서 클라이언트로 전송된다.
        5. 현재 당장은 클라이언트에게 보이지는 않고 JS를 통해서 클라이언트가 "/topic/chat/{roomId}" 주소를 구독하고 있어야 받을 수 있다.
        6. 클라이언트 측에서 구독한 주소로 메시지가 도착하면, JavaScript 코드를 통해 메시지를 처리하고 화면에 표시하는 로직을 구현해야 한다.
     */

    @MessageMapping("/chat.addUser/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, @DestinationVariable String roomId, SimpMessageHeaderAccessor headerAccessor) {
        // chatMessage(메시지의 본문), URL의 {roomId} 부분을 추출하여 roomId 변수에 저장, headerAccessor는 메시지의 헤더 정보에 접근할 수 있게 해주는 객체
        String userNickname = chatMessage.getSenderNickname();
        headerAccessor.getSessionAttributes().put("userNickname", chatMessage.getSenderNickname());
        // 세션 속성에 사용자의 닉네임을 저장, 이후 세션을 통해 사용자를 식별할 수 있다.
        headerAccessor.getSessionAttributes().put("roomId", roomId);
        // 세션 속성에 현재 채팅방 id를 저장한다. 사용자가 어느 채팅방에 있는지 추적한다.

        // 채팅방 타입에 따라 처리방식을 따로 함
        if (chatMessage.getRoomType() == ChatMessage.RoomType.PERSONAL) {
            // 1:1 채팅방 처리 로직
            chatMessage.setContent(userNickname + "님이 입장하셨습니다.");
        } else if (chatMessage.getRoomType() == ChatMessage.RoomType.GROUP) {
            // 그룹 채팅방 처리 로직
            chatMessage.setContent(userNickname + "님이 입장하셨습니다.");
        }

        // 서비스 메소드에 addUserToRoom을 호출
        ChatMessage resultMessage = chatService.addUserToRoom(chatMessage, roomId, userNickname);
        return resultMessage;

    }

    //채팅방을 종료
    @MessageMapping("/chat.leaveRoom/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessage leaveRoom(@Payload ChatMessage chatMessage, @DestinationVariable String roomId, SimpMessageHeaderAccessor headerAccessor) {
        String userNickname = (String) headerAccessor.getSessionAttributes().get("userNickname");
        // 채팅방을 나가는 것은 채팅방에 존재하는 유저의 정보를 불러오는 것이기 때문에 .get을 사용
        if (userNickname != null) {
            chatMessage.setType(ChatMessage.MessageType.LEAVE); // enum 타입
            chatMessage.setSenderNickname(userNickname);
            return chatService.leaveUserFromRoom(chatMessage, roomId);
        }
        return chatMessage;
    }

    // 이전 채팅내용 불러오기
    @MessageMapping("/chat.getMessages/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    public List<ChatMessage> getMessages(@DestinationVariable String roomId, @Payload String userId, SimpMessageHeaderAccessor headerAccessor) {
        // 사용자 인증 정보 확인 (옵션)
        String authenticatedUserId = (String) headerAccessor.getSessionAttributes().get("userId");
        if (authenticatedUserId == null || !authenticatedUserId.equals(userId)) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }

        return chatService.getMessagesAfterUserJoin(roomId, userId);
        // 이전 채팅 내역을 불러오는 메소드
    }


}