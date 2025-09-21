// src/main/java/com/example/tikicktaka/support/ThumbnailResolver.java
package com.example.tikicktaka.support;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.storyRoom.StoryRoomPost; // 실제 패키지에 맞게 수정
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ThumbnailResolver {

    @Value("${companion.default-images.baseball}")
    private String defaultBaseballImage;

    @Value("${companion.default-images.travel}")
    private String defaultTravelImage;

    /// 동행찾기: 비었으면 travelStatus 기준으로 기본값 반환
    public String resolveForCompanion(CompanionPost post) {
        if (post == null) return defaultBaseballImage;
        if (StringUtils.hasText(post.getThumbnailUrl())) return post.getThumbnailUrl();
        return (post.getTravelStatus() == CompanionPost.TravelStatus.Travel)
                ? defaultTravelImage
                : defaultBaseballImage;
    }

    //이야기방: 작성/조회 모두 기본값은 Travel
    public String resolveForStory(StoryRoomPost post) {
        if (post == null) return defaultTravelImage;
        if (StringUtils.hasText(post.getThumbnailUrl())) return post.getThumbnailUrl();
        return defaultTravelImage;
    }
}
