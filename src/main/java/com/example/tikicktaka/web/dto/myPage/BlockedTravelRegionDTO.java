package com.example.tikicktaka.web.dto.myPage;
public record BlockedTravelRegionDTO(
        Long id,
        String name,
        String address,
        String thumbnailUrl,
        boolean blocked         // 항상 true
) {}
