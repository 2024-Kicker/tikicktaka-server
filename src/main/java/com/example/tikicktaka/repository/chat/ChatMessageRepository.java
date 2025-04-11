package com.example.tikicktaka.repository.chat;

import com.example.tikicktaka.domain.chat.ChatMessage;
import com.example.tikicktaka.domain.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoom(ChatRoom chatRoom);
    void deleteByChatRoom(ChatRoom chatRoom);

}
