# s22u-butler 설계 명세 (spec)

[버전] spec v0.1 (2026-05-15 작성, 명세 합의 대기)
[트랙] 네이티브 Kotlin (지침서 Part 7~8 / §107~§120)
[작업 모드] 정밀 모드 (§56) — 백그라운드·권한·루트 다중 결합, 실기기 재현 한정
[§73 권한] 지시와 지침서 충돌 시 .md 우선 + 즉시 보고. 분기점4(레포) 환경 불일치는 보고 완료.
[§80-A 룰] 본 명세 합의 후 코드 작성. 명세 없이 코드 먼저 금지.

---

## 0. 프로젝트 식별 정보 (§107 양식)

| 항목 | 값 | 확정 |
|---|---|---|
| 앱 폴더 | `s22u-butler/` (claude-code-test 레포 내, 분기점4 권장안) | 분기점4 답 대기 |
| 패키지명 (applicationId/namespace) | `com.myong.butler` | Myong 확정 대기 |
| 앱 라벨 (strings app_name) | "S22U 집사" (제안) | Myong 확정 대기 |
| 키스토어 | `E:\APK AAB\shared-key.jks` alias `cleaner` (§83 재사용, 신규 생성 금지) | 확정 |
| Firebase | 미사용 (1인 본인용, §112/§120 Auth·승인·디바이스잠금 전부 스킵) | 확정 |
| AGP / Gradle / KGP / JDK | 8.7.3 / 8.9 / 2.0.21 / 21 (§108 검증 매트릭스) | 확정 |
| compileSdk / minSdk / targetSdk | 36 / 24 / 36 (§108) | minSdk 확인 대기 |
| versionCode / versionName | 1 / 0.1 (테스트 버전) | 확정 |
| 빌드 주체 | Myong, Android Studio 직접 (§77/§117). Claude는 gradlew 실행 금지 | 확정 |

---

## 1. 목표

루팅된 Galaxy S22 Ultra 전용 개인 자동화 앱. 외부 자동화 앱(MacroDroid 등)
대체. 1차 테스트 버전 2개 기능:

- F1. 집 도착 감지 → WiFi 자동 ON
- F2. 특정 시각 도달 → 슈퍼캐쉬 최적화 자동 실행 (이번엔 스텁, 명령은 다음 세션 확정)

슈퍼캐쉬 통합은 차후. 지금은 파이프라인(트리거→리시버→실행→알림)을
end-to-end로 동작·검증 가능한 골격까지.

---

## 2. 입력

### F1 (집 도착 → WiFi)
- 집 좌표 lat, lng (설정 화면: "현재 위치 가져오기" 또는 수동 입력)
- geofence 반경 m (기본 150)
- F1 ON/OFF 토글
- 권한: `ACCESS_FINE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`(Android10+), 루트 su

### F2 (시각 → 최적화)
- 알람 시각 HH:MM
- 반복 정책: 매일 1회 (제안, 분기점)
- F2 ON/OFF 토글
- 권한: `SCHEDULE_EXACT_ALARM`/`USE_EXACT_ALARM`(Android12+), `RECEIVE_BOOT_COMPLETED`, `POST_NOTIFICATIONS`(13+), 루트 su

설정 저장: 일반 SharedPreferences (민감정보 없음 → EncryptedPrefs §113 불필요).

---

## 3. 출력

- F1: geofence ENTER → `SuRunner.runOneShot("svc wifi enable")` → WiFi OFF→ON.
  결과 알림(선택, 기본 무음 IMPORTANCE_LOW).
- F2: 알람 발화 → `NotificationHelper.showProgress` → `SuperCashOptimizer.run()`
  (현재 스텁, no-op) → 성공/에러 알림 갱신 (§115 E, 동일 NOTIFICATION_ID).
- 재부팅 후 BootReceiver가 geofence + alarm 재등록 (§115).

---

## 4. 제약

- 빌드는 Myong이 Android Studio 직접 (§77/§117). Claude `gradlew` 실행 금지.
- 클라우드 git 환경: 코드 작성/커밋만. 키스토어·google-services.json 없음(불필요).
  signingConfig는 `keystore.properties`(gitignore) 참조 — Myong PC에서 채움.
- su 호출: 1 세션 stdin 주입, `waitFor` 최후 1회 (§84). 각 명령 `2>/dev/null` (§114 stderr deadlock 방지).
- §85: 한국 삼성 펌웨어 = 재부팅 후 자동적용 신뢰도 낮음.
  → 부팅 후 30초 sleep + `goAsync()` (§115 B), 알림 + 수동 폴백 병행.
- 루트(Magisk) 필수. su 거부 시 graceful 처리(크래시 금지).
- 슈퍼캐쉬 실제 명령 미확정 → `SuperCashOptimizer`는 인터페이스 + 빈 `run()` (TODO).
  F2 파이프라인은 빈 명령으로 동작 검증 가능하게 설계.
- §118 robocopy 미적용(클라우드에 복사할 원본 네이티브 앱 없음) → §108 검증
  버전매트릭스로 신규 생성. zero-config 위험은 버전 고정으로 상쇄.

---

## 5. 성공 기준

