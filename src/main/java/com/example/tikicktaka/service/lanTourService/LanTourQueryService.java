package com.example.tikicktaka.service.lanTourService;

import com.example.tikicktaka.domain.lanTour.Inquiry;
import com.example.tikicktaka.domain.lanTour.LanTour;
import com.example.tikicktaka.domain.lanTour.Review;
import org.springframework.data.domain.Page;

public interface LanTourQueryService {
    LanTour getLanTourContent(Long lanTourId);

    Page<LanTour> getLanTourList(String region, String orderType, Integer page);

    Page<Review> getReviewList(Long lanTourId, Integer page);

    Page<Inquiry> getInquiryList(Long lanTourId, Integer page);

    Page<LanTour> searchLanTour(String title, Integer page);
}
