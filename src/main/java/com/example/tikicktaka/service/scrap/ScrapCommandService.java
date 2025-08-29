package com.example.tikicktaka.service.scrap;

import com.example.tikicktaka.domain.enums.ScrapTargetType;
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
    public boolean hasScrap(Long memberId, ScrapTargetType type, Long targetId) {
        return scrapRepository.existsByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId);
    }

    /**
     * 스크랩 소유자면 삭제하고 true, 아니면 false.
     * 컨트롤러에서 결과에 따라 메시지 분기할 때 사용.
     */
    @Transactional
    public boolean removeIfOwned(Long memberId, ScrapTargetType type, Long targetId) {
        if (!hasScrap(memberId, type, targetId)) return false;
        scrapRepository.deleteByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId);
        return true;
    }

    /* ===== 동행찾기 ===== */
    @Transactional
    public boolean toggleCompanionPost(Long memberId, Long postId) {
        ensureExists(ScrapTargetType.COMPANION_POST, postId);
        return toggle(memberId, ScrapTargetType.COMPANION_POST, postId);
    }

    @Transactional
    public void addCompanionPost(Long memberId, Long postId) {
        ensureExists(ScrapTargetType.COMPANION_POST, postId);
        add(memberId, ScrapTargetType.COMPANION_POST, postId);
    }

    @Transactional
    public void removeCompanionPost(Long memberId, Long postId) {
        guardedRemove(memberId, ScrapTargetType.COMPANION_POST, postId);
    }

    /* ===== 이야기방 ===== */
    @Transactional
    public boolean toggleStoryPost(Long memberId, Long storyPostId) {
        ensureExists(ScrapTargetType.STORY_POST, storyPostId);
        return toggle(memberId, ScrapTargetType.STORY_POST, storyPostId);
    }

    @Transactional
    public void addStoryPost(Long memberId, Long storyPostId) {
        ensureExists(ScrapTargetType.STORY_POST, storyPostId);
        add(memberId, ScrapTargetType.STORY_POST, storyPostId);
    }

    @Transactional
    public void removeStoryPost(Long memberId, Long storyPostId) {
        guardedRemove(memberId, ScrapTargetType.STORY_POST, storyPostId);
    }

    /* ===== 장소 추천 ===== */
    @Transactional
    public boolean toggleTravelRegion(Long memberId, Long regionId) {
        ensureExists(ScrapTargetType.TRAVEL_REGION, regionId);
        return toggle(memberId, ScrapTargetType.TRAVEL_REGION, regionId);
    }

    @Transactional
    public void addTravelRegion(Long memberId, Long regionId) {
        ensureExists(ScrapTargetType.TRAVEL_REGION, regionId);
        add(memberId, ScrapTargetType.TRAVEL_REGION, regionId);
    }

    @Transactional
    public void removeTravelRegion(Long memberId, Long regionId) {
        guardedRemove(memberId, ScrapTargetType.TRAVEL_REGION, regionId);
    }

    /* ===== 내부 공통 ===== */

    private boolean toggle(Long memberId, ScrapTargetType type, Long targetId) {
        if (scrapRepository.existsByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId)) {
            scrapRepository.deleteByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId);
            return false; // 해제됨
        }
        add(memberId, type, targetId);
        return true; // 스크랩됨
    }

    private void add(Long memberId, ScrapTargetType type, Long targetId) {
        if (scrapRepository.existsByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId)) return; // 멱등
        try {
            scrapRepository.save(Scrap.of(memberId, type, targetId));
        } catch (DataIntegrityViolationException ignored) {
            // 동시성 경쟁으로 UNIQUE 충돌 시에도 멱등 보장
        }
    }

    private void guardedRemove(Long memberId, ScrapTargetType type, Long targetId) {
        if (!scrapRepository.existsByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId)) {
            throw new IllegalStateException("해당 콘텐츠를 스크랩한 이력이 없습니다.");
        }
        scrapRepository.deleteByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId);
    }

    private void ensureExists(ScrapTargetType type, Long targetId) {
        boolean ok = switch (type) {
            case COMPANION_POST -> companionPostRepository.existsById(targetId);
            case STORY_POST     -> storyRoomPostRepository.existsById(targetId);
            case TRAVEL_REGION  -> travelRegionRepository.existsById(targetId);
        };
        if (!ok) throw new IllegalArgumentException("Target not found. type=" + type + " id=" + targetId);
    }
}
