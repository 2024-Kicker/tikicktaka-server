//package com.example.tikicktaka.service.chatService;
//
//import com.example.tikicktaka.service.chatService.ChatInviteService;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//@Service
//public class ChatInviteServiceImpl implements ChatInviteService {
//
//    private final Map<String, String> inviteCodes = new HashMap<>();
//
////    @Override
////    public String generateInviteCode(Long postId, Long ownerId) {
////        String code = UUID.randomUUID().toString().substring(0, 6);
////        // roomId를 생성하는 로직 추가
////        // 예시로 postId와 ownerId를 사용하여 roomId를 생성하는 방식으로 처리 (필요시 실제 로직을 추가)
////        String roomId = "room-" + postId + "-" + ownerId; // roomId는 postId와 ownerId를 기반으로 생성
////
////        // 초대 코드와 roomId를 매핑하여 저장 (예시로 Map을 사용)
////        inviteCodes.put(code, roomId);
////
////        return code; // 생성된 초대 코드 반환
////    }
//
////    @Override
////    public boolean validateInviteCode(String code, String roomId) {
////        return inviteCodes.getOrDefault(code, "").equals(roomId);
////    }
//}
//
