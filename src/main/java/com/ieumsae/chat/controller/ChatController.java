package com.ieumsae.chat.controller;

import com.ieumsae.chat.domain.Chat;
import com.ieumsae.chat.domain.GroupChat;
import com.ieumsae.chat.repository.ChatEntranceLogRepository;
import com.ieumsae.chat.service.ChatService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ChatController {

    private final ChatService chatService;
    private final ChatEntranceLogRepository chatEntranceLogRepository;
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);


    @Autowired
    public ChatController(ChatService chatService, ChatEntranceLogRepository chatEntranceLogRepository) {
        this.chatService = chatService;
        this.chatEntranceLogRepository = chatEntranceLogRepository;
    }

    // 채팅 페이지 연결
    @GetMapping("/chat")
    public String chatPage() {
        return "chat";  // chat.html을 렌더링
    }

    // 채팅방 연결
    @PostMapping("/enterChat")
    public String enterChat(@RequestParam("studyIdx") Integer studyIdx, HttpSession session, Model model) {
        // studyIdx 값은 프론트에서 {studyIdx} URL GET 방식으로 받아옴

        // 세션에서 userIdx를 받아오기
        // session.getAttribute는 반환타입이 객체타입
        Integer userIdx = (Integer) session.getAttribute("userIdx");

        // 만든 chatIdx값을 가져옴 (int)
        int chatIdx = chatService.createChatIdx(userIdx, studyIdx);

        // 파라미터 유효성 검사 및 로깅
        if (studyIdx == 0 || userIdx == 0) {
            logger.error("Invalid parameters: chatIdx={}, userIdx={}", chatIdx, userIdx);
            return "error"; // 에러 페이지로 리다이렉트
        }

        logger.info("Entering chat: chatIdx={}, userIdx={}", chatIdx, userIdx);

        try {
            logger.info("생성된 chatIdx={}", chatIdx);

            // CHAT_ENTRANCE_LOG 테이블에 존재하는지 확인
            if (!chatEntranceLogRepository.existsByChatIdxAndUserIdx(chatIdx, userIdx)) {
                // 최초 접속 시
                logger.info("최초 접속: chatIdx={}, userIdx={}", chatIdx, userIdx);
            } else {
                // 재접속 시
                logger.info("재접속: chatIdx={}, userIdx={}", chatIdx, userIdx);
                List<Chat> previousMessages = chatService.getPreviousMessages(chatIdx, userIdx);
                model.addAttribute("previousMessages", previousMessages);
            }
            return "chatRoom"; // chatRoom.html로 이동
        } catch (Exception e) {
            logger.error("Error while entering chat: chatIdx={}, userIdx={}", chatIdx, userIdx, e);
            return "error";
        }
    }


    // 개인 채팅 메시지 전송
    @MessageMapping("/chat.sendMessage/{chatIdx}")
    @SendTo("/topic/chat/{chatIdx}")
    public Chat sendMessage(@DestinationVariable Integer chatIdx, @Payload Chat chatMessage) {
        if (chatIdx == null || chatMessage.getChatIdx() == null) {
            throw new IllegalArgumentException("chatIdx cannot be null");
        }
        chatMessage.setChatIdx(chatIdx);
        return chatService.saveAndFormatChatMessage(chatMessage);
    }

    // 그룹 채팅 메시지 전송
    @MessageMapping("/groupChat.sendMessage/{groupChatIdx}")
    @SendTo("/topic/groupChat/{groupChatIdx}")
    public GroupChat sendGroupMessage(@DestinationVariable Integer groupChatIdx, @Payload GroupChat
            groupChatMessage) {
        return chatService.saveAndFormatGroupChatMessage(groupChatMessage);
    }

    // 개인 채팅방 입장
    @MessageMapping("/chat.addUser/{chatIdx}")
    @SendTo("/topic/chat/{chatIdx}")
    public Chat addUser(@DestinationVariable Integer chatIdx, @Payload Chat chatMessage) {
        return chatService.addUserToChat(chatMessage, chatIdx);
    }

    // 그룹 채팅방 입장
    @MessageMapping("/groupChat.addUser/{groupChatIdx}")
    @SendTo("/topic/groupChat/{groupChatIdx}")
    public GroupChat addUserToGroupChat(@DestinationVariable Integer groupChatIdx, @Payload GroupChat
            groupChatMessage) {
        return chatService.addUserToGroupChat(groupChatMessage, groupChatIdx);
    }
}