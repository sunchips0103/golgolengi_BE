# 온보딩 분기 처리 구현안 (수정)

## 문제 요약

`TokenResponse`에 `isNewMember` 하나만 존재해 온보딩 완료 여부를 구분할 수 없음.
기존 플랜은 가족 생성/합류 시 `onboardingCompleted = true`로 설정했으나 —
가족만 만들고 나간 뒤 재진입하면 온보딩이 스킵되는 버그 존재.

**핵심 수정**: `onboardingCompleted`는 가족 생성 시점이 아니라,
프론트가 온보딩의 **마지막 단계를 완료했을 때** 명시적으로 API를 호출해 `true`로 전환.

---

## 상태 분기표

| 상황 | `isNewMember` | `onboardingCompleted` | `familyId` | 이동 |
|---|---|---|---|---|
| 최초 가입 | `true` | `false` | `null` | 온보딩 처음부터 |
| 가입 후 중단 (가족 없음) | `false` | `false` | `null` | 온보딩 재개 |
| 가족 생성했지만 온보딩 미완료 | `false` | `false` | `"abc..."` | 온보딩 재개 |
| 온보딩 전 과정 완료 | `false` | `true` | `"abc..."` | 메인 |

> `familyId` 존재 ≠ `onboardingCompleted = true`. 두 값은 독립적.

---

## 변경 파일 목록

### 1. `Member.java` — 필드 추가
```java
private boolean onboardingCompleted;  // 프론트가 완료 API 호출 시 true
```
`@Builder` 기본값 `false`.

### 2. `TokenResponse.java` — 필드 추가
```java
private boolean onboardingCompleted;
```

### 3. `AuthService.findOrCreateAndIssue()` — 응답에 포함
```java
return TokenResponse.builder()
    .accessToken(...)
    .refreshToken(...)
    .isNewMember(isNew)
    .onboardingCompleted(member.isOnboardingCompleted())
    .build();
```

### 4. `MemberController` + `MemberService` — 두 엔드포인트 신설

#### GET /auth/me
앱 재실행 시 프론트가 라우팅 결정에 사용.
```json
{
  "memberId": "664abc...",
  "name": "홍길동",
  "email": "hong@gmail.com",
  "profileImageUrl": "https://...",
  "onboardingCompleted": false,
  "familyId": "665def..."   // family 도메인 미구현 시 null 고정
}
```

#### PATCH /member/onboarding
프론트가 온보딩 마지막 화면에서 "완료" 처리 시 1회 호출.
```
PATCH /member/onboarding
Authorization: Bearer {accessToken}
→ 204 No Content
```
- 서버는 해당 Member의 `onboardingCompleted = true`로 업데이트.
- **이 API 외의 경로로 `onboardingCompleted`를 변경해서는 안 됨.**

---

## onboardingCompleted 진입점

| 시점 | 처리 |
|---|---|
| `PATCH /member/onboarding` 호출 | `true`로 변경 (유일한 진입점) |
| 가족 생성/합류 완료 | **변경 없음** — 가족 생성은 온보딩 과정 중 하나일 뿐 |
| 로그인 / 토큰 갱신 | 현재 값 그대로 읽어서 응답 |

---

## API 흐름

### 앱 최초 실행 (로그인)
```
GET /oauth/google/login  or  POST /oauth/apple/callback
→ TokenResponse { accessToken, refreshToken, isNewMember, onboardingCompleted }

onboardingCompleted == false  →  온보딩 화면
onboardingCompleted == true   →  메인 화면
```

### 앱 재실행 (토큰 보유 시)
```
GET /auth/me
→ { onboardingCompleted, familyId, ... }

onboardingCompleted == false  →  온보딩 재개 (어느 단계부터인지는 프론트가 판단)
onboardingCompleted == true   →  메인 화면
```

### 온보딩 완료 처리
```
프론트가 온보딩 마지막 단계 확인 후:
PATCH /member/onboarding
→ 204
→ 이후 GET /auth/me 또는 다음 로그인 시 onboardingCompleted == true
```

---

## 구현 순서

1. `Member.java` — `onboardingCompleted` 필드 추가
2. `TokenResponse.java` — `onboardingCompleted` 필드 추가
3. `AuthService.java` — `buildTokenResponse()`에서 `member.isOnboardingCompleted()` 포함
4. `MemberService.java` — `getMe()`, `completeOnboarding()` 구현
5. `MemberController.java` — `GET /auth/me`, `PATCH /member/onboarding` 추가
6. `SecurityConfig.java` — `/auth/me` 인증 필요 경로 확인 (현재 `.anyRequest().authenticated()`로 커버됨)

---

## 주의사항

- `familyId`는 family 도메인 구현 전까지 `/auth/me`에서 `null` 고정 응답.
  family 도메인 구현 후 `family_members` 컬렉션 조회로 교체.
- `onboardingCompleted` 변경 진입점은 `PATCH /member/onboarding` 단 하나.
  FamilyService·HealthService 등 다른 도메인에서 건드리지 않는다.