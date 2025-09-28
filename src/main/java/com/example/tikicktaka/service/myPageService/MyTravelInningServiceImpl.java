package com.example.tikicktaka.service.myPageService;

import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.exception.handler.MemberHandler;
import com.example.tikicktaka.apiPayload.exception.handler.TeamHandler;
import com.example.tikicktaka.converter.myTravelInning.MyTravelInningConverter;
import com.example.tikicktaka.domain.MyTravelInning;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.mapping.member.MemberTeam;
import com.example.tikicktaka.domain.teams.Team;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.repository.member.MemberTeamRepository;
import com.example.tikicktaka.repository.myTravelInning.MyTravelInningRepository;
import com.example.tikicktaka.repository.team.TeamRepository;
import com.example.tikicktaka.service.UtilService;
import com.example.tikicktaka.web.dto.myPage.MyTravelInningRequestDTO;
import com.example.tikicktaka.web.dto.myPage.MyTravelInningResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MyTravelInningServiceImpl implements MyTravelInningService {

    private final MyTravelInningRepository repository;
    private final MemberRepository memberRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final TeamRepository teamRepository;
    private final UtilService utilService;

    private static final String DEFAULT_IMAGE_URL =
            "https://tikicktaka-bucket.s3.ap-northeast-2.amazonaws.com/logo/Companion_Travel.png";

    @Override
    @Transactional
    public MyTravelInningResponseDTO.Detail create(Long memberId, MyTravelInningRequestDTO.Create dto, MultipartFile image) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        MemberTeam memberTeam = memberTeamRepository.findByMemberId(memberId)
                .orElseThrow(()->new MemberHandler(ErrorStatus.MEMBER_TEAM_NOT_FOUND));
        Team preferTeam = memberTeam.getTeam();
        if (preferTeam == null) {
            throw new TeamHandler(ErrorStatus.TEAM_NOT_FOUND);
        }
        Team opponentTeam = teamRepository.findById(dto.getOpponentTeamId())
                .orElseThrow(() -> new TeamHandler(ErrorStatus.TEAM_NOT_FOUND));
        String imageUrl = (image != null && !image.isEmpty())
                ? utilService.uploadS3Img("travel-inning", image)
                : DEFAULT_IMAGE_URL;
        MyTravelInning entity = MyTravelInningConverter.toEntity(dto, member, preferTeam, opponentTeam, imageUrl);
        repository.save(entity);

        return MyTravelInningConverter.toDetailDTO(entity);
    }


}
