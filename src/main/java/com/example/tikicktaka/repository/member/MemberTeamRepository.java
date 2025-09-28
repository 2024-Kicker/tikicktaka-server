package com.example.tikicktaka.repository.member;

import com.example.tikicktaka.domain.mapping.member.MemberTeam;
import com.example.tikicktaka.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberTeamRepository extends JpaRepository<MemberTeam, Long> {
    Optional<MemberTeam> findByMember(Member member);
    Optional<MemberTeam> findByMemberId(Long memberId);
    Optional<MemberTeam> findTopByMember_IdOrderByCreatedAtDesc(Long memberId);

}
