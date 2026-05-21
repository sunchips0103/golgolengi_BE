# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

**서비스명**: 골골방지 (Family Health OS)
**패키지**: `com.golgolengi`
**목적**: 가족을 하나의 건강 운영 단위로 관리하는 AI 기반 예방 헬스케어 백엔드
**기술 스택**: Java 21 + Spring Boot 3.3.5, MongoDB 7.x, JWT (JJWT 0.12.6), Firebase Admin SDK (FCM)

## Commands

```powershell
gradlew build           # 빌드
gradlew bootRun         # 서버 실행
gradlew test            # 전체 테스트
gradlew test --tests "com.golgolengi.SomeTest"   # 단일 테스트 클래스
gradlew clean build     # 클린 빌드
```

환경변수 필요: `MONGODB_URI`, `JWT_SECRET`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
(미설정 시 `bootRun` 실패 — `.env` 파일 또는 IntelliJ Run Configuration에 등록)

JWT_SECRET 생성 (PowerShell):
```powershell
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])
```

## 아키텍처

### 계층 구조 (도메인별 패키지)
```
com.golgolengi/
├── global/          ← 전역 (JWT, Security, Exception, ApiResponse)
├── auth/            ← Google/Apple OAuth + JWT 발급·갱신·로그아웃
├── member/          ← 회원 기본 정보
├── family/          ← 가족 그룹 생성·초대·합류·공개범위
├── health/          ← 건강 프로필·가족력·생활습관
├── mission/         ← 챌린지 조회·체크인·배지
├── risk/            ← 리스크 점수 계산·저장·조회
└── notification/    ← FCM Push 설정
```
각 도메인 내부는 `controller / service / repository / domain / dto(request, response)` 5-계층.

### 핵심 설계 결정사항

**Redis 없음** — 로그아웃 토큰 블랙리스트를 `logout_tokens` MongoDB 컬렉션 + TTL 인덱스로 대체.

**Apple OAuth 커스텀 구현** — Spring Security OAuth2 Client 기본 미지원. 흐름:
1. GET `/oauth/apple/login` → Apple 인가 URL 리다이렉트
2. POST `/oauth/apple/callback` → id_token 수신 → Apple JWKS로 서명 검증 → JWT 발급

**배지 자동 부여** — `POST /badges/award`는 클라이언트 직접 호출 불가(403). `MissionService.checkIn()` 완료 후 `BadgeService.checkAndAward()` 내부 호출만 허용.

**리스크 점수 가중치** (MVP는 룰 기반, Phase 2에서 ML 교체 예정):
```
유전 0.30 / 생활습관 0.25 / 행동 0.20 / 환경 0.15 / 임상 0.10
```

**healthData 배열** — 동기화 시 30일 이전 자동 제거:
```java
healthData.removeIf(d -> d.getRecordedAt().isBefore(LocalDateTime.now().minusDays(30)));
```

### MongoDB 컬렉션 및 인덱스

| 컬렉션 | 주요 인덱스 |
|---|---|
| `members` | `{socialProvider, socialId}` unique |
| `logout_tokens` | `{token}` unique + TTL |
| `families` | `{inviteCode}` unique |
| `family_members` | `{familyId, memberId}` unique |
| `health_profiles` | `{memberId}` unique |
| `risk_scores` | `{memberId, calculatedAt DESC}` (이력 보존, 덮어쓰기 X) |

### API 우선순위

**P0 (MVP 필수)**: `global` → `auth` → `member` → `family` → `health` → `mission` → `risk`
**P1**: Apple OAuth, 배지·랭킹·스트리크, Explainability
**P2**: 소프트 탈퇴, FCM 실제 연동

전체 46개 엔드포인트 목록 및 상세 설계: `HANDOFF_골골방지_백엔드.md` 참고