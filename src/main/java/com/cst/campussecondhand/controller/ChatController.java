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

import java.util.Collections;
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

    // 修改：获取两个用户间的历史消息，返回安全的 Map 列表
    @GetMapping("/api/messages/{recipientId}")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getChatHistory(@PathVariable Integer recipientId, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        List<ChatMessage> messages = chatMessageRepository.findBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderByTimestampAsc(
                currentUser.getId(), recipientId, recipientId, currentUser.getId());

        // 将 ChatMessage 转换为 Map
        List<Map<String, Object>> response = messages.stream().map(message -> {
            Map<String, Object> msgMap = new java.util.HashMap<>();
            msgMap.put("content", message.getContent());
            msgMap.put("timestamp", message.getTimestamp());

            Map<String, Object> senderMap = new java.util.HashMap<>();
            senderMap.put("id", message.getSender().getId());
            senderMap.put("nickname", message.getSender().getNickname());
            msgMap.put("sender", senderMap);

            return msgMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // 修改：获取当前用户的所有对话列表
    @GetMapping("/api/conversations")
    @ResponseBody
    public ResponseEntity<?> getConversations(HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        // 1. 调用新的方法获取伙伴ID列表
        List<Integer> partnerIds = chatMessageRepository.findConversationPartnerIds(currentUser.getId());
        if (partnerIds.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // 2. 根据ID列表一次性查询所有 User 对象
        List<User> partners = userRepository.findAllById(partnerIds);

        // 3. 将 User 列表转换为安全的 Map 列表
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