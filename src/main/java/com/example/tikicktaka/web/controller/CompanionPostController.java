package com.example.tikicktaka.web.controller;


import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.enums.CompanionPostSortType;
import com.example.tikicktaka.domain.enums.CompanionPostStatus;
import com.example.tikicktaka.service.CompanionPostService.CompanionPostService;
import com.example.tikicktaka.service.memberService.MemberCommandService;
import com.example.tikicktaka.service.scrap.ScrapCommandService;
import com.example.tikicktaka.domain.images.CompanionPostImg;

import com.example.tikicktaka.service.memberService.MemberQueryService;
import com.example.tikicktaka.web.dto.companionPost.CompanionPostListResponseDTO;
import com.example.tikicktaka.repository.companionPost.CompanionPostImageRepository;
import com.example.tikicktaka.web.dto.companionPost.CompanionPostResponseDTO;
import com.example.tikicktaka.web.dto.companionPost.UpdatePostStatusRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.member;

@RestController
@RequestMapping("/api/companionPost")
@Tag(name = "companionPost", description = "동행찾기 게시판")
public class CompanionPostController {

    @Autowired
    private CompanionPostService postService;

    @Autowired
    private MemberQueryService memberQueryService; // MemberQueryService 주입

    @Autowired
    private CompanionPostImageRepository companionPostImageRepository;

    @Autowired
    private  CompanionPostService companionPostService;

    @Autowired
    private ScrapCommandService scrapCommandService;



    @PostMapping(value = "/create", consumes = "multipart/form-data")
    @Operation(summary = "동행찾기 게시판 게시글 작성", description = "request: 날짜, 홈팀, 어웨이팀")
    public ApiResponse<CompanionPostResponseDTO> createPost(@RequestParam String title,
                                                            @RequestParam String content,
                                                            @RequestParam(required = false) List<MultipartFile> imageFiles, // 여러 이미지 처리
                                                            @RequestParam CompanionPost.PostStatus status,
                                                            @RequestParam CompanionPost.TravelStatus travelStatus,
                                                            Authentication authentication) {

        // 인증된 사용자 정보 가져오기
        if (authentication == null || authentication.getName() == null) {
            return ApiResponse.onFailure(ErrorStatus._UNAUTHORIZED.getCode(),
                    ErrorStatus._UNAUTHORIZED.getMessage(),
                    null);
        }

        Long memberId = Long.valueOf(authentication.getName());

        // 회원 정보 조회
        memberQueryService.findMemberById(memberId).orElseThrow(() ->
                new RuntimeException("회원 정보를 찾을 수 없습니다.") // 예외 처리
        );

        // 게시글 생성
        CompanionPost post = postService.createPostWithImages(title, content, memberId, imageFiles, status, travelStatus);

        // 게시글의 이미지 URL 가져오기
        List<String> imageUrls = companionPostImageRepository.findByCompanionPost(post).stream()
                .map(CompanionPostImg::getImageUrl)
                .collect(Collectors.toList());

        // 게시글을 응답 DTO로 변환
        CompanionPostResponseDTO responseDTO = new CompanionPostResponseDTO(post, imageUrls);

        return ApiResponse.onSuccess(responseDTO);
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제", description = "본인이 작성한 게시글을 삭제합니다.")
    public ApiResponse<CompanionPostResponseDTO> deletePost(@PathVariable Long postId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ApiResponse.onFailure(ErrorStatus._UNAUTHORIZED.getCode(),
                    ErrorStatus._UNAUTHORIZED.getMessage(),
                    null);
        }

        Long memberId = Long.valueOf(authentication.getName());
        CompanionPost deletedPost = postService.deletePost(postId, memberId);

        // 삭제된 게시글의 이미지 URL 가져오기
        List<String> imageUrls = companionPostImageRepository.findByCompanionPost(deletedPost).stream()
                .map(CompanionPostImg::getImageUrl)
                .collect(Collectors.toList());

        return ApiResponse.onSuccess(new CompanionPostResponseDTO(deletedPost, imageUrls));
    }

//    @GetMapping("/list")
//    @Operation(summary = "게시글 목록 조회", description = "모든 게시글 목록을 조회합니다.")
//    public ApiResponse<Page<CompanionPostListResponseDTO>> getPostList(
//            @ParameterObject
//            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
//        Page<CompanionPostListResponseDTO> postList = postService.getPostList(pageable);
//        return ApiResponse.onSuccess(postList);
//    }

    @GetMapping("/list")
    @Operation(summary = "게시글 목록 조회", description = "로그인한 사용자가 차단한 게시글을 제외한 목록을 조회합니다.")
    public ApiResponse<Page<CompanionPostListResponseDTO>> getPostList(
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(defaultValue = "LATEST") CompanionPostSortType sortType,
            @RequestParam(defaultValue = "ALL") CompanionPostStatus statusFilter,
            Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            return ApiResponse.onFailure(ErrorStatus._UNAUTHORIZED.getCode(),
                    ErrorStatus._UNAUTHORIZED.getMessage(),
                    null);
        }

        Long memberId = Long.valueOf(authentication.getName());
        Page<CompanionPostListResponseDTO> postList =
                postService.getPostList(memberId, pageable, sortType, statusFilter);
        return ApiResponse.onSuccess(postList);
    }

