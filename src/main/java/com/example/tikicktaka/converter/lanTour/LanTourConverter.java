package com.example.tikicktaka.converter.lanTour;

import com.example.tikicktaka.domain.images.LanTourImg;
import com.example.tikicktaka.web.dto.lanTour.LanTourResponseDTO;

public class LanTourConverter {

    public static LanTourResponseDTO.LanTourImageResponseDTO toLanTourImgDTO(LanTourImg lanTourImg){
        return LanTourResponseDTO.LanTourImageResponseDTO.builder()
                .isThumbNail(lanTourImg.getThumbnail())
                .lanTourImgUrl(lanTourImg.getImgUrl())
                .build();
    }
}
