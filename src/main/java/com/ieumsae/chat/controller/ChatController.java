package com.ieumsae.chat.controller;

import com.ieumsae.chat.domain.Chat;
import com.ieumsae.chat.domain.GroupChat;
import com.ieumsae.chat.repository.ChatEntranceLogRepository;
import com.ieumsae.chat.repository.GroupChatEntranceLogRepository;
import com.ieumsae.chat.service.ChatService;
import com.ieumsae.user.domain.CustomOAuth2User;
import com.ieumsae.user.domain.CustomUserDetails;
import com.ieumsae.user.domain.User;
import com.ieumsae.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class ChatController {

    private final ChatService chatService;
    private final ChatEntranceLogRepository chatEntranceLogRepository;
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final GroupChatEntranceLogRepository groupChatEntranceLogRepository;
    private final UserRepository userRepository;


    @Autowired
    public ChatController(ChatService chatService, ChatEntranceLogRepository chatEntranceLogRepository, GroupChatEntranceLogRepository groupChatEntranceLogRepository, UserRepository userRepository) {
        this.chatService = chatService;
        this.chatEntranceLogRepository = chatEntranceLogRepository;
        this.groupChatEntranceLogRepository = groupChatEntranceLogRepository;
        this.userRepository = userRepository;
    }

    // 채팅 페이지 연결
    @GetMapping("/chat")
    public String chatPage() {
        return "chat";  // chat.html을 렌더링
    }

    // 채팅방 연결
    @PostMapping("/enterChat")
    public String enterChat(@RequestParam("studyIdx") Long studyIdx, HttpSession session, Model model, @RequestParam("chatType") String chatType) {

        // SecurityContextHolder를 사용하여 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        Long userIdx = null;

        if (principal instanceof CustomOAuth2User oAuth2User) {
            userIdx = oAuth2User.getUserIdx(); // CustomOAuth2User에 getUserIdx() 메서드 추가 필요
        } else if (principal instanceof CustomUserDetails userDetails) {
            userIdx = userDetails.getUserIdx(); // CustomUserDetails에 getUserIdx() 메서드 추가 필요
        } else {
            logger.error("알 수 없는 사용자 타입: {}", principal.getClass().getName());
            return "error";
        }

        if (userIdx == null) {
            logger.error("User index not found");
            return "error";
        }

        // 파라미터 유효성 검사
        if (userIdx == null || chatType == null) {
            logger.error("Invalid parameters: studyIdx={}, userIdx={}, chatType={}", studyIdx, userIdx, chatType);
            return "error"; // 에러 페이지로 리다이렉트
        }

        //chatIdx 생성하는 로직
        Long chatIdx = chatService.createChatIdx(userIdx, studyIdx, chatType);

        if ("PERSONAL".equals(chatType)) {
            logger.info("Entering chat: chatIdx={}, userIdx={}", chatIdx, userIdx);
            try {
                logger.info("생성된 chatIdx={}", chatIdx);

                if (!chatEntranceLogRepository.existsByChatIdxAndUserIdx(chatIdx, userIdx)) {
                    logger.info("최초 접속: chatIdx={}, userIdx={}", chatIdx, userIdx);
                } else {
                    List<Chat> previousMessages = chatService.getPreviousMessages(chatIdx, userIdx, chatType);
                    model.addAttribute("previousMessages", previousMessages);
                }
                return "personalChatRoom"; // chatRoom.html로 이동
            } catch (Exception e) {
                logger.error("Error while entering chat: chatIdx={}, userIdx={}", chatIdx, userIdx, e);
                return "error";
            }

        } else if ("GROUP".equals(chatType)) {
            logger.info("Entering group: chatIdx={}, userIdx={}", chatIdx, userIdx);

            try {
                if (!groupChatEntranceLogRepository.existsByChatIdxAndUserIdx(chatIdx, userIdx)) {
                    logger.info("최초 접속: chatIdx={}, userIdx={}", chatIdx, userIdx);
                } else {
                    List<Chat> previousMessages = chatService.getPreviousMessages(chatIdx, userIdx, chatType);
                    model.addAttribute("previousMessages", previousMessages);
                }
                return "groupChatRoom"; // groupChatRoom.html로 이동
            } catch (Exception e) {
                logger.error("Error while entering chat: chatIdx={}, userIdx={}", chatIdx, userIdx, e);
                return "error";
            }
        }

        logger.error("Invalid chatType: {}", chatType);
        return "error"; // 에러 페이지로 리다이렉트
    }


    // 개인 채팅 메시지 전송
    @MessageMapping("/chat.sendMessage/{chatIdx}")
    @SendTo("/topic/chat/{chatIdx}")
    public Chat sendMessage(@DestinationVariable Long chatIdx, @Payload Chat chatMessage) {
        if (chatIdx == null || chatMessage.getChatIdx() == null) {
            throw new IllegalArgumentException("chatIdx cannot be null");
        }
        chatMessage.setChatIdx(chatIdx);
        return chatService.saveAndFormatChatMessage(chatMessage);
    }

    // 그룹 채팅 메시지 전송
    @MessageMapping("/groupChat.sendMessage/{groupChatIdx}")
    @SendTo("/topic/groupChat/{groupChatIdx}")
    public GroupChat sendGroupMessage(@DestinationVariable Long groupChatIdx, @Payload GroupChat
            groupChatMessage) {
        return chatService.saveAndFormatGroupChatMessage(groupChatMessage);
    }

    // 개인 채팅방 입장
    @MessageMapping("/chat.addUser/{chatIdx}")
    @SendTo("/topic/chat/{chatIdx}")
    public Chat addUser(@DestinationVariable Long chatIdx, @Payload Chat chatMessage) {
        return chatService.addUserToChat(chatMessage, chatIdx);
    }

    // 그룹 채팅방 입장
    @MessageMapping("/groupChat.addUser/{groupChatIdx}")
    @SendTo("/topic/groupChat/{groupChatIdx}")
    public GroupChat addUserToGroupChat(@DestinationVariable Long groupChatIdx, @Payload GroupChat
            groupChatMessage) {
        return chatService.addUserToGroupChat(groupChatMessage, groupChatIdx);
    }
}