# s22u-auto 설계 명세 (spec)

[버전] spec v0.3 (2026-05-15, R1~R6 확정 — 명세 OK, 구현 착수)
[앱] "자동화" — 범용 안드로이드 자동화 앱 (MacroDroid 미사용 자체완결, 좀비폰 §3-A~3-D 원칙)
[트랙] 네이티브 Kotlin (지침서 Part 7~8 / §107~§120)
[작업 모드] 정밀 모드 (§56)
[§73 권한] 지침서 충돌 시 .md 우선 + 즉시 보고.
[§80-A 룰] 명세 합의 후 코드. 룰3: .md .backup 별도 파일 금지.
[§90] APP_VERSION v0.1.0 / versionCode 1. 빌드 직전까지 변경 금지.

---

## 0. 프로젝트 식별 정보 (§107 양식)

| 항목 | 값 |
|---|---|
| 레포 폴더 | `s22u-auto/` (claude-code-test 내, 집 PC에서 추후 분리) — D8 |
| 패키지명 | `com.myong.auto` — D2 |
| 앱 라벨 | "자동화" — D3 |
| APP_VERSION / versionCode / versionName | v0.1.0 / 1 / 0.1.0 — D? §90 |
| 키스토어 | `shared-key.jks` alias `cleaner` (§83 재사용, 신규 금지) |
| Firebase | 전부 미사용 |
| AGP/Gradle/KGP/JDK | 8.7.3 / 8.9 / 2.0.21 / 21 (§108) |
| compileSdk/minSdk/targetSdk | 36 / 24 / 36 (§108, minSdk24 — D7) |
| 빌드 주체 | Myong, Android Studio 직접 (§77/§117). Claude gradlew 금지 |

---

## 1. 목표

사용자가 **규칙(Rule = Trigger + Action)** 을 만들어 폰을 자동화하는
범용 앱. 특정 기능 박지 않음. 슈퍼캐쉬 명령은 더 이상 블로커 아님 —
"su 명령 실행" 액션의 입력값으로 처리(명령 확정 시 룰 1개 추가로 끝).

v0.1.0 = 최소 동작 버전. Rule:Trigger:Action = 1:1:1 (체인/AND·OR 없음).
- Trigger 2종: 시간(AlarmManager), 지오펜스(GeofencingClient) — D4
- Action 3종: WiFi 토글(su), su 명령 실행, 알림 표시 — D5

---

## 2. 입력

### 공통 (룰)
- 규칙명 (문자열), 활성/비활성 토글

### Trigger
- TIME: 시각 HH:MM, 반복(매일 / 1회 — 추천: 매일, 1회 옵션)
- GEOFENCE: lat, lng, 반경 m(기본 150), transition(ENTER / EXIT — 추천 기본 ENTER, 선택형)

### Action
- WIFI: 모드(ON / OFF). **TOGGLE 제외(R3, 본업 안전 §87)** — 트리거 시점
  폰 상태 변동 시 의도 반대 동작 위험(콜 끊김 방지). v0.2.0+ 재검토.
- RUN_SU: 단일 명령(긴 문자열 OK, `&&` 연결 허용). 멀티라인 입력 필드,
  1줄 강제 변환 X. v0.1.0 SuRunner.runOneShot.
- NOTIFY: 제목, 본문

### 권한
`ACCESS_FINE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`(10+),
`RECEIVE_BOOT_COMPLETED`, `SCHEDULE_EXACT_ALARM`/`USE_EXACT_ALARM`(12+),
`POST_NOTIFICATIONS`(13+), 루트 su. 의존성: `play-services-location`.

---

## 3. 출력

- 룰 저장 → `RuleEngine.register(rule)`
  - TIME: `AlarmManager.setExactAndAllowWhileIdle`
  - GEOFENCE: `GeofencingClient.addGeofences`
