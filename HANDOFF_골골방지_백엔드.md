# 골골방지 백엔드 — Claude Code 핸드오프

> 이 문서는 Claude.ai에서 진행한 설계·기획 작업을 Claude Code에서 이어받아
> 바로 개발에 착수할 수 있도록 작성된 인수인계 문서입니다.

---

## 1. 프로젝트 개요

**서비스명**: 골골방지 (Family Health OS)
**레포명**: `golgolengi-server`
**패키지**: `com.golgolengi`
**목적**: 가족을 하나의 건강 운영 단위로 보고, 리스크 점수 산출·가족 공동 챌린지·행동 변화를 제공하는 AI 기반 예방 헬스케어 백엔드

---

## 2. 확정된 기술 스택

| 영역 | 기술 | 비고 |
|---|---|---|
| 언어 / 프레임워크 | Java 21 + Spring Boot 3.3.x | Virtual Threads 사용 가능 |
| 데이터베이스 | MongoDB 7.x | 로컬 직접 설치 (Docker 없음) |
| 캐시 | **없음** | Redis 제외 결정 — 로그아웃은 MongoDB logout_tokens 컬렉션으로 대체 |
| 인증 | Google / Apple OAuth 2.0 + JWT (HS256) | Apple은 Spring 기본 미지원 → 커스텀 구현 필요 |
| Push 알림 | Firebase Admin SDK (FCM / APNs) | 배지 자동 부여, 리스크 경고 알림 |
| 빌드 | Gradle Groovy DSL | |
| 코드 간소화 | Lombok + MapStruct | |
| 개발 OS | Windows | |

---

## 3. 확정된 build.gradle

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.5'
    id 'io.spring.dependency-management' version '1.1.6'
}

group = 'com.golgolengi'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Web
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // Security + OAuth2 (Google)
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'

    // MongoDB
    implementation 'org.springframework.boot:spring-boot-starter-data-mongodb'

    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    runtimeOnly    'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly    'io.jsonwebtoken:jjwt-jackson:0.12.6'

    // FCM
    implementation 'com.google.firebase:firebase-admin:9.3.0'

    // Actuator
    implementation 'org.springframework.boot:spring-boot-starter-actuator'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // MapStruct
    implementation 'org.mapstruct:mapstruct:1.6.2'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.6.2'
    annotationProcessor 'org.projectlombok:lombok-mapstruct-binding:0.2.0'

    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    testImplementation 'de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring31x:4.13.1'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

---

## 4. 확정된 application.yml

```yaml
spring:
  application:
    name: golgolengi

  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/golgolengi}
      auto-index-creation: true

  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: email, profile
            redirect-uri: "{baseUrl}/oauth/google/callback"

jwt:
  secret: ${JWT_SECRET}
  access-expiration: 3600000
  refresh-expiration: 2592000000

server:
  port: 8080

logging:
  level:
    com.golgolengi: DEBUG
    org.springframework.security: DEBUG
```

---

## 5. 확정된 디렉터리 구조

```
src/main/java/com/golgolengi/
│
├── GolgolengiApplication.java
│
├── global/
│   ├── config/
│   │   ├── SecurityConfig
│   │   └── MongoConfig           ← @EnableMongoAuditing
│   ├── exception/
│   │   ├── GlobalExceptionHandler
│   │   ├── CustomException
│   │   └── ErrorCode
│   ├── jwt/
│   │   ├── JwtProperties
│   │   ├── JwtProvider
│   │   └── JwtAuthFilter
│   └── response/
│       └── ApiResponse
│
├── auth/
│   ├── controller/
│   │   └── AuthController
│   ├── dto/
│   │   ├── request/
│   │   │   ├── TermsAgreeRequest
│   │   │   └── ConsentRequest
│   │   └── response/
│   │       └── TokenResponse
│   ├── service/
│   │   └── AuthService
│   ├── repository/
│   │   ├── ConsentRepository
│   │   └── LogoutTokenRepository  ← Redis 대체 (MongoDB)
│   └── document/
│       ├── Consent
│       └── LogoutToken            ← 로그아웃 블랙리스트
│
├── member/
│   ├── controller/
│   │   └── MemberController
│   ├── dto/
│   │   ├── request/
│   │   │   ├── UpdateMemberRequest
│   │   │   └── GuardianEmailRequest
│   │   └── response/
│   │       └── MemberResponse
│   ├── service/
│   │   └── MemberService
│   ├── repository/
│   │   └── MemberRepository
│   └── document/
│       └── Member
│
├── family/
│   ├── controller/
│   │   └── FamilyController
│   ├── dto/
│   │   ├── request/
│   │   │   ├── CreateFamilyRequest
│   │   │   ├── JoinFamilyRequest
│   │   │   └── UpdatePrivacySettingRequest
│   │   └── response/
│   │       ├── FamilyResponse
│   │       ├── FamilyOverviewResponse
│   │       └── InviteCodeResponse
│   ├── service/
│   │   └── FamilyService
│   ├── repository/
│   │   ├── FamilyRepository
│   │   └── FamilyMemberRepository
│   └── document/
│       ├── Family
│       ├── FamilyMember
│       └── PrivacySetting
│
├── health/
│   ├── controller/
│   │   └── HealthController
│   ├── dto/
│   │   ├── request/
│   │   │   ├── CreateHealthProfileRequest
│   │   │   ├── UpdateLifestyleRequest
│   │   │   └── SyncHealthDataRequest
│   │   └── response/
│   │       └── HealthProfileResponse
│   ├── service/
│   │   └── HealthService
│   ├── repository/
│   │   └── HealthProfileRepository
│   └── document/
│       └── HealthProfile
│
├── mission/
│   ├── controller/
│   │   └── MissionController
│   ├── dto/
│   │   ├── request/
│   │   │   ├── CreateMissionRequest
│   │   │   ├── CheckInRequest
│   │   │   └── PostponeMissionRequest
│   │   └── response/
│   │       ├── MissionResponse
│   │       ├── MissionLogResponse
│   │       └── BadgeResponse
│   ├── service/
│   │   ├── MissionService
│   │   └── BadgeService           ← 서버 내부 이벤트 트리거 전용
│   ├── repository/
│   │   ├── MissionRepository
│   │   ├── MissionLogRepository
│   │   └── BadgeRepository
│   └── document/
│       ├── Mission
│       ├── MissionLog
│       └── Badge
│
├── risk/
│   ├── controller/
│   │   └── RiskController
│   ├── dto/
│   │   └── response/
│   │       ├── RiskScoreResponse
│   │       ├── RiskBreakdownResponse
│   │       └── RiskExplanationResponse
│   ├── service/
│   │   ├── RiskCalculatorService  ← 룰 기반 계산 + DB 저장
│   │   └── RiskQueryService       ← 저장값 조회, Explainability
│   ├── repository/
│   │   └── RiskScoreRepository
│   └── document/
│       └── RiskScore
│
└── notification/
    ├── controller/
    │   └── NotificationController
    ├── dto/
    │   ├── request/
    │   │   └── UpdateNotificationSettingRequest
    │   └── response/
    │       └── NotificationSettingResponse
    ├── service/
    │   ├── NotificationService
    │   └── FcmService             ← Firebase Admin SDK
    ├── repository/
    │   └── NotificationSettingRepository
    └── document/
        └── NotificationSetting
```

---

## 6. MongoDB 컬렉션 목록

| 컬렉션 | 주요 인덱스 | 비고 |
|---|---|---|
| `members` | `{socialProvider,socialId}` unique | |
| `consents` | `{memberId}` unique | |
| `logout_tokens` | `{token}` unique, TTL index | Redis 대체 |
| `families` | `{inviteCode}` unique | |
| `family_members` | `{familyId,memberId}` unique | |
| `health_profiles` | `{memberId}` unique | healthData 최근 30일치만 유지 |
| `missions` | `{familyId,status}`, `{endDate}` | |
| `mission_logs` | `{missionId,memberId}` | |
| `badges` | `{memberId,badgeType}` unique | 서버 내부 트리거 전용 |
| `risk_scores` | `{memberId,calculatedAt DESC}` | 이력 보존 (덮어쓰기 X) |
| `notification_settings` | `{memberId}` unique | |

