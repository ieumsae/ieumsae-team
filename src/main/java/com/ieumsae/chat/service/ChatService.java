package com.ieumsae.chat.service;

import com.ieumsae.common.entity.*;
import com.ieumsae.common.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatService(ChatRoomRepository chatRoomRepository,
                       ChatMemberRepository chatMemberRepository,
                       MessageRepository messageRepository,
                       UserRepository userRepository,
                       StudyMemberRepository studyMemberRepository,
                       SimpMessagingTemplate messagingTemplate) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.studyMemberRepository = studyMemberRepository;
        this.messagingTemplate = messagingTemplate;
    }

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);


    /**
     * @return ChatRoom 객체 타입
     * @note findByStudyIdAndChatType 메소드를 통해 기존에 채팅방이 존재하는지 확인
     * @note 기존에 채팅방이 존재하지 않는다면 새로운 ChatRoom 객체에 studyId, chatType을 추가해서 채팅방을 생성
     */

    @Transactional
    public ChatRoom getOrCreateChatRoom(Long studyId, ChatRoom.ChatType chatType, Long userId) {
        if (chatType == ChatRoom.ChatType.PERSONAL) {
            // PERSONAL 채팅방의 경우 먼저 기존 채팅방 찾기
            Optional<ChatRoom> existingRoom = chatRoomRepository.findPersonalChatRoomByUserIdAndStudyId(userId, studyId);

            if (existingRoom.isPresent()) {
                return existingRoom.get();
            }

            // 기존 채팅방이 없으면 새로운 채팅방 생성
            ChatRoom newChatRoom = new ChatRoom();
            newChatRoom.setStudyId(studyId);
            newChatRoom.setChatType(chatType);
            ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);

            // 새로운 채팅방에 사용자 추가
            addUserToChat(savedChatRoom.getChatRoomId(), userId, chatType, studyId);

            return savedChatRoom;
        } else {
            // GROUP 채팅방의 경우 기존 로직 유지
            return chatRoomRepository.findByStudyIdAndChatType(studyId, chatType)
                    .orElseGet(() -> {
                        ChatRoom newChatRoom = new ChatRoom();
                        newChatRoom.setStudyId(studyId);
                        newChatRoom.setChatType(chatType);
                        return chatRoomRepository.save(newChatRoom);
                    });
        }
    }
    /**
     * @note 그룹채팅의 경우 유저가 해당 스터디원인지 확인
     * @note 각 chatRoomId와 userId를 통해 유효성 확인
     * @note chatRoomId와 userId로 조회했을 때, 채팅멤버에 맞는 값이 없다면 chatRoomId, userId, joinedAt DB에 저장
     * @note entryMessage에 입장메시지를 담아서 messagingTemplate로 해당 채팅방에 입장메시지를 띄워줌
     * @note ChatMember 테이블에 채팅방 인원을 추가하는 메소드
     */

    @Transactional
    public void addUserToChat(Long chatRoomId, Long userId, ChatRoom.ChatType chatType, Long studyId) {

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        if (chatType == ChatRoom.ChatType.GROUP && !canJoinGroupChat(studyId, userId)) {
            throw new IllegalArgumentException("해당 유저는 스터디원이 아닙니다.");
        }

        if (chatType == ChatRoom.ChatType.PERSONAL) {
            long memberCount = chatMemberRepository.countByChatRoomId(chatRoomId);
            if (memberCount > 2) {
                throw new IllegalStateException("개인 채팅방은 최대 2명까지만 참여할 수 있습니다.");
            }
        }

        // chatRoomId, userId로 ChatMember 테이블에 정보가 있는지 확인하고 없으면 addChatMember 메소드로 데이터를 저장
        if (!chatMemberRepository.existsByChatRoomIdAndUserId(chatRoomId, userId)) {
            addChatMember(chatRoomId, userId);
            String entryMessage = createEntryMessage(userId);
            messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId, entryMessage);
        }

        // 유저가 채팅방에 속해있는지 확인
        verifyChatRoomAndUserExistence(chatRoomId, userId);
    }

    private void verifyChatRoomAndUserExistence(Long chatRoomId, Long userId) {
        chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        userRepository.findById(userId)
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
        User userChat = userRepository.findById(userId)
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
     * @return List 형태
     * @note 이전 채팅 기록 불러오기 메소드
     */

    public List<Message> getPreviousMessages(Long chatRoomId) {
        return messageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId);
    }


    /**
     * @return 채팅방에 참여할 수 있는지 없는지 확인
     * @note .map은 일반적은 map이 아니라 Optional 타입에서 값이 존재할 때만 특정 메소드(isStatus)를 호출 -> boolean 값을 반환
     * @note studyId, userId를 통해 StudyMember 테이블에서 해당 스터디의 멤버 정보를 조회
     * @note 즉, userId를 가진 회원이 스터디에 속해있는지 확인하는 메소드
     */

    public boolean canJoinGroupChat(Long studyId, Long userId) {
        boolean canJoin = studyMemberRepository.findByStudyIdAndUserId(studyId, userId)
                .map(StudyMember::isStatus)
                .orElse(false);

        log.info("Checking if user {} can join study {}: {}", userId, studyId, canJoin);
        return canJoin;
    }

    /**
     * @param userId
     * @return '닉네임' 님이 입장하셨습니다. 라는 입장메시지가 반환
     * @note User 테이블에서 userId에 해당하는 닉네임을 불러온다.
     */

    public String createEntryMessage(Long userId) {
        User userChat = userRepository.findById(userId)
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
     *
     * @param studyId
     * @param currentUserId
     * @return
     */
    // 해당 studyId를 가진 모든 채팅방을 조회 -> 스터디 방장이 스터디 상세보기 페이지에서 모든 1:1채팅을 볼 수 있게 만드는 메소드
    // 구조상으로 스터디방장 ↔ 일반유저 상의 1:1 채팅만 가능 하기 때문에 스터디 방장만 1:1 채팅에 접근할 수 있도록 하면 된다.
    public List<Map<String, Object>> getPersonalChatRoomsForStudy(Long studyId, Long currentUserId) {
        // 1. 해당 스터디의 PERSONAL 타입 채팅방 조회
        List<ChatRoom> personalChatRooms = chatRoomRepository.findAllByStudyIdAndChatType(studyId, ChatRoom.ChatType.PERSONAL);

        // 2. 조회된 채팅방 ID 리스트 생성
        List<Long> chatRoomIds = personalChatRooms.stream().map(ChatRoom::getChatRoomId).collect(Collectors.toList());

        // 3. 채팅방 멤버 조회
        List<ChatMember> chatMembers = chatMemberRepository.findByChatRoomIdIn(chatRoomIds);

        // 4. 채팅방별 상대방 userId 매핑
        Map<Long, Long> chatRoomToOtherUserId = new HashMap<>();
        for (ChatMember member : chatMembers) {
            if (!member.getUserId().equals(currentUserId)) {
                chatRoomToOtherUserId.put(member.getChatRoomId(), member.getUserId());
            }
        }

        // 5. 상대방 userId로 User 정보 조회
        Map<Long, User> userMap = chatRoomToOtherUserId.values().stream()
                .distinct()
                .map(userRepository::findByUserId)
                .collect(Collectors.toMap(User::getUserId, user -> user));

        // 6. 결과 Map 생성 및 반환
        return personalChatRooms.stream().map(chatRoom -> {
            Map<String, Object> result = new HashMap<>();
            result.put("chatRoomId", chatRoom.getChatRoomId());
            Long otherUserId = chatRoomToOtherUserId.get(chatRoom.getChatRoomId());
            User otherUser = userMap.get(otherUserId);
            result.put("otherUserNickname", otherUser != null ? otherUser.getNickname() : "Unknown");
            // TODO: 여기에 lastMessagePreview와 lastMessageTime 설정 로직 추가 (필요시)
            // HashMap 형태로 데이터를 추가
            return result;
        }).collect(Collectors.toList());
    }

//    /**
//     * @param authentication
//     * @return userId 값을 반환
//     * @note 현재 인증된 사용자의 "Authentication" 객체에서 userId를 가져오는 역할을 한다.
//     * @note 인증된 사용자의 principal 객체가 OAuth / UserDetails 중 어떤 유형인지 확인하고 해당 객체에서 userId를 추출한다.
//     */
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