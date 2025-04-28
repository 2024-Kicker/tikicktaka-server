package com.example.tikicktaka.web.dto.companionPost;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdatePostStatusRequestDTO {
    private CompanionPost.PostStatus status;
}

