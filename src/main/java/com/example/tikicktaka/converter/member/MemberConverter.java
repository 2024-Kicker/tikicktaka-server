package com.example.tikicktaka.converter.member;

import com.example.tikicktaka.converter.lanTour.LanTourConverter;
import com.example.tikicktaka.domain.enums.MemberRole;
import com.example.tikicktaka.domain.enums.MemberStatus;
import com.example.tikicktaka.domain.images.ProfileImg;
import com.example.tikicktaka.domain.mapping.lanTour.LanTourPurchase;
import com.example.tikicktaka.domain.mapping.member.ChargeCoin;
import com.example.tikicktaka.domain.mapping.member.MemberTeam;
import com.example.tikicktaka.domain.mapping.member.MemberTerm;
import com.example.tikicktaka.domain.member.Auth;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.member.RegisterSeller;
import com.example.tikicktaka.domain.member.Term;
import com.example.tikicktaka.domain.teams.Team;
import com.example.tikicktaka.web.dto.lanTour.LanTourResponseDTO;
import com.example.tikicktaka.web.dto.member.MemberRequestDTO;
import com.example.tikicktaka.web.dto.member.MemberResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MemberConverter {

    public static MemberResponseDTO.JoinResultDTO toJoinResultDTO(Member member){
        return MemberResponseDTO.JoinResultDTO.builder()
                .memberId(member.getId())
                .nickname(member.getName())
                .email(member.getEmail())
                .createdAt(member.getCreatedAt())
                .build();
    }

    public static Member toMember(MemberRequestDTO.JoinDTO request, BCryptPasswordEncoder encoder){
        return Member.builder()
                //.nickname(request.getNickname())
                .name(request.getNickname())
                .password(encoder.encode(request.getPassword()))
                .email(request.getEmail())
                .birthday(request.getBirthday())
                .gender(request.getGender())
                .phone(request.getPhone())
                .point(0L)
                .memberRole(MemberRole.MEMBER)
                .memberTermList(new ArrayList<>())
                .build();
    }

    public static MemberResponseDTO.LoginResultDTO toLoginResultDTO(String jwt){
        return MemberResponseDTO.LoginResultDTO.builder()
                .jwt(jwt)
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

    public static MemberResponseDTO.EmailAuthSendResultDTO toEmailAuthSendResultDTO(Auth auth){
        return MemberResponseDTO.EmailAuthSendResultDTO.builder()
                .email(auth.getEmail())
                .authCode(auth.getCode())
                .build();
    }

    public static Auth toEmailAuth(String email, String code, Boolean expired){
        return Auth.builder()
                .email(email)
                .code(code)
                .expireDate(LocalDateTime.now().plusMinutes(5))
                .expired(expired)
                .build();
    }

    public static MemberResponseDTO.EmailAuthConfirmResultDTO toEmailAuthConfirmResultDTO(String email, Boolean checkEmail){
        return MemberResponseDTO.EmailAuthConfirmResultDTO.builder()
                .checkEmail(checkEmail)
                .email(email)
                .build();
    }


    public static MemberResponseDTO.CompleteSignupResultDTO toCompleteSignupResultDTO(Member member) {
        return new MemberResponseDTO.CompleteSignupResultDTO(
                member.getId(),
                member.getName(),
                //member.getNickname(),
                member.getEmail()
        );
    }

    public static ProfileImg toProfileImg(String url, Member member) {
        return ProfileImg.builder()
                .url(url)
                .member(member)
                .build();
    }

    public static ChargeCoin toChargeCoin(Member member, Long amount){
        return ChargeCoin.builder()
                .amount(amount)
                .member(member)
                .build();
    }

    public static MemberResponseDTO.ChargeCoinResultDTO toChargeCoinResultDTO(Member member){
        return MemberResponseDTO.ChargeCoinResultDTO.builder()
                .nickname(member.getName())
                .point(member.getPoint())
                .build();
    }

    public static Member toUpdateProfile(Member member, MemberRequestDTO.UpdateMemberDTO request) {
        List<MemberTerm> memberTermList = member.getMemberTermList();

        if (memberTermList == null) {
            memberTermList = new ArrayList<>();
        }

        return Member.builder()
                .id(member.getId())
                .name(request.getNickname())
                //.nickname(request.getNickname())
                .password(member.getPassword())
                .email(member.getEmail())
                .birthday(request.getBirthday())
                .gender(member.getGender())
                .phone(request.getPhone())
                .memberRole(member.getMemberRole())
                .memberStatus(member.getMemberStatus())
                .memberTermList(memberTermList)
                .profileImg(member.getProfileImg())
                .build();
    }

    public static MemberResponseDTO.memberProfileDTO memberProfileDTO(Member member){
        List<MemberTerm> memberTermList = member.getMemberTermList();

        if (memberTermList == null) {
            memberTermList = new ArrayList<>();
        }

        ProfileImg profileImg = member.getProfileImg();
        MemberTeam preferTeam = member.getMemberTeam();

        String profileImgUrl = null;
        String preferTeamName = null;

        if(profileImg != null){
            profileImgUrl = profileImg.getUrl();
        }

        if(preferTeam != null){
            preferTeamName = preferTeam.getTeam().getTeamName();
        }

        return MemberResponseDTO.memberProfileDTO.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .nickname(member.getName())
                //.name(member.getName())
                .profileImgUrl(profileImgUrl)
                .teamName(preferTeamName)
                .phoneNumber(member.getPhone())
                .gender(member.getGender().name())
                .phoneNumber(member.getPhone())
                .point(member.getPoint())
                .memberTerm(memberTermList.stream().map(memberTeam -> memberTeam.getMemberAgree()).toList())
                .build();
    }

    public static MemberResponseDTO.ProfileModifyResultDTO toProfileModify(Member member) {

        return MemberResponseDTO.ProfileModifyResultDTO.builder()
                .nickname(member.getName())
                .build();

    }

    public static MemberResponseDTO.UpdateProfileResultDTO toProfileUpdate(Member member) {

        return MemberResponseDTO.UpdateProfileResultDTO.builder()
                .memberId(member.getId())
                .nickname(member.getName())
                .email(member.getEmail())
                .updatedAt(member.getUpdatedAt())
                .build();

    }

    public static MemberResponseDTO.MemberPreferTeamDTO toMemberPreferTeamDTO(MemberTeam memberTeam){
        return MemberResponseDTO.MemberPreferTeamDTO.builder()
                .memberTeamId(memberTeam.getId())
                .createdAt(memberTeam.getCreatedAt())
                .build();
    }

    public static MemberTeam toPreferTeam(Member member, Team team){
        return MemberTeam.builder()
                .member(member)
                .team(team)
                .build();
    }

    public static MemberResponseDTO.SearchIdDTO toSearchIdResultDTO(Member member) {
        return MemberResponseDTO.SearchIdDTO.builder()
                .id(member.getId())
                .email(member.getEmail())
                .build();
    }

    public static RegisterSeller toRegisterSeller(MemberRequestDTO.RegisterSellerDTO request, Member member){
        return RegisterSeller.builder()
                .title(request.getTitle())
                .contents(request.getContents())
                .member(member)
                .build();
    }

    public static MemberResponseDTO.RegisterSellerResultDTO toRegisterSellerResultDTO(RegisterSeller registerSeller){
        return MemberResponseDTO.RegisterSellerResultDTO.builder()
                .nickname(registerSeller.getMember().getName())
                .createdAt(registerSeller.getCreatedAt())
                .build();
    }

    public static MemberResponseDTO.ModifySellerResultDTO toModifySellerResultDTO(Member member){
        return MemberResponseDTO.ModifySellerResultDTO.builder()
                .nickname(member.getName())
                .memberRole(member.getMemberRole().name())
                .email(member.getEmail())
                .build();
    }

    public static MemberResponseDTO.PurchaseLanTourDetailDTO toPurchaseLanTourDetailDTO(LanTourPurchase lanTourPurchase){
        List<LanTourResponseDTO.LanTourImageResponseDTO> lanTourImageResponseDTOList = lanTourPurchase.getLanTour().getLanTourImgList().stream()
                .map(LanTourConverter::toLanTourImgDTO)
                .filter(LanTourResponseDTO.LanTourImageResponseDTO::getIsThumbNail).collect(Collectors.toList());

        return MemberResponseDTO.PurchaseLanTourDetailDTO.builder()
                .memberName(lanTourPurchase.getMember().getName())
                .lanTourTitle(lanTourPurchase.getLanTour().getTitle())
                .price(lanTourPurchase.getLanTour().getPrice())
                .salesCount(lanTourPurchase.getLanTour().getSalesCount())
                .location(lanTourPurchase.getLanTour().getLocation())
                .lanTourCategory(lanTourPurchase.getLanTour().getLanTourCategory().name())
                .createdAt(lanTourPurchase.getCreatedAt())
                .lanTourImgList(lanTourImageResponseDTOList)
                .build();
    }

    public static MemberResponseDTO.PurchaseLanTourPreviewDTO purchaseLanTourPreviewDTO(LanTourPurchase lanTourPurchase){
        List<LanTourResponseDTO.LanTourImageResponseDTO> lanTourImageResponseDTOList = lanTourPurchase.getLanTour().getLanTourImgList().stream()
                .map(LanTourConverter::toLanTourImgDTO)
                .filter(LanTourResponseDTO.LanTourImageResponseDTO::getIsThumbNail).collect(Collectors.toList());

        return MemberResponseDTO.PurchaseLanTourPreviewDTO.builder()
                .purchaseLanTourId(lanTourPurchase.getId())
                .lanTourTitle(lanTourPurchase.getLanTour().getTitle())
                .price(lanTourPurchase.getLanTour().getPrice())
                .lanTourImgList(lanTourImageResponseDTOList)
                .lanTourCategory(lanTourPurchase.getLanTour().getLanTourCategory().name())
                .createdAt(lanTourPurchase.getCreatedAt())
                .build();
    }

    public static MemberResponseDTO.PurchaseLanTourPreviewListDTO purchaseLanTourPreviewListDTO(Page<LanTourPurchase> purchaseLanTourList){

        List<MemberResponseDTO.PurchaseLanTourPreviewDTO> purchaseLanTourPreviewDTOList = purchaseLanTourList.stream()
                .map(MemberConverter::purchaseLanTourPreviewDTO).collect(Collectors.toList());

        return MemberResponseDTO.PurchaseLanTourPreviewListDTO.builder()
                .purchaseLanTourPreviewDTOList(purchaseLanTourPreviewDTOList)
                .isFirst(purchaseLanTourList.isFirst())
                .isLast(purchaseLanTourList.isLast())
                .listSize(purchaseLanTourList.getSize())
                .totalElements(purchaseLanTourList.getTotalElements())
                .totalPage(purchaseLanTourList.getTotalPages())
                .build();
    }

}
