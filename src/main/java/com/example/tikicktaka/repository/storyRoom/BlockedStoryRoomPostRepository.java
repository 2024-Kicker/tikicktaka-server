//package com.example.tikicktaka.repository.storyRoom;
//
//import com.example.tikicktaka.domain.storyRoom.BlockedStoryRoomPost;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//
//public interface BlockedStoryRoomPostRepository extends JpaRepository<BlockedStoryRoomPost, Long> {
//
//    boolean existsByMemberIdAndStoryRoomPostId(Long memberId, Long storyRoomPostId);
//
//    @Query("SELECT b.storyRoomPost.id FROM BlockedStoryRoomPost b WHERE b.member.id = :memberId")
////    List<Long> findBlockedPostIdsByMemberId(@Param("memberId") Long memberId);
//}
//