---

## 7. API 엔드포인트 목록 (46개)

### 001 인증·JWT
| 메서드 | 엔드포인트 | 우선순위 |
|---|---|---|
| GET | /oauth/google/login | P0 |
| GET | /oauth/apple/login | P0 |
| GET | /oauth/google/callback | P0 |
| POST | /oauth/apple/callback | P0 |
| POST | /auth/terms | P0 |
| POST | /auth/refresh | P0 |
| POST | /auth/logout | P0 |

### 002 온보딩
| 메서드 | 엔드포인트 | 우선순위 |
|---|---|---|
| POST | /consents | P0 |
| PATCH | /members/{member_id} | P1 |
| POST | /families | P0 |
| POST | /families/{family_id}/invite | P0 |
| POST | /families/join | P0 |
| POST | /family-members | P0 |
| PATCH | /family-members/{family_member_id} | P1 |
| POST | /health-profiles | P0 |
| POST | /health-profiles/{id}/conditions | P0 |
| POST | /health-profiles/{id}/family-history | P0 |
| PATCH | /health-profiles/{id}/lifestyle | P0 |
| POST | /goals | P0 |
| POST | /risk-scores/calculate | P0 |
| GET | /missions/recommended | P0 |

### 003 홈 대시보드
| 메서드 | 엔드포인트 | 우선순위 |
|---|---|---|
| GET | /risk-scores | P0 |
| GET | /families/{family_id}/risk-summary | P0 |
| GET | /families/{family_id}/today-status | P0 |

### 004 챌린지·체크인
| 메서드 | 엔드포인트 | 우선순위 |
|---|---|---|
| GET | /missions | P0 |
| POST | /missions | P1 |
| POST | /mission-logs | P0 |
| PATCH | /missions/{mission_id}/postpone | P1 |

### 005 가족 관리
| 메서드 | 엔드포인트 | 우선순위 |
|---|---|---|
| GET | /families/{family_id}/overview | P0 |
| GET | /families/{family_id}/members | P0 |
| GET | /families/{family_id}/invite-code | P0 |
| PATCH | /privacy-settings/{member_id} | P0 |
| DELETE | /family-members/{member_id} | P1 |

### 006 마이·게임화
| 메서드 | 엔드포인트 | 우선순위 |
|---|---|---|
| GET | /members/{member_id}/risk-summary | P0 |
| GET | /members/{member_id}/gamification | P0 |
| GET | /badges | P1 |
| POST | /badges/award | P0 (서버 내부 전용) |
| GET | /families/{family_id}/ranking | P1 |
| GET | /members/{member_id}/streak-calendar | P1 |
| GET | /notification-settings/{member_id} | P1 |
| PATCH | /notification-settings/{member_id} | P1 |
| PATCH | /users/me/status | P2 |

### 007 리포트
| 메서드 | 엔드포인트 | 우선순위 |
|---|---|---|
| GET | /risk-scores/history | P0 |
| GET | /risk-scores/breakdown | P0 |
| GET | /risk-scores/explanation | P1 |
| POST | /health-data/sync | P1 |

---

## 8. 핵심 설계 결정사항

### 8.1 Redis 제외
- 로그아웃 토큰 블랙리스트 → `logout_tokens` MongoDB 컬렉션으로 대체
- `{token: String, expiresAt: ISODate}` + TTL 인덱스로 자동 만료 처리

```java
// LogoutToken.java
@Document(collection = "logout_tokens")
public class LogoutToken {
    @Id private ObjectId id;
    @Indexed(unique = true)
    private String token;
    private LocalDateTime expiresAt;  // TTL 인덱스
    @CreatedDate private LocalDateTime createdAt;
}
```

### 8.2 Apple OAuth 커스텀 구현
Spring Security OAuth2 Client 기본 미지원 → 직접 구현 필요

```
1. GET /oauth/apple/login  → Apple 인가 URL 리다이렉트
2. POST /oauth/apple/callback  → Apple id_token 수신
3. Apple JWKS endpoint에서 공개키 조회 (캐시 권장)
4. jjwt로 id_token 서명 검증
5. sub 필드 → socialId, email 추출
6. JWT(access+refresh) 발급
```

필요한 Apple Developer 정보:
- Team ID
- Key ID
- .p8 파일 (AuthKey)
- Service ID (redirect_uri 등록용)

### 8.3 배지 자동 부여 — 서버 내부 전용
`POST /badges/award`는 클라이언트 직접 호출 불가 (403)
`MissionService.checkIn()` 완료 후 `BadgeService.checkAndAward()` 내부 호출

### 8.4 리스크 점수 가중치
```
유전(genetic)     × 0.30
생활습관(lifestyle) × 0.25
행동(behavior)    × 0.20
환경(environment) × 0.15
임상(clinical)    × 0.10
```
MVP는 룰 기반(if-else), Phase 2에서 ML 모델 교체 예정

### 8.5 healthData 배열 관리
`health_profiles.healthData` 배열은 동기화 시 30일 이전 자동 제거
```java
this.healthData.removeIf(d -> d.getRecordedAt().isBefore(LocalDateTime.now().minusDays(30)));
```

---

## 9. 개발 우선순위

### P0 — MVP 필수 (먼저 구현)
1. `global` 패키지 전체 (JWT, Security, Exception, Response)
2. `auth` — Google OAuth + JWT 발급·갱신·로그아웃
3. `member` — 회원 기본 CRUD
4. `family` — 가족 그룹 생성·초대·합류·공유 설정
5. `health` — 건강 프로필·가족력·생활습관
6. `mission` — 챌린지 조회·체크인 (웨어러블·수동)
7. `risk` — 리스크 점수 계산·저장·조회

### P1 — MVP+1
- Apple OAuth 커스텀 구현
- 배지·랭킹·스트리크 캘린더
- Explainability API
- 웨어러블 동기화

### P2 — 이후
- 소프트 탈퇴 (`/users/me/status`)
- FCM Push 실제 연동

---

## 10. 환경변수 목록 (.env)

```bash
MONGODB_URI=mongodb://localhost:27017/golgolengi
JWT_SECRET=<PowerShell로 생성>
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
APPLE_TEAM_ID=
APPLE_KEY_ID=
# APPLE_PRIVATE_KEY_PATH=src/main/resources/AuthKey_XXXXXXXXXX.p8
# FIREBASE_CREDENTIAL_PATH=src/main/resources/firebase-service-account.json
```

JWT_SECRET 생성 (PowerShell):
```powershell
[Convert]::ToBase64String(
  (1..64 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]]
)
```

---

## 11. 다음 작업 지시

Claude Code에서 아래 순서로 진행해주세요.

```
1. Spring Initializr로 생성된 프로젝트에 위 build.gradle 내용 교체
2. src/main/resources/application.yml 생성 (위 내용 그대로)
3. 아래 패키지 구조 디렉터리 생성
4. global 패키지부터 구현 시작
   - ApiResponse.java
   - ErrorCode.java
   - CustomException.java
   - GlobalExceptionHandler.java
   - JwtProperties.java
   - JwtProvider.java
   - JwtAuthFilter.java
   - SecurityConfig.java
   - MongoConfig.java
5. auth 도메인 구현
   - LogoutToken.java (MongoDB TTL)
   - Consent.java
   - AuthService.java (Google OAuth 처리)
   - AuthController.java
6. member → family → health → mission → risk → notification 순서로 진행
```
