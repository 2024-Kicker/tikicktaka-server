package com.example.tikicktaka.service.lanTourService;

import com.example.tikicktaka.domain.lanTour.Review;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.web.dto.lanTour.LanTourRequestDTO;

public interface LanTourCommandService {

    Review uploadReview(LanTourRequestDTO.UploadReviewRequestDTO request, Long lanTourId, Member member);
}
