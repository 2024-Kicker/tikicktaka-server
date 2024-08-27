package com.example.tikicktaka.service.lanTourService;

import com.example.tikicktaka.domain.lanTour.Inquiry;
import com.example.tikicktaka.domain.lanTour.InquiryAnswer;
import com.example.tikicktaka.domain.lanTour.LanTour;
import com.example.tikicktaka.domain.lanTour.Review;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.web.dto.lanTour.LanTourRequestDTO;

public interface LanTourCommandService {

    Review uploadReview(LanTourRequestDTO.UploadReviewRequestDTO request, Long lanTourId, Member member);

    Inquiry uploadInquiry(LanTourRequestDTO.UploadInquiryRequestDTO request, Long lanTourId, Member member);

    InquiryAnswer uploadInquiryAnswer(LanTourRequestDTO.UploadInquiryAnswerRequestDTO request, Long inquiryId, Member member);

    LanTour purchaseLanTour(Long lanTourId, Member member);
}
