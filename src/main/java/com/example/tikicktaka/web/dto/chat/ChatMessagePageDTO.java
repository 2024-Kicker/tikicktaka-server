package com.example.tikicktaka.web.dto.chat;

import lombok.*;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessagePageDTO {
    private boolean authorView;           // 방장/작성자 시 true
    private Long prevCursor;              // 페이지 맨 앞 메시지 id
    private Long nextCursor;              // 페이지 맨 뒤 메시지 id
    private List<ChatMessageItemDTO> messages;

    //private String inviteCode;    // 1:1 + authorView=true 일 때만 세팅


}
