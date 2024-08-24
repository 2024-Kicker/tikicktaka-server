package com.example.tikicktaka.service.lanTourService;

import com.example.tikicktaka.domain.lanTour.LanTour;
import org.springframework.data.domain.Page;

public interface LanTourQueryService {
    LanTour getLanTourContent(Long lanTourId);

    Page<LanTour> getLanTourList(String region, String orderType, Integer page);
}
