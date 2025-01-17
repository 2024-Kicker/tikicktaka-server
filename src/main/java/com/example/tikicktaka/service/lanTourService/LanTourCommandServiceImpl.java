package com.example.tikicktaka.service.lanTourService;

import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.exception.handler.LanTourHandler;
import com.example.tikicktaka.apiPayload.exception.handler.MemberHandler;
import com.example.tikicktaka.converter.lanTour.LanTourConverter;
import com.example.tikicktaka.domain.enums.InquiryStatus;
import com.example.tikicktaka.domain.lanTour.Inquiry;
import com.example.tikicktaka.domain.lanTour.InquiryAnswer;
import com.example.tikicktaka.domain.lanTour.LanTour;
import com.example.tikicktaka.domain.lanTour.Review;
import com.example.tikicktaka.domain.mapping.lanTour.LanTourPurchase;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.repository.lanTour.InquiryAnswerRepository;
import com.example.tikicktaka.repository.lanTour.InquiryRepository;
import com.example.tikicktaka.repository.lanTour.LanTourRepository;
import com.example.tikicktaka.repository.lanTour.ReviewRepository;
import com.example.tikicktaka.repository.member.LanTourPurchaseRepository;
import com.example.tikicktaka.web.dto.lanTour.LanTourRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class LanTourCommandServiceImpl implements LanTourCommandService{

    private final LanTourRepository lanTourRepository;
    private final LanTourPurchaseRepository lanTourPurchaseRepository;
    private final ReviewRepository reviewRepository;
    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;

    @Override
    @Transactional
    public Review uploadReview(LanTourRequestDTO.UploadReviewRequestDTO request, Long lanTourId, Member member) {
        LanTour lanTour = lanTourRepository.findById(lanTourId).orElseThrow(() -> new LanTourHandler(ErrorStatus.LAN_TOUR_NOT_FOUND));
        Review review = LanTourConverter.uploadReviewDTO(request, member, lanTour);
        reviewRepository.save(review);
        return review;
    }

    @Override
    @Transactional
    public Inquiry uploadInquiry(LanTourRequestDTO.UploadInquiryRequestDTO request, Long lanTourId, Member member) {
        LanTour lanTour = lanTourRepository.findById(lanTourId).orElseThrow(() -> new LanTourHandler(ErrorStatus.LAN_TOUR_NOT_FOUND));
        Inquiry inquiry = LanTourConverter.uploadInquiryDTO(request, member, lanTour);
        inquiryRepository.save(inquiry);
        return inquiry;
    }

    @Override
    @Transactional
    public InquiryAnswer uploadInquiryAnswer(LanTourRequestDTO.UploadInquiryAnswerRequestDTO request, Long inquiryId, Member member) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId).orElseThrow(() -> new LanTourHandler(ErrorStatus.INQUIRY_NOT_FOUND));

        if(!member.getMemberRole().name().equals("SELLER") || !inquiry.getMember().getId().equals(member.getId())){
            throw new MemberHandler(ErrorStatus.MEMBER_NOT_SELLER);
        }

        InquiryAnswer inquiryAnswer = LanTourConverter.uploadInquiryAnswerDTO(request, member, inquiry);
        inquiryAnswerRepository.save(inquiryAnswer);
        inquiry.setInquiryStatus(InquiryStatus.COMPLETE);

        return inquiryAnswer;
    }

    @Override
    @Transactional
    public LanTour purchaseLanTour(Long lanTourId, Member member) {
        LanTour lanTour = lanTourRepository.findById(lanTourId).orElseThrow(() -> new LanTourHandler(ErrorStatus.LAN_TOUR_NOT_FOUND));

        Optional<LanTourPurchase> purchaseRecord = lanTourPurchaseRepository.findByMemberAndLanTour(member, lanTour);

        if(member.getPoint() < lanTour.getPrice()){
            throw new MemberHandler(ErrorStatus.MEMBER_NOT_ENOUGH_COIN);
        } else{
            member.spendCoin(lanTour.getPrice());
        }

        if (purchaseRecord.isPresent()){
            throw new LanTourHandler(ErrorStatus.LAN_TOUR_ALREADY_PURCHASE);
        } else{
            LanTourPurchase lanTourPurchase = LanTourConverter.purchaseLanTourDTO(lanTour, member);
            lanTourPurchaseRepository.save(lanTourPurchase);
        }

        return lanTour;
    }
}
