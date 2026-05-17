# APAP Backend

APAP 프로젝트의 백엔드 서버입니다.

현재는 초보자도 IntelliJ에서 바로 실행해볼 수 있도록 `Spring Boot + H2 DB` 기반으로 구성했습니다.

## 실행 환경

- Java 17 이상
- IntelliJ IDEA
- 인터넷 연결

첫 실행 시 Maven이 Spring Boot 라이브러리를 내려받기 때문에 인터넷 연결이 필요합니다.

## IntelliJ로 실행하는 방법

1. GitHub 저장소를 클론합니다.

```powershell
git clone -b apap-main-cooperation https://github.com/KiHyeonLee1121/APAP-test.git
cd APAP-test
```

2. IntelliJ에서 프로젝트를 엽니다.

- IntelliJ 실행
- `File` -> `Open`
- `APAP-test/backend` 폴더 선택
- Maven 프로젝트로 인식되면 `Load Maven Project` 또는 `Trust Project` 선택

3. JDK를 설정합니다.

- `File` -> `Project Structure`
- `Project SDK`를 Java 17 이상으로 설정

4. 서버를 실행합니다.

- `src/main/java/com/apap/backend/ApapBackendApplication.java` 파일 열기
- `main` 메서드 왼쪽의 초록색 실행 버튼 클릭

정상 실행되면 콘솔에 다음과 비슷하게 표시됩니다.

```text
Tomcat started on port 8080
Started ApapBackendApplication
```

5. 브라우저에서 확인합니다.

```text
http://localhost:8080/api/health
```

`{"success":true,"data":{"status":"ok"}}` 형태의 응답이 나오면 서버가 정상 실행된 것입니다.

## H2 DB 확인

브라우저에서 접속합니다.

```text
http://localhost:8080/h2-console
```

입력값:

```text
JDBC URL: jdbc:h2:file:./data/apap-local-db
User Name: sa
Password: 비워두기
```

## 초보자 테스트 순서

## Postman으로 한눈에 테스트하기

Postman에서 직접 요청을 만들지 말고, 아래 파일을 가져오면 됩니다.

- `backend/postman/APAP.postman_collection.json`
- `backend/postman/APAP.local.postman_environment.json`

가져오는 방법:

1. Postman 왼쪽 위 `Import` 클릭
2. `files` 선택
3. 위 두 파일을 선택해서 import
4. 오른쪽 위 환경 선택에서 `APAP Local` 선택
5. Spring Boot 서버가 켜져 있는지 확인
6. `0. Health`부터 위에서 아래 순서대로 실행

권장 실행 순서:

```text
0. Health / Health Check
1. Users / Signup
2. Scenarios / Create Scenario
3. Cameras / Videos / Create Video Source
4. Analysis / Create Analysis Job
4. Analysis / AI Callback - Abnormal Event
5. Events / List Detection Events
6. Alerts / List Alerts
7. Dashboard / Dashboard Summary
```

`Signup`, `Create Scenario`, `Create Video Source`, `Create Analysis Job`은 응답에서 생성된 ID를 자동으로 Postman 변수에 저장하도록 해두었습니다.

### 1. 회원가입

```http
POST http://localhost:8080/api/auth/signup
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "1234",
  "name": "준래"
}
```

응답에서 `data.id`가 사용자 ID입니다. 아래 예시에서는 `1`이라고 가정합니다.

### 2. 로그인

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "1234"
}
```

현재는 학습용 기초 코드라서 실제 JWT 인증을 붙이지 않았습니다. 응답의 `accessToken`은 나중에 정식 인증 구현 때 교체할 값입니다.

### 3. 감지 시나리오 생성

```http
POST http://localhost:8080/api/scenarios
Content-Type: application/json

{
  "userId": 1,
  "name": "무인매장 절도 의심",
  "targetLocation": "무인매장",
  "behaviorText": "물건을 가방에 넣고 결제 없이 출구로 이동",
  "conditionsJson": "{\"handNearBag\": true, \"paymentDetected\": false}",
  "threshold": 0.8,
  "active": true
}
```

### 4. 영상 소스 등록

```http
POST http://localhost:8080/api/videos
Content-Type: application/json

{
  "userId": 1,
  "type": "UPLOAD",
  "name": "sample-video",
  "sourceUrl": "uploads/sample.mp4"
}
```

### 5. AI 분석 작업 생성

```http
POST http://localhost:8080/api/analysis/jobs
Content-Type: application/json

{
  "scenarioId": 1,
  "videoSourceId": 1
}
```

이 API는 실제 AI 서버를 호출하지 않고, AI 서버에 보낼 payload를 만들어줍니다.

### 6. AI callback 테스트

AI 서버가 분석을 끝냈다고 가정하고, 직접 callback을 호출합니다.

```http
POST http://localhost:8080/api/analysis/callback
Content-Type: application/json

{
  "jobId": 1,
  "status": "DONE",
  "errorMessage": null,
  "events": [
    {
      "eventType": "ABNORMAL",
      "severity": "HIGH",
      "confidenceScore": 0.91,
      "detectedAt": "2026-07-01T12:10:00",
      "snapshotUrl": "uploads/snapshot-1.jpg",
      "clipUrl": "uploads/clip-1.mp4",
      "resultJson": "{\"reason\": \"주변 탐색과 가방 접근이 동시에 감지됨\"}"
    }
  ]
}
```

### 7. 대시보드 요약 조회

```http
GET http://localhost:8080/api/dashboard/summary?userId=1
```

## 지금 코드에서 알아야 할 것

- `controller`: API 주소를 만드는 곳
- `entity`: DB 테이블 구조
- `repository`: DB 저장/조회 도구
- `application.yml`: 서버 포트, DB 설정
- `H2`: 개발용 가벼운 DB
- `JPA`: Java 객체를 DB 테이블로 연결해주는 기술

## 다음 구현 과제

- JWT 인증 정식 구현
- Swagger 문서 추가
- PostgreSQL 전환
- AI 서버 실제 HTTP 호출
- 영상 파일 저장 정책 정리
- 이벤트 검색/필터링 API 추가
