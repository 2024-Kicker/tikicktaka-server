package com.example.tikicktaka.service.myPageService;

import com.example.tikicktaka.web.dto.myPage.MyTravelInningRequestDTO;
import com.example.tikicktaka.web.dto.myPage.MyTravelInningResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MyTravelInningService {
    MyTravelInningResponseDTO.Detail create(Long memberId, MyTravelInningRequestDTO.Create dto, MultipartFile image);
    List<MyTravelInningResponseDTO.ListItem>  findByMemberId(Long memberId);
}
