package com.ieumsae.chat.service;

import com.ieumsae.chat.domain.Chat;
import com.ieumsae.chat.domain.ChatEntranceLog;
import com.ieumsae.chat.domain.GroupChat;
import com.ieumsae.chat.repository.ChatEntranceLogRepository;
import com.ieumsae.chat.repository.ChatRepository;
import com.ieumsae.chat.repository.GroupChatEntranceLogRepository;
import com.ieumsae.chat.repository.GroupChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final ChatRepository chatRepository;
    private final GroupChatRepository groupChatRepository;
    private final ChatEntranceLogRepository chatEntranceLogRepository;
    private final GroupChatEntranceLogRepository groupChatEntranceLogRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository, GroupChatRepository groupChatRepository,
                       ChatEntranceLogRepository chatEntranceLogRepository,
                       GroupChatEntranceLogRepository groupChatEntranceLogRepository) {
        this.chatRepository = chatRepository;
        this.groupChatRepository = groupChatRepository;
        this.chatEntranceLogRepository = chatEntranceLogRepository;
        this.groupChatEntranceLogRepository = groupChatEntranceLogRepository;
    }

    public Chat saveAndFormatChatMessage(Chat chatMessage) {
        if ("PERSONAL".equals(chatMessage.getChatType())) {
            if (chatMessage.getChatIdx() == null) {
                throw new IllegalArgumentException("chat_idx cannot be null");
            }
            // 채팅 메시지의 content 부분에 시간 나오는 기능을 삭제
            // String formattedTime = chatMessage.getSendDateTime().format(TIME_FORMATTER);
            String formattedContent = String.format("%s: %s", chatMessage.getUserIdx(), chatMessage.getContent());
            chatMessage.setContent(formattedContent);
            return chatRepository.save(chatMessage);
        } else {
            throw new UnsupportedOperationException("그룹 채팅은 현재 지원되지 않습니다.");
        }
    }

    public GroupChat saveAndFormatGroupChatMessage(GroupChat groupChatMessage) {
        throw new UnsupportedOperationException("그룹 채팅은 현재 지원되지 않습니다.");
    }


    public Chat addUserToChat(Chat chatMessage, Integer chatIdx) {
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

            // 특별한 타입의 메시지를 생성합니다.
            chatMessage.setContent(chatMessage.getUserIdx() + "님이 입장하셨습니다.");
            chatMessage.setChatType("ENTRANCE");  // 새로운 타입 추가
            return chatMessage;
        } else {
            throw new UnsupportedOperationException("그룹 채팅은 현재 지원되지 않습니다.");
        }
    }

    public GroupChat addUserToGroupChat(GroupChat groupChatMessage, Integer groupChatIdx) {
        throw new UnsupportedOperationException("그룹 채팅은 현재 지원되지 않습니다.");
    }

    // 이전 채팅 내용 가져오기
    public List<Chat> getPreviousMessages(Integer chatIdx, Integer userIdx) {
        LocalDateTime firstEntranceDateTime = chatEntranceLogRepository
                .findFirstByChatIdxAndUserIdxOrderByEntranceDateTimeDesc(chatIdx, userIdx) // 최근 입장기록 하나만 가져옴
                .map(ChatEntranceLog::getEntranceDateTime) // 찾은 입장기록에서 입장 시간만 추출한다.
                .orElse(null); // 입장기록이 없을 경우 가장 오래된 시간
        
        //최초 입장 시간 이후의 채팅 내용을 시간순으로 조회
        return chatRepository.findByChatIdxAndSendDateTimeGreaterThanOrderBySendDateTimeDesc(chatIdx, firstEntranceDateTime);
    }

}