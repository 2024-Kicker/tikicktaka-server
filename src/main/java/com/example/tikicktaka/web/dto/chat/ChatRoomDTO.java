package com.example.tikicktaka.web.dto.chat;

import com.example.tikicktaka.domain.companionPostChat.ChatRoom;
import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.member.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomDTO {

    private String roomId;  // 채팅방 ID
    private Long ownerId;   // 채팅방 소유자 ID
    private Boolean isGroup; // 그룹 채팅방 여부
    private String inviteCode;  // 초대 코드
    private Long postId;


    // ChatRoom을 ChatRoomDto로 변환하는 생성자
    public ChatRoomDTO(ChatRoom chatRoom) {
        this.roomId = chatRoom.getRoomId();
        this.ownerId = chatRoom.getOwner() != null ? chatRoom.getOwner().getId() : null;  // owner에서 ID 가져오기
        this.isGroup = chatRoom.getIsGroup();
        this.inviteCode = chatRoom.getInviteCode();
        this.postId = chatRoom.getCompanionPost() != null ? chatRoom.getCompanionPost().getId() : null;  // companionPost에서 ID 가져오기
    }

    // ChatRoomDTO를 ChatRoom 엔티티로 변환
    public ChatRoom toEntity(Member owner, CompanionPost companionPost) {
        return ChatRoom.builder()
                .owner(owner)  // 실제 Member 객체
                .companionPost(companionPost)  // 실제 CompanionPost 객체
                .isGroup(this.isGroup)
                .inviteCode(this.inviteCode)
                .build();
    }

}
