package com.example.tikicktaka.converter.member;

import com.example.tikicktaka.converter.lanTour.LanTourConverter;
import com.example.tikicktaka.domain.enums.MemberRole;
import com.example.tikicktaka.domain.enums.MemberStatus;
import com.example.tikicktaka.domain.images.ProfileImg;
import com.example.tikicktaka.domain.lanTour.Inquiry;
import com.example.tikicktaka.domain.lanTour.InquiryAnswer;
import com.example.tikicktaka.domain.lanTour.Review;
import com.example.tikicktaka.domain.mapping.lanTour.LanTourPurchase;
import com.example.tikicktaka.domain.mapping.member.ChargeCoin;
import com.example.tikicktaka.domain.mapping.member.Dibs;
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

    public static ChargeCoin toChargeCoin(Member member, MemberRequestDTO.ChargeCoinRequestDTO request){
        return ChargeCoin.builder()
                .amount(request.getAmount())
                .title(request.getTitle())
                .member(member)
                .leftover(member.getPoint())
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

    public static MemberResponseDTO.DibsLanTourPreviewDTO dibsLanTourPreviewDTO(Dibs dibs){
        List<LanTourResponseDTO.LanTourImageResponseDTO> lanTourImageResponseDTOList = dibs.getLanTour().getLanTourImgList().stream()
                .map(LanTourConverter::toLanTourImgDTO)
                .filter(LanTourResponseDTO.LanTourImageResponseDTO::getIsThumbNail).collect(Collectors.toList());

        return MemberResponseDTO.DibsLanTourPreviewDTO.builder()
                .lanTourId(dibs.getLanTour().getId())
                .lanTourTitle(dibs.getLanTour().getTitle())
                .lanTourCategory(dibs.getLanTour().getLanTourCategory().name())
                .lanTourImgList(lanTourImageResponseDTOList)
                .price(dibs.getLanTour().getPrice())
                .createdAt(dibs.getCreatedAt())
                .build();
    }

    public static MemberResponseDTO.DibsLanTourPreviewListDTO dibsLanTourPreviewListDTO(Page<Dibs> dibsLanTourList){
        List<MemberResponseDTO.DibsLanTourPreviewDTO> dibsLanTourPreviewDTOList = dibsLanTourList.stream()
                .map(MemberConverter::dibsLanTourPreviewDTO).collect(Collectors.toList());

        return MemberResponseDTO.DibsLanTourPreviewListDTO.builder()
                .dibsLanTourPreviewDTOList(dibsLanTourPreviewDTOList)
                .isFirst(dibsLanTourList.isFirst())
                .isLast(dibsLanTourList.isLast())
                .listSize(dibsLanTourList.getSize())
                .totalElements(dibsLanTourList.getTotalElements())
                .totalPage(dibsLanTourList.getTotalPages())
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

    public static MemberResponseDTO.LanTourDibsResultDTO lanTourDibsResultDTO(Dibs dibs){
        return MemberResponseDTO.LanTourDibsResultDTO.builder()
                .lanTourId(dibs.getLanTour().getId())
                .build();
    }

    public static MemberResponseDTO.LanTourDibsDeleteDTO lanTourDibsDeleteDTO(Dibs dibs){
        return MemberResponseDTO.LanTourDibsDeleteDTO.builder()
                .lanTourId(dibs.getLanTour().getId())
                .memberId(dibs.getMember().getId())
                .build();
    }

    public static MemberResponseDTO.ChargeCoinPreviewDTO chargeCoinPreviewDTO(ChargeCoin chargeCoin){
        return MemberResponseDTO.ChargeCoinPreviewDTO.builder()
                .title(chargeCoin.getTitle())
                .amount(chargeCoin.getAmount())
                .leftover(chargeCoin.getLeftover())
                .createdAt(chargeCoin.getCreatedAt())
                .build();
    }

    public static MemberResponseDTO.ChargeCoinPreviewListDTO chargeCoinPreviewListDTO(Member member, Page<ChargeCoin> chargeCoinList){
        List<MemberResponseDTO.ChargeCoinPreviewDTO> chargeCoinPreviewDTOList = chargeCoinList.stream()
                .map(MemberConverter::chargeCoinPreviewDTO).collect(Collectors.toList());

        return MemberResponseDTO.ChargeCoinPreviewListDTO.builder()
                .amount(member.getPoint())
                .chargeCoinPreviewDTOList(chargeCoinPreviewDTOList)
                .isFirst(chargeCoinList.isFirst())
                .isLast(chargeCoinList.isLast())
                .listSize(chargeCoinList.getSize())
                .totalPage(chargeCoinList.getTotalPages())
                .totalElements(chargeCoinList.getTotalElements())
                .build();
    }

    public static MemberResponseDTO.SpendCoinPreviewDTO spendCoinPreviewDTO(LanTourPurchase lanTourPurchase){
        List<LanTourResponseDTO.LanTourImageResponseDTO> lanTourImageResponseDTOList = lanTourPurchase.getLanTour().getLanTourImgList().stream()
                .map(LanTourConverter::toLanTourImgDTO)
                .filter(LanTourResponseDTO.LanTourImageResponseDTO::getIsThumbNail).collect(Collectors.toList());

        return MemberResponseDTO.SpendCoinPreviewDTO.builder()
                .title(lanTourPurchase.getLanTour().getTitle())
                .price(lanTourPurchase.getLanTour().getPrice())
                .createdAt(lanTourPurchase.getCreatedAt())
                .lanTourImgList(lanTourImageResponseDTOList)
                .build();
    }

    public static MemberResponseDTO.SpendCoinPreviewListDTO spendCoinPreviewListDTO(Member member, Page<LanTourPurchase> lanTourList){
        List<MemberResponseDTO.SpendCoinPreviewDTO> spendCoinPreviewDTOList = lanTourList.stream()
                .map(MemberConverter::spendCoinPreviewDTO).collect(Collectors.toList());

        return MemberResponseDTO.SpendCoinPreviewListDTO.builder()
                .amount(member.getPoint())
                .spendCoinPreviewDTOList(spendCoinPreviewDTOList)
                .isFirst(lanTourList.isFirst())
                .isLast(lanTourList.isLast())
                .listSize(lanTourList.getSize())
                .totalPage(lanTourList.getTotalPages())
                .totalElements(lanTourList.getTotalElements())
                .build();
    }

    public static MemberResponseDTO.MyReviewPreviewDTO myReviewPreviewDTO(Review review){
        return MemberResponseDTO.MyReviewPreviewDTO.builder()
                .lanTourId(review.getLanTour().getId())
                .nickname(review.getMember().getName())
                .contents(review.getContents())
                .ratings(review.getRatings())
                .profileImgUrl(review.getMember().getProfileImg().getUrl())
                .createdAt(review.getCreatedAt())
                .build();
    }

    public static MemberResponseDTO.MyReviewPreviewListDTO myReviewPreviewListDTO(Page<Review> reviewList){
        List<MemberResponseDTO.MyReviewPreviewDTO> myReviewPreviewDTOList = reviewList.stream()
                .map(MemberConverter::myReviewPreviewDTO).collect(Collectors.toList());

        return MemberResponseDTO.MyReviewPreviewListDTO.builder()
                .myReviewPreviewDTOList(myReviewPreviewDTOList)
                .isFirst(reviewList.isFirst())
                .isLast(reviewList.isLast())
                .listSize(reviewList.getSize())
                .totalPage(reviewList.getTotalPages())
                .totalElements(reviewList.getTotalElements())
                .build();
    }

    public static MemberResponseDTO.MyInquiryPreviewDTO myInquiryPreviewDTO(Inquiry inquiry){
        InquiryAnswer inquiryAnswer = inquiry.getInquiryAnswer();
        LanTourResponseDTO.LanTourInquiryAnswerPreviewDTO inquiryAnswerResultDTO = null;
        if(inquiryAnswer == null){
            inquiryAnswerResultDTO = null;
        } else{
            inquiryAnswerResultDTO = LanTourConverter.lanTourInquiryAnswerPreviewDTO(inquiryAnswer);
        }

        return MemberResponseDTO.MyInquiryPreviewDTO.builder()
                .nickname(inquiry.getMember().getName())
                .profileImgUrl(inquiry.getMember().getProfileImg().getUrl())
                .title(inquiry.getTitle())
                .contents(inquiry.getContents())
                .inquiryStatus(inquiry.getStatus().name())
                .secret(inquiry.getSecret())
                .lanTourInquiryAnswer(inquiryAnswerResultDTO)
                .createdAt(inquiry.getCreatedAt())
                .build();
    }

    public static MemberResponseDTO.MyInquiryPreviewListDTO myInquiryPreviewListDTO(Page<Inquiry> inquiryList){
        List<MemberResponseDTO.MyInquiryPreviewDTO> myInquiryPreviewDTOList = inquiryList.stream()
                .map(MemberConverter::myInquiryPreviewDTO).collect(Collectors.toList());

        return MemberResponseDTO.MyInquiryPreviewListDTO.builder()
                .myInquiryPreviewDTOList(myInquiryPreviewDTOList)
                .isFirst(inquiryList.isFirst())
                .isLast(inquiryList.isLast())
                .listSize(inquiryList.getSize())
                .totalPage(inquiryList.getTotalPages())
                .totalElements(inquiryList.getTotalElements())
                .build();
    }

    public static MemberResponseDTO.ChangePasswordResultDTO changePasswordResultDTO(Member member){
        return MemberResponseDTO.ChangePasswordResultDTO.builder()
                .nickname(member.getName())
                .build();
    }
}
