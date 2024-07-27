package com.ieumsae.chat.service;

import com.ieumsae.common.entity.*;
import com.ieumsae.common.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepositoryChat;
    private final StudyMemberRepository studyMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatService(ChatRoomRepository chatRoomRepository,
                       ChatMemberRepository chatMemberRepository,
                       MessageRepository messageRepository,
                       UserRepository userRepositoryChat,
                       StudyMemberRepository studyMemberRepository,
                       SimpMessagingTemplate messagingTemplate) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.messageRepository = messageRepository;
        this.userRepositoryChat = userRepositoryChat;
        this.studyMemberRepository = studyMemberRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * @param studyId
     * @param chatType
     * @return ChatRoom 객체 타입
     * @note findByStudyIdAndChatType 메소드를 통해 기존에 채팅방이 존재하는지 확인
     * @note 기존에 채팅방이 존재하지 않는다면 새로운 ChatRoom 객체에 studyId, chatType을 추가해서 채팅방을 생성
     */

    public ChatRoom getOrCreateChatRoom(Long studyId, ChatRoom.ChatType chatType) {
        return chatRoomRepository.findByStudyIdAndChatType(studyId, chatType)
                .orElseGet(() -> {
                    ChatRoom newChatRoom = new ChatRoom();
                    newChatRoom.setStudyId(studyId);
                    newChatRoom.setChatType(chatType);
                    return chatRoomRepository.save(newChatRoom);
                });
    }

    /**
     * @param chatRoomId
     * @param userId
     * @param chatType
     * @param studyId
     * @note 그룹채팅의 경우 유저가 해당 스터디원인지 확인
     * @note 각 chatRoomId와 userId를 통해 유효성 확인
     * @note chatRoomId와 userId로 조회했을 때, 채팅멤버에 맞는 값이 없다면 chatRoomId, userId, joinedAt DB에 저장
     * @note entryMessage에 입장메시지를 담아서 messagingTemplate로 해당 채팅방에 입장메시지를 띄워줌
     * @note ChatMember 테이블에 채팅방 인원을 추가하는 메소드
     */

    @Transactional
    public void addUserToChat(Long chatRoomId, Long userId, ChatRoom.ChatType chatType, Long studyId) {
        if (chatType == ChatRoom.ChatType.GROUP && !canJoinGroupChat(studyId, userId)) {
            throw new IllegalArgumentException("해당 유저는 스터디원이 아닙니다.");
        }

        // 유저가 채팅방에 속해있는지 확인
        verifyChatRoomAndUserExistence(chatRoomId, userId);

        // chatRoomId, userId로 ChatMember 테이블에 정보가 있는지 확인하고 없으면 addChatMember 메소드로 데이터를 저장
        if (!chatMemberRepository.existsByChatRoomIdAndUserId(chatRoomId, userId)) {
            addChatMember(chatRoomId, userId);
            String entryMessage = createEntryMessage(userId);
            messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId, entryMessage);
        }
    }

    private void verifyChatRoomAndUserExistence(Long chatRoomId, Long userId) {
        chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        userRepositoryChat.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
    }

    private void addChatMember(Long chatRoomId, Long userId) {
        ChatMember chatMember = new ChatMember();
        chatMember.setChatRoomId(chatRoomId);
        chatMember.setUserId(userId);
        chatMember.setJoinedAt(LocalDateTime.now());
        chatMemberRepository.save(chatMember);
    }

    /**
     * @param chatRoomId
     * @param userId
     * @param content
     * @return Message 객체 타입
     * @note 메시지를 받아서 저장하고 문자열 포맷팅 후 채팅방에 띄워주는 메소드
     */

    @Transactional
    public Message saveAndSendMessage(Long chatRoomId, Long userId, String content, Long currentUserId) {

        // 채팅방 존재 여부 확인
        chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        // 유저 존재 여부 확인
        User userChat = userRepositoryChat.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // 메시지 포맷팅
        String formattedContent;
        if (userId.equals(currentUserId)) {
            formattedContent = String.format("나: %s", content);
        } else {
            formattedContent = String.format("%s: %s", userChat.getNickname(), content);
        }

        // 메시지 생성 및 저장
        Message message = new Message();
        message.setChatRoomId(chatRoomId);
        message.setUserId(userId);
        message.setContent(formattedContent);
        message.setSentAt(LocalDateTime.now());

        // WebSocket을 통해 메시지 전송
        return messageRepository.save(message);
    }

    /**
     * @param chatRoomId
     * @return List 형태
     * @note 이전 채팅 기록 불러오기 메소드
     */

    public List<Message> getPreviousMessages(Long chatRoomId) {
        return messageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId);
    }


    /**
     * @param studyId
     * @param userId
     * @return 채팅방에 참여할 수 있는지 없는지 확인
     * @note .map은 일반적은 map이 아니라 Optional 타입에서 값이 존재할 때만 특정 메소드(isStatus)를 호출 -> boolean 값을 반환
     * @note studyId, userId를 통해 StudyMember 테이블에서 해당 스터디의 멤버 정보를 조회
     * @note 즉, userId를 가진 회원이 스터디에 속해있는지 확인하는 메소드
     */

    public boolean canJoinGroupChat(Long studyId, Long userId) {
        return studyMemberRepository.findByStudyIdAndUserId(studyId, userId)
                .map(StudyMember::isStatus) // Optional 값이 존재할 경우 isStatus 메소드를 호출, isStatus는 status 필드에 대한 getter
                .orElse(false); // Optional 객체의 값이 존재하지 않을 경우 default 값을 false로 설정
    }

    /**
     * @param userId
     * @return '닉네임' 님이 입장하셨습니다. 라는 입장메시지가 반환
     * @note User 테이블에서 userId에 해당하는 닉네임을 불러온다.
     */

    public String createEntryMessage(Long userId) {
        User userChat = userRepositoryChat.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));
        return userChat.getNickname() + "님이 입장하셨습니다.";
    }

    /**
     * @param chatRoomId
     * @return 채팅방을 조회하는 기능
     */

    public ChatRoom getChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
    }

    /**
     * @param authentication
     * @return userId 값을 반환
     * @note 현재 인증된 사용자의 "Authentication" 객체에서 userId를 가져오는 역할을 한다.
     * @note 인증된 사용자의 principal 객체가 OAuth / UserDetails 중 어떤 유형인지 확인하고 해당 객체에서 userId를 추출한다.
     */
//    public Long getCurrentUserId(Authentication authentication) {
//        Object principal = authentication.getPrincipal();
//        if (principal instanceof CustomOAuth2User) {
//            return ((CustomOAuth2User) principal).getUserId();
//        } else if (principal instanceof CustomUserDetails) {
//            return ((CustomUserDetails) principal).getUserId();
//        }
//        throw new RuntimeException("알 수 없는 유저 타입입니다. " + principal.getClass().getName());
//    }
}