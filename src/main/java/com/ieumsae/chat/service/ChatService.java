package com.ieumsae.chat.service;

import com.ieumsae.chat.domain.ChatMessage;
import com.ieumsae.chat.domain.ChatRoom;
import com.ieumsae.chat.repository.ChatMessageRepository;
import com.ieumsae.chat.repository.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    // 시간 형식을 시:분 형태로 상수정의

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Autowired
    public ChatService(ChatMessageRepository chatMessageRepository, ChatRoomRepository chatRoomRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
    }


    public void formatPersonalChatMessage(ChatMessage chatMessage) {
        // 1. 시간 형식 지정 (시:분 까지)
        String formattedTime = chatMessage.getTimestamp().format(TIME_FORMATTER);

        // 2. 메시지 내용 포맷팅
        String formattedContent = String.format("[개인] %s (%s) %s", chatMessage.getSenderNickname(), formattedTime, chatMessage.getContent());
        // String.format()은 JAVA에서 문자열을 포맷팅하는 유용한 메소드
        // 일반 텍스트와 포맷 지정자를 포함하며 (format, args ...) 형태로 만들어져 1:1 대응이 되어야 한다.
        // format 지정자에 args값이 들어와 새로운 문자열을 만들어 준다.

        // 3. 포맷팅된 내용을 chatMessage 객체에 설정
        chatMessage.setContent(formattedContent);

    }


    public void formatGroupChatMessage(ChatMessage chatMessage) {
        // 1. 시간 형식 지정 (시:분)
        String formattedTime = chatMessage.getTimestamp().format(TIME_FORMATTER);

        // 2. 메시지 내용 포맷팅
        String formattedContent = String.format("[개인] %s (%s) %s", chatMessage.getSenderNickname(), formattedTime, chatMessage.getContent());

        //3. 포맷팅 된 내용을 chatMessage 객체에 설정
        chatMessage.setContent(formattedContent);

    }


    public ChatMessage addUserToRoom(ChatMessage chatMessage, String roomId, String userNickname) {
        // 채팅방 존재 여부 확인
        Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findByRoomId(roomId);
        // 채팅방이 존재하지 않을 경우
        if (!chatRoomOptional.isPresent()) {
            throw new RuntimeException(roomId + "채팅방을 찾을 수 없습니다.");
        }

        ChatRoom chatRoom = chatRoomOptional.get(); // 객체 내의 값을 불러옴

        chatMessage.setChatRoom(chatRoom);
        chatMessage.setType(ChatMessage.MessageType.JOIN);
        // 메시지 타입을 JOIN으로 설정한다. 사용자가 채팅방에 입장했음을 나타낸다.
        // domain에서 설정한 enum MessageType에서의 JOIN을 나타낸다.
        chatMessage.setContent(userNickname + "님이 입장하셨습니다.");
        // 메시지 내용을 설정한다. 입장메시지를 다른 사용자들에게 알린다.
        chatMessage.setTimestamp(LocalDateTime.now());

        // 사용자의 입장 시간이 없을 경우에만 저장 (최초 입장 시에만 저장)
        if (!chatRoom.getUserJoinTimes().containsKey(chatMessage.getSenderId())) {
            // .containsKey는 지정된 키가 Map에 있으면 true, 없으면 false
            // chatMessage.getSenderId()가 키가 되고 해당 키를 Map에서 찾는다.
            chatRoom.getUserJoinTimes().put(chatMessage.getSenderId(), LocalDateTime.now());
            chatRoomRepository.save(chatRoom);
        }

        return chatMessageRepository.save(chatMessage);
        // 수정된 chatMessage 객체를 반환하고 @SendTo 어노테이션에 의해 지정된 주제로 브로드캐스트

    }


    public ChatMessage leaveUserFromRoom(ChatMessage chatMessage, String roomId) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException(roomId + " 채팅방을 찾을 수 없습니다."));

        chatMessage.setChatRoom(chatRoom);
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setType(ChatMessage.MessageType.LEAVE);

        if (chatMessage.getRoomType() == ChatMessage.RoomType.PERSONAL) {
            chatMessage.setContent("[개인] " + chatMessage.getSenderNickname() + "님이 채팅방을 나갔습니다.");
        } else {
            chatMessage.setContent("[그룹] " + chatMessage.getSenderNickname() + "님이 채팅방을 나갔습니다.");
        }

        return chatMessageRepository.save(chatMessage);
    }

    // 이전 채팅 기록 가져오기, 채팅방에 참여했을 때,
    public List<ChatMessage> getMessagesAfterUserJoin(String roomId, String userId) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException(roomId + " 채팅방을 찾을 수 없습니다."));

        LocalDateTime joinTime = chatRoom.getUserJoinTimes().get(userId);
        if (joinTime == null) {
            throw new RuntimeException("사용자의 입장 시간 정보를 찾을 수 없습니다.");
        }

        List<ChatMessage> allMessages = chatMessageRepository.findByChatRoomOrderByTimestampAsc(chatRoom);
        // 특정 채팅방(ChatRoom)의 모든 채팅 메시지를 시간 순서대로 조회한다.

        List<ChatMessage> messagesAfterJoin = allMessages.stream().filter(message -> message.getTimestamp().isAfter(joinTime)).collect(Collectors.toList());
        // 조회한 모든 데이터 (allMessages)
        // stream(): 데이터의 흐름을 나타내고, 여러 연산을 체이닝해서 수행할 수 있게 만들어준다.
        // filter(): 스트림의 요소들을 특정 조건에 따라 필터링
        // message.getTimestamp().isAfter(joinTime)): 조회한 메시지의 timestamp를 가져와서 joinTime보다 이후인지 확인한다.
        // collect: 스트림의 요소들을 최종 결과로 반환한다.
        // Collectors.toList(): 스트림의 요소들을 새로운 List로 수집한다.
        // 정리: 모든 메시지를 스트림으로 변환하고 사용자 입장시간(joinTime) 이후인 메시지만 필터링한다. 필터링 된 메시지들을 새로운 List로 수집한다.

        if (chatRoom.getRoomType().equals("PERSONAL")) {
            System.out.println("1:1 채팅 메시지를 조회합니다.");
        } else if (chatRoom.getRoomType().equals("GROUP")) {
            System.out.println("그룹 채팅 메시지를 조회합니다.");
        }

        return messagesAfterJoin;
    }


}