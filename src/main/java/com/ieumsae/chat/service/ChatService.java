package com.ieumsae.chat.service;

import com.ieumsae.chat.domain.Chat;
import com.ieumsae.chat.domain.ChatEntranceLog;
import com.ieumsae.chat.domain.GroupChat;
import com.ieumsae.chat.domain.GroupChatEntranceLog;
import com.ieumsae.chat.repository.ChatEntranceLogRepository;
import com.ieumsae.chat.repository.ChatRepository;
import com.ieumsae.chat.repository.GroupChatEntranceLogRepository;
import com.ieumsae.chat.repository.GroupChatRepository;
import com.ieumsae.chat.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final GroupChatRepository groupChatRepository;
    private final ChatEntranceLogRepository chatEntranceLogRepository;
    private final GroupChatEntranceLogRepository groupChatEntranceLogRepository;
    private final UserInfoRepository userInfoRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository, GroupChatRepository groupChatRepository,
                       ChatEntranceLogRepository chatEntranceLogRepository,
                       GroupChatEntranceLogRepository groupChatEntranceLogRepository,
                       UserInfoRepository userInfoRepository) {
        this.chatRepository = chatRepository;
        this.groupChatRepository = groupChatRepository;
        this.chatEntranceLogRepository = chatEntranceLogRepository;
        this.groupChatEntranceLogRepository = groupChatEntranceLogRepository;
        this.userInfoRepository = userInfoRepository;
    }

    // 개인 채팅 메시지 저장
    public Chat savePersonalChatMessage(Chat chatMessage) {
        if ("PERSONAL".equals(chatMessage.getChatType())) {
            if (chatMessage.getChatIdx() == null) {
                throw new IllegalArgumentException("chat_idx cannot be null");
            }
            return chatRepository.save(chatMessage);
        } else {
            throw new UnsupportedOperationException("Only personal chat is supported in this method.");
        }
    }

    // 그룹 채팅 메시지 저장
    public GroupChat saveGroupChatMessage(GroupChat groupChatMessage) {
        if ("GROUP".equals(groupChatMessage.getGroupChatType())) {
            if (groupChatMessage.getGroupChatIdx() == null) {
                throw new IllegalArgumentException("group_chat_idx cannot be null");
            }
            return groupChatRepository.save(groupChatMessage);
        } else {
            throw new UnsupportedOperationException("Only group chat is supported in this method.");
        }
    }

    // 개인 채팅방 입장
    public Chat addUserToPersonalChat(Chat chatMessage, Integer chatIdx) {
        if ("PERSONAL".equals(chatMessage.getChatType())) {
            chatMessage.setChatIdx(chatIdx);
            chatMessage.setSendDateTime(LocalDateTime.now());

            Optional<ChatEntranceLog> existingLog = chatEntranceLogRepository.findByChatIdxAndUserIdx(chatIdx, chatMessage.getUserIdx());

            if (existingLog.isEmpty()) {
                ChatEntranceLog entranceLog = new ChatEntranceLog();
                entranceLog.setChatIdx(chatIdx);
                entranceLog.setUserIdx(chatMessage.getUserIdx());
                entranceLog.setEntranceDateTime(LocalDateTime.now());
                chatEntranceLogRepository.save(entranceLog);
            }

            // 입장 메시지 생성
            chatMessage.setContent(chatMessage.getNickName() + "님이 입장하셨습니다."); // nickName 사용
            chatMessage.setChatType("ENTRANCE");
            return chatMessage;
        } else {
            throw new UnsupportedOperationException("Only personal chat is supported in this method.");
        }
    }

    // 그룹 채팅방 입장
    public GroupChat addUserToGroupChat(GroupChat groupChatMessage, Integer groupChatIdx) {
        if ("GROUP".equals(groupChatMessage.getGroupChatType())) {
            groupChatMessage.setGroupChatIdx(groupChatIdx);
            groupChatMessage.setSendDateTime(LocalDateTime.now());

            Optional<GroupChatEntranceLog> existingLog = groupChatEntranceLogRepository.findByGroupChatIdxAndUserIdx(groupChatIdx, groupChatMessage.getUserIdx());

            if (existingLog.isEmpty()) {
                GroupChatEntranceLog entranceLog = new GroupChatEntranceLog();
                entranceLog.setGroupChatIdx(groupChatIdx);
                entranceLog.setUserIdx(groupChatMessage.getUserIdx());
                entranceLog.setEntranceDateTime(LocalDateTime.now());
                groupChatEntranceLogRepository.save(entranceLog);
            }

            // 입장 메시지 생성
            groupChatMessage.setContent(groupChatMessage.getNickName() + "님이 입장하셨습니다."); // nickName 사용
            groupChatMessage.setGroupChatType("ENTRANCE");
            return groupChatMessage;
        } else {
            throw new UnsupportedOperationException("Only group chat is supported in this method.");
        }
    }

    // 개인 채팅의 이전 메시지 가져오기
    public List<Chat> getPreviousPersonalMessages(Integer chatIdx, Integer userIdx) {
        Optional<ChatEntranceLog> entranceLog = chatEntranceLogRepository.findFirstByChatIdxAndUserIdxOrderByEntranceDateTimeDesc(chatIdx, userIdx);
        if (entranceLog.isPresent()) {
            LocalDateTime firstEntranceDateTime = entranceLog.get().getEntranceDateTime();
            List<Chat> messages = chatRepository.findByChatIdxAndSendDateTimeGreaterThanOrderBySendDateTimeAsc(chatIdx, firstEntranceDateTime);
            return addNickNamesToChats(messages);
        } else {
            return List.of(); // 빈 리스트 반환
        }
    }

    // 그룹 채팅의 이전 메시지 가져오기
    public List<GroupChat> getPreviousGroupMessages(Integer groupChatIdx, Integer userIdx) {
        Optional<GroupChatEntranceLog> entranceLog = groupChatEntranceLogRepository.findFirstByGroupChatIdxAndUserIdxOrderByEntranceDateTimeDesc(groupChatIdx, userIdx);
        if (entranceLog.isPresent()) {
            LocalDateTime firstEntranceDateTime = entranceLog.get().getEntranceDateTime();
            List<GroupChat> messages = groupChatRepository.findByGroupChatIdxAndSendDateTimeGreaterThanOrderBySendDateTimeAsc(groupChatIdx, firstEntranceDateTime);
            return addNickNamesToGroupChats(messages);
        } else {
            return List.of(); // 빈 리스트 반환
        }
    }

    // 개인 채팅방 접속 여부 확인
    public boolean existsByChatIdxAndUserIdx(Integer chatIdx, Integer userIdx) {
        return chatEntranceLogRepository.existsByChatIdxAndUserIdx(chatIdx, userIdx);
    }

    // 그룹 채팅방 접속 여부 확인
    public boolean existsByGroupChatIdxAndUserIdx(Integer groupChatIdx, Integer userIdx) {
        return groupChatEntranceLogRepository.existsByGroupChatIdxAndUserIdx(groupChatIdx, userIdx);
    }

    // 그룹 채팅 메시지 생성
    public GroupChat createGroupChatMessage(Integer groupChatIdx, Chat chatMessage) {
        GroupChat groupChatMessage = new GroupChat();
        groupChatMessage.setGroupChatIdx(groupChatIdx);
        groupChatMessage.setUserIdx(chatMessage.getUserIdx());
        groupChatMessage.setContent(chatMessage.getContent());
        groupChatMessage.setSendDateTime(chatMessage.getSendDateTime());
        return groupChatMessage;
    }

    // 채팅 메시지 리스트에 닉네임 추가
    private List<Chat> addNickNamesToChats(List<Chat> messages) {
        messages.forEach(chat -> {
            userInfoRepository.findByUserIdx(chat.getUserIdx()).ifPresent(userInfo -> chat.setNickName(userInfo.getNickName()));
        });
        return messages;
    }

    // 그룹 채팅 메시지 리스트에 닉네임 추가
    private List<GroupChat> addNickNamesToGroupChats(List<GroupChat> messages) {
        messages.forEach(chat -> {
            userInfoRepository.findByUserIdx(chat.getUserIdx()).ifPresent(userInfo -> chat.setNickName(userInfo.getNickName()));
        });
        return messages;
    }
    
    // 추가 로직 예정 
}
