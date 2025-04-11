package com.example.tikicktaka.service.CompanionPostService;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.images.CompanionPostImg;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.repository.companionPost.CompanionPostRepository;
import com.example.tikicktaka.repository.companionPost.CompanionPostImageRepository;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.service.UtilService;
import com.example.tikicktaka.service.chatService.ChatRoomService;
import com.example.tikicktaka.service.chatService.InviteCodeGeneratorService;
import com.example.tikicktaka.web.dto.companionPost.CompanionPostListResponseDTO;
import com.example.tikicktaka.web.dto.companionPost.CompanionPostResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
//import jakarta.transaction.Transactional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class CompanionPostServiceImpl implements CompanionPostService {

    @Autowired
    private CompanionPostRepository companionPostRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private final ChatRoomService chatRoomService;

    @Autowired
    private UtilService utilService;

    @Autowired
    private CompanionPostImageRepository  companionPostImageRepository;

    @Autowired
    private InviteCodeGeneratorService InviteCodeGenerator;


    @Override
    @Transactional
    public CompanionPost createPostWithImages(String title, String content, Long memberId, List<MultipartFile> imageFiles, CompanionPost.PostStatus status, CompanionPost.TravelStatus travelStatus) {
        // Member 찾기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

        // 게시글 생성
        CompanionPost post = CompanionPost.builder()
                .title(title)
                .content(content)
                .status(status)
                .travelStatus(travelStatus)
                .author(member)
                .build();

        // 게시글 저장 (우선 저장 후 ID 생성됨)
        companionPostRepository.save(post);

        // 초대 코드 생성 (초대 코드 생성 방식은 예시로 "INVITE1234"로 넣음, 실제 코드에 맞게 생성해야 함)
        //String inviteCode = InviteCodeGenerator.generateInviteCode(); // 8자리 랜덤 초대 코드 생성

        // 단체 채팅방 생성
        //ChatRoomDTO chatRoomDto = chatRoomService.createGroupChatRoomWithInvite(post.getId(), memberId, inviteCode);

        // 게시글에 생성된 채팅방 ID 및 초대 코드 설정
        //post.setChatRoomId(chatRoomDto.getRoomId()); // chatRoomId 업데이트
       // post.setInviteCode(inviteCode); // 초대 코드 설정

        // 이미지 업로드 및 저장
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<CompanionPostImg> images = new ArrayList<>();
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    // UtilService를 사용하여 S3에 이미지 업로드
                    String imageUrl = utilService.uploadS3Img("companionPost", file);

                    // CompanionPostImage 객체 생성
                    CompanionPostImg image = CompanionPostImg.builder()
                            .imageUrl(imageUrl)
                            .companionPost(post)
                            .build();

                    images.add(image);
                }
            }
            // 이미지 저장
            companionPostImageRepository.saveAll(images);

            // 첫 번째 이미지를 대표 이미지(썸네일)로 설정
            if (!images.isEmpty()) {
                post.setThumbnailUrl(images.get(0).getImageUrl());
            }
        }

        return post;
    }

    @Override
    @Transactional
    public CompanionPost deletePost(Long postId, Long memberId) {
        //게시글 조회
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 본인 게시글인지 확인
        if (!post.getAuthor().getId().equals(memberId)) {
            throw new IllegalStateException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        // 게시글에 연결된 이미지 리스트 조회
        List<CompanionPostImg> images = companionPostImageRepository.findByCompanionPost(post);

        if (images != null && !images.isEmpty()) {
            //S3에서 이미지 삭제
            images.forEach(image -> utilService.deleteS3Img(image.getImageUrl()));

            //DB에서 이미지 삭제
            companionPostImageRepository.deleteAll(images);
        }

        //게시글 삭제하면 채팅방도 삭제되게
        chatRoomService.deleteRoomsByPostId(postId);  //


        //DB에서 게시글 삭제
        companionPostRepository.delete(post);

        return post;
    }

    @Override
    @Transactional(readOnly = true)
    public CompanionPostResponseDTO getPostDetail(Long postId){
        //게시글 조회
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));
        List<String> imageUrls = companionPostImageRepository.findByCompanionPost(post).stream()
                .map(CompanionPostImg::getImageUrl)
                .collect(Collectors.toList());

        return new CompanionPostResponseDTO(post, imageUrls);
    }

    //@Transactional(readOnly = true)
    @Override
    @Transactional(readOnly = true)
    public Page<CompanionPostListResponseDTO> getPostList(Pageable pageable) {
        return companionPostRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(CompanionPostListResponseDTO::new);
    }

    // 게시글 ID로 조회하는 메서드 추가
    @Override
    @Transactional(readOnly = true)
    public CompanionPost findById(Long postId) {
        return companionPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + postId));
    }

}

