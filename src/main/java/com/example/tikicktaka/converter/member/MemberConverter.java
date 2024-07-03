package com.example.tikicktaka.converter.member;

import com.example.tikicktaka.domain.enums.MemberRole;
import com.example.tikicktaka.domain.mapping.member.MemberTerm;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.member.Term;
import com.example.tikicktaka.web.dto.member.MemberRequestDTO;
import com.example.tikicktaka.web.dto.member.MemberResponseDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MemberConverter {

    public static MemberResponseDTO.JoinResultDTO toJoinResultDTO(Member member){
        return MemberResponseDTO.JoinResultDTO.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .loginId(member.getLoginId())
                .createdAt(member.getCreatedAt())
                .build();
    }

    public static Member toMember(MemberRequestDTO.JoinDTO request){
        return Member.builder()
                .nickname(request.getNickname())
                .name(request.getName())
                .password(request.getPassword())
                .email(request.getEmail())
                .loginId(request.getLoginId())
                .birthday(request.getBirthday())
                .gender(request.getGender())
                .phone(request.getPhone())
                .memberRole(MemberRole.MEMBER)
                .memberTermList(new ArrayList<>())
                .build();
    }

    public static MemberResponseDTO.LoginIdDuplicateConfirmResultDTO toLoginIdDuplicateConfirmResultDTO(Boolean checkLoginId){
        return MemberResponseDTO.LoginIdDuplicateConfirmResultDTO.builder()
                .checkLoginId(checkLoginId)
                .build();
    }

    public static MemberResponseDTO.NicknameDuplicateConfirmResultDTO toNicknameDuplicateConfirmResultDTO(Boolean checkNickname){
        return MemberResponseDTO.NicknameDuplicateConfirmResultDTO.builder()
                .checkNickname(checkNickname)
                .build();
    }

    public static List<MemberTerm> toMemberTermList(HashMap<Term, Boolean> termList) {

        return termList.entrySet().stream()
                .map(entry -> MemberTerm.builder()
                        .term(entry.getKey())
                        .memberAgree(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}
