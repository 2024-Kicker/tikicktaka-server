package com.example.tikicktaka.web.dto.stadiumAttraction;

import lombok.*;

//public class StadiumAttractionResponseDTO {
//
//    @Getter
//    @Builder
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public static class Item {
//        private Long id;
//        private String name;         // title 매핑
//        private String category;     // contentTypeId 등에서 변환 가능
//        private Double rating;       // 없으면 null 유지
//        private Long reviewCount;    // 없으면 null 유지
//        private Double distanceKm;   // lat/lng 주면 계산, 없으면 null
//        private Double lat;          // mapY
//        private Double lng;          // mapX
//        private String imageUrl;     // firstImage 등 있다면 매핑
//        private Long teamId;         // 어떤 팀(구장) 기준인지
//        private String stadiumName;  // 선택(팀에서 추출 가능 시)
//    }
//}

public class StadiumAttractionResponseDTO {

    @Getter @Builder
    @AllArgsConstructor @NoArgsConstructor
    public static class Item {
        private Long id;
        private String name;        // 장소 이름
        //private String description; // 장소 설명(요약/overview)
        private String imageUrl;    // 대표 이미지
        private Double distanceKm;  // 구장으로부터의 거리 (지금은 null; 추후 좌표 기반 계산)
    }
}
