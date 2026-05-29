# s22u-addr2map 설계 명세 (spec)

[버전] spec v0.1 (2026-05-15, 명세 합의 대기)
[앱] "주소→맵" — 주소 복사/선택 시 티맵·카카오맵으로 바로 보내는 개인 앱
[트랙] 네이티브 Kotlin (지침서 Part 7~8 / §107~§120), 개인 루팅폰(S22U)
[작업 모드] 정밀 모드 (§56)
[§73] 지침/현실 충돌 시 .md·사실 우선 + 즉시 보고
[§80-A] 명세 합의 후 코드. .md .backup 금지
[§90] APP_VERSION v0.1.0 / versionCode 1, 빌드 직전까지 변경 금지

---

## 0. 프로젝트 식별 (§107 양식)

| 항목 | 값 |
|---|---|
| 폴더 | `s22u-addr2map/` (claude-code-test 내) |
| 패키지 | `com.myong.addr2map` |
| 앱 라벨 | "주소→맵" |
| 버전 | v0.1.0 / versionCode 1 / versionName 0.1.0 |
| 키스토어 | `shared-key.jks` alias `cleaner` (§83 재사용, 신규 금지) |
| Firebase | 미사용 |
| AGP/Gradle/KGP/JDK | 8.7.3 / 8.9 / 2.0.21 / 21 (§108) |
| compile/min/target | 36 / 24 / 36 |
| 빌드 주체 | Myong, Android Studio 직접 (§77/§117) |

---

## 1. 목표

루팅 S22U에서 **주소를 복사**하면 작은 팝업이 떠서 **[티맵][카카오]**
중 골라 그 앱을 주소 검색 상태로 연다. 복사 자동(A)이 메인,
주소 드래그→선택메뉴 항목(B)이 보험.

확정 결정:
- 방식 = **A + B** (A=접근성 복사감지, B=PROCESS_TEXT 선택메뉴)
- 복사 후 = **작은 팝업 [티맵][카카오] 2버튼** (티맵=기본/왼쪽)
- 주소 판별 = **켬** (숫자 + 동/로/길/번지/구/동 등 패턴일 때만)
- 기본 맵 = **티맵**

---

## 2. 입력

- 클립보드 텍스트 (A: 접근성 서비스의 ClipboardManager 리스너)
- 선택 텍스트 (B: ACTION_PROCESS_TEXT 인텐트 EXTRA)
- 설정: 기본 맵(티맵/카카오), 주소판별 on/off, 팝업 자동닫힘 초

---

## 3. 출력

- 주소로 판별되면 오버레이 팝업 표시 → 버튼 탭 시:
  - 티맵: `tmap://search?name=<urlenc>`
  - 카카오: `kakaomap://search?q=<urlenc>`
- 미설치 시: 토스트 + 웹지도/플레이스토어 폴백
- 오버레이 권한 없으면: 알림(2액션 버튼)으로 대체

---

## 4. 제약 / 핵심 난점 (솔직히)

- **Android 12+(S22U)는 백그라운드 클립보드 읽기 차단.** 그래서
  순수 서비스 폴링 불가 → **접근성 서비스**의 ClipboardManager
  리스너로 우회(A). 일부 One UI 버전은 그것마저 막을 수 있어
  **B(PROCESS_TEXT)를 항상 병행**(절대 안 깨지는 경로).
- 백그라운드 Activity 시작 제한 → 팝업은 Activity 말고
  **오버레이(SYSTEM_ALERT_WINDOW)** 사용. 없으면 알림 폴백.
- 맵 스킴(`tmap://search?name=`, `kakaomap://search?q=`)은
  앱 버전 따라 바뀔 수 있음 → **Myong 실기기 검증 필요**(§73 명시).
- Android 11+ 패키지 가시성 → Manifest `<queries>` 등록 필수.
- 접근성/오버레이 권한은 사용자 1회 허용. 루트로 자동 부여·유지
  가능(`settings put secure enabled_accessibility_services`,
  `appops set <pkg> SYSTEM_ALERT_WINDOW allow`) — SuRunner(§114).
- 빌드 Myong 직접(§77). 클라우드 git=코드/커밋만.

---

## 5. 성공 기준

1. Myong PC 빌드 성공 (§117).
2. 한글 주소 복사 → 팝업 → [티맵] 탭 → 티맵이 그 주소 검색 상태로 열림.
3. [카카오] 탭 → 카카오맵 동일.
4. 주소 드래그 → 선택메뉴 "주소→맵" → 동일 동작(B).
5. 주소 아닌 텍스트 복사 → 팝업 안 뜸(판별).
6. 티맵/카카오 미설치 → 크래시 없이 폴백.
7. 접근성 꺼짐 → 메인 화면이 상태 안내 + 1탭 켜기(루트 시 자동).

---

## 6. 실패 케이스

| 케이스 | 처리 |
|---|---|
| 접근성 OFF | 메인에 안내 + 켜기 인텐트(루트면 su 자동 ON) |
| 오버레이 권한 없음 | 알림(2액션)으로 팝업 대체 |
| 클립보드 여전히 차단(특정 ROM) | B 경로로 사용 가능(병행 이유) |
| 티맵/카카오 미설치 | 토스트 + 웹지도/플레이스토어 |
| 스킴 변경됨 | Myong 검증 후 스킴 상수 1곳 수정 |
| 비주소 복사 | AddressDetector가 거름 |
| POST_NOTIFICATIONS 거부(13+) | SecurityException 흡수, 동작 유지 |