//    @GetMapping("/{postId}")
//    @Operation(summary = "동행찾기 게시글 상세 조회 API", description = "게시글 ID를 기반으로 상세 내용을 조회합니다.")
//    public ApiResponse<CompanionPostResponseDTO> getPostDetail(@PathVariable Long postId) {
//        CompanionPostResponseDTO responseDTO = postService.getPostDetail(postId);
//        return ApiResponse.onSuccess(responseDTO);
//    }

    @GetMapping("/{postId}")
    @Operation(summary = "동행찾기 게시글 상세 조회 API", description = "게시글 ID를 기반으로 상세 내용을 조회합니다.")
    public ApiResponse<CompanionPostResponseDTO> getPostDetail(@PathVariable Long postId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ApiResponse.onFailure(ErrorStatus._UNAUTHORIZED.getCode(),
                    ErrorStatus._UNAUTHORIZED.getMessage(),
                    null);
        }

        Long memberId = Long.valueOf(authentication.getName());

        try {
            CompanionPostResponseDTO postDetail = postService.getPostDetail(postId, memberId);
            return ApiResponse.onSuccess(postDetail);
        } catch (IllegalStateException e) {
            return ApiResponse.onFailure(ErrorStatus.BLOCKED_POST_FORBIDDEN.getCode(),
                    "차단된 게시글입니다.",
                    null);
        }
    }

    //공유 게시글 조회
    @GetMapping("/public/{postId}")
    @Operation(summary = "공개용 동행찾기 게시글 상세 조회", description = "비회원도 접근 가능한 게시글 상세 조회 API입니다.")
    public ApiResponse<CompanionPostResponseDTO> getPublicPostDetail(@PathVariable Long postId) {
        CompanionPostResponseDTO responseDTO = postService.getPostDetail(postId);
        return ApiResponse.onSuccess(responseDTO);
    }

    //게시글 차단
    @PostMapping("/{postId}/block")
    @Operation(summary = "게시글 차단", description = "사용자가 특정 게시글을 차단합니다.")
    public ApiResponse<String> blockPost(@PathVariable Long postId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ApiResponse.onFailure(ErrorStatus._UNAUTHORIZED.getCode(),
                    ErrorStatus._UNAUTHORIZED.getMessage(),
                    null);
        }

        Long memberId = Long.valueOf(authentication.getName());
        try {
            postService.blockPost(memberId, postId);
            return ApiResponse.onSuccess("게시글이 차단되었습니다.");
        } catch (Exception e) {
            return ApiResponse.onFailure(ErrorStatus._BAD_REQUEST.getCode(),
                    "차단 처리에 실패했습니다.",
                    null);
        }
    }

    //게시글 차단해제
    @DeleteMapping("/{postId}/block")
    @Operation(summary = "게시글 차단 해제", description = "사용자가 차단한 게시글을 차단 해제합니다.")
    public ApiResponse<String> unblockPost(@PathVariable Long postId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ApiResponse.onFailure(ErrorStatus._UNAUTHORIZED.getCode(),
                    ErrorStatus._UNAUTHORIZED.getMessage(),
                    null);
        }

        Long memberId = Long.valueOf(authentication.getName());

        try {
            postService.unblockPost(memberId, postId);
            return ApiResponse.onSuccess("게시글 차단이 해제되었습니다.");
        } catch (Exception e) {
            return ApiResponse.onFailure(ErrorStatus._BAD_REQUEST.getCode(),
                    "차단 해제 처리에 실패했습니다.",
                    null);
        }
    }



    @PatchMapping("/{postId}/status")
    @Operation(summary = "게시글 상태 변경", description = "게시글 상태를 변경합니다.(FOUND, FINDING)")
    public ApiResponse<Void> updatePostStatus(@PathVariable Long postId,
                                              @RequestBody UpdatePostStatusRequestDTO request,
                                              Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ApiResponse.onFailure(ErrorStatus._UNAUTHORIZED.getCode(),
                    ErrorStatus._UNAUTHORIZED.getMessage(),
                    null);
        }

        Long memberId = Long.valueOf(authentication.getName());
        companionPostService.updatePostStatus(postId, request.getStatus(), memberId);

        return ApiResponse.onSuccess(null);
    }

    // 스크랩 추가
    @PostMapping("/scrap/{postId}")
    @Operation(summary = "게시글 스크랩", description = "게시글을 스크랩합니다.")
    public ApiResponse<Void> scrapPost(@PathVariable Long postId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ApiResponse.onFailure(ErrorStatus._UNAUTHORIZED.getCode(),
                    ErrorStatus._UNAUTHORIZED.getMessage(), null);
        }
        Long memberId = Long.valueOf(authentication.getName());
        // 통합 scrap 로직 사용
        scrapCommandService.addCompanionPost(memberId, postId);
        return ApiResponse.onSuccess(null);
    }

    // 스크랩 취소
    @DeleteMapping("/scrap/{postId}")
    @Operation(summary = "게시글 스크랩 취소", description = "스크랩한 게시글을 취소합니다.")
    public ApiResponse<Void> unScrapPost(@PathVariable Long postId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ApiResponse.onFailure(ErrorStatus._UNAUTHORIZED.getCode(),
                    ErrorStatus._UNAUTHORIZED.getMessage(), null);
        }
        Long memberId = Long.valueOf(authentication.getName());
        // 통합 scrap 로직 사용
        scrapCommandService.removeCompanionPost(memberId, postId);
        return ApiResponse.onSuccess(null);
    }
}