- 트리거 발화 → Receiver → `RuleEngine.execute(rule)` → Action 실행
  - WIFI_TOGGLE → `SuRunner.runOneShot("svc wifi enable|disable")`
  - RUN_SU → `SuRunner.runOneShot(cmd)`
  - SHOW_NOTIFICATION → `NotificationHelper`
- BOOT_COMPLETED → 활성 룰 전부 재등록 (§115 goAsync + 30s sleep)
- 실행 결과는 알림(무음 IMPORTANCE_LOW)으로 가시화(선택)

---

## 4. 제약

- 빌드 Myong 직접 (§77/§117). Claude gradlew 실행 금지.
- 클라우드 git = 코드 작성/커밋만. 키스토어·google-services 없음.
  signingConfig는 `keystore.properties`(gitignore) 참조, Myong PC에서 채움.
- su: 1세션 stdin 주입, waitFor 최후 1회, 명령에 `2>/dev/null` (§84/§114).
- §85: 한국 삼성 펌웨어 재부팅 후 자동적용 신뢰도 낮음 →
  BootReceiver goAsync + 30s sleep + 알림 (§115 B/E).
- 루트(Magisk) 필수. su 거부 시 graceful(크래시 금지).
- v0.1.0 Rule:Trigger:Action 1:1:1. 다중 트리거/체인은 차기.
- GeofencingClient = Google Play Services 의존(가정: S22U 기본 탑재.
  지침 선례 없음 — §73로 명시). 미탑재/구버전 시 graceful 에러.
- §118 robocopy 미적용(복사할 원본 없음) → §108 버전매트릭스 신규 생성.

---

## 5. 성공 기준

1. Myong PC Android Studio 빌드 성공 (§117 사이클).
2. TIME 룰: 설정 시각 도달 → Action 실행 실측.
3. GEOFENCE 룰: 반경 ENTER → Action 실행 실측.
4. WIFI_TOGGLE: su로 WiFi ON/OFF 실측.
5. RUN_SU: 입력 명령 실행 + 결과 알림.
6. SHOW_NOTIFICATION: 알림 표시.
7. 재부팅 후 활성 룰 자동 재등록.
8. 룰 Room 영속 + 목록 활성/비활성 토글 동작.
9. 권한·su 거부 시 크래시 없이 안내.

---

## 6. 실패 케이스 처리

| 케이스 | 처리 |
|---|---|
| su 거부 | 알림 "루트 권한 거부", 스킵, 크래시 X |
| 위치 권한 거부 | 설정 배너, 지오펜스 룰 등록 보류 |
| 백그라운드 위치 미허용(10+) | "항상 허용" 안내, 지오펜스 부정확 명시 |
| POST_NOTIFICATIONS 거부(13+) | SecurityException 흡수, 작업 진행 (§115 D) |
| SCHEDULE_EXACT_ALARM 미허용(12+) | 설정 유도 인텐트, 미허용 시 inexact 폴백+안내 |
| Doze/절전 | setExactAndAllowWhileIdle, 그래도 지연 가능 명시 |
| OneUI 자동시작 차단 | 첫 실행 시 안내 1회 (§115 D) |
| 지오펜스 미발화(GPS off) | 한계 명시, 폴백 없음 |
| Play Services 부재/구버전 | 지오펜스 룰 생성 시 graceful 에러 안내 |

---

## 7. 롤백

- 신규 단독 앱 → 시스템/본업 영향 0. 앱 삭제 = 완전 원복.
- WIFI_TOGGLE OFF·RUN_SU만 잠재 영향 → 사용자가 룰 정의(책임 명확).
- git 기능별 커밋 분리, 문제 시 revert.
- §80-A 룰3: spec.md·지침 .md .backup 금지. 안전망 = git 이력.

---

## 8. 검증

### 정적 (Claude, 커밋 전)
- Manifest 권한·receiver·exported 선언, 패키지 일치
- §108 매트릭스 일치(AGP8.7.3/Gradle8.9/Kotlin2.0.21/jvmTarget 11 통일 §108-주의4)
- gradle.properties §111 필수설정
- Room 스키마 export, 잔재 grep 0건

