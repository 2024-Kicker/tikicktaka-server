package com.example.tikicktaka.web.dto.myPage;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScrapTravelRegionDTO {
    private Long id;
    private String name;
    private String address;
    private String thumbnailUrl;
    private boolean scrapped; // true
}
