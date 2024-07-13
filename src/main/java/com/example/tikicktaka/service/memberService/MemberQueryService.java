package com.example.tikicktaka.service.memberService;

import com.example.tikicktaka.domain.member.Member;

import java.util.Optional;

public interface MemberQueryService {

    Optional<Member> findMemberById(Long id);

    Optional<Member> findMemberByName(String name);
}
