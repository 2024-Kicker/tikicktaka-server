package com.example.tikicktaka.service.memberService;

import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.exception.handler.MemberHandler;
import com.example.tikicktaka.domain.enums.LanTourCategory;
import com.example.tikicktaka.domain.mapping.lanTour.LanTourPurchase;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.repository.member.LanTourPurchaseRepository;
import com.example.tikicktaka.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryServiceImpl implements MemberQueryService{

    private final MemberRepository memberRepository;
    private final LanTourPurchaseRepository lanTourPurchaseRepository;

    @Override
    public Optional<Member> findMemberById(Long id) {
        return memberRepository.findById(id);
    }

    @Override
    public Optional<Member> findMemberByName(String name) {
        return memberRepository.findByName(name);
    }

    @Override
    public Member getMemberProfile(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    @Transactional
    public Page<LanTourPurchase> getMyLanTourPurchaseList(Member member, Integer page) {
        Page<LanTourPurchase> lanTourPurchasePage = lanTourPurchaseRepository.findAllByMember(member, PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        return lanTourPurchasePage;
    }

    @Override
    public Page<LanTourPurchase> getMyCategoryLanTourPurchaseList(Member member, LanTourCategory lanTourCategory, Integer page) {
        Page<LanTourPurchase> lanTourPurchasePage = lanTourPurchaseRepository.findAllByMemberAndLanTourLanTourCategory(member, lanTourCategory, PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        return lanTourPurchasePage;
    }


}
