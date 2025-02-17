package com.example.tikicktaka.repository.member;

import com.example.tikicktaka.domain.mapping.member.MemberTravelStyle;
import com.example.tikicktaka.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberTravelStyleRepository extends JpaRepository<MemberTravelStyle, Long> {
    Optional<MemberTravelStyle> findByMember(Member member); // 특정 회원의 여행 스타일 조회
    void deleteByMember(Member member); // 특정 회원의 여행 스타일 삭제 (재설정 시 사용)
}

