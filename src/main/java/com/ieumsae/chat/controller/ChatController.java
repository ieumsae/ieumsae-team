package com.ieumsae.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieumsae.chat.domain.Chat;
import com.ieumsae.chat.domain.GroupChat;
import com.ieumsae.chat.repository.ChatEntranceLogRepository;
import com.ieumsae.chat.service.ChatService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @GetMapping("/chat")
    public String chatPage() {
        return "chat";  // chat.html을 렌더링
    }

    //메소드 간소화 예정
    @PostMapping("/enterChat")
    public String enterChat(@RequestParam(value = "chatIdx", required = false) Integer chatIdx,
                            @RequestParam(value = "userIdx", required = false) Integer userIdx,
                            @RequestParam(value = "chatType", required = false) String chatType,
                            Model model) {

        if (chatIdx == null || userIdx == null || chatType == null) {
            logger.error("Invalid parameters: chatIdx={}, userIdx={}, chatType={}", chatIdx, userIdx, chatType);
            return "error"; // 에러 페이지로 리다이렉트
        }

        logger.info("Entering chat: chatIdx={}, userIdx={}, chatType={}", chatIdx, userIdx, chatType);

        try {
            boolean isFirstTime = false;
            if ("PERSONAL".equals(chatType)) {
                isFirstTime = !chatService.existsByChatIdxAndUserIdx(chatIdx, userIdx);
            } else if ("GROUP".equals(chatType)) {
                isFirstTime = !chatService.existsByGroupChatIdxAndUserIdx(chatIdx, userIdx);
            }

            if (isFirstTime) {
                logger.info("First-time access for chatIdx={}, userIdx={}", chatIdx, userIdx);
                model.addAttribute("chatIdx", chatIdx);
                model.addAttribute("userIdx", userIdx);
                model.addAttribute("chatType", chatType);
            } else {
                logger.info("Re-entering chat: chatIdx={}, userIdx={}", chatIdx, userIdx);

                if ("PERSONAL".equals(chatType)) {
                    List<Chat> previousMessages = chatService.getPreviousPersonalMessages(chatIdx, userIdx);
                    List<Map<String, String>> processedMessages = previousMessages.stream()
                            .map(msg -> Map.of("nickname", msg.getNickName(), "content", msg.getContent()))
                            .collect(Collectors.toList());
                    model.addAttribute("previousMessages", new ObjectMapper().writeValueAsString(processedMessages));
                } else if ("GROUP".equals(chatType)) {
                    List<GroupChat> previousMessages = chatService.getPreviousGroupMessages(chatIdx, userIdx);
                    List<Map<String, String>> processedMessages = previousMessages.stream()
                            .map(msg -> Map.of("nickname", msg.getNickName(), "content", msg.getContent()))
                            .collect(Collectors.toList());
                    model.addAttribute("previousMessages", new ObjectMapper().writeValueAsString(processedMessages));
                }
            }

            model.addAttribute("chatIdx", chatIdx);
            model.addAttribute("userIdx", userIdx);
            model.addAttribute("chatType", chatType);

            if ("PERSONAL".equals(chatType)) {
                return "personalChat";  // personalChat.html로 이동
            } else if ("GROUP".equals(chatType)) {
                return "groupChat";  // groupChat.html로 이동
            } else {
                logger.error("Invalid chatType: {}", chatType);
                return "error"; //  에러 페이지로 리다이렉트
            }

        } catch (Exception e) {
            logger.error("Error while entering chat: chatIdx={}, userIdx={}, chatType={}", chatIdx, userIdx, chatType, e);
            return "error"; // 에러 페이지로 리다이렉트
        }
    }

    @MessageMapping("/chat.sendMessage/{chatIdx}")
    @SendTo("/topic/chat/{chatIdx}")
    public Object sendMessage(@DestinationVariable Integer chatIdx, @Payload Chat chatMessage) {
        if (chatIdx == null || chatMessage.getChatIdx() == null) {
            throw new IllegalArgumentException("chatIdx cannot be null");
        }

        chatMessage.setChatIdx(chatIdx);
        if ("PERSONAL".equals(chatMessage.getChatType())) {
            return chatService.savePersonalChatMessage(chatMessage);
        } else if ("GROUP".equals(chatMessage.getChatType())) {
            GroupChat groupChatMessage = chatService.createGroupChatMessage(chatIdx, chatMessage);
            return chatService.saveGroupChatMessage(groupChatMessage);
        } else {
            throw new IllegalArgumentException("Invalid chatType: " + chatMessage.getChatType());
        }
    }

    @MessageMapping("/chat.addUser/{chatIdx}")
    @SendTo("/topic/chat/{chatIdx}")
    public Object addUser(@DestinationVariable Integer chatIdx, @Payload Chat chatMessage) {
        if ("PERSONAL".equals(chatMessage.getChatType())) {
            return chatService.addUserToPersonalChat(chatMessage, chatIdx);
        } else if ("GROUP".equals(chatMessage.getChatType())) {
            GroupChat groupChatMessage = chatService.createGroupChatMessage(chatIdx, chatMessage);
            groupChatMessage.setContent(chatMessage.getNickName() + "님이 입장하셨습니다."); // nickName 사용
            groupChatMessage.setSendDateTime(LocalDateTime.now());
            return chatService.addUserToGroupChat(groupChatMessage, chatIdx);
        } else {
            throw new IllegalArgumentException("Invalid chatType: " + chatMessage.getChatType());
        }
    }
}

