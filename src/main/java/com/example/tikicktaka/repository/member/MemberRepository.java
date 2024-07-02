package com.example.tikicktaka.repository.member;

import com.example.tikicktaka.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByNickname(String nickname);

    Optional<Member> findByLoginId(String loginId);

    Optional<Member> findByPhone(String phone);
}
