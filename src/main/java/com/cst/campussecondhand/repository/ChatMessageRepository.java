package com.cst.campussecondhand.repository;

import com.cst.campussecondhand.entity.ChatMessage;
import com.cst.campussecondhand.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    // 查找两个用户之间的所有聊天记录，并按时间升序排列
    List<ChatMessage> findBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderByTimestampAsc(
            Integer senderId1, Integer recipientId1, Integer senderId2, Integer recipientId2);

    // 查找与指定用户相关的所有对话伙伴
    @Query("SELECT DISTINCT CASE WHEN m.sender.id = ?1 THEN m.recipient ELSE m.sender END FROM ChatMessage m WHERE m.sender.id = ?1 OR m.recipient.id = ?1")
    List<User> findConversationPartners(Integer userId);
}