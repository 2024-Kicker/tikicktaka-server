package com.example.tikicktaka.repository.member;

import com.example.tikicktaka.domain.enums.LanTourCategory;
import com.example.tikicktaka.domain.mapping.lanTour.LanTourPurchase;
import com.example.tikicktaka.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanTourPurchaseRepository extends JpaRepository<LanTourPurchase, Long> {
    Page<LanTourPurchase> findAllByMember(Member member, PageRequest pageRequest);
    Page<LanTourPurchase> findAllByMemberAndLanTourLanTourCategory(Member member, LanTourCategory lanTourCategory, PageRequest pageRequest);
}
