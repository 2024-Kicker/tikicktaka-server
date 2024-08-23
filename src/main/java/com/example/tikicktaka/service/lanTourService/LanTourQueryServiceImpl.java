package com.example.tikicktaka.service.lanTourService;

import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.exception.handler.LanTourHandler;
import com.example.tikicktaka.domain.lanTour.LanTour;
import com.example.tikicktaka.repository.lanTour.LanTourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
