package com.example.tikicktaka.service.KBOmatchService;

import com.example.tikicktaka.domain.matches.GameSchedule;
import com.example.tikicktaka.repository.match.GameScheduleRepository;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;


@Service
public class GameScheduleService {

    @Autowired
    private GameScheduleRepository gameScheduleRepository;

    private static final String URL = "https://www.koreabaseball.com/Schedule/Schedule.aspx";

    public void crawlAndSaveGameScheduleForAllMatches() {
        // 크롬 드라이버 옵션 설정 (헤드리스 모드)
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--no-sandbox");

        WebDriver driver = new ChromeDriver(options);
        driver.get(URL);

        // 1월부터 12월까지 순차적으로 크롤링 수행
        for (int month = 3; month <= 12; month++) {
            String monthStr = String.format("%02d", month); // 01, 02, ... 12 형태로 포맷팅

            // 드롭다운에서 월 선택
            Select select = new Select(driver.findElement(By.id("ddlMonth")));
            select.selectByValue(monthStr);

            // 경기 일정 테이블 추출
            var table = driver.findElement(By.className("tbl-type06"));
            System.out.println(month+"table: "+table);
            var tbody = table.findElement(By.tagName("tbody"));
            System.out.println(month+"tbody: "+tbody);
            var rows = tbody.findElements(By.tagName("tr"));
            System.out.println(month+"rows: "+ rows);

            if (rows.isEmpty()) {
                continue; // 해당 월에 경기가 없으면 넘어갑니다.
            }

            String previousGameDate = null;
            String cleanedDate = null;

            // 각 행을 순회하며 경기 정보 추출
            for (var row : rows) {
                List<String> rowData = row.findElements(By.tagName("td")).stream()
                        .map(td -> td.getText().trim())
                        .collect(Collectors.toList());
                System.out.println("rowData: "+ rowData);

                if (rowData.get(0).contains("데이터가 없습니다")) continue;
                // 날짜가 있는 경우(=새로운 날짜임) 이전 날짜를 갱신하고, 없는 경우(=같은 날짜임) 이전 날짜를 사용
                if (rowData.get(0).contains(".")) {
                    // 현재 날짜를 저장
                    previousGameDate = rowData.get(0).split("\\(")[0].replace(".", "-"); // "03-22"
                }
                else{
                    rowData.add(0,previousGameDate);
                    System.out.println("날짜 추가입니다: "+rowData.get(0));
                }

                String gameDate = rowData.get(0);
                System.out.println("gameDate: "+gameDate);
                String matchTime = rowData.get(1); // 경기 시간 정보
                System.out.println("matchTime: "+matchTime);
                String teams = rowData.get(2); // "롯데vsLG" 형식의 팀 정보
                System.out.println("teams: "+teams);
                //String homeTeam = rowData.get(1);
                //String awayTeam = rowData.get(2);
                String matchField = rowData.get(7);
                System.out.println("matchField: "+matchField);

                // "vs"를 기준으로 홈팀과 어웨이팀 분리
                String[] teamsSplit = teams.split("vs");
                String awayTeam = teamsSplit[0].trim(); // 어웨이팀
                String homeTeam = teamsSplit[1].trim(); // 홈팀
                System.out.println("homeTeam: "+homeTeam);
                System.out.println("awayTeam: "+awayTeam);


                // 현재 연도를 자동으로 가져옴
                int currentYear = LocalDate.now().getYear();  // 현재 연도 (예: 2025)

                // 날짜에서 요일을 제거하고 yyyy-MM-dd 형태로 변환
                //cleanedDate = gameDate.split("\\(")[0].replace(".", "-"); // "03-22"
                cleanedDate = previousGameDate;
                System.out.println("cleanedDate: "+cleanedDate);

                String fullDate = currentYear + "-" + cleanedDate; // "2025-03-22"
                System.out.println("fullDate: "+fullDate);


                // LocalDate로 변환
                LocalDate matchDate = LocalDate.parse(fullDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                System.out.println("matchDate: "+matchDate);

                // 경기 시간 파싱 (예: "14:00")
                String matchDateTimeStr = matchDate + " " + matchTime;
                System.out.println("matchDateTimeStr: "+matchDateTimeStr);

                // LocalDateTime을 "yyyy-MM-dd HH:mm:ss" 형태로 변환하기 위해 포맷 수정
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime matchDateTime = LocalDateTime.parse(matchDateTimeStr, formatter);

                // GameSchedule 객체 생성
                GameSchedule gameSchedule = new GameSchedule();
                //gameSchedule.setMatchDate(LocalDate.parse(gameDate.replace(".", "-"))); // "03.22" -> "03-22"로 변환
                gameSchedule.setMatchDate(matchDate);
                gameSchedule.setHomeTeam(homeTeam);
                gameSchedule.setAwayTeam(awayTeam);
                gameSchedule.setMatchField(matchField);
                gameSchedule.setMatchDateTime(matchDateTime); // 경기 날짜와 시간 설정
                gameSchedule.setMatchStatus(false);  // 경기 예정 상태는 false로 설정


                // 데이터베이스에 저장
                gameScheduleRepository.save(gameSchedule);
            }
        }

        driver.quit();
    }
}


