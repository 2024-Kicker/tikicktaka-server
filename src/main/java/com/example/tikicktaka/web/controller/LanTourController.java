package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.exception.handler.MemberHandler;
import com.example.tikicktaka.converter.lanTour.LanTourConverter;
import com.example.tikicktaka.converter.member.MemberConverter;
import com.example.tikicktaka.domain.lanTour.Inquiry;
import com.example.tikicktaka.domain.lanTour.LanTour;
import com.example.tikicktaka.domain.lanTour.Review;
import com.example.tikicktaka.domain.mapping.lanTour.LanTourPurchase;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.service.lanTourService.LanTourCommandService;
import com.example.tikicktaka.service.lanTourService.LanTourQueryService;
import com.example.tikicktaka.service.memberService.MemberQueryService;
import com.example.tikicktaka.web.dto.lanTour.LanTourRequestDTO;
import com.example.tikicktaka.web.dto.lanTour.LanTourResponseDTO;
import com.example.tikicktaka.web.dto.member.MemberResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@Tag(name = "LanTour", description = "LanTour 관련 API")
@RequestMapping("/api/lan-tour")
public class LanTourController {

    private final LanTourQueryService lanTourQueryService;
    private final LanTourCommandService lanTourCommandService;
    private final MemberQueryService memberQueryService;

    @GetMapping(value = "/{lanTourId}")
    @Operation(summary = "랜선투어 상품 상세조회 api", description = "request: 조회하고자 하는 랜선 투어 상품 아이디를 입력해주시면 됩니다.")
    public ApiResponse<LanTourResponseDTO.LanTourDetailResponseDTO> getLanTourDetail(@PathVariable Long lanTourId){
        LanTour lanTour = lanTourQueryService.getLanTourContent(lanTourId);
        return ApiResponse.onSuccess(LanTourConverter.toLanTourDetailDTO(lanTour));
    }

    @GetMapping(value = "/list")
    @Operation(summary = "랜선투어 상품 전체 조회 API", description = "랜선투어 상품 전체 조회를 위한 API이며, request parameter로 입력 값을 받습니다. \n\n" +
            "page : 상품 조회 페이지 번호 \n\n region : 지역 이름(String) \n\n orderType : 조회 타입(String)")
    @Parameters(value = {
            @Parameter(name = "page", description = "페이지 번호, 1 이상의 숫자를 입력해주세요."),
            @Parameter(name = "region", description = "지역 이름으로, (전체, 서울특별시, 경기도, 강원도, 충청도, 경상도, 전라도, 제주도) 중 입력해주세요."),
            @Parameter(name = "orderType", description = "조회 타입으로, new, popular, salesCount 중 하나의 값을 입력해주세요.")
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ApiResponse<LanTourResponseDTO.LanTourPreviewListDTO> getLanTourList(@RequestParam String region,
                                                                                @RequestParam String orderType,
                                                                                @RequestParam Integer page){
        Page<LanTour> lanTourPage = lanTourQueryService.getLanTourList(region, orderType, page - 1);
        return ApiResponse.onSuccess(LanTourConverter.lanTourPreviewListDTO(lanTourPage));
    }

    @PostMapping(value = "/upload/review/{lanTourId}")
    @Operation(summary = "랜선투어 리뷰 등록 API", description = "랜선투어 리뷰 등록을 위한 API이며, request body, path variable 로 입력 값을 받습니다. \n\n" +
            "lanTourId : 랜선투어 id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ApiResponse<LanTourResponseDTO.UploadReviewResultDTO> uploadReview(@RequestBody @Valid LanTourRequestDTO.UploadReviewRequestDTO request,
                                                                              @PathVariable Long lanTourId,
                                                                              Authentication authentication){
        Member member = memberQueryService.findMemberById(Long.valueOf(authentication.getName().toString())).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Review review = lanTourCommandService.uploadReview(request, lanTourId, member);
        return ApiResponse.onSuccess(LanTourConverter.uploadReviewResultDTO(review));
    }

    @PostMapping(value = "/upload/inquiry/{lanTourId}")
    @Operation(summary = "랜선투어 문의 등록 API", description = "랜선투어 문의 등록을 위한 API이며, request body, path variable 로 입력 값을 받습니다. \n\n" +
            "lanTourId : 랜선투어 id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ApiResponse<LanTourResponseDTO.UploadInquiryResultDTO> uploadInquiry(@RequestBody @Valid LanTourRequestDTO.UploadInquiryRequestDTO request,
                                                                                @PathVariable Long lanTourId,
                                                                                Authentication authentication){
        Member member = memberQueryService.findMemberById(Long.valueOf(authentication.getName().toString())).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Inquiry inquiry = lanTourCommandService.uploadInquiry(request, lanTourId, member);
        return ApiResponse.onSuccess(LanTourConverter.uploadInquiryResultDTO(inquiry));
    }

    @GetMapping(value = "/review/{lanTourId}")
    @Operation(summary = "랜선투어 리뷰 조회 API", description = "랜선투어 리뷰 조회를 위한 API이며, request param, path variable 로 입력 값을 받습니다. \n\n" +
            "page : 상품 조회 페이지 번호 \n\n lanTourId : 랜선투어 id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ApiResponse<LanTourResponseDTO.LanTourReviewPreviewListDTO> getReviewList(@PathVariable Long lanTourId,
                                                                                     @RequestParam Integer page){
        Page<Review> reviewPage = lanTourQueryService.getReviewList(lanTourId, page - 1);
        return ApiResponse.onSuccess(LanTourConverter.lanTourReviewPreviewListDTO(reviewPage));
    }
}
