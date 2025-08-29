package com.example.tikicktaka.repository.storyRoom;

import com.example.tikicktaka.domain.enums.StoryRoomStatus;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.storyRoom.StoryRoomPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface StoryRoomPostRepository extends JpaRepository<StoryRoomPost, Long> {
    @Query("SELECT p FROM StoryRoomPost p WHERE p.status = :status")
    List<StoryRoomPost> findAllByStatus(@Param("status") StoryRoomStatus status);

    List<StoryRoomPost> findAllByAuthor(Member author);


    //List<StoryRoomPost> findAllByStatus(StoryRoomStatus status);

}

