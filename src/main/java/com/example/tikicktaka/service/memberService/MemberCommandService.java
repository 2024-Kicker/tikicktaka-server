package com.example.tikicktaka.service.memberService;

import com.example.tikicktaka.domain.mapping.member.MemberTeam;
import com.example.tikicktaka.domain.member.Auth;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.web.dto.member.MemberRequestDTO;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


public interface MemberCommandService {

    Member join(MemberRequestDTO.JoinDTO request);

    String login(String email, String password);

    Boolean confirmEmailDuplicate(MemberRequestDTO.EmailDuplicateConfirmDTO request);

    Boolean confirmNicknameDuplicate(MemberRequestDTO.NicknameDuplicateConfirmDTO request);

    Auth sendEmailAuth(String email);

    Boolean confirmEmailAuth(MemberRequestDTO.EmailAuthConfirmDTO request);

    MemberTeam setPreferTeam(Member member, Long teamId);

    @Transactional
    Member completeSignup(Long memberId, MemberRequestDTO.CompleteSignupDTO request);

    Member profileModify(MultipartFile profile, String nickname, Member member);

}
