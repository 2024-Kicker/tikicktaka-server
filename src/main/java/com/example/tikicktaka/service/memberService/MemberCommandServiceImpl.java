package com.example.tikicktaka.service.memberService;

import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.exception.handler.LanTourHandler;
import com.example.tikicktaka.apiPayload.exception.handler.MemberHandler;
import com.example.tikicktaka.apiPayload.exception.handler.TeamHandler;
import com.example.tikicktaka.config.MailConfig;
import com.example.tikicktaka.converter.lanTour.LanTourConverter;
import com.example.tikicktaka.converter.member.MemberConverter;
import com.example.tikicktaka.domain.enums.MemberRole;
import com.example.tikicktaka.domain.enums.MemberStatus;
import com.example.tikicktaka.domain.images.ProfileImg;
import com.example.tikicktaka.domain.lanTour.LanTour;
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
import com.example.tikicktaka.repository.lanTour.LanTourRepository;
import com.example.tikicktaka.repository.member.*;
import com.example.tikicktaka.repository.team.TeamRepository;
import com.example.tikicktaka.service.UtilService;
import com.example.tikicktaka.web.dto.member.MemberRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.example.tikicktaka.config.springSecurity.utils.JwtUtil.createJwt;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MemberCommandServiceImpl implements MemberCommandService{

    private final MemberRepository memberRepository;
    private final TermRepository termRepository;
    private final TeamRepository teamRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final ProfileImgRepository profileImgRepository;
    private final RegisterSellerRepository registerSellerRepository;
    private final ChargeCoinRepository chargeCoinRepository;
    private final LanTourPurchaseRepository lanTourPurchaseRepository;
    private final LanTourRepository lanTourRepository;
    private final DibsRepository dibsRepository;
    private final BCryptPasswordEncoder encoder;
    private final AuthRepository authRepository;
    private final MailConfig mailConfig;
    private final UtilService utilService;

    @Value("${jwt.token.secret}")
    private String key;

    private int expiredMs = 1000 * 60 * 60 * 24 * 5;

    @Transactional
    @Override
    public Member join(MemberRequestDTO.JoinDTO request) {

        Member existingMember = null;

        Optional<Member> existingNickname = memberRepository.findByName(request.getNickname());
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
                    request.getPassword(), request.getEmail(),
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
    @Transactional
    public RegisterSeller registerSeller(MemberRequestDTO.RegisterSellerDTO request, Member member) {

        RegisterSeller registerSeller = MemberConverter.toRegisterSeller(request,member);
        registerSellerRepository.save(registerSeller);
        return registerSeller;
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
        Optional<Member> member = memberRepository.findByName(request.getNickname());
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

    @Override
    @Transactional
    public Auth sendEmailAuth(String email) {

        String code = Integer.toString((int) (Math.random() * 899999) + 100000);
        Boolean expired = false;
        mailConfig.sendMail(email,code);

        Auth auth = MemberConverter.toEmailAuth(email,code,expired);

        Optional<Auth> delete = authRepository.findByEmail(email);
        if(delete.isPresent()){
            Auth deleteAuth = delete.get();
            authRepository.delete(deleteAuth);
        }

        authRepository.save(auth);
        return auth;
    }

    @Override
    @Transactional
    public Boolean confirmEmailAuth(MemberRequestDTO.EmailAuthConfirmDTO request) {
        Boolean checkEmail = false;
        Auth auth = authRepository.findByEmail(request.getEmail()).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_EMAIL_AUTH_ERROR));
        LocalDateTime current = LocalDateTime.now();

        if(auth.getCode().equals(request.getCode()) && !auth.getExpired() && auth.getExpireDate().isAfter(current)){
            checkEmail = true;

            auth.useToken();
            authRepository.save(auth);
        } else {
            throw new MemberHandler(ErrorStatus.MEMBER_EMAIL_AUTH_ERROR);
        }

        return checkEmail;
    }

    @Override
    @Transactional
    public MemberTeam setPreferTeam(Member member, Long teamId) {

        Optional<MemberTeam> beforeMemberTeam = memberTeamRepository.findByMember(member);
        if(beforeMemberTeam.isPresent()){
            MemberTeam deleteMemberTeam = beforeMemberTeam.orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_TEAM_NOT_FOUND));
            memberTeamRepository.delete(deleteMemberTeam);
            memberTeamRepository.flush();
        }

        Team team = teamRepository.findById(teamId).orElseThrow(() -> new TeamHandler(ErrorStatus.TEAM_NOT_FOUND));//error 처리 추가하기

        MemberTeam memberTeam = MemberConverter.toPreferTeam(member,team);

        return memberTeamRepository.save(memberTeam);//Member Team repository 생성
    }

    @Transactional
    @Override
    public Member completeSignup(Long memberId, MemberRequestDTO.CompleteSignupDTO request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

////        // Convert request terms to MemberTerm entities
////        HashMap<Term, Boolean> termMap = new HashMap<>();
////        for (int i = 0; i < request.getMemberTerm().size(); i++) {
////            termMap.put(termRepository.findById(i + 1L)
////                    .orElseThrow(() -> new MemberHandler(ErrorStatus.TERM_NOT_FOUND)), request.getMemberTerm().get(i));
////        }
//
//        List<MemberTerm> memberTermList = MemberConverter.toMemberTermList(termMap);

        // Update member with additional info
        member.updateAdditionalInfo(
                request.getBirthday(),
                request.getPhone()
        );


        return memberRepository.save(member);
    }

    @Override
    @Transactional
    public Member profileImageUpload(MultipartFile profile, Member member) {

        Optional<ProfileImg> older = profileImgRepository.findByMember_Id(member.getId());
        if (older.isPresent()) {
            ProfileImg old = older.get();
            profileImgRepository.delete(old);
            profileImgRepository.flush();
        }

        String profileUrl = utilService.uploadS3Img("member", profile);

        ProfileImg profileImg = MemberConverter.toProfileImg(profileUrl, member);
        member.setProfileImg(profileImg);
        profileImgRepository.save(profileImg);

        return member;
    }

    @Override
    @Transactional
    public Member modifyProfile(MemberRequestDTO.UpdateMemberDTO request, Member member) {

        Member update = MemberConverter.toUpdateProfile(member, request);
        memberRepository.save(update);

        return update;
    }

    @Override
    @Transactional
    public Member findByPhone(String phone) {
        return memberRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional
    public void deleteMember(Long memberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(()-> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        member.setMemberStatus(MemberStatus.INACTIVE);
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public Member modifySeller(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(()-> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        member.setMemberRole(MemberRole.SELLER);
        memberRepository.save(member);
        return member;
    }

    @Override
    @Transactional
    public Member chargeCoin(Member member, MemberRequestDTO.ChargeCoinRequestDTO request) {
        member.updatePoints(request.getAmount());
        ChargeCoin chargeCoin = MemberConverter.toChargeCoin(member,request);
        chargeCoinRepository.save(chargeCoin);
        return member;
    }

    @Override
    public LanTourPurchase getPurchaseLanTourDetail(Long lanTourPurchaseId) {

        return lanTourPurchaseRepository.findById(lanTourPurchaseId).orElseThrow(() -> new LanTourHandler(ErrorStatus.LAN_TOUR_PURCHASE_NOT_FOUND));
    }

    @Override
    @Transactional
    public Dibs dibsLanTour(Long lanTourId, Member member) {
        LanTour lanTour = lanTourRepository.findById(lanTourId).orElseThrow(() -> new LanTourHandler(ErrorStatus.LAN_TOUR_NOT_FOUND));
        Dibs dibs = LanTourConverter.toDibs(lanTour, member);
        dibsRepository.save(dibs);
        lanTour.increaseDibs();
        return dibs;
    }

    @Override
    @Transactional
    public Dibs deleteDibsLanTour(Long lanTourId, Member member) {
        LanTour lanTour = lanTourRepository.findById(lanTourId).orElseThrow(() -> new LanTourHandler(ErrorStatus.LAN_TOUR_NOT_FOUND));
        Dibs dibs = dibsRepository.findByLanTourAndMember(lanTour, member).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_DIBS_NOT_FOUND));
        dibsRepository.delete(dibs);
        dibsRepository.flush();
        lanTour.decreaseDibs();
        return dibs;
    }

}
