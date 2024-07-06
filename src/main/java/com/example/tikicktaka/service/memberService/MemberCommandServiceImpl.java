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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.example.tikicktaka.config.springSecurity.utils.JwtUtil.createJwt;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberCommandServiceImpl implements MemberCommandService{

    private final MemberRepository memberRepository;
    private final TermRepository termRepository;
    private final BCryptPasswordEncoder encoder;

    @Value("${JWT_TOKEN_SECRET}")
    private String key;

    private int expiredMs = 1000 * 60 * 60 * 24 * 5;

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

        Optional<Member> existingEmail = memberRepository.findByEmail((request.getEmail()));
        if (existingEmail.isPresent()) {
                if (!existingEmail.get().getMemberStatus().equals(MemberStatus.INACTIVE)) {
                    throw new MemberHandler(ErrorStatus.MEMBER_ID_DUPLICATED);
                } else {
                    existingMember = existingEmail.get();
                }
            }

        Member member;
        if (existingMember != null) {
            //탈퇴한 회원이 다시 회원가입하는 경우
            existingMember.setMemberStatus(MemberStatus.ACTIVE);
            memberRepository.reregister(existingMember.getId(), request.getNickname(),
                    request.getName(), request.getPassword(), request.getEmail(),
                    request.getBirthday(), request.getGender(), request.getPhone());
            return existingMember;
        } else {
            member = MemberConverter.toMember(request, encoder);
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
    public String login(String email, String password) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_EMAIL_NOT_FOUND));

        if(!encoder.matches(password, member.getPassword())){
            throw new MemberHandler(ErrorStatus.MEMBER_PASSWORD_NOT_EQUAL);
        }

        List<String> roles = new ArrayList<>();
        roles.add("ROLE_USER");

        return createJwt(member.getId(), member.getName(), expiredMs, key, roles);
    }

    @Override
    public Boolean confirmEmailDuplicate(MemberRequestDTO.EmailDuplicateConfirmDTO request) {
        Optional<Member> member = memberRepository.findByEmail(request.getEmail());
        Boolean checkEmail = false;

        if (!member.isPresent()) {

            checkEmail = true;
        }else{
            if(member.get().getMemberStatus().equals(MemberStatus.INACTIVE)){
                checkEmail =true;
            }
        }
        return checkEmail;
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
