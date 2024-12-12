package com.example.tikicktaka.service.matchService;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.tikicktaka.domain.matches.Match;
import com.example.tikicktaka.repository.match.MatchRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class CrawlerService {

    @Autowired
    private MatchRepository matchRepository;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public void crawlData() {

        // WebDriver 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // 브라우저 UI를 표시하지 않음
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://data.kleague.com");

            // 필요한 경우 프레임 이동 등의 작업 수행
            List<WebElement> frames = driver.findElements(By.tagName("frame"));
            for (WebElement frame : frames) {
                if (frame.getAttribute("src").contains("https://portal.kleague.com")) {
                    driver.get(frame.getAttribute("src"));
                }
            }

//            // JavaScript 실행
//            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
//            jsExecutor.executeScript("moveMainFrame('0025')");
//
//            // 데이터 크롤링
//            List<WebElement> rows = driver.findElements(By.cssSelector("table tr"));
//            for (WebElement row : rows) {
//                List<WebElement> cols = row.findElements(By.tagName("td"));
//                if (cols.size() > 0) {
//                    Match match = new Match();
//                    match.setAwayTeam(cols.get(4).getText());
//                    match.setHomeTeam(cols.get(5).getText());
//                    match.setMatchDate(cols.get(3).getText());
//                    match.setMatchField(cols.get(5).getText());
//                    match.setScore(cols.get(7).getText());
//                    //match.setMatchDate(cols.get(0).getText());
//                    //match.setHomeTeam(cols.get(1).getText());
//                    //match.setAwayTeam(cols.get(2).getText());
//                    //match.setScore(cols.get(3).getText());
//
//                    // 데이터베이스에 저장
//                    matchRepository.save(match);
//                }
//            }
            // JavaScript 실행 (테이블 데이터가 로드된 후 JavaScript로 프레임을 이동)
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            jsExecutor.executeScript("moveMainFrame('0025')");

            Thread.sleep(5000);  // 5초 대기
            // 자바스크립트로부터 jsonResultData 가져오기
            String jsonData = (String) jsExecutor.executeScript("return JSON.stringify(jsonResultData);");

            // jsonData가 비어있지 않을 경우 파싱
            if (jsonData != null && !jsonData.isEmpty()) {
                JSONArray jsonArray = new JSONArray(jsonData);

//                // 각 데이터를 순회하면서 데이터베이스에 저장
//                for (int i = 0; i < jsonArray.length(); i++) {
//                     JSONObject jsonObject = jsonArray.getJSONObject(i);
//
//                    Match match = new Match();
//                    //match.setMatchDate(jsonObject.getString("defer_date"));
//                    match.setHomeTeam(jsonObject.getString("Home_Name"));
//                    match.setAwayTeam(jsonObject.getString("Away_Name"));
//                    match.setMatchField(jsonObject.getString("field_Name"));
//
//                    // 날짜를 String으로 받아 LocalDateTime으로 변환
//                    String dateString = jsonObject.getString("defer_date");  // 2024/03/10 형식
//                    LocalDate matchDate = LocalDate.parse(dateString, dateFormatter);  // String -> LocalDate 변환
//                    LocalDateTime matchDateTime = matchDate.atStartOfDay();  // LocalDateTime으로 변환 (00:00 시간 설정)
//                    match.setMatchDate(matchDate);  // 변환된 LocalDateTime 설정
//
//                    // 데이터베이스에 저장
//                    matchRepository.save(match);
//                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    // 경기 구분을 K리그1만 필터링 (예: leagueType 속성 사용)
                    String leagueType = jsonObject.getString("meet_full_name"); // 예시 속성 이름

                    if (leagueType.contains("K리그1")) { // K리그1만 저장
                        Match match = new Match();
                        match.setHomeTeam(jsonObject.getString("Home_Name"));
                        match.setAwayTeam(jsonObject.getString("Away_Name"));
                        match.setMatchField(jsonObject.getString("field_Name"));

                        // 날짜를 String으로 받아 LocalDateTime으로 변환
                        String dateString = jsonObject.getString("defer_date");  // 2024/03/10 형식
                        LocalDate matchDate = LocalDate.parse(dateString, dateFormatter);  // String -> LocalDate 변환
                        LocalDateTime matchDateTime = matchDate.atStartOfDay();  // LocalDateTime으로 변환 (00:00 시간 설정)
                        match.setMatchDate(matchDate);  // 변환된 LocalDateTime 설정

                        // 데이터베이스에 저장
                        matchRepository.save(match);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit(); // 브라우저 종료
        }
    }

    public String crawlMatchTime(String homeTeam, String awayTeam) {
        // WebDriver 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // 브라우저 UI를 표시하지 않음
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://portal.kleague.com/main/schedule/popupTodayGames.do");

            // 경기 정보를 담고 있는 li 요소들을 모두 찾기
            List<WebElement> matchElements = driver.findElements(By.cssSelector("#div-main-dropdown-show > ul > li"));
            System.out.println("Found " + matchElements.size() + " match elements."); // 경기가 몇 개 있는지 출력

            // 각 경기 정보를 순회하면서 팀 이름과 경기 시간을 확인
            for (WebElement matchElement : matchElements) {
                // 각 matchElement 내부에서 팀 이름이 있는 요소와 경기 시간이 있는 요소를 찾기
                WebElement timeElement = matchElement.findElement(By.cssSelector("div:nth-child(1) > dl.flL.pl20.pt10 > dd.main-today-txt01.mb5"));
                WebElement hometeamElement = matchElement.findElement(By.cssSelector("div.main-device-three.pt10 > div > span.main-today-txt04-left"));
                WebElement awayteamElement = matchElement.findElement(By.cssSelector("div.main-device-three.pt10 > div > span.main-today-txt04-right"));

                // 크롤링한 요소에서 경기 시간과 팀 이름 추출
                String matchTime = timeElement.getText();  // 경기 시간
                String hometeamText = hometeamElement.getText();   // 홈팀 이름
                String awayteamText = awayteamElement.getText();   // 어웨이팀 이름

                // 출력 로그
                System.out.println("Match Time: " + matchTime);
                System.out.println("Home Team: " + hometeamText);
                System.out.println("Away Team: " + awayteamText);

                // 팀 이름이 홈 팀과 어웨이 팀 모두 포함되어 있는지 확인
                if (hometeamText.contains(homeTeam) && awayteamText.contains(awayTeam)) {
                    System.out.println("Match found for teams: " + homeTeam + " vs " + awayTeam);
                    return matchTime; // 팀 이름이 일치하는 경우 경기 시간 반환
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit(); // 브라우저 종료
        }

        System.out.println("No match found for teams: " + homeTeam + " vs " + awayTeam);
        return null; // 팀 이름에 해당하는 경기 시간을 찾지 못한 경우
    }

    public MatchDetails crawlMatchDetails(String homeTeam, String awayTeam) {
        // WebDriver 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // 브라우저 UI를 표시하지 않음
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://portal.kleague.com/main/schedule/popupTodayGames.do");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // 10초까지 대기

            // 경기 정보를 담고 있는 li 요소들을 모두 찾기
            List<WebElement> matchElements = driver.findElements(By.cssSelector("#div-main-dropdown-show > ul > li"));

            // 각 경기 정보를 순회하면서 팀 이름, 스코어, 경기 현황을 확인
            for (WebElement matchElement : matchElements) {
                // 홈팀, 어웨이팀 이름을 찾기
                WebElement hometeamElement = matchElement.findElement(By.cssSelector("div.main-device-three.pt10 > div > span.main-today-txt04-left"));
                WebElement awayteamElement = matchElement.findElement(By.cssSelector("div.main-device-three.pt10 > div > span.main-today-txt04-right"));

                String hometeamText = hometeamElement.getText(); // 홈팀 이름
                String awayteamText = awayteamElement.getText(); // 어웨이팀 이름

                // 홈팀과 어웨이팀 이름이 일치하는지 확인
                if (homeTeam.equals(hometeamText) && awayTeam.equals(awayteamText)) {
                    // 스코어와 경기 현황을 크롤링
//                    WebElement homeScoreElement = matchElement.findElement(By.cssSelector("div.main-device-three.pt10 > div > div > span.main-today-txt05.pl5.blue"));
//                    WebElement awayScoreElement = matchElement.findElement(By.cssSelector("div.main-device-three.pt10 > div > div > span.main-today-txt05.pr5.red"));
                    WebElement homeScoreElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.main-device-three.pt10 > div > div > span.main-today-txt05.pl5.blue")));
                    WebElement awayScoreElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.main-device-three.pt10 > div > div > span.main-today-txt05.pr5.red")));

                    String homeScore = homeScoreElement.getText(); // 홈팀 스코어
                    String awayScore = awayScoreElement.getText(); // 어웨이팀 스코어

                    // 경기 현황 (경기 종료 상태와 라이브 상태 모두 확인)
                    String halfStatus = "";
                    String liveStatus = "";

                    try {
                        // 먼저 라이브 상태를 확인
                        WebElement halfStatusElement = matchElement.findElement(By.cssSelector("div:nth-child(3) > dl.flR.pl20.pt10 > div > span.main-today-txt08.pl5"));
                        //라이브 상태이면 현재 몇분인지도 적어두기
                        WebElement liveStatusElement = matchElement.findElement(By.cssSelector("div:nth-child(3) > dl.flR.pl20.pt10 > div > span.main-today-txt09"));

                        halfStatus = halfStatusElement.getText();
                        liveStatus = liveStatusElement.getText();
                    } catch (Exception e) {
                        // 라이브 없으면 종료 상태 현황 시도
                        WebElement halfStatusElement = matchElement.findElement(By.cssSelector("div:nth-child(3) > dl.flR.pl20.pt10 > div > span.main-today-txt07"));
                        halfStatus = halfStatusElement.getText();
                        liveStatus = "경기가 종료되었습니다.";
                    }

                    // MatchDetails 객체에 값 설정
                    MatchDetails details = new MatchDetails();
                    details.setScore(homeScore + " - " + awayScore); // 예: "2 - 1"
                    details.setHalfStatus(halfStatus); // 예: "Live, 경기 종료"
                    details.setliveStatus(liveStatus); // 예: "후반전"

                    return details; // 세부 정보 반환
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit(); // 브라우저 종료
        }

        return new MatchDetails(); // 팀 이름에 해당하는 경기 세부 내용을 찾지 못한 경우
    }


    // 경기 세부 내용을 담는 클래스 (경기 스코어와 전후반 상태)
    public static class MatchDetails {
        private String score;
        private String halfStatus;
        private String liveStatus;

        public String getScore() {
            return score;
        }

        public void setScore(String score) {
            this.score = score;
        }

        public String getHalfStatus() {
            return halfStatus;
        }

        public void setHalfStatus(String halfStatus) {
            this.halfStatus = halfStatus;
        }

        public String getLiveStatus() {
            return liveStatus;
        }

        public void setliveStatus(String liveStatus) {
            this.liveStatus = liveStatus;
        }


    }
}

