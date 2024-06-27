package com.example.tikicktaka.repository.member;

import com.example.tikicktaka.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
