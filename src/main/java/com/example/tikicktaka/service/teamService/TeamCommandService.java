package com.example.tikicktaka.service.teamService;

import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.teams.Team;
import org.springframework.web.multipart.MultipartFile;

public interface TeamCommandService {

    Team teamImageUpload(MultipartFile logo, MultipartFile stadium, Team team);
}
