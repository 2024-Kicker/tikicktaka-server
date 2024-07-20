package com.example.tikicktaka.service.memberService;

import com.example.tikicktaka.domain.member.Auth;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.web.dto.member.MemberRequestDTO;
import org.springframework.transaction.annotation.Transactional;

public interface MemberCommandService {

    Member join(MemberRequestDTO.JoinDTO request);

    String login(String email, String password);

    Boolean confirmEmailDuplicate(MemberRequestDTO.EmailDuplicateConfirmDTO request);

    Boolean confirmNicknameDuplicate(MemberRequestDTO.NicknameDuplicateConfirmDTO request);

    Auth sendEmailAuth(String email);

    Boolean confirmEmailAuth(MemberRequestDTO.EmailAuthConfirmDTO request);

    @Transactional
    Member completeSignup(Long memberId, MemberRequestDTO.CompleteSignupDTO request);
}
