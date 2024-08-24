package com.example.tikicktaka.service.lanTourService;

import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.exception.handler.LanTourHandler;
import com.example.tikicktaka.domain.lanTour.LanTour;
import com.example.tikicktaka.domain.mapping.lanTour.LanTourPurchase;
import com.example.tikicktaka.repository.lanTour.LanTourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class LanTourQueryServiceImpl implements LanTourQueryService{

    private final LanTourRepository lanTourRepository;

    @Override
    public LanTour getLanTourContent(Long lanTourId) {
        LanTour lanTour = lanTourRepository.findById(lanTourId).orElseThrow(() -> new LanTourHandler(ErrorStatus.LAN_TOUR_NOT_FOUND));
        return lanTour;
    }

    @Override
    public Page<LanTour> getLanTourList(String region, String orderType, Integer page) {
        Page<LanTour> lanTourPage = null;
        String filter = "temp";
        if(orderType.equals("new")){
            filter = "createdAt";
        } else if(orderType.equals("popular")){
            filter = "dibsCount";
        } else if(orderType.equals("salesCount")){
            filter = "salesCount";
        }

        if(region.equals("전체")){
            lanTourPage = lanTourRepository.findAll(PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, filter)));
        } else {
            lanTourPage = lanTourRepository.findAllByLocation(region, PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, filter)));
        }

        return lanTourPage;
    }
}
