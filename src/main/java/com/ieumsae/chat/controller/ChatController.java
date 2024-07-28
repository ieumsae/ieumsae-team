package com.ieumsae.chat.controller;

import com.ieumsae.chat.service.ChatService;
import com.ieumsae.common.entity.ChatRoom;
import com.ieumsae.common.entity.Message;
import com.ieumsae.common.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RequestMapping("/chat")
@Controller
public class ChatController {

    private final ChatService chatService;
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public String chatPage() {
        return "chat"; //
    }

    @GetMapping("/study/{studyId}")
    public String enterStudyDetail(@PathVariable Long studyId, Model model) {
        model.addAttribute("studyId", studyId);
        return "study_detail";
    }

    /**
     * 
     * @param studyId
     * @param chatType
     * @param model
     * @return 로직 수행 후, html로 이동
     * @note 현재 접속한 회원의 userId 값으로 채팅방을 조회하거나 생성한 후, 해당 채팅방으로 이동
     */

    @GetMapping("/enterChat")
    public String enterChat(@RequestParam("studyId") Long studyId, @RequestParam("chatType") ChatRoom.ChatType chatType, Model model) {
        Long userId = SecurityUtils.getCurrentUserId();

        try {
            ChatRoom chatRoom = chatService.getOrCreateChatRoom(studyId, chatType);
            chatService.addUserToChat(chatRoom.getChatRoomId(), userId, chatType, studyId);

            model.addAttribute("chatRoomId", chatRoom.getChatRoomId()); // 생성되거나 조회된 채팅방 id
            model.addAttribute("userId", userId); // 현재 접속한 회원의 userId
            model.addAttribute("chatType", chatType); // enum으로 정의한 "PERSONAL", "GROUP"
            model.addAttribute("entryMessage", chatService.createEntryMessage(userId)); // 입장메시지

            List<Message> previousMessages = chatService.getPreviousMessages(chatRoom.getChatRoomId()); // 이전 채팅 기록 불러오기
            model.addAttribute("previousMessages", previousMessages); // 채팅방에 띄워주기 위해 model 객체에 추가

            return "chat";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "study_detail";
        }
    }

    /**
     * @param chatRoomId
     * @param message
     * @return Message 객체 타입의 chatRoomId, userId, content 값이 반환
     * @DestinationVariable @MessageMapping / @SendTo 에 있는 chatRoomId 값을 Long chatRoomId 라는 변수에 바인딩
     * @payload 메시지 본문
     * @note chatRoomId와 message 본문을 DB에 저장하고 채팅방에 띄워주는 기능을 하는 메소드
     */

    @MessageMapping("/chat.sendMessage/{chatRoomId}")
    @SendTo("/topic/chat/{chatRoomId}")
    public Message sendMessage(@DestinationVariable Long chatRoomId, @Payload Message message) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        logger.info("Received message for room {}: {} from user {}", chatRoomId, message.getContent(), currentUserId);

        Message savedMessage = chatService.saveAndSendMessage(chatRoomId, message.getUserId(), message.getContent(), currentUserId);

        logger.info("Sending message: {}", savedMessage);
        return savedMessage;
    }

    /**
     * @param chatRoomId
     * @param message
     * @return 채팅방과 메시지에 관한 정보를 유저를 채팅방에 추가 + 입장 메시지를 반환
     */
    
    @MessageMapping("/chat.join/{chatRoomId}")
    @SendTo("/topic/chat/{chatRoomId}")
    public String addUser(@DestinationVariable Long chatRoomId, @Payload Message message) {
        ChatRoom chatRoom = chatService.getChatRoomById(chatRoomId);
        chatService.addUserToChat(chatRoomId, message.getUserId(), chatRoom.getChatType(), chatRoom.getStudyId());
        return chatService.createEntryMessage(message.getUserId());
    }

    /**
     * @note 위의 enterChat 메소드에서 이전 채팅 기록을 불러오는 부분이 있는데 그 부분은 model 객체에 DB에서 불러온 채팅 내용을 List 형태로 저장하는 동작이고
     * @note 현재의 getPreviousMessages 메소드는 chat.js 의 메소드를 실질적으로 수행해서 유저가 보는 화면에 채팅 내용을 띄워주는 역할을 한다.
     */

    // 이전 메시지를 가져오는 엔드포인트 추가
    @GetMapping("/api/chat/{chatRoomId}/messages")
    @ResponseBody
    public List<Message> getPreviousMessages(@PathVariable Long chatRoomId) {
        return chatService.getPreviousMessages(chatRoomId);
    }
}
