package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.code.status.SuccessStatus;
import com.example.tikicktaka.apiPayload.exception.handler.MemberHandler;
import com.example.tikicktaka.converter.member.MemberConverter;
import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.mapping.member.MemberTeam;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.storyRoom.StoryRoomPost;
import com.example.tikicktaka.domain.travel.TravelRegion;
import com.example.tikicktaka.repository.companionPost.CompanionPostRepository;
import com.example.tikicktaka.repository.storyRoom.StoryRoomPostRepository;
import com.example.tikicktaka.repository.travelRegion.TravelRegionRepository;
import com.example.tikicktaka.service.blocked.BlockedService;
import com.example.tikicktaka.service.memberService.MemberCommandService;
import com.example.tikicktaka.service.memberService.MemberQueryService;
import com.example.tikicktaka.domain.enums.TargetType;
import com.example.tikicktaka.service.myPageService.MyPageService;
import com.example.tikicktaka.service.myPageService.MyScrapQueryService;
import com.example.tikicktaka.web.dto.member.MemberRequestDTO;
import com.example.tikicktaka.web.dto.member.MemberResponseDTO;
import com.example.tikicktaka.web.dto.myPage.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@io.swagger.v3.oas.annotations.tags.Tag(name = "MyPage", description = "MyPage 관련 API")
@RequestMapping("/api/mypage")
public class MyPageController {

    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;
    private final MyPageService myPageService;
    private final MyScrapQueryService myScrapQueryService;
    private final BlockedService blockedService;
    private final CompanionPostRepository companionPostRepository;
    private final StoryRoomPostRepository storyRoomPostRepository;
    private final TravelRegionRepository travelRegionRepository;

