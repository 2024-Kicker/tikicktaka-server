package com.example.tikicktaka.service.teamService;

import com.example.tikicktaka.domain.teams.Team;
import com.example.tikicktaka.repository.team.TeamRepository;
import com.example.tikicktaka.service.UtilService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TeamCommandServiceImpl implements TeamCommandService {

    private final TeamRepository teamRepository;
    private final UtilService utilService;

    @Override
    @Transactional
    public Team teamImageUpload(MultipartFile logo, MultipartFile stadium, Team team) {
        String logoUrl = (logo != null && !logo.isEmpty())
                ? utilService.uploadS3Img("logo", logo)
                : team.getLogoUrl(); // 새 업로드 없으면 기존 유지


        team.setLogoUrl(logoUrl);

        return teamRepository.save(team);
    }
}