---

## 7. 롤백

- 단독 앱, 삭제=완전 원복.
- su 변경(접근성/overlay appop)은 가역 + 문서화. 시스템 영구변경 없음.
- git 단계별 커밋, 문제 시 revert. §80-A: .md .backup 금지.

---

## 8. 검증

- 정적(Claude): Manifest 권한·queries·service 선언, §108 매트릭스,
  AddressDetector 순수 JVM 단위테스트(이번엔 환경서 실행 가능).
- 동적(Myong S22U): 스킴 실작동, 접근성/오버레이 권한, 팝업,
  복사/선택 양 경로, 미설치 폴백.
- 보고: 폰 가독성, 단계마다 멈춤, 복사용 코드블록 1개 첨부.

---

## 9. 구성

```
com.myong.addr2map
├ service/  AddrAccessibilityService (클립보드 리스너) + xml config
├ ui/       ChooserOverlay (오버레이 팝업 2버튼) / fallback 알림
│           ProcessTextActivity (B 경로 진입)
│           MainActivity (상태·권한·기본맵·테스트)
├ core/     AddressDetector(주소 판별)  MapLauncher(스킴+폴백)
└ service/  SuRunner (§114 재사용, 접근성/overlay 루트 보조)
```

---

## 10. 구현 순서 (명세 합의 후, 단계마다 멈춤·보고)

1. spec.md (현재)
2. Gradle + Manifest + 권한 + `<queries>` (§108/§111)
3. AddressDetector + JVM 단위테스트
4. MapLauncher (티맵/카카오 스킴 + 미설치 폴백)
5. SuRunner (§114 재사용)
6. ChooserOverlay (오버레이 2버튼) + 알림 폴백
7. ProcessTextActivity (B 경로)
8. AddrAccessibilityService + accessibility config xml (A 경로)
9. MainActivity (상태/권한 안내/기본맵 토글/루트 1탭/샘플 테스트)
10. README + UPDATE_LOGS

---

## 11. 잔여 결정 (추천 포함, §80-A 룰2)

| # | 항목 | 추천 | 비고 |
|---|---|---|---|
| Q1 | 팝업 자동닫힘 | 6초 후 자동 닫힘 | 본업 방해 최소 |
| Q2 | 같은 주소 연속복사 | 5초 내 중복 무시 | 팝업 도배 방지 |
| Q3 | 카카오 스킴 | `kakaomap://search?q=` | route는 좌표 필요→차기 |
| Q4 | 티맵 패키지 | `com.skt.tmap.ku` 가정 | Myong 기기서 확인 |
| Q5 | 앱 라벨 | "주소→맵" | 변경 가능 |

D(방식/팝업/판별/기본맵) 확정 완료. Q1~Q5만 답 주시면 반영.

---

## 12. 차기 작업 메모 (집에서)

- 앱 내 "주소 직접 입력"(붙여넣기→티맵/카카오) 추가 완료.
- 카카오대리 콜화면 도착지 추출 → **헤드업 알림(옵션1)** 스캐폴드 완료.
  자동 팝업 X, 헤드업 알림 띠 뜨면 사용자가 [티맵]/[카카오] 탭.
- 띠 위치: 옵션1(헤드업 알림) = 화면 상단이라 슈퍼멀티 띠와 충돌 없음(과거 메모 폐기).

## 13. 카카오대리 스캐폴드 (밖에서 완료, v0.1.0)

지침 §149/§150 박제 패턴 적용:

- `core/KakaoExtractor.kt`
  - `PKG = "com.kakao.driver"` (집 정찰로 실제값 교체 — TODO)
  - `dumpNode` 안전 패턴 (§149.2 MAX_DEPTH 20 / MAX_COUNT 100)
  - `handle()` 진입 가드 (§149.9: debounce 1.5s / doneLock 8s)
  - `findDestination(root)` = **TODO (집 클로드 코드가 정찰결과 박음)**
- `service/AddrAccessibilityService.kt` `onAccessibilityEvent`
  - `PrefsStore.isKakaoOn` 체크 → packageName == PKG 만 처리 (§149.6)
  - TYPE_WINDOW_STATE_CHANGED / TYPE_WINDOW_CONTENT_CHANGED
  - 도착지 잡히면 `NotificationHelper.showChooser` (헤드업 IMPORTANCE_HIGH, 무음)
- `res/xml/accessibility_service_config.xml`
  - canRetrieveWindowContent=true, flagRetrieveInteractiveWindows|flagReportViewIds
- `service/A11yAuto.kt` + `service/BootReceiver.kt` (§85/§115 보강)
  - 부팅 30s 후 + onResume시 자동 접근성 ON (루트, PrefsStore.isAutoA11y)
- 옵션 탭 새 카드 "카카오대리": 자동감지 / 정찰모드 / 부팅 자동 ON

### 집 작업 (Claude Code, 실기기 + 콜 1번)
1. 카카오대리 켜고 `dumpsys window | grep mCurrentFocus` → `PKG` 실제값 확정 → `KakaoExtractor.PKG` 교체
2. 옵션탭 "정찰 모드" ON
3. 콜 1번 잡아 도착지 화면에서 `adb logcat -s KakaoRecon:V` 로 노드 확인
4. 도착지 노드 식별(viewId/text 라벨) → `KakaoExtractor.findDestination()` 채움
5. 정찰 모드 OFF + 실콜 검증