    private Long currentMemberId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }



    @GetMapping("/my/profile")
    @Operation(summary = "나의 프로필 조회 API", description = "나의 프로필 정보 조회를 위한 API")
    public ApiResponse<MemberResponseDTO.memberProfileDTO> memberProfile(Authentication authentication){

        Member member = memberQueryService.findMemberById(Long.valueOf(authentication.getName().toString())).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        return ApiResponse.onSuccess(MemberConverter.memberProfileDTO(member));
    }

    @PutMapping(value = "/profile-image/upload", consumes = "multipart/form-data")
    @Operation(summary = "마이페이지 프로필 사진 등록 api", description = "request : 프로필 이미지")
    public ApiResponse<MemberResponseDTO.ProfileModifyResultDTO> profileModify(@RequestParam("profile") MultipartFile profile,
                                                                               Authentication authentication) {
        Member member = memberQueryService.findMemberById(Long.valueOf(authentication.getName().toString())).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Member modifyMember = memberCommandService.profileImageUpload(profile, member);

        return ApiResponse.onSuccess(MemberConverter.toProfileModify(modifyMember));
    }

    @PutMapping(value = "/profile/modify")
    @Operation(summary = "프로필 수정 api", description = "request : 닉네임, 전화번호, 생년월일")
    public ApiResponse<MemberResponseDTO.UpdateProfileResultDTO> updateProfile(@RequestBody MemberRequestDTO.UpdateMemberDTO request,
                                                                               Authentication authentication){

        Member member = memberQueryService.findMemberById(Long.valueOf(authentication.getName().toString())).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Member updateMember = memberCommandService.modifyProfile(request, member);

        return ApiResponse.onSuccess(MemberConverter.toProfileUpdate(updateMember));
    }

    @PostMapping(value = "/teams/{teamId}")
    @Operation(summary = "선호 구단 등록 API", description = "request : 팀 id.")
    public ApiResponse<MemberResponseDTO.MemberPreferTeamDTO> preferTeam(@PathVariable("teamId") Long teamId,
                                                                         Authentication authentication){
        Member member = memberQueryService.findMemberById(Long.valueOf(authentication.getName().toString())).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        MemberTeam memberTeam = memberCommandService.setPreferTeam(member, teamId);
        return ApiResponse.onSuccess(MemberConverter.toMemberPreferTeamDTO(memberTeam));
    }

    @PostMapping("/travel/style")
    @Operation(summary = "사용자의 선호 여행 스타일 저장/변경 API", description = "여행 스타일 2개를 선택하여 저장 또는 변경합니다.")
    public ApiResponse<String> saveOrUpdateTravelStyles(@RequestBody @Valid MemberRequestDTO.TravelStyleDTO request,
                                                        Authentication authentication) {
        Member member = memberQueryService.findMemberById(Long.valueOf(authentication.getName()))
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        memberCommandService.saveOrUpdateMemberTravelStyles(member, request.getStyleOne(), request.getStyleTwo());
        return ApiResponse.onSuccess("여행 스타일이 저장되었습니다.");
    }

    // 여행 스타일 조회 API
    @GetMapping("/travel/style")
    @Operation(summary = "사용자의 선호 여행 스타일 조회 API")
    public ApiResponse<MemberResponseDTO.MemberPreferTravelStyleDTO> getTravelStyles(Authentication authentication) {
        Member member = memberQueryService.findMemberById(Long.valueOf(authentication.getName()))
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        return ApiResponse.onSuccess(memberCommandService.getMemberTravelStyles(member));
    }

    // 작성글 모아보기 - 동행
    @GetMapping("/companion-posts")
    @Operation(summary = "사용자가 작성한 동행 찾기 게시글 목록 api ")
    public ApiResponse<List<MyPostItemDTO>> myCompanionPosts(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ApiResponse.onFailure(
                    ErrorStatus._UNAUTHORIZED.getCode(),
                    ErrorStatus._UNAUTHORIZED.getMessage(),
                    null
            );
        }
        Long memberId = Long.valueOf(authentication.getName());
        return ApiResponse.onSuccess(myPageService.getMyCompanionPosts(memberId));
    }

    // 작성글 모아보기 - 이야기
    @GetMapping("/story-posts")
    @Operation(summary = "사용자가 작성한 이야기방 게시글 목록 api ")
    public ApiResponse<List<MyPostItemDTO>> myStoryPosts(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ApiResponse.onFailure(
                    ErrorStatus._UNAUTHORIZED.getCode(),
                    ErrorStatus._UNAUTHORIZED.getMessage(),
                    null
            );
        }
        Long memberId = Long.valueOf(authentication.getName());
        return ApiResponse.onSuccess(myPageService.getMyStoryPosts(memberId));
    }

    @DeleteMapping(value = "/delete")
    @Operation(summary = "회원 탈퇴 api")
    public ApiResponse<?> deleteMember(Authentication authentication){

        Member member = memberQueryService.findMemberById(Long.valueOf(authentication.getName().toString())).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        memberCommandService.deleteMember(member.getId());
        return ApiResponse.of(SuccessStatus.MEMBER_DELETE_SUCCESS, null);
    }


    @GetMapping("scraps/companion-posts")
    @Operation(summary = "사용자가 스크랩한 동행찾기 게시글 목록 api ")
    public ApiResponse<List<ScrapCompanionPostDTO>> getCompanionPostScraps(Authentication auth) {
        Long memberId = (Long) auth.getPrincipal(); // 또는 SecurityContext에서 memberId 꺼내는 프로젝트 방식 사용
        return ApiResponse.onSuccess(myScrapQueryService.getCompanionPostScraps(memberId));
    }

    @GetMapping("scraps/story-posts")
    @Operation(summary = "사용자가 스크랩한 이야기방 게시글 목록 api ")
    public ApiResponse<List<ScrapStoryRoomPostDTO>> getStoryPostScraps(Authentication auth) {
        Long memberId = (Long) auth.getPrincipal();
        return ApiResponse.onSuccess(myScrapQueryService.getStoryPostScraps(memberId));
    }

    @GetMapping("scraps/travel-regions")
    @Operation(summary = "사용자가 스크랩한 여행지 목록 api ")
    public ApiResponse<List<ScrapTravelRegionDTO>> getTravelRegionScraps(Authentication auth) {
        Long memberId = (Long) auth.getPrincipal();
        return ApiResponse.onSuccess(myScrapQueryService.getTravelRegionScraps(memberId));
    }

    // ===== 동행찾기 차단 목록 =====
    @GetMapping("/blocked/companion-posts")
    @Operation(summary = "차단한 동행찾기 게시글 목록")
    public ApiResponse<List<BlockedCompanionPostDTO>> getBlockedCompanionPosts(Authentication auth) {
        Long memberId = currentMemberId(auth);

        var blockedIds = blockedService.blockedIds(memberId, TargetType.COMPANION_POST);
        var posts = blockedIds.isEmpty() ? List.<CompanionPost>of()
                : companionPostRepository.findAllByIdIn(blockedIds);

        var dtos = posts.stream().map(p -> {

            return BlockedCompanionPostDTO.builder()
                    .id(p.getId())
                    .title(p.getTitle())
                    .content(p.getContent())
                    .thumbnailUrl(p.getThumbnailUrl())
                    .authorName(p.getAuthor() != null ? p.getAuthor().getName() : null)
                    .status(p.getStatus() != null ? p.getStatus().name() : null)
                    .createdAt(p.getCreatedAt())
                    .blocked(true)
                    .build();
        }).toList();

        return ApiResponse.onSuccess(dtos);
    }

    // ===== 이야기방 차단 목록 =====
    @GetMapping("/blocked/story-posts")
    @Operation(summary = "차단한 이야기방 게시글 목록")
    public ApiResponse<List<BlockedStoryRoomPostDTO>> getBlockedStoryPosts(Authentication auth) {
        Long memberId = currentMemberId(auth);

        var blockedIds = blockedService.blockedIds(memberId, TargetType.STORY_POST);
        var posts = blockedIds.isEmpty() ? List.<StoryRoomPost>of()
                : storyRoomPostRepository.findAllById(blockedIds); // 네 레포에 맞는 메서드 사용

        var dtos = posts.stream().map(p ->
                new BlockedStoryRoomPostDTO(
                        p.getId(),
                        p.getTitle(),
                        p.getContent(),
                        p.getThumbnailUrl(),
                        p.getAuthor() != null ? p.getAuthor().getName() : null,
                        p.getTopic() != null ? p.getTopic().name() : null,
                        p.getCreatedAt(),
                        true
                )
        ).toList();

        return ApiResponse.onSuccess(dtos);
    }

    // ===== 여행지 차단(숨김) 목록 =====
    @GetMapping("/blocked/travel-regions")
    @Operation(summary = "차단(숨김)한 여행지 목록")
    public ApiResponse<List<BlockedTravelRegionDTO>> getBlockedTravelRegions(Authentication auth) {
        Long memberId = currentMemberId(auth);

        var blockedIds = blockedService.blockedIds(memberId, TargetType.TRAVEL_REGION);
        var regions = blockedIds.isEmpty() ? List.<TravelRegion>of()
                : travelRegionRepository.findAllById(blockedIds);

        var dtos = regions.stream().map(r ->
                new BlockedTravelRegionDTO(
                        r.getId(),
                        r.getTitle(),
                        r.getAddr1(),
                        r.getFirstImage() != null ? r.getFirstImage() : r.getFirstImage2(),
                        true
                )
        ).toList();

        return ApiResponse.onSuccess(dtos);
    }
}
