package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.domain.enums.LimitTime;
import com.example.tikicktaka.domain.enums.StoryRoomPostSortType;
import com.example.tikicktaka.domain.enums.StoryRoomStatus;
import com.example.tikicktaka.domain.enums.Topic;
import com.example.tikicktaka.service.storyRoom.StoryRoomService;
import com.example.tikicktaka.web.dto.storyRoom.StoryRoomDetailResponseDTO;
import com.example.tikicktaka.web.dto.storyRoom.StoryRoomPostResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/storyRoom")
@RequiredArgsConstructor
@Tag(name = "storyRoom", description = "이야기방 API")
public class StoryRoomController {

    private final StoryRoomService storyRoomService;



//    @GetMapping("/list")
//    @Operation(summary = "이야기방 게시글 목록 조회", description = "이야기방 게시판의 글 목록을 페이징하여 조회합니다.")
//    public ApiResponse<Page<StoryRoomListResponseDTO>> getStoryRoomList(
//            @PageableDefault(size = 10) Pageable pageable) {
//        Page<StoryRoomListResponseDTO> storyRooms = storyRoomService.getStoryRoomList(pageable);
//        return ApiResponse.onSuccess(storyRooms);
//    }

//    @GetMapping("/{storyRoomId}")
//    @Operation(summary = "이야기방 상세 조회", description = "특정 이야기방의 상세 내용을 조회합니다.")
//    public ApiResponse<StoryRoomDetailResponseDTO> getStoryRoomDetail(@PathVariable Long storyRoomId) {
//        StoryRoom storyRoom = storyRoomService.getStoryRoomDetail(storyRoomId);
//        return ApiResponse.onSuccess(new StoryRoomDetailResponseDTO(storyRoom));
//    }

    // 이야기방 게시글 작성 API
    @PostMapping(value = "/post/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이야기방 게시글 생성", description = "이야기방 게시글 생성 API입니다.")
    public ResponseEntity<?> createStoryRoomPost(@RequestParam String title,
                                                 @RequestParam String content,
                                                 @RequestParam Topic topic,
                                                 @RequestParam LimitTime limitTime,
                                                 @RequestParam(required = false) List<MultipartFile> imageFiles,
                                                 Authentication authentication) {
        // 인증된 사용자 ID 추출
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        Long memberId = Long.valueOf(authentication.getName());

        try {
            StoryRoomPostResponseDTO post = storyRoomService.createStoryRoomPost(title, content, topic, limitTime, imageFiles, memberId);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 작성 중 오류 발생");
        }
    }

