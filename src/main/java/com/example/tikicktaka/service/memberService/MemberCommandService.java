package com.example.tikicktaka.service.memberService;

import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.web.dto.member.MemberRequestDTO;

public interface MemberCommandService {

    Member join(MemberRequestDTO.JoinDTO request);

    Boolean confirmLoginIdDuplicate(MemberRequestDTO.LoginIdDuplicateConfirmDTO request);

    Boolean confirmNicknameDuplicate(MemberRequestDTO.NicknameDuplicateConfirmDTO request);
}
