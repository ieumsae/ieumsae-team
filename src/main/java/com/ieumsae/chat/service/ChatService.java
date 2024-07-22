package com.ieumsae.chat.service;

import com.ieumsae.chat.domain.Chat;
import com.ieumsae.chat.domain.ChatEntranceLog;
import com.ieumsae.chat.domain.GroupChat;
import com.ieumsae.chat.domain.GroupChatEntranceLog;
import com.ieumsae.chat.repository.*;
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
    private final StudyGroupLogRepository studyGroupLogRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository, GroupChatRepository groupChatRepository,
                       ChatEntranceLogRepository chatEntranceLogRepository,
                       GroupChatEntranceLogRepository groupChatEntranceLogRepository, StudyGroupLogRepository studyGroupLogRepository) {
        this.chatRepository = chatRepository;
        this.groupChatRepository = groupChatRepository;
        this.chatEntranceLogRepository = chatEntranceLogRepository;
        this.groupChatEntranceLogRepository = groupChatEntranceLogRepository;
        this.studyGroupLogRepository = studyGroupLogRepository;
    }

    // 1:1 채팅 내용 포맷팅 및 DB 저장
    public Chat saveAndFormatChatMessage(Chat chatMessage) {
        if ("PERSONAL".equals(chatMessage.getChatType())) {
            if (chatMessage.getChatIdx() == null) {
                throw new IllegalArgumentException("chat_idx은 null이 될 수 없습니다.");
            }

            String formattedContent = String.format("%s: %s", chatMessage.getUserIdx(), chatMessage.getContent());
            chatMessage.setContent(formattedContent);
            chatMessage.setSendDateTime(LocalDateTime.now());
            return chatRepository.save(chatMessage);
        } else {
            throw new IllegalArgumentException("적절한 chatType이 아닙니다." + chatMessage.getChatType());
        }
    }

    // 그룹 채팅 내용 포맷팅 및 DB 저장
    public GroupChat saveAndFormatGroupChatMessage(GroupChat groupChatMessage) {
        if ("GROUP".equals(groupChatMessage.getChatType())) {
            if (groupChatMessage.getChatIdx() == null) {
                throw new IllegalArgumentException("chat_idx은 null이 될 수 없습니다.");
            }

            String formattedContent = String.format("%s: %s", groupChatMessage.getUserIdx(), groupChatMessage.getContent());
            groupChatMessage.setContent(formattedContent);
            groupChatMessage.setSendDateTime(LocalDateTime.now());
            return groupChatRepository.save(groupChatMessage);
        } else {
            throw new IllegalArgumentException("적절한 chatType이 아닙니다." + groupChatMessage.getChatType());
        }
    }


    // 채팅방에 입장 시 CHAT_ENTRANCE_LOG 테이블에 데이터 저장 및 입장메시지 출력
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

            // 특별한 타입의 메시지를 생성
            chatMessage.setContent(chatMessage.getUserIdx() + "님이 입장하셨습니다.");
            chatMessage.setChatType("ENTRANCE");  // 입장 시
            return chatMessage;
        } else {
            throw new UnsupportedOperationException("적절한 chatType이 아닙니다." + chatMessage.getChatType());
        }
    }

    public GroupChat addUserToGroupChat(GroupChat groupChatMessage, Integer chatIdx) {
        if ("GROUP".equals(groupChatMessage.getChatType())) {
            groupChatMessage.setChatIdx(chatIdx);
            groupChatMessage.setSendDateTime(LocalDateTime.now());

            Optional<GroupChatEntranceLog> existingLog = groupChatEntranceLogRepository.findByChatIdxAndUserIdx(chatIdx, groupChatMessage.getUserIdx());

            if (existingLog.isEmpty()) {
                GroupChatEntranceLog entranceLog = new GroupChatEntranceLog();
                entranceLog.setChatIdx(chatIdx);
                entranceLog.setUserIdx(groupChatMessage.getUserIdx());
                entranceLog.setEntranceDateTime(LocalDateTime.now());
                groupChatEntranceLogRepository.save(entranceLog);
            }

            // 특별한 타입의 메시지를 생성
            groupChatMessage.setContent(groupChatMessage.getUserIdx() + "님이 입장하셨습니다.");
            groupChatMessage.setChatType("ENTRANCE"); // 입장 시
            return groupChatMessage;
        } else {
            throw new IllegalArgumentException("적절한 chatType이 아닙니다." + groupChatMessage.getChatType());
        }
    }

    // 이전 채팅 내용 가져오기
    public List<Chat> getPreviousMessages(Integer chatIdx, Integer userIdx, String chatType) {

        if ("PERSONAL".equals(chatType)) {
            LocalDateTime firstEntranceDateTime = chatEntranceLogRepository
                    .findFirstByChatIdxAndUserIdxOrderByEntranceDateTimeDesc(chatIdx, userIdx) // 최근 입장기록 하나만 가져옴
                    .map(ChatEntranceLog::getEntranceDateTime) // 찾은 입장기록에서 입장 시간만 추출한다.
                    .orElse(null); // 입장기록이 없을 경우 가장 오래된 시간

            //최초 입장 시간 이후의 채팅 내용을 시간순으로 조회
            return chatRepository.findByChatIdxAndSendDateTimeGreaterThanOrderBySendDateTimeAsc(chatIdx, firstEntranceDateTime);

        } else if ("GROUP".equals(chatType)) {
            LocalDateTime firstEntranceDateTime = groupChatEntranceLogRepository
                    .findFirstByChatIdxAndUserIdxOrderByEntranceDateTimeDesc(chatIdx, userIdx) // 최근 입장기록 하나만 가져옴
                    .map(GroupChatEntranceLog::getEntranceDateTime) // 찾은 입장기록에서 입장 시간만 추출한다.
                    .orElse(null); // 입장기록이 없을 경우 가장 오래된 시간

            //최초 입장 시간 이후의 채팅 내용을 시간순으로 조회
            return groupChatRepository.findByChatIdxAndSendDateTimeGreaterThanOrderBySendDateTimeAsc(chatIdx, firstEntranceDateTime);
        }

        // 모든 경로에서 값을 반환하도록 빈 리스트 반환
        return List.of();
        // 반환값이 "PERSONAL"과 "GROUP" 둘 다 아닐 때 (예상치 못한 예외상황이 발생했을 때)
    }

    // 1:1 chatIdx 만들기
    public int createChatIdx(Integer userIdx, Integer studyIdx, String chatType) {

        if ("PERSONAL".equals(chatType)) {

            // studyIdx와 매칭되는 userIdx를 가져옴 (STUDY_GROUP_LOG 테이블에 studyIdx를 통해 userIdx를 불러온다. -> 스터디 방장의 userIdx)
            Integer matchingUserIdx = studyGroupLogRepository.findUserIdxByStudyIdx(studyIdx)
                    .orElseThrow(() -> new IllegalArgumentException("일치하는 studyIdx 값이 없습니다."));

            // 1 + userIdx(사용자) + matchingUserIdx (스터디 방장) (찾은 userIdx 값으로 chatIdx값을 생성, 9자리)
            String chatIdxString = "1" + String.format("%04d", userIdx) + String.format("%04d", matchingUserIdx);

            // Integer 타입으로 형변환
            return Integer.parseInt(chatIdxString);

        } else {
            // 그룹 chatIdx 만들기

            // studyIdx와 매칭되는 userIdx를 가져옴 (STUDY_GROUP_LOG 테이블에 studyIdx를 통해 userIdx를 불러온다. -> 스터디 방장의 userIdx)
            Integer matchingUserIdx = studyGroupLogRepository.findUserIdxByStudyIdx(studyIdx)
                    .orElseThrow(() -> new IllegalArgumentException("일치하는 studyIdx 값이 없습니다."));

            // 2 + userIdx (스터디 방장) + studyIdx (스터디 번호)
            String chatIdxString = "2" + String.format("%04d", studyIdx) + String.format("%04d", matchingUserIdx);

            // Integer 타입으로 형변환
            return Integer.parseInt(chatIdxString);
        }


    }
}










