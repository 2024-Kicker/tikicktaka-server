package com.example.tikicktaka.web.dto.travelRegion;

import lombok.*;

@Getter @Builder
@AllArgsConstructor @NoArgsConstructor
public class TravelRegionScrapDTO {
    private Long id;            // 여행지 ID
    private String title;       // 여행지명 (TravelRegion.title)
    private String imageUrl;    // 대표 이미지 (TravelRegion.firstImage)
    private Boolean scrapped;   // 항상 true
    // 필요하면 아래 주석 해제해서 쓰면 됨
    // private String categoryCode; // contentTypeId를 문자열로 넘기고 싶을 때
    // private String teamName;     // 구장/팀명
    // private String createdAt;    // 스크랩 생성시각(엔티티에 있으면)
}
