package com.example.tikicktaka.service.storyRoom;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.domain.enums.LimitTime;
import com.example.tikicktaka.domain.enums.Topic;
import com.example.tikicktaka.web.dto.storyRoom.StoryRoomCreateRequestDTO;
import com.example.tikicktaka.web.dto.storyRoom.StoryRoomDetailResponseDTO;
import com.example.tikicktaka.web.dto.storyRoom.StoryRoomListResponseDTO;
import com.example.tikicktaka.web.dto.storyRoom.StoryRoomPostResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Service
public interface StoryRoomService {
    Long createStoryRoom(StoryRoomCreateRequestDTO request, Long creatorId);

    StoryRoomPostResponseDTO getStoryRoomPostDetail(Long postId);

    StoryRoomDetailResponseDTO getStoryRoomDetail(Long id);
    List<StoryRoomListResponseDTO> getAllStoryRooms();

    StoryRoomPostResponseDTO createStoryRoomPost(String title, String content, Topic topic, LimitTime limitTime, List<MultipartFile> imageFiles, Long memberId);
    ApiResponse<?> deleteStoryRoomPost(Long storyRoomPostId, Long memberId); //

    boolean isScrapped(Long memberId, Long postId);

    List<StoryRoomPostResponseDTO> getScrappedPosts(Long memberId);

    void unscrap(Long memberId, Long postId);

    void scrap(Long memberId, Long postId);
}
