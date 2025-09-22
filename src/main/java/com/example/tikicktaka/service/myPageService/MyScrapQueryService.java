package com.example.tikicktaka.service.myPageService;

import com.example.tikicktaka.service.chatService.utils.UnreadUtils;
import com.example.tikicktaka.domain.enums.TargetType;
import com.example.tikicktaka.domain.mapping.scrap.Scrap;
import com.example.tikicktaka.repository.scrap.ScrapRepository;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.repository.companionPost.CompanionPostRepository;

import com.example.tikicktaka.domain.storyRoom.StoryRoomPost;
import com.example.tikicktaka.repository.storyRoom.StoryRoomPostRepository;

import com.example.tikicktaka.domain.travel.TravelRegion;
import com.example.tikicktaka.repository.travelRegion.TravelRegionRepository;

import com.example.tikicktaka.web.dto.myPage.ScrapCompanionPostDTO;
import com.example.tikicktaka.web.dto.myPage.ScrapStoryRoomPostDTO;
import com.example.tikicktaka.web.dto.myPage.ScrapTravelRegionDTO;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatRoomRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatParticipantRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatMessageRepository;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyScrapQueryService {

    private final ScrapRepository scrapRepository;
    private final CompanionPostRepository companionPostRepository;
    private final StoryRoomPostRepository storyRoomPostRepository;
    private final TravelRegionRepository travelRegionRepository;
    private final CompanionPostChatRoomRepository companionPostChatRoomRepository;
    private final CompanionPostChatParticipantRepository companionPostChatParticipantRepository;
    private final CompanionPostChatMessageRepository companionPostChatMessageRepository;



    public List<ScrapCompanionPostDTO> getCompanionPostScraps(Long memberId) {
        List<Long> ids = scrapRepository
                .findByMemberIdAndTargetTypeOrderByCreatedAtDesc(memberId, TargetType.COMPANION_POST)
                .stream().map(Scrap::getTargetId).toList();

        if (ids.isEmpty()) return List.of();

        Map<Long, CompanionPost> map = companionPostRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(CompanionPost::getId, it -> it));

        return ids.stream().map(map::get).filter(Objects::nonNull)
                .map(p -> {
                    return ScrapCompanionPostDTO.builder()
                            .id(p.getId())
                            .title(p.getTitle())
                            .content(p.getContent())
                            .thumbnailUrl(p.getThumbnailUrl())
                            .authorName(p.getAuthor()!=null? p.getAuthor().getName(): null)
                            .status(p.getStatus()!=null? p.getStatus().name(): null)
                            .createdAt(p.getCreatedAt())
                            .scrapped(true)
                            .build();
                })
                .toList();
    }

    public List<ScrapStoryRoomPostDTO> getStoryPostScraps(Long memberId) {
        List<Long> ids = scrapRepository
                .findByMemberIdAndTargetTypeOrderByCreatedAtDesc(memberId, TargetType.STORY_POST)
                .stream().map(Scrap::getTargetId).toList();

        if (ids.isEmpty()) return List.of();

        Map<Long, StoryRoomPost> map = storyRoomPostRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(StoryRoomPost::getId, it -> it));

        return ids.stream().map(map::get).filter(Objects::nonNull)
                .map(p -> ScrapStoryRoomPostDTO.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .topic(p.getTopic() != null ? p.getTopic().name() : null)
                        .thumbnailUrl(p.getThumbnailUrl())
                        .createdAt(p.getCreatedAt())
                        .scrapped(true)
                        .build())
                .toList();
    }

    public List<ScrapTravelRegionDTO> getTravelRegionScraps(Long memberId) {
        List<Long> ids = scrapRepository
                .findByMemberIdAndTargetTypeOrderByCreatedAtDesc(memberId, TargetType.TRAVEL_REGION)
                .stream().map(Scrap::getTargetId).toList();

        if (ids.isEmpty()) return List.of();

        Map<Long, TravelRegion> map = travelRegionRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(TravelRegion::getId, it -> it));

        return ids.stream().map(map::get).filter(Objects::nonNull)
                .map(r -> ScrapTravelRegionDTO.builder()
                        .id(r.getId())
                        .name(r.getTitle())
                        .address(r.getAddr1())
                        .thumbnailUrl(r.getFirstImage())
                        .scrapped(true)
                        .build())
                .toList();
    }

    //CompanionPost의 작성자 닉네임을 안전하게 추출.
    private String resolveCompanionAuthorName(CompanionPost post) {
        if (post == null) return null;
        // 시도할 게터 이름들
        String[] ownerGetters = {"getMember", "getAuthor", "getWriter"};
        for (String getter : ownerGetters) {
            try {
                Method m = post.getClass().getMethod(getter);
                Object owner = m.invoke(post);
                if (owner != null) {
                    try {
                        Method nick = owner.getClass().getMethod("getNickname");
                        Object v = nick.invoke(owner);
                        return v != null ? v.toString() : null;
                    } catch (NoSuchMethodException ignored) {
                        // 닉네임 메서드가 다른 이름일 수 있음 → 다음 후보 시도
                    }
                }
            } catch (NoSuchMethodException ignored) {
                // 해당 게터 없음 → 다음 후보 시도
            } catch (Exception ignored) {
                // 호출 실패 → 다음 후보 시도
            }
        }
        return null;
    }
}
