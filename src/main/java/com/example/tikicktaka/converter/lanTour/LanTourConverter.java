package com.example.tikicktaka.converter.lanTour;

import com.example.tikicktaka.domain.images.LanTourImg;
import com.example.tikicktaka.domain.lanTour.LanTour;
import com.example.tikicktaka.web.dto.lanTour.LanTourResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

public class LanTourConverter {

    public static LanTourResponseDTO.LanTourImageResponseDTO toLanTourImgDTO(LanTourImg lanTourImg){
        return LanTourResponseDTO.LanTourImageResponseDTO.builder()
                .isThumbNail(lanTourImg.getThumbnail())
                .lanTourImgUrl(lanTourImg.getImgUrl())
                .build();
    }

    public static LanTourResponseDTO.LanTourDetailResponseDTO toLanTourDetailDTO(LanTour lanTour){
        List<LanTourResponseDTO.LanTourImageResponseDTO> lanTourImageResponseDTOList = lanTour.getLanTourImgList().stream()
                .map(LanTourConverter::toLanTourImgDTO)
                .filter(LanTourResponseDTO.LanTourImageResponseDTO::getIsThumbNail).collect(Collectors.toList());

        return LanTourResponseDTO.LanTourDetailResponseDTO.builder()
                .title(lanTour.getTitle())
                .contents(lanTour.getContents())
                .lanTourCategory(lanTour.getLanTourCategory().name())
                .lanTourImgList(lanTourImageResponseDTOList)
                .dibsCount(lanTour.getDibsCount())
                .price(lanTour.getPrice())
                .salesCount(lanTour.getSalesCount())
                .createdAt(lanTour.getCreatedAt())
                .location(lanTour.getLocation())
                .build();
    }
}
