package com.example.tikicktaka.web.controller;


import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.service.CompanionPostService.CompanionPostService;
import com.example.tikicktaka.service.memberService.MemberCommandService;
import com.example.tikicktaka.service.memberService.MemberQueryService;
import com.example.tikicktaka.web.dto.companionPost.CompanionPostResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.member;

@RestController
@RequestMapping("/api/companionPost")
@Tag(name = "companionPost", description = "동행찾기 게시판")
public class CompanionPostController {

    @Autowired
    private CompanionPostService postService;

    @Autowired
    private MemberQueryService memberQueryService; // MemberQueryService 주입

    @PostMapping(value="/create", consumes = "multipart/form-data")
    @Operation(summary = "동행찾기 게시판 게시글 작성", description = "request: 날짜, 홈팀, 어웨이팀")
    public ApiResponse<CompanionPostResponseDTO> createPost(@RequestParam String title,
                                                            @RequestParam String content,
                                                            @RequestParam(required = false) List<MultipartFile> imageFiles, // 여러 이미지 처리
                                                            @RequestParam CompanionPost.PostStatus status,
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
        CompanionPost post = postService.createPostWithImages(title, content, memberId, imageFiles, status);

        // 게시글을 응답 DTO로 변환
        CompanionPostResponseDTO responseDTO = new CompanionPostResponseDTO(post);

        return ApiResponse.onSuccess(responseDTO);
    }

}