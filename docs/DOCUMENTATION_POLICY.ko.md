# Android 문서 정책

[English canonical](DOCUMENTATION_POLICY.md) | **한국어**

> 버전: v1.0.20-docs
> 상태: 현행 앱 문서 거버넌스

## 1. 권위 순서

1. 웹/서버 권위 `PROJECT_PLAN.md`
2. 웹/서버 `INTEGRATED_PLANNING_MASTER.md`
3. 위 문서가 명시적으로 채택한 웹/서버 상세 명세
4. 앱 구현 사실에 대한 exact Android source와 생성/runtime API 증거
5. 이 저장소의 유지관리 앱 문서
6. 과거 앱 가이드, 루트 실행계획, 업데이트 기록

앱 문서는 웹 권위 기획이 명시적으로 차단하거나 supersede한 제품 동작을 승인할 수 없다.

## 2. 변경 워크플로

중요 앱 문서 변경은 전용 브랜치를 사용한다. 편집 전 최신 웹 권위, 앱 운영 지침, 관련 앱 스냅샷을 다시 읽는다. 작업 중간 및 통합 직전 웹·앱 `main`을 재확인한다.

문서 전용 변경은 APK 빌드, Test 배포, 백엔드 검증, Production 릴리스를 의미하지 않는다.

## 3. 언어

영문이 canonical이고 신규 유지 앱 거버넌스/명세 문서는 한국어를 2차 언어로 함께 유지한다. 과거/내부 기록은 역사 상태가 명확하면 미쌍으로 보존할 수 있다.

## 4. 상태/증거

`DRAFT`, `PLANNING`, `IMPLEMENTED`, `TEST_VERIFIED`, `PRODUCTION_VERIFIED`, `HISTORICAL`, `SUPERSEDED`, `AUTHORITY_DRIFT`를 사용한다.

구현 주장은 exact app commit을 식별하고 API/backend 동작이 관련되면 호환되는 exact 웹/backend candidate도 식별한다. 컴파일 성공만으로 end-to-end runtime 검증을 주장하지 않는다.

## 5. 레거시 분류

- `docs/APP_SPEC_AND_USER_GUIDE.ko.md`: 명시적 재정합 전까지 역사 v2026.09.22.343 스냅샷.
- `implementation_plan.md`: 실행 메모/이력, 제품 canonical 권위 아님.
- `docs/api/`: 통합 스냅샷이며 최신 생성/runtime 계약과 웹 기획 권위에 종속.
- `docs/updates/`: 역사 버전/변경 근거.

정리를 이유로 삭제하지 않고 상태 배너와 최신 인덱스를 우선해 기존 링크를 보존한다.
