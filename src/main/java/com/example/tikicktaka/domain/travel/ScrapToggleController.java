package com.example.tikicktaka.domain.travel;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.service.scrap.ScrapCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scraps")
public class ScrapToggleController {

    private final ScrapCommandService scrapCommandService;

    private Long currentMemberId(Authentication auth) {
        // 프로젝트의 인증 객체에 맞춰 memberId 추출 로직을 사용하세요.
        // 예: 커스텀 Principal이 있다면 ((AuthUser)auth.getPrincipal()).getId()
        return (Long) auth.getPrincipal();
    }

    // 1) 동행찾기 스크랩 토글
    @PostMapping("/companion-posts/{postId}/toggle")
    public ApiResponse<ToggleResponse> toggleCompanionPost(@PathVariable Long postId, Authentication auth) {
        Long memberId = currentMemberId(auth);
        boolean scrapped = scrapCommandService.toggleCompanionPost(memberId, postId);
        return ApiResponse.onSuccess(new ToggleResponse(scrapped));
    }

    // 2) 이야기방 스크랩 토글
    @PostMapping("/story-posts/{storyPostId}/toggle")
    public ApiResponse<ToggleResponse> toggleStoryPost(@PathVariable Long storyPostId, Authentication auth) {
        Long memberId = currentMemberId(auth);
        boolean scrapped = scrapCommandService.toggleStoryPost(memberId, storyPostId);
        return ApiResponse.onSuccess(new ToggleResponse(scrapped));
    }

    // 3) 장소 추천 스크랩 토글
    @PostMapping("/travel-regions/{regionId}/toggle")
    public ApiResponse<ToggleResponse> toggleTravelRegion(@PathVariable Long regionId, Authentication auth) {
        Long memberId = currentMemberId(auth);
        boolean scrapped = scrapCommandService.toggleTravelRegion(memberId, regionId);
        return ApiResponse.onSuccess(new ToggleResponse(scrapped));
    }

    public record ToggleResponse(boolean scrapped) {}
}

