package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MemberDisplayNameServiceImpl implements MemberDisplayNameService {

    private final MemberRepository memberRepository;

    @Override
    public Map<Long, String> namesOf(List<Long> memberIds) {
        List<Member> members = memberRepository.findAllById(memberIds);
        return members.stream()
                .collect(Collectors.toMap(Member::getId, Member::getName));
    }
}

