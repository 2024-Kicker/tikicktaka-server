package com.example.tikicktaka.web.dto.myPage;
import lombok.Builder;


@Builder
public record BlockedTravelRegionDTO(
        Long id,
        String name,
        String address,
        String thumbnailUrl,
        boolean blocked         // 항상 true
) {}
