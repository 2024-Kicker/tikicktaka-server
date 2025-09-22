package com.example.tikicktaka.service.myPageService;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.storyRoom.StoryRoomPost;
import com.example.tikicktaka.repository.companionPost.CompanionPostRepository;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.repository.storyRoom.StoryRoomPostRepository;
import com.example.tikicktaka.web.dto.myPage.MyPostItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final MemberRepository memberRepository;
    private final CompanionPostRepository companionPostRepository;
    private final StoryRoomPostRepository storyRoomPostRepository;

    @Value("${app.share.baseUrl:https://example.com}")
    private String baseUrl;

    public List<MyPostItemDTO> getMyCompanionPosts(Long memberId) {
        Member me = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음: " + memberId));

        List<CompanionPost> posts = companionPostRepository.findByAuthor(me);

        return posts.stream()
                .map(p -> {
                    String linkId = String.valueOf(p.getId());
                    String shareUrl = baseUrl + "/api/companionPost/public/" + linkId;
                    return MyPostItemDTO.builder()
                            .postId(p.getId())
                            .title(p.getTitle())
                            .content(p.getContent())
                            .type("COMPANION")
                            .shareUrl(shareUrl)
                            .thumbnailUrl(p.getThumbnailUrl())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<MyPostItemDTO> getMyStoryPosts(Long memberId) {
        Member me = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음: " + memberId));

        List<StoryRoomPost> posts = storyRoomPostRepository.findAllByAuthor(me);

        return posts.stream()
                .map(p -> {
                    String linkId = String.valueOf(p.getId());
                    String shareUrl = baseUrl + "/api/storyRoom/post/public/" + linkId;
                    return MyPostItemDTO.builder()
                            .postId(p.getId())
                            .title(p.getTitle())
                            .type("STORY")
                            .shareUrl(shareUrl)
                            .build();
                })
                .collect(Collectors.toList());
    }
}

