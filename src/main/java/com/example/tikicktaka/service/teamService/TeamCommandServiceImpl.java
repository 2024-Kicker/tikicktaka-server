package com.example.tikicktaka.service.teamService;

import com.example.tikicktaka.converter.member.MemberConverter;
import com.example.tikicktaka.converter.team.TeamConverter;
import com.example.tikicktaka.domain.images.ProfileImg;
import com.example.tikicktaka.domain.images.TeamImg;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.teams.Team;
import com.example.tikicktaka.repository.team.TeamImgRepository;
import com.example.tikicktaka.repository.team.TeamRepository;
import com.example.tikicktaka.service.UtilService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TeamCommandServiceImpl implements TeamCommandService{

    private final TeamImgRepository teamImgRepository;
    private final UtilService utilService;

    @Override
    @Transactional
    public Team teamImageUpload(MultipartFile logo, MultipartFile stadium, Team team) {

        Optional<TeamImg> older = teamImgRepository.findByTeam_Id(team.getId());
        if(older.isPresent()){
            TeamImg old = older.get();
            teamImgRepository.delete(old);
            teamImgRepository.flush();
        }

        String logoUrl = utilService.uploadS3Img("logo", logo);
        String stadiumUrl = utilService.uploadS3Img("stadium", stadium);

        TeamImg teamImg = TeamConverter.toTeamImg(logoUrl, stadiumUrl, team);
        team.setTeamImg(teamImg);
        teamImgRepository.save(teamImg);

        return team;
    }
}
