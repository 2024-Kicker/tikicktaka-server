package com.example.tikicktaka.web.dto.myPage;
import java.util.List;

public record MyBlockedResponseDTO(
        List<BlockedCompanionPostDTO> companionPosts,  // 차단한 동행글
        List<BlockedStoryRoomPostDTO> storyPosts,      // 차단한 이야기글
        List<BlockedTravelRegionDTO> travelRegions  // 차단(숨김)한 장소
) {}


