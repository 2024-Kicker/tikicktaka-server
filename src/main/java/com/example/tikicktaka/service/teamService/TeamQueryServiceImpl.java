package com.example.tikicktaka.service.teamService;

import com.example.tikicktaka.domain.teams.Team;
import com.example.tikicktaka.repository.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamQueryServiceImpl implements TeamQueryService{

    private final TeamRepository teamRepository;

    @Override
    public Optional<Team> findTeamById(Long teamId) {
        return teamRepository.findById(teamId);
    }
}
