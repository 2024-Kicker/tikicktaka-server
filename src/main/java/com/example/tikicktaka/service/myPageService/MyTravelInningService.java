package com.example.tikicktaka.service.myPageService;

import com.example.tikicktaka.web.dto.myPage.MyTravelInningRequestDTO;
import com.example.tikicktaka.web.dto.myPage.MyTravelInningResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface MyTravelInningService {
    MyTravelInningResponseDTO.Detail create(Long memberId, MyTravelInningRequestDTO.Create dto, MultipartFile image);
}