### 동적 (Myong 실기기 S22U)
- §117 빌드 디버깅 사이클(한 번에 하나, 에러 메시지 우선)
- TIME/GEOFENCE × WIFI/RUN_SU/NOTI 매트릭스 + 재부팅 재등록 + 권한거부

### 보고
- 폰 가독성, 단계마다 멈춰 보고. 파일경로+핵심 변경점. 빌드명령 별도 코드블럭.

---

## 9. 구조 (간소화 Clean Architecture)

```
com.myong.auto
├ data/      Room(RuleDao, RuleEntity, TriggerEntity, ActionEntity), PrefsStore
├ domain/    Rule/Trigger/Action 모델 + RuleEngine(register/execute)
├ ui/        MainActivity, RuleEditActivity, SettingsActivity, ViewModels
└ service/   SuRunner(§114), NotificationHelper, BootReceiver(§115),
             GeofenceReceiver, AlarmReceiver
```

DB(D6): RuleEntity(id,name,enabled,createdAt) + TriggerEntity(ruleId FK,
type,paramsJson) + ActionEntity(ruleId FK,type,paramsJson). 1:1:1.
파라미터 = JSON TEXT 컬럼. 직렬화 = kotlinx.serialization.
DB↔domain 변환은 Repository에서.

domain sealed class (R5 확정):
```
TriggerParams.Time(hour, minute, repeat: Daily | Once(dateTimeMillis))
TriggerParams.Geofence(lat, lon, radiusM, transition: Enter|Exit|Both)  // 기본 Enter
ActionParams.Wifi(state: On | Off)        // TOGGLE 없음
ActionParams.RunSu(command: String)
ActionParams.Notify(title, message)
```

알림 정책 (R6 확정): 성공·실패 모두 IMPORTANCE_LOW(무음, 본업 중
소리/진동 금지 우선). 성패는 아이콘/이모지(✅/⚠️)로 구분. 전역 토글
1개 "실행 결과 알림 표시"(SettingsActivity). 룰 단위 토글 v0.2.0+.

---

## 10. 구현 순서 (명세 합의 후, 단계마다 멈춤·보고)

1. spec.md (본 문서)  ← 현재
2. Gradle(루트/app .kts)+settings+libs.versions.toml+gradle.properties+Manifest+권한
3. Room 스키마 (Rule/Trigger/Action Entity+Dao+DB)
4. domain 모델 + RuleEngine 스켈레톤
5. SuRunner (§114)
6. NotificationHelper (§115 E)
7. AlarmReceiver + 시간 트리거 등록
8. GeofenceReceiver + 지오펜스 트리거 등록
9. BootReceiver (§115, 룰 재등록)
10. 액션 실행 함수 (WiFi/su/알림)
11. MainActivity (룰 목록)
12. RuleEditActivity (룰 편집 플로우)
13. SettingsActivity (권한/루트 테스트)
14. keystore.properties + .gitignore
15. README.md + UPDATE_LOGS (한 블록 누적)

---

## 11. 결정 확정 (R1~R6, 명세 OK)

| # | 확정 |
|---|---|
| R1 | TIME 반복 = 매일반복 / 1회만(특정 일시) 2모드, UI 선택 |
| R2 | GEOFENCE = ENTER/EXIT/둘다 선택형, 기본 ENTER |
| R3 | WIFI = ON/OFF 2개만. TOGGLE 제외(§87 본업 안전). v0.2.0+ 재검토 |
| R4 | RUN_SU = 단일 명령(긴 문자열·`&&` OK), 멀티라인 입력, 강제변환 X |
| R5 | 파라미터 = JSON TEXT + kotlinx.serialization, domain sealed class(§9) |
| R6 | 결과 알림 = 성공·실패 모두 무음(IMPORTANCE_LOW), ✅/⚠️ 구분, 전역토글1 |

D1~D8 + R1~R6 전부 확정. 본 명세로 구현 진행(§10 순서).