    //게시글 삭제
    @DeleteMapping("/post/{postId}")
    @Operation(summary = "이야기방 게시글 삭제", description = "게시글을 삭제하며, 해당 게시글과 연결된 채팅방도 함께 삭제됩니다.")
    public ResponseEntity<ApiResponse<?>> deleteStoryRoomPost(@PathVariable Long postId,
                                                              Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.onFailure(ErrorStatus._UNAUTHORIZED.getCode(), ErrorStatus._UNAUTHORIZED.getMessage(), null)
            );
        }

        Long memberId = Long.valueOf(authentication.getName());
        ApiResponse<?> response = storyRoomService.deleteStoryRoomPost(postId, memberId);

        // 상태코드 처리 (isSuccess가 false면 실패)
        if (!response.getIsSuccess()) {
            ErrorStatus errorStatus = ErrorStatus.fromCode(response.getCode());
            return ResponseEntity.status(errorStatus.getHttpStatus()).body(response);
        }


        return ResponseEntity.ok(response);
    }

    // 공개용 이야기방 게시글 조회
    @Transactional
    @GetMapping("/post/public/{postId}")
    @Operation(summary = "공개용 이야기방 게시글 상세 조회", description = "비회원도 접근 가능한 이야기방 게시글 상세 조회 API입니다.")
    public ApiResponse<StoryRoomPostResponseDTO> getPublicStoryRoomPostDetail(@PathVariable Long postId) {
        StoryRoomPostResponseDTO responseDTO = storyRoomService.getStoryRoomPostDetail(postId);
        return ApiResponse.onSuccess(responseDTO);
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "이야기방 게시글 상세 조회", description = "로그인한 사용자만 확인할 수 있는 이야기방 게시글 상세 조회 API입니다.")
    public ResponseEntity<?> getStoryRoomPostDetail(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        Long memberId = Long.valueOf(authentication.getName());
        StoryRoomPostResponseDTO responseDTO = storyRoomService.getStoryRoomPostDetail(postId, memberId);
        return ResponseEntity.ok(responseDTO);
    }



    //이야기방 게시글 스크랩
    @Transactional
    @PostMapping("post/scrap/{postId}")
    @Operation(summary = "게시글 스크랩", description = "이야기방 게시글을 스크랩합니다.")
    public ApiResponse<String> scrapStoryRoom(@PathVariable Long postId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null)
            return ApiResponse.onFailure(ErrorStatus._UNAUTHORIZED.getCode(), "로그인이 필요합니다.", null);

        Long memberId = Long.valueOf(authentication.getName());
        storyRoomService.scrap(memberId, postId);
        return ApiResponse.onSuccess("스크랩 완료");
    }

    //이야기방 게시글 스크랩 해제
    @Transactional
    @DeleteMapping("post/scrap/{postId}")
    @Operation(summary = "게시글 스크랩 해제", description = "이야기방 게시글 스크랩을 해제합니다.")
    public ApiResponse<String> unscrapStoryRoom(@PathVariable Long postId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null)
            return ApiResponse.onFailure(ErrorStatus._UNAUTHORIZED.getCode(), "로그인이 필요합니다.", null);

        Long memberId = Long.valueOf(authentication.getName());
        storyRoomService.unscrap(memberId, postId);
        return ApiResponse.onSuccess("스크랩 해제 완료");
    }

    //스크랩 한 이야기방 게시글 목록 조회
    @Transactional
    @GetMapping("post/scraps")
    @Operation(summary = "스크랩한 이야기방 게시글 목록 조회", description = "로그인한 사용자가 스크랩한 이야기방 게시글 목록을 조회합니다.")
    public ApiResponse<List<StoryRoomPostResponseDTO>> getScrappedPosts(Authentication authentication) {
        if (authentication == null || authentication.getName() == null)
            return ApiResponse.onFailure(ErrorStatus._UNAUTHORIZED.getCode(), "로그인이 필요합니다.", null);

        Long memberId = Long.valueOf(authentication.getName());
        List<StoryRoomPostResponseDTO> scraps = storyRoomService.getScrappedPosts(memberId);
        return ApiResponse.onSuccess(scraps);
    }

    //이야기방 게시글 필터 조회
    // 이야기방 게시글 필터 조회
    @GetMapping("/post/list")
    @Operation(summary = "이야기방 게시글 목록 조회", description = "이야기방 게시글을 필터링하여 조회합니다.")
    public ApiResponse<List<StoryRoomPostResponseDTO>> getFilteredStoryRoomPosts(
            @RequestParam(required = false) StoryRoomStatus status,
            @RequestParam(required = false) StoryRoomPostSortType sortType,
            @RequestParam(required = false) Topic topic,
            Authentication authentication) {
        Long memberId = null;
        if (authentication != null && authentication.getName() != null) {
            memberId = Long.valueOf(authentication.getName());
        }

        List<StoryRoomPostResponseDTO> filteredPosts = storyRoomService.getFilteredStoryRoomPosts(status, sortType, topic, memberId);
        return ApiResponse.onSuccess(filteredPosts);
    }

    //이여기방 게시글 신고하기 = 차단하기
    @PostMapping("/post/block/{postId}")
    @Operation(summary = "게시글 차단", description = "사용자가 특정 이야기방 게시글을 차단합니다.")
    public ApiResponse<String> blockStoryRoomPost(@PathVariable Long postId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ApiResponse.onFailure(ErrorStatus._UNAUTHORIZED.getCode(), "로그인이 필요합니다.", null);
        }

        Long memberId = Long.valueOf(authentication.getName());

        storyRoomService.blockStoryRoomPost(memberId, postId);
        return ApiResponse.onSuccess("게시글이 차단되었습니다.");
    }
}

