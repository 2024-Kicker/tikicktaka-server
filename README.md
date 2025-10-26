# 🧢 TravelInning — 야구 원정 팬 동행·여행 통합 플랫폼

> **Spring Boot 기반 야구 원정 팬 커뮤니티 & 여행 추천 서비스**

야구 원정 팬들이 **동행자를 찾고**, **실시간 채팅으로 소통하며**,
**구장 주변 맛집과 관광지를 탐색할 수 있는** 통합 플랫폼입니다.

---

## 🚀 주요 기능
<img width="512" height="1054" alt="Image" src="https://github.com/user-attachments/assets/590a209a-1f5e-4ec0-8693-18811db9fa5e" /> <img width="512" height="576" alt="Image" src="https://github.com/user-attachments/assets/e0260225-47f6-4a53-bd2f-d1e8953ab05e" /> <img width="512" height="550" alt="Image" src="https://github.com/user-attachments/assets/4436649b-cfa4-46c3-b054-399209efa291" />
* **동행 게시판** — 조건 필터링, 차단/차단 해제, 공유 링크
* **실시간 채팅** — Redis Pub/Sub 구조로 지연·중복 문제 해결
* **구장 주변 여행지 추천** — 관광/맛집/명소 API
* **마이페이지** — 프로필 및 참여 내역 관리

---

## 🧱 기술 스택

| 분류       | 사용 기술                                  |
| -------- | -------------------------------------- |
| Backend  | Spring Boot, JPA, Spring Security, JWT |
| Database | MySQL, Redis                           |
| Infra    | AWS S3, EC2                            |
| Realtime | Socket.IO, Redis Pub/Sub               |
| Tools    | Gradle, JUnit                          |

---

## ⚙️ 아키텍처

```
Client (iOS / Android)
   │
   ▼
Spring Boot API
   ├─ Companion (동행 게시판)
   ├─ Chat (실시간 메시징)
   ├─ Attraction (구장 주변 관광)
   └─ MyPage
   │
MySQL · Redis · AWS S3
```

---

## 🏆 주요 성과

* 동행 게시판 조회 성능 **35% 개선** (Projection 기반 DTO 적용)
* Redis Pub/Sub 기반 채팅으로 **메시지 지연 ≤ 0.5초**
* 창업 아이디어 경진대회 **혁신상 수상**

---

## 📸 미리보기

> (서비스 화면, ERD, Swagger 캡처 등 추가 예정)

---

## 🧭 향후 계획

* 초대 알림 기능 추가 및 Travel Recommendation API 고도화 등 프로젝트 리팩토링

---

## 👩🏻‍💻 개발

**Hyojeong Choi** — Backend Developer

---

> 마지막 수정: 2025-10-26 (Asia/Seoul)
