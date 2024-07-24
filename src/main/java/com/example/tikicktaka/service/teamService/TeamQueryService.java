package com.example.tikicktaka.service.teamService;

import com.example.tikicktaka.domain.teams.Team;

import java.util.Optional;

public interface TeamQueryService {

    Optional<Team> findTeamById(Long teamId);
}