1. Myong PC Android Studio 빌드 성공 (§117 디버깅 사이클 적용).
2. F1: 집 geofence ENTER 시 WiFi OFF→ON 실기기 실측.
3. F2: 설정 시각 도달 → AlarmReceiver 발화 + 진행/완료 알림 표시
   (스텁이라 명령 no-op, 파이프라인 동작이 합격선).
4. 재부팅 후 geofence·alarm 자동 재등록 확인.
5. 설정값(집 좌표/반경/시각/토글) 앱 재시작 후 영속.
6. 권한 거부·su 거부 등 실패케이스에서 크래시 없이 안내.

---

## 6. 실패 케이스 처리

| 케이스 | 처리 |
|---|---|
| su 거부 (루트 없음/거부) | 알림 "루트 권한 거부", 작업 스킵, 크래시 X |
| 위치 권한 거부 | 설정 화면 배너 안내, geofence 미등록 |
| 백그라운드 위치 미허용(10+) | "항상 허용 필요" 안내, geofence 부정확 명시 |
| POST_NOTIFICATIONS 거부(13+) | SecurityException 흡수, 작업은 진행 (§115 D) |
| Doze/절전 | setExactAndAllowWhileIdle 사용, 그래도 지연 가능 명시 |
| OneUI 자동시작 차단 | 첫 실행 시 자동시작 허용 안내 1회 (§115 D) |
| geofence 미발화(GPS off 등) | 한계 명시. 루트라도 위치는 OS 의존, 폴백 없음 |
| SCHEDULE_EXACT_ALARM 미허용(12+) | 설정 유도 인텐트, 미허용 시 inexact 폴백 + 안내 |

---

## 7. 롤백

- 신규 단독 앱 → 기존 시스템/본업(콜잡기) 영향 0. 앱 삭제 = 완전 원복.
- WiFi enable은 비파괴(끄지 않음). 최적화는 현재 스텁이라 무해.
- git: 기능별 커밋 분리. 문제 시 해당 커밋 revert.
- §80-A 룰3: spec.md·지침 .md 백업 파일 생성 금지. 코드 안전망은 git 이력.

---

## 8. 검증

### 정적 (Claude, 커밋 전)
- AndroidManifest: 권한·receiver·exported 선언, 패키지 일치
- 잔재 grep: 임시/플레이스홀더 식별자 0건
- 버전매트릭스(§108) 일치: AGP 8.7.3 / Gradle 8.9 / Kotlin 2.0.21 / jvmTarget 11 통일(§108 주의4)
- gradle.properties §111 필수설정 포함

### 동적 (Myong, 실기기 S22U)
- §117 빌드 디버깅 사이클(한 번에 하나, 에러 메시지 우선)
- F1/F2 시나리오 표 + 재부팅 재등록 + 권한 거부 경로

### 보고 형식
- 폰 가독성 분할(지시 #8). 파일 트리 → 핵심 파일 → 검증 체크리스트 순.
- 단계별 커밋 + 한 줄 요약.

---

## 9. 구현 순서 (지시 #2, 명세 합의 후 착수)

1. Gradle(루트/app .kts) + settings + libs.versions.toml + gradle.properties + Manifest + 권한
2. SuRunner (§114: runOneShot/runMultipleSerially/runWithProgress/reboot)
3. NotificationHelper (§115 E)
4. PrefsStore (집좌표/반경/시각/토글 — 일반 SharedPreferences)
5. SuperCashOptimizer (인터페이스 + 빈 run(), TODO 마커)
6. GeofenceManager + GeofenceReceiver (ENTER → svc wifi enable)
7. AlarmScheduler + AlarmReceiver (시각 → SuperCashOptimizer.run())
8. BootReceiver (§115: BOOT_COMPLETED + goAsync, geofence·alarm 재등록)
9. MainActivity + 설정 화면 (Material3 카드 + 하단 탭 §116, 색상 §116 E)
10. keystore.properties 템플릿 + .gitignore + README

---

## 10. 결정 분기점 (명세 확정 시 Myong 답, §80-A 룰2 추천 포함)

| # | 항목 | 추천 | 이유 |
|---|---|---|---|
| D1 | 패키지명 `com.myong.butler` | 그대로 | §107/§118 com.myong.* 관례 일치 |
| D2 | 앱 한글명 | "S22U 집사" | butler 직역, s22u-zombie와 구분 |
| D3 | F1 WiFi 동작 | ENTER만 ON, EXIT 무동작 | 본업 안전(§87 원칙), 집 나갈 때 WiFi 끄면 데이터 누락 위험 |
| D4 | F2 반복 | 매일 1회 고정 | 단순. 요일 선택은 다음 버전 |
| D5 | minSdk | 24 유지 | §108 표준, S22U는 충분 상회 |
| D6 | 분기점4 레포 | 이 레포 `s22u-butler/` 폴더 | 환경 제약(별도 레포 불가), 폰 작업 가능, 추후 분리 |

---

## 11. 미해결/차기 세션

- 슈퍼캐쉬 실제 su 최적화 명령 세트 (집 PC 소스). 확정 시 SuperCashOptimizer.run() 채움.
- 슈퍼캐쉬 앱 통합 여부 (현재 미통합).
- F1/F2 외 추가 자동화 트리거 (차기).
