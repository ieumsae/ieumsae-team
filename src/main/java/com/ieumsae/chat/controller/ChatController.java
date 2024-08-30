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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

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
     * @param studyId
     * @param chatType
     * @param model
     * @return 로직 수행 후, html로 이동
     * @note 현재 접속한 회원의 userId 값으로 채팅방을 조회하거나 생성한 후, 해당 채팅방으로 이동
     */

    @GetMapping("/enterChat")
    public String enterChat(@RequestParam("studyId") Long studyId,
                            @RequestParam("chatType") ChatRoom.ChatType chatType,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Entering chat. StudyId: {}, ChatType: {}, UserId: {}", studyId, chatType, userId);

        try {
            if (chatType == ChatRoom.ChatType.GROUP && !chatService.canJoinGroupChat(studyId, userId)) {
                log.warn("User {} cannot join group chat for study {}", userId, studyId);
                throw new IllegalArgumentException("스터디에 속해있어야 그룹채팅에 참가할 수 있습니다.");
            }

            ChatRoom chatRoom = chatService.getOrCreateChatRoom(studyId, chatType, userId);

            log.info("User {} successfully entered chat room {}", userId, chatRoom.getChatRoomId());

            model.addAttribute("chatRoomId", chatRoom.getChatRoomId());
            model.addAttribute("userId", userId);
            model.addAttribute("chatType", chatType);
            model.addAttribute("entryMessage", chatService.createEntryMessage(userId));

            List<Message> previousMessages = chatService.getPreviousMessages(chatRoom.getChatRoomId());
            model.addAttribute("previousMessages", previousMessages);

            return "chat";
        } catch (IllegalArgumentException e) {
            log.error("IllegalArgumentException in enterChat: {}", e.getMessage());
            redirectAttributes.addAttribute("errorMessage", URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
            return "redirect:/study/" + studyId;
        } catch (Exception e) {
            log.error("Unexpected error in enterChat", e);
            redirectAttributes.addFlashAttribute("errorMessage", "예기치 못한 오류가 발생했습니다.");
            return "redirect:/study/" + studyId;
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