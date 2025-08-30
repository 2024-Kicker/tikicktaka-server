package com.example.tikicktaka.repository.storyRoom;

import com.example.tikicktaka.domain.storyRoom.StoryRoom;
import com.example.tikicktaka.domain.storyRoom.StoryRoomPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoryRoomRepository extends JpaRepository<StoryRoom, Long> {

    // post 엔티티로 찾기
    Optional<StoryRoom> findByPost(StoryRoomPost post);

    // postId로 찾고 싶을 때 사용 가능 (원하면 서비스 코드에서 이걸 써도 됨)
    Optional<StoryRoom> findByPostId(Long postId);

    //  게시글 지울 때 방도 함께 지우는 용도
    void deleteByPost(StoryRoomPost post);

    // 존재 여부 체크가 필요할 때
    boolean existsByPostId(Long postId);
}
