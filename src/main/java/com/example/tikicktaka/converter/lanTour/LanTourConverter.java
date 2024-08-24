package com.example.tikicktaka.converter.lanTour;

import com.example.tikicktaka.converter.member.MemberConverter;
import com.example.tikicktaka.domain.enums.InquiryStatus;
import com.example.tikicktaka.domain.images.LanTourImg;
import com.example.tikicktaka.domain.lanTour.Inquiry;
import com.example.tikicktaka.domain.lanTour.LanTour;
import com.example.tikicktaka.domain.lanTour.Review;
import com.example.tikicktaka.domain.mapping.lanTour.LanTourPurchase;
import com.example.tikicktaka.domain.mapping.member.Dibs;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.web.dto.lanTour.LanTourRequestDTO;
import com.example.tikicktaka.web.dto.lanTour.LanTourResponseDTO;
import com.example.tikicktaka.web.dto.member.MemberResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class LanTourConverter {

    public static Dibs toDibs(LanTour lanTour, Member member){
        return Dibs.builder()
                .lanTour(lanTour)
                .member(member)
                .build();
    }

    public static LanTourResponseDTO.LanTourImageResponseDTO toLanTourImgDTO(LanTourImg lanTourImg){
        return LanTourResponseDTO.LanTourImageResponseDTO.builder()
                .isThumbNail(lanTourImg.getThumbnail())
                .lanTourImgUrl(lanTourImg.getImgUrl())
                .build();
    }

    public static LanTourResponseDTO.LanTourDetailResponseDTO toLanTourDetailDTO(LanTour lanTour){
        List<LanTourResponseDTO.LanTourImageResponseDTO> lanTourImageResponseDTOList = lanTour.getLanTourImgList().stream()
                .map(LanTourConverter::toLanTourImgDTO).collect(Collectors.toList());

        return LanTourResponseDTO.LanTourDetailResponseDTO.builder()
                .title(lanTour.getTitle())
                .contents(lanTour.getContents())
                .lanTourCategory(lanTour.getLanTourCategory().name())
                .lanTourImgList(lanTourImageResponseDTOList)
                .dibsCount(lanTour.getDibsCount())
                .price(lanTour.getPrice())
                .salesCount(lanTour.getSalesCount())
                .reviewCount(lanTour.getReviewList().size())
                .inquiryCount(lanTour.getInquiryList().size())
                .createdAt(lanTour.getCreatedAt())
                .location(lanTour.getLocation())
                .build();
    }

    public static LanTourResponseDTO.LanTourPreviewDTO lanTourPreviewDTO(LanTour lanTour){
        List<LanTourResponseDTO.LanTourImageResponseDTO> lanTourImageResponseDTOList = lanTour.getLanTourImgList().stream()
                .map(LanTourConverter::toLanTourImgDTO)
                .filter(LanTourResponseDTO.LanTourImageResponseDTO::getIsThumbNail).collect(Collectors.toList());

        return LanTourResponseDTO.LanTourPreviewDTO.builder()
                .title(lanTour.getTitle())
                .price(lanTour.getPrice())
                .dibsCount(lanTour.getDibsCount())
                .salesCount(lanTour.getSalesCount())
                .location(lanTour.getLocation())
                .lanTourImgList(lanTourImageResponseDTOList)
                .createdAt(lanTour.getCreatedAt())
                .build();
    }

    public static LanTourResponseDTO.LanTourPreviewListDTO lanTourPreviewListDTO(Page<LanTour> lanTourList){
        List<LanTourResponseDTO.LanTourPreviewDTO> lanTourPreviewDTOList = lanTourList.stream()
                .map(LanTourConverter::lanTourPreviewDTO).collect(Collectors.toList());

        return LanTourResponseDTO.LanTourPreviewListDTO.builder()
                .LanTourPreviewDTOList(lanTourPreviewDTOList)
                .isFirst(lanTourList.isFirst())
                .isLast(lanTourList.isLast())
                .listSize(lanTourList.getSize())
                .totalElements(lanTourList.getTotalElements())
                .totalPage(lanTourList.getTotalPages())
                .build();
    }

    public static Review uploadReviewDTO(LanTourRequestDTO.UploadReviewRequestDTO request, Member member, LanTour lanTour){
        return Review.builder()
                .lanTour(lanTour)
                .member(member)
                .contents(request.getContents())
                .ratings(request.getRatings())
                .build();
    }

    public static LanTourResponseDTO.UploadReviewResultDTO uploadReviewResultDTO(Review review){
        return LanTourResponseDTO.UploadReviewResultDTO.builder()
                .memberId(review.getMember().getId())
                .build();
    }

    public static Inquiry uploadInquiryDTO(LanTourRequestDTO.UploadInquiryRequestDTO request, Member member, LanTour lanTour){
        return Inquiry.builder()
                .member(member)
                .lanTour(lanTour)
                .status(InquiryStatus.BEFORE)
                .contents(request.getContents())
                .secret(request.getSecret())
                .build();
    }

    public static LanTourResponseDTO.UploadInquiryResultDTO uploadInquiryResultDTO(Inquiry inquiry){
        return LanTourResponseDTO.UploadInquiryResultDTO.builder()
                .memberId(inquiry.getMember().getId())
                .build();
    }

    public static LanTourResponseDTO.LanTourReviewPreviewDTO lanTourReviewPreviewDTO(Review review){
        return LanTourResponseDTO.LanTourReviewPreviewDTO.builder()
                .memberNickname(review.getMember().getName())
                .profileImgUrl(review.getMember().getProfileImg().getUrl())
                .contents(review.getContents())
                .ratings(review.getRatings())
                .createdAt(review.getCreatedAt())
                .build();
    }

    public static LanTourResponseDTO.LanTourReviewPreviewListDTO lanTourReviewPreviewListDTO(Page<Review> lanTourReviewList){
        List<LanTourResponseDTO.LanTourReviewPreviewDTO> lanTourPreviewDTOList = lanTourReviewList.stream()
                .map(LanTourConverter::lanTourReviewPreviewDTO).collect(Collectors.toList());

        return LanTourResponseDTO.LanTourReviewPreviewListDTO.builder()
                .LanTourReviewPreviewDTOList(lanTourPreviewDTOList)
                .isFirst(lanTourReviewList.isFirst())
                .isLast(lanTourReviewList.isLast())
                .listSize(lanTourReviewList.getSize())
                .totalElements(lanTourReviewList.getTotalElements())
                .totalPage(lanTourReviewList.getTotalPages())
                .build();
    }
}
