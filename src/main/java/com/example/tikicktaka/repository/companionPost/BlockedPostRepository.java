//package com.example.tikicktaka.repository.companionPost;
//
//import com.example.tikicktaka.domain.companionPost.BlockedPost;
//import com.example.tikicktaka.domain.companionPost.CompanionPost;
//import com.example.tikicktaka.domain.member.Member;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import java.util.List;
//import java.util.Optional;
//
//public interface BlockedPostRepository extends JpaRepository<BlockedPost, Long> {
//
//    List<BlockedPost> findByMember(Member member);
//
//    boolean existsByMemberAndBlockedPost(Member member, CompanionPost post);
//    Optional<BlockedPost> findByMemberAndBlockedPost(Member member, CompanionPost blockedPost);
//
//    @Query("SELECT bp.blockedPost.id FROM BlockedPost bp WHERE bp.member.id = :memberId")
//    List<Long> findPostIdsByMemberId(Long memberId);
//
//    @Query("SELECT CASE WHEN COUNT(bp) > 0 THEN true ELSE false END FROM BlockedPost bp WHERE bp.member.id = :memberId AND bp.blockedPost.id = :postId")
//    boolean existsByMemberIdAndPostId(Long memberId, Long postId);
//
//
//}
