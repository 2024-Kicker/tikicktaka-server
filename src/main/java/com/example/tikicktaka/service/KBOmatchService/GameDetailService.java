package com.example.tikicktaka.service.KBOmatchService;

import com.example.tikicktaka.domain.matches.GameSchedule;
import com.example.tikicktaka.domain.matches.Match;
import com.example.tikicktaka.repository.match.GameScheduleRepository;
import com.example.tikicktaka.repository.match.MatchRepository;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@EnableScheduling
public class GameDetailService {


    private static final String KBO_URL = "https://www.koreabaseball.com/Schedule/Schedule.aspx";
    @Autowired
    private GameScheduleRepository gameScheduleRepository;
    GameSchedule gameSchedule = new GameSchedule();


    // 오전 9시에 시작 -> 수정하기
    @Scheduled(cron = "0 0 14 * * *")
    // 10분마다 경기 세부 내용 업데이트
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void updateGameDetails() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        //LocalDate today = LocalDate.of(2024,04,02);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 날짜 및 시간 파싱
        //LocalDateTime now = LocalDateTime.of(2024,4,2,19,0);


        List<GameSchedule> todayMatches = gameScheduleRepository.findByMatchDate(today);
        System.out.println("todayMatches:" + todayMatches);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        try {
            driver.get(KBO_URL); // KBO 경기 일정 페이지로 이동
            //오늘 일자의 경기를 크롤링
            Select selectYear = new Select(driver.findElement(By.id("ddlYear"))); //연도 설정
            selectYear.selectByValue(String.format("%02d", today.getYear()));

            //월 설정
            Select selectMonth = new Select(driver.findElement(By.id("ddlMonth")));
            selectMonth.selectByValue(String.format("%02d", today.getMonthValue()));

            // 오늘 경기가 데이터베이스에 없으면 종료
            if (todayMatches.isEmpty()) {
                System.out.println("오늘 경기가 없습니다.");
                return; // 오늘 경기가 없으면 종료
            }

            // 경기 일정 테이블 추출
            var table = driver.findElement(By.className("tbl-type06"));
            var tbody = table.findElement(By.tagName("tbody"));
            var rows = tbody.findElements(By.tagName("tr"));
            String previousGameDate = null;
            String cleanedDate = null;

            // 각 행을 순회하며 경기 정보 추출
            for (var row : rows) {
                List<String> rowData = row.findElements(By.tagName("td")).stream()
                        .map(td -> td.getText().trim())
                        .collect(Collectors.toList());
                System.out.println("rowData: " + rowData);

                if (rowData.get(0).contains("데이터가 없습니다")) continue;

                // 날짜가 있는 경우(=새로운 날짜임) 이전 날짜를 갱신하고, 없는 경우(=같은 날짜임) 이전 날짜를 사용
                if (rowData.get(0).contains(".")) {
                    // 현재 날짜를 저장
                    previousGameDate = rowData.get(0).split("\\(")[0].replace(".", "-"); // "03-22"
                } else {
                    rowData.add(0, previousGameDate);
                    System.out.println("날짜 추가입니다: " + rowData.get(0));
                }
                // 날짜에서 요일을 제거하고 yyyy-MM-dd 형태로 변환
                String GameDate = today.getYear() + "-" + rowData.get(0).split("\\(")[0].replace(".", "-"); // "03-22"
                LocalDate matchDate = LocalDate.parse(GameDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                System.out.println("matchDate: " + matchDate);

                // 오늘 날짜와 비교하여 일치하지 않으면 넘어감
                if (!matchDate.isEqual(today)) {
                    System.out.println("오늘 경기가 아님, 건너뜁니다: " + matchDate);
                    continue;  // 오늘 경기가 아니면 다음 row로 넘어감
                }

                String teams = rowData.get(2); // "롯데vsLG" 형식
                String matchField = rowData.get(7);
                System.out.println("teams: " + teams);
                System.out.println("matchField: " + matchField);

                // 안전한 팀과 점수 추출 로직
                String[] parts = teams.split("vs");

                if (parts.length == 2) {
                    String awayTeamRaw = parts[0].trim();  // NC7
                    String homeTeamRaw = parts[1].trim();  // 5LG

                    // 숫자와 문자 분리 (정규 표현식 사용)
                    String awayTeam = awayTeamRaw.replaceAll("\\d", ""); // "NC"
                    int awayScore = Integer.parseInt(awayTeamRaw.replaceAll("\\D", "")); // "7"

                    String homeTeam = homeTeamRaw.replaceAll("\\d", ""); // "LG"
                    int homeScore = Integer.parseInt(homeTeamRaw.replaceAll("\\D", "")); // "5"

                    System.out.println("awayTeam: " + awayTeam);
                    System.out.println("awayScore: " + awayScore);
                    System.out.println("homeScore: " + homeScore);
                    System.out.println("homeTeam: " + homeTeam);

                    // 오늘 경기에 해당하는지 확인
                    for (GameSchedule gameschedule : todayMatches) {
                        if (gameschedule.getHomeTeam().equals(homeTeam) && gameschedule.getAwayTeam().equals(awayTeam)) {
                            LocalDateTime matchStartTime = gameschedule.getMatchDateTime();

                            // 경기가 진행 중이면 (현재 시간이 경기 시작시간과 그 이후 8시간 이내일 때)
                            if (now.isAfter(matchStartTime) && now.isBefore(matchStartTime.plusHours(4))) {
                                try {
                                    List<WebElement> gameElements = driver.findElements(By.className("tbl-type06"));
                                    for (WebElement element : gameElements) {
                                        if (element.getText().contains(homeTeam) && element.getText().contains(awayTeam)) {
                                            String score = awayScore + "-" + homeScore; // 예: "7-5" 형식으로 저장
                                            System.out.println("score: " + score);

                                            String gameStatus=rowData.get(3);
                                            System.out.println("gameStatus: " + gameStatus);

                                            gameschedule.setScore(score);
                                            gameschedule.setMatchStatus(!gameStatus.contains("리뷰") || !rowData.get(8).isEmpty()); // 리뷰가 있거나, 비고가 차있으면 게임 종료

                                            // 데이터베이스에 업데이트
                                            gameScheduleRepository.save(gameschedule);

                                            System.out.println("경기 업데이트 완료: " + homeTeam + " vs " + awayTeam);
                                            System.out.println("현재 스코어: " + score);
                                            System.out.println("경기 상태: " + gameStatus);
                                        }
                                    }
                                } catch (Exception e) {
                                    System.err.println("경기 업데이트 실패: " + homeTeam + " vs " + awayTeam);
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } else {
                    System.err.println("팀과 점수를 파싱할 수 없습니다: " + teams);
                }
            }
        } catch (Exception e) {
            System.err.println("크롤링 중 전체 오류 발생: " + e.getMessage());
            e.printStackTrace();
        } finally {
            driver.quit(); // 크롤링 종료 후 브라우저 종료
        }

    }
}
