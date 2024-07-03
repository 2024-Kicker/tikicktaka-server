package com.example.tikicktaka.service.memberService;

import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.exception.handler.MemberHandler;
import com.example.tikicktaka.converter.member.MemberConverter;
import com.example.tikicktaka.domain.enums.MemberStatus;
import com.example.tikicktaka.domain.mapping.member.MemberTerm;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.member.Term;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.repository.member.TermRepository;
import com.example.tikicktaka.web.dto.member.MemberRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MemberCommandServiceImpl implements MemberCommandService{

    private final MemberRepository memberRepository;
    private final TermRepository termRepository;

    @Transactional
    @Override
    public Member join(MemberRequestDTO.JoinDTO request) {

        Member existingMember = null;

        Optional<Member> existingNickname = memberRepository.findByNickname(request.getNickname());
        if (existingNickname.isPresent()) {
            if (!existingNickname.get().getMemberStatus().equals(MemberStatus.INACTIVE)) {
                throw new MemberHandler(ErrorStatus.MEMBER_NICKNAME_DUPLICATED);
            } else {
                existingMember = existingNickname.get();
            }
        }

        Optional<Member> existingLoginId = memberRepository.findByLoginId((request.getLoginId()));
        if (existingLoginId.isPresent()) {
                if (!existingLoginId.get().getMemberStatus().equals(MemberStatus.INACTIVE)) {
                    throw new MemberHandler(ErrorStatus.MEMBER_ID_DUPLICATED);
                } else {
                    existingMember = existingLoginId.get();
                }
            }

        Member member;
        if (existingMember != null) {
            //탈퇴한 회원이 다시 회원가입하는 경우
            existingMember.setMemberStatus(MemberStatus.ACTIVE);
            memberRepository.reregister(existingMember.getId(), request.getNickname(),
                    request.getName(), request.getPassword(), request.getLoginId(), request.getEmail(),
                    request.getBirthday(), request.getGender(), request.getPhone());
            return existingMember;
        } else {
            member = MemberConverter.toMember(request);
        }

        // 약관 동의 저장 로직
        HashMap<Term, Boolean> termMap = new HashMap<>();
        for (int i = 0; i < request.getMemberTerm().size(); i++) {
            termMap.put(termRepository.findById(i + 1L).orElseThrow(() -> new MemberHandler(ErrorStatus.TERM_NOT_FOUND)), request.getMemberTerm().get(i));
        }

        List<MemberTerm> memberTermList = MemberConverter.toMemberTermList(termMap);
        memberTermList.forEach(memberTerm -> {
            memberTerm.setMember(member);
        });

        memberRepository.save(member);
        return member;
    }

    @Override
    public Boolean confirmLoginIdDuplicate(MemberRequestDTO.LoginIdDuplicateConfirmDTO request) {
        Optional<Member> member = memberRepository.findByLoginId(request.getLoginId());
        Boolean checkLoginId = false;

        if (!member.isPresent()) {

            checkLoginId = true;
        }else{
            if(member.get().getMemberStatus().equals(MemberStatus.INACTIVE)){
                checkLoginId =true;
            }
        }
        return checkLoginId;
    }

    @Override
    public Boolean confirmNicknameDuplicate(MemberRequestDTO.NicknameDuplicateConfirmDTO request) {
        Optional<Member> member = memberRepository.findByNickname(request.getNickname());
        Boolean checkNickname = false;

        if (!member.isPresent()) {

            checkNickname = true;
        }else{
            if(member.get().getMemberStatus().equals(MemberStatus.INACTIVE)){
                checkNickname =true;
            }
        }
        return checkNickname;
    }
}
