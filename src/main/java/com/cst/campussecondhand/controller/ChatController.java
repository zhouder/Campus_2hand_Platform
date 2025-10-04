package com.cst.campussecondhand.controller;

import com.cst.campussecondhand.entity.ChatMessage;
import com.cst.campussecondhand.entity.User;
import com.cst.campussecondhand.repository.ChatMessageRepository;
import com.cst.campussecondhand.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private UserRepository userRepository;

    // 处理发送来的 WebSocket 消息
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        // 为消息设置时间戳并保存到数据库
        chatMessage.setTimestamp(new Date());
        chatMessageRepository.save(chatMessage);

        // 将消息发送到指定接收者的私有队列
        // 格式: /user/{userId}/queue/messages
        messagingTemplate.convertAndSendToUser(
                String.valueOf(chatMessage.getRecipient().getId()),
                "/queue/messages",
                chatMessage
        );
    }

    // 获取两个用户间的历史消息
    @GetMapping("/api/messages/{recipientId}")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable Integer recipientId, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        List<ChatMessage> messages = chatMessageRepository.findBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderByTimestampAsc(
                currentUser.getId(), recipientId, recipientId, currentUser.getId());
        return ResponseEntity.ok(messages);
    }

    // 获取当前用户的所有对话列表
    @GetMapping("/api/conversations")
    @ResponseBody
    public ResponseEntity<?> getConversations(HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        // 注意：这里我们将返回类型从 Object 改为 User
        List<User> partners = chatMessageRepository.findConversationPartners(currentUser.getId());

        // 手动将 User 列表转换为 Map 列表，只包含前端需要的安全字段
        List<Map<String, Object>> response = partners.stream().map(user -> {
            Map<String, Object> userMap = new java.util.HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("nickname", user.getNickname());
            userMap.put("avatarUrl", user.getAvatarUrl());
            userMap.put("avatarBgColor", user.getAvatarBgColor());
            return userMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }


}