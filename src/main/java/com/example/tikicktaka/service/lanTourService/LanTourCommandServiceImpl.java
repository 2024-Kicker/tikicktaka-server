package com.example.tikicktaka.service.lanTourService;

import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.exception.handler.LanTourHandler;
import com.example.tikicktaka.converter.lanTour.LanTourConverter;
import com.example.tikicktaka.domain.lanTour.Inquiry;
import com.example.tikicktaka.domain.lanTour.LanTour;
import com.example.tikicktaka.domain.lanTour.Review;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.repository.lanTour.InquiryRepository;
import com.example.tikicktaka.repository.lanTour.LanTourRepository;
import com.example.tikicktaka.repository.lanTour.ReviewRepository;
import com.example.tikicktaka.web.dto.lanTour.LanTourRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class LanTourCommandServiceImpl implements LanTourCommandService{

    private final LanTourRepository lanTourRepository;
    private final ReviewRepository reviewRepository;
    private final InquiryRepository inquiryRepository;

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
}
