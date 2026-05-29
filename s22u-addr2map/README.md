# 주소→맵 (s22u-addr2map)

루팅 Galaxy S22U 개인용. 주소를 복사하거나 드래그 선택하면
티맵/카카오맵으로 바로 보내는 앱. (지침서 Part 7~8 네이티브 Kotlin 트랙)

## 동작
- A 경로(복사 자동): 접근성 서비스가 클립보드 변경 감지 → 주소면 팝업
- B 경로(선택 메뉴): 주소 드래그 → 메뉴 "주소→맵" → 팝업
- 팝업에서 [티맵]/[카카오] 선택 → 해당 앱이 주소 검색 상태로 열림
- 오버레이 권한 없으면 알림(2버튼)으로 폴백

## 빌드 (Myong PC, §77)
```
git checkout claude/mobile-updates-2uPOO
cd s22u-addr2map
cp keystore.properties.template keystore.properties   # 실제 값 입력
./gradlew test                 # AddressDetector 단위검증 (기기 불필요)
# Android Studio로 열어 빌드 → S22U 설치
./gradlew connectedDebugAndroidTest   # (있으면) 기기 검증
```
키스토어: shared-key.jks alias cleaner 재사용(§83), 신규 생성 금지.

## 최초 1회 설정 (집)
- 설정 > 접근성 > "주소→맵" 켜기  (또는 앱 내 "루트로 접근성 자동 켜기")
- 설정 > 다른 앱 위에 표시 > 허용  (또는 앱 내 "루트로 오버레이 자동 허용")
- 알림 권한 허용 (Android 13+)
- 티맵/카카오맵 설치

## 실기기 검증 필요 (spec Q3/Q4)
- 스킴: `tmap://search?name=` / `kakaomap://search?q=`
- 패키지: `com.skt.tmap.ku` / `net.daum.android.map`
- 틀리면 `core/MapLauncher.kt` 한 곳만 수정

## UPDATE_LOGS
(§90: 1빌드=1버전, 빌드 전까지 한 블록에 누적)

### v0.1.0 (개발 중, 미빌드)
- 신규: 주소 복사/선택 → 티맵·카카오 팝업 전송
- A=접근성 클립보드 감지, B=PROCESS_TEXT 선택메뉴 (병행)
- 주소 판별(숫자+동/로/길/번지 등), 같은주소 5초 중복무시
- 팝업 6초 자동닫힘, 오버레이 없으면 알림 폴백
- 메인: 권한 상태/루트 1탭 켜기/기본맵/샘플테스트
- 키스토어 shared-key.jks(cleaner) 재사용, Firebase 미사용
