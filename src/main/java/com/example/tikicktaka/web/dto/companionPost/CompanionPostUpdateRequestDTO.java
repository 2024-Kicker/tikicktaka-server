package com.example.tikicktaka.web.dto.companionPost;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanionPostUpdateRequestDTO {
    // null이면 변경 없음
    private String title;
    private String content;
    private CompanionPost.PostStatus status;
    private CompanionPost.PostType postType;

    private Boolean replaceAllImages = Boolean.FALSE;
}
