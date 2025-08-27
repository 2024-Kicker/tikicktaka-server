package com.example.tikicktaka.service.homeService;

import com.example.tikicktaka.domain.gameSchedule.GameSchedule;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.mapping.member.MemberTeam;
import com.example.tikicktaka.domain.teams.Team;
import com.example.tikicktaka.web.dto.home.HomeHeaderResponseDTO;
import com.example.tikicktaka.repository.match.GameScheduleRepository;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.repository.member.MemberTeamRepository;
import com.example.tikicktaka.repository.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * 홈 상단에 노출되는 헤더 정보를 구성한다.
 *
 * 규칙
 * - 기준 구단(baseTeam) 결정 우선순위: 요청 파라미터 teamId > 회원 선호구단 > 기본값(1)
 * - GameSchedule 은 팀을 "약칭(예: 두산, SSG)" 으로 저장한다.
 * - Team 엔티티는 "풀네임(teamName: 두산 베어스)" 과 "약칭(shortName: 두산)" 을 모두 가진다.
 * - 다음 경기 선정: 오늘(Asia/Seoul) 이후, 기준 구단이 홈/원정인 경기 각 1건을 조회하여 더 이른 경기 선택
 * - 구장명: "해당 경기의 홈팀" Team.location 사용
 * - 응답: 닉네임, 경기 날짜(LocalDate), 기준 구단(풀네임), 상대 팀(풀네임), 구장명
 */
@Service
@RequiredArgsConstructor
public class HomeHeaderService {

    /** 선호구단이 없을 때 기본 팀 ID */
    private static final Long DEFAULT_TEAM_ID = 1L;

    private final GameScheduleRepository gameScheduleRepository;
    private final MemberRepository memberRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final TeamRepository teamRepository;

    /**
     * 선택 구단(teamIdParam) > 유저 선호구단 > team_id=1 우선순위로 기준 구단을 결정하고,
     * 기준 구단의 "가장 가까운 다음 경기(오늘 이후)"를 조회하여 헤더 정보를 구성한다.
     *
     * @param teamIdParam 요청 파라미터로 넘어온 기준 구단 ID(선택)
     * @return 홈 헤더 응답 DTO
     */
    @Transactional(readOnly = true)
    public HomeHeaderResponseDTO getHomeHeader(Long teamIdParam) {

        // 1) 현재 로그인 사용자 조회 (닉네임/선호구단 결정을 위해)
        Member member = getCurrentMember();

        // 2) 기준 구단 ID 결정: 파라미터 > 선호구단 > 기본값
        Long baseTeamId = resolveBaseTeamId(teamIdParam, member);

        // 3) 기준 구단 엔티티 조회 (풀네임/약칭 모두 사용)
        Team baseTeam = teamRepository.findById(baseTeamId)
                .orElseThrow(() -> new IllegalStateException("기준 구단을 찾을 수 없습니다. teamId=" + baseTeamId));
        String baseTeamFullName = baseTeam.getTeamName();   // 예: "두산 베어스"
        String baseTeamShort    = baseTeam.getShortName();  // 예: "두산"

        // 4) 오늘(Asia/Seoul) 이후로, 기준 구단이 홈/원정인 경기 각각 1건 조회 후 더 이른 경기 선택
        LocalDateTime nowKst = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        Optional<GameSchedule> asHome = gameScheduleRepository
                .findFirstByHomeTeamAndMatchDateTimeGreaterThanEqualOrderByMatchDateTimeAsc(baseTeamShort, nowKst);
        Optional<GameSchedule> asAway = gameScheduleRepository
                .findFirstByAwayTeamAndMatchDateTimeGreaterThanEqualOrderByMatchDateTimeAsc(baseTeamShort, nowKst);

        GameSchedule next = pickEarlier(asHome, asAway);

        // 5) 다가오는 경기가 없으면: 닉네임/기준구단만 내려주고 나머지는 null
        if (next == null) {
            return HomeHeaderResponseDTO.builder()
                    .nickname(member.getName())     // Member.name 사용
                    .baseTeamName(baseTeamFullName) // 풀네임
                    .gameDate(null)
                    .opponentTeamName(null)
                    .stadiumName(null)
                    .build();
        }

        // 6) 상대팀 계산
        //    GameSchedule 은 약칭을 저장하므로, 기준 구단 약칭과 비교하여 상대 약칭을 구한 뒤,
        //    TeamRepository 로 상대 팀 엔티티를 찾아 "풀네임"을 응답에 사용한다.
        boolean baseIsHome      = baseTeamShort.equals(next.getHomeTeam());
        String opponentShort    = baseIsHome ? next.getAwayTeam() : next.getHomeTeam(); // 약칭
        String opponentFullName = teamRepository.findByShortName(opponentShort)
                .map(Team::getTeamName)   // 풀네임 변환
                .orElse(opponentShort);   // 혹시 매핑 실패 시 약칭 그대로

        // 7) 구장명 계산
        //    "홈팀"의 Team.Teaminfo 을 구장명으로 사용한다. (DB 규칙)
        String homeShort    = next.getHomeTeam();
        String stadiumName  = teamRepository.findByShortName(homeShort)
                .map(Team::getTeamInfo)
                .orElse(null);

        // 8) 응답 조립 (날짜는 GameSchedule.matchDate(LocalDate) 그대로 사용)
        return HomeHeaderResponseDTO.builder()
                .nickname(member.getName())
                .gameDate(next.getMatchDate())       // LocalDate
                .baseTeamName(baseTeamFullName)      // 풀네임
                .opponentTeamName(opponentFullName)  // 풀네임
                .stadiumName(stadiumName)            // 홈팀의 location
                .build();
    }

    /**
     * SecurityContext 의 Authentication.principal 에 있는 memberId(Long)로 Member 엔티티를 조회한다.
     * 인증 정보가 없거나, 회원을 찾지 못하면 예외를 던진다.
     */
    private Member getCurrentMember() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        Long memberId = (Long) auth.getPrincipal();
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다. id=" + memberId));
    }

    /**
     * 기준 구단 ID를 결정한다.
     * 1) 요청 파라미터 teamId 가 있으면 그대로 사용
     * 2) 없으면 회원의 선호구단(MemberTeam)을 조회하여 사용
     * 3) 그래도 없으면 DEFAULT_TEAM_ID 반환
     */
    private Long resolveBaseTeamId(Long teamIdParam, Member member) {
        if (teamIdParam != null) return teamIdParam;

        return memberTeamRepository.findByMember(member)
                .map(MemberTeam::getTeam)
                .map(Team::getId)
                .orElse(DEFAULT_TEAM_ID);
    }

    /**
     * 홈/원정으로 각각 조회해 온 Optional<GameSchedule> 중 "더 이른 경기"를 선택한다.
     * - 둘 중 하나가 비어 있으면 다른 쪽을 반환
     * - 둘 다 존재하면 matchDateTime 을 비교하여 더 빠른 쪽을 반환
     * - 둘 다 비어 있으면 null
     */
    private GameSchedule pickEarlier(Optional<GameSchedule> a, Optional<GameSchedule> b) {
        if (a.isEmpty()) return b.orElse(null);
        if (b.isEmpty()) return a.get();
        return a.get().getMatchDateTime().isBefore(b.get().getMatchDateTime()) ? a.get() : b.get();
    }
}
