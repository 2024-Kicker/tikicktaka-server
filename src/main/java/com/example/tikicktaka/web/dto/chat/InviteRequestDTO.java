package com.example.tikicktaka.web.dto.chat;

public class InviteRequestDTO {
    private Long postId;
    private Long ownerId;

    // 기본 생성자
    public InviteRequestDTO() {}

    // 생성자
    public InviteRequestDTO(Long postId, Long ownerId) {
        this.postId = postId;
        this.ownerId = ownerId;
    }

    // getters and setters
    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
}

