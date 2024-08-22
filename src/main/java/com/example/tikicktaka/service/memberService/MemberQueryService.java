package com.example.tikicktaka.service.memberService;

import com.example.tikicktaka.domain.enums.LanTourCategory;
import com.example.tikicktaka.domain.mapping.lanTour.LanTourPurchase;
import com.example.tikicktaka.domain.member.Member;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface MemberQueryService {

    Optional<Member> findMemberById(Long id);

    Optional<Member> findMemberByName(String name);

    Member getMemberProfile(Long memberId);

    Page<LanTourPurchase> getMyLanTourPurchaseList(Member member, Integer page);

    Page<LanTourPurchase> getMyCategoryLanTourPurchaseList(Member member, LanTourCategory lanTourCategory, Integer page);
}
