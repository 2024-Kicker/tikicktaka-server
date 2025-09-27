package com.example.tikicktaka.service.myPageService;

import com.example.tikicktaka.web.dto.myPage.MyPostItemDTO;

import java.util.List;

public interface MyPageService {

    List<MyPostItemDTO> getMyCompanionPosts(Long memberId);

    List<MyPostItemDTO> getMyStoryPosts(Long memberId);

}
