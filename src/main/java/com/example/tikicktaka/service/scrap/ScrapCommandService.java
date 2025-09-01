package com.example.tikicktaka.service.scrap;

import com.example.tikicktaka.domain.enums.TargetType;
import com.example.tikicktaka.domain.mapping.scrap.Scrap;
import com.example.tikicktaka.repository.scrap.ScrapRepository;

import com.example.tikicktaka.repository.companionPost.CompanionPostRepository;
import com.example.tikicktaka.repository.storyRoom.StoryRoomPostRepository;
import com.example.tikicktaka.repository.travelRegion.TravelRegionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScrapCommandService {

    private final ScrapRepository scrapRepository;

    // 존재 검증용
    private final CompanionPostRepository companionPostRepository;
    private final StoryRoomPostRepository storyRoomPostRepository;
    private final TravelRegionRepository travelRegionRepository;

    /* ========= 공통 유틸 ========= */

    /** 내가 이 타깃을 스크랩했는지 여부 */
    @Transactional(readOnly = true)
    public boolean hasScrap(Long memberId, TargetType type, Long targetId) {
        return scrapRepository.existsByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId);
    }

    /**
     * 스크랩 소유자면 삭제하고 true, 아니면 false.
     * 컨트롤러에서 결과에 따라 메시지 분기할 때 사용.
     */
    @Transactional
    public boolean removeIfOwned(Long memberId, TargetType type, Long targetId) {
        if (!hasScrap(memberId, type, targetId)) return false;
        scrapRepository.deleteByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId);
        return true;
    }

    /* ===== 동행찾기 ===== */
    @Transactional
    public boolean toggleCompanionPost(Long memberId, Long postId) {
        ensureExists(TargetType.COMPANION_POST, postId);
        return toggle(memberId, TargetType.COMPANION_POST, postId);
    }

    @Transactional
    public void addCompanionPost(Long memberId, Long postId) {
        ensureExists(TargetType.COMPANION_POST, postId);
        add(memberId, TargetType.COMPANION_POST, postId);
    }

    @Transactional
    public void removeCompanionPost(Long memberId, Long postId) {
        guardedRemove(memberId, TargetType.COMPANION_POST, postId);
    }

    /* ===== 이야기방 ===== */
    @Transactional
    public boolean toggleStoryPost(Long memberId, Long storyPostId) {
        ensureExists(TargetType.STORY_POST, storyPostId);
        return toggle(memberId, TargetType.STORY_POST, storyPostId);
    }

    @Transactional
    public void addStoryPost(Long memberId, Long storyPostId) {
        ensureExists(TargetType.STORY_POST, storyPostId);
        add(memberId, TargetType.STORY_POST, storyPostId);
    }

    @Transactional
    public void removeStoryPost(Long memberId, Long storyPostId) {
        guardedRemove(memberId, TargetType.STORY_POST, storyPostId);
    }

    /* ===== 장소 추천 ===== */
    @Transactional
    public boolean toggleTravelRegion(Long memberId, Long regionId) {
        ensureExists(TargetType.TRAVEL_REGION, regionId);
        return toggle(memberId, TargetType.TRAVEL_REGION, regionId);
    }

    @Transactional
    public void addTravelRegion(Long memberId, Long regionId) {
        ensureExists(TargetType.TRAVEL_REGION, regionId);
        add(memberId, TargetType.TRAVEL_REGION, regionId);
    }

    @Transactional
    public void removeTravelRegion(Long memberId, Long regionId) {
        guardedRemove(memberId, TargetType.TRAVEL_REGION, regionId);
    }

    /* ===== 내부 공통 ===== */

    private boolean toggle(Long memberId, TargetType type, Long targetId) {
        if (scrapRepository.existsByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId)) {
            scrapRepository.deleteByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId);
            return false; // 해제됨
        }
        add(memberId, type, targetId);
        return true; // 스크랩됨
    }

    private void add(Long memberId, TargetType type, Long targetId) {
        if (scrapRepository.existsByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId)) return; // 멱등
        try {
            scrapRepository.save(Scrap.of(memberId, type, targetId));
        } catch (DataIntegrityViolationException ignored) {
            // 동시성 경쟁으로 UNIQUE 충돌 시에도 멱등 보장
        }
    }

    private void guardedRemove(Long memberId, TargetType type, Long targetId) {
        if (!scrapRepository.existsByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId)) {
            throw new IllegalStateException("해당 콘텐츠를 스크랩한 이력이 없습니다.");
        }
        scrapRepository.deleteByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId);
    }

    private void ensureExists(TargetType type, Long targetId) {
        boolean ok = switch (type) {
            case COMPANION_POST -> companionPostRepository.existsById(targetId);
            case STORY_POST     -> storyRoomPostRepository.existsById(targetId);
            case TRAVEL_REGION  -> travelRegionRepository.existsById(targetId);
            case MEMBER         -> throw new UnsupportedOperationException("Scrap does not support MEMBER target type");
        };
        if (!ok) throw new IllegalArgumentException("Target not found. type=" + type + " id=" + targetId);
    }
}
