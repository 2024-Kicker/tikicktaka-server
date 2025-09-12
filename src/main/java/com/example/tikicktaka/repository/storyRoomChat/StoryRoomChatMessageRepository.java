package com.example.tikicktaka.repository.storyRoomChat;

import com.example.tikicktaka.domain.storyRoomChat.StoryChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoryRoomChatMessageRepository extends JpaRepository<StoryChatMessage, Long> {

    // 최초 진입: 최신부터 N개
    List<StoryChatMessage> findByStoryRoom_RoomIdOrderByIdDesc(String roomId, Pageable pageable);

    // 이전 페이지: id < cursor 내림차 N개
    List<StoryChatMessage> findByStoryRoom_RoomIdAndIdLessThanOrderByIdDesc(String roomId, Long cursor, Pageable pageable);

    // 다음 페이지: id > cursor 오름차 N개
    List<StoryChatMessage> findByStoryRoom_RoomIdAndIdGreaterThanOrderByIdAsc(String roomId, Long cursor, Pageable pageable);

    // 마지막(가장 최신) 1건
    Optional<StoryChatMessage> findTop1ByStoryRoom_RoomIdOrderByIdDesc(String roomId);

    void deleteByStoryRoom_RoomId(String roomId);
}
