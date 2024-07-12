package com.ieumsae.chat.controller;

import com.ieumsae.chat.domain.Chat;
import com.ieumsae.chat.domain.GroupChat;
import com.ieumsae.chat.service.ChatService;
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

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // 채팅 페이지 연결
    @GetMapping("/chat")
    public String chatPage() {
        return "chat";  // chat.html을 렌더링


    }

    // 채팅방 연결
    @PostMapping("/enterChat")
    public String enterChat(@RequestParam("chatIdx") int chatIdx,
                            @RequestParam("userIdx") int userIdx,
                            Model model) {
        // 채팅방 정보 설정
        model.addAttribute("chatIdx", chatIdx);
        model.addAttribute("userIdx", userIdx);
        model.addAttribute("chatType", "PERSONAL");

        // 이전 메시지 불러오기
        List<Chat> previousMessages = chatService.getPreviousMessages(chatIdx, userIdx);
        model.addAttribute("previousMessages", previousMessages);

        return "chatRoom";  // chatRoom.html로 이동
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
    public GroupChat sendGroupMessage(@DestinationVariable Integer groupChatIdx, @Payload GroupChat groupChatMessage) {
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
    public GroupChat addUserToGroupChat(@DestinationVariable Integer groupChatIdx, @Payload GroupChat groupChatMessage) {
        return chatService.addUserToGroupChat(groupChatMessage, groupChatIdx);
    }

    // 이전 개인 채팅 메시지 로드
    @MessageMapping("/chat.getMessages/{chatIdx}")
    @SendTo("/topic/chat/{chatIdx}")
    public List<Chat> getMessages(@DestinationVariable Integer chatIdx, @Payload Integer userIdx) {
        return chatService.getMessagesAfterUserJoin(chatIdx, userIdx);
    }

    // 이전 그룹 채팅 메시지 로드
    @MessageMapping("/groupChat.getMessages/{groupChatIdx}")
    @SendTo("/topic/groupChat/{groupChatIdx}")
    public List<GroupChat> getGroupMessages(@DestinationVariable Integer groupChatIdx, @Payload Integer userIdx) {
        return chatService.getGroupMessagesAfterUserJoin(groupChatIdx, userIdx);
    }
}

