package com.example.tikicktaka.web.dto.companionPost;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.images.CompanionPostImg;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class CompanionPostResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String thumbnailUrl;
    private String authorName;
    private String authorProfileImageUrl;
    private CompanionPost.PostStatus status;
    private CompanionPost.PostType PostType;
    private LocalDateTime createdAt;
    private Boolean isScraped;              // 로그인 사용자가 스크랩했는지 여부
    private Boolean isMine;
    private String chatRoomId;
    private List<String> imageUrls;


    public CompanionPostResponseDTO(CompanionPost post, List<String> imageUrls) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorName = (post.getAuthor() != null) ? post.getAuthor().getName() : "Unknown";
        this.status = post.getStatus();
        this.PostType = post.getPostType();
        this.thumbnailUrl = post.getThumbnailUrl();
        //this.chatRoomId = post.getChatRoomId();
        this.imageUrls = (imageUrls != null) ? imageUrls : new ArrayList<>(); // Null 체크
    }

    public static CompanionPostResponseDTO of (CompanionPost post,
                                    List<String> imageUrls,
                                    String authorProfileImageUrl,
                                    Boolean isScraped,
                                    Boolean isMine) {
        CompanionPostResponseDTO dto = new CompanionPostResponseDTO(post, imageUrls);
        dto.authorProfileImageUrl = authorProfileImageUrl;
        dto.isScraped = isScraped;
        dto.isMine = isMine;
        return dto;
    }
}
