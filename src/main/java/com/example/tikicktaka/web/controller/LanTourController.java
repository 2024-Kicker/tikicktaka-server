package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.converter.lanTour.LanTourConverter;
import com.example.tikicktaka.converter.member.MemberConverter;
import com.example.tikicktaka.domain.lanTour.LanTour;
import com.example.tikicktaka.domain.mapping.lanTour.LanTourPurchase;
import com.example.tikicktaka.service.lanTourService.LanTourQueryService;
import com.example.tikicktaka.web.dto.lanTour.LanTourResponseDTO;
import com.example.tikicktaka.web.dto.member.MemberResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@Tag(name = "LanTour", description = "LanTour 관련 API")
@RequestMapping("/api/lan-tour")
public class LanTourController {

    private final LanTourQueryService lanTourQueryService;

    @GetMapping(value = "/{lanTourId}")
    @Operation(summary = "랜선투어 상품 상세조회 api", description = "request: 조회하고자 하는 랜선 투어 상품 아이디를 입력해주시면 됩니다.")
    public ApiResponse<LanTourResponseDTO.LanTourDetailResponseDTO> getLanTourDetail(@PathVariable Long lanTourId){
        LanTour lanTour = lanTourQueryService.getLanTourContent(lanTourId);
        return ApiResponse.onSuccess(LanTourConverter.toLanTourDetailDTO(lanTour));
    }
}
