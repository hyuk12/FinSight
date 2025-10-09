# 💰 FinSight
> Kotlin 기반 Spring Boot 백엔드 + AI 소비 예측 서비스

---

## 🚀 프로젝트 개요
**FinSight**는 사용자의 실제 **계좌 거래내역(Open Banking API)** 을 분석하고  
AI 모델을 통해 **미래 소비 패턴을 예측**하는 백엔드 중심 프로젝트입니다.  

백엔드 서버는 Kotlin + Spring Boot로 작성되어 있으며,  
데이터 수집, 예측 API 연동, 결과 저장을 담당합니다.

---

## ⚙️ 아키텍처 개요

```
[사용자 계좌] → [Spring Boot API 서버]
↓ ↓
[DB (PostgreSQL)] ←→ [AI 예측 엔진 (Python ML)]
```


- Spring Boot: 계좌 연동 / 데이터 파이프라인 / REST API 제공  
- ML Service: Python 기반 시계열 모델 (Prophet, LSTM 등)  
- Airflow: 예측 스케줄링 및 배치 파이프라인  
- MLflow: 모델 버전 관리 및 실험 추적

---

## 🧩 기술 스택

| 영역 | 기술 |
|------|------|
| Language | Kotlin |
| Framework | Spring Boot (Web, Data JPA, Scheduler) |
| DB | PostgreSQL |
| Infra | Docker Compose, AWS S3 |
| ML | Python (Prophet / LSTM / Scikit-learn), MLflow |
| Auth | OAuth2 + JWT (Open Banking API 인증) |

---

## 📊 주요 기능

- 🔗 **계좌 연동**: OpenBanking API를 통한 거래내역 자동 수집  
- 🧮 **거래 분석**: 카테고리별 소비 패턴 집계  
- 🔮 **미래 예측**: ML 모델을 통해 향후 7~30일 소비 추정  
- 💾 **결과 저장 및 조회**: 예측 결과를 DB에 저장 후 REST API로 제공  
- 🕒 **스케줄링**: Airflow 기반 일일 데이터 갱신 및 예측 자동화  

---

## 📁 디렉토리 구조 예시

```
FinSight/
├── apps/
│   └── api/                           # Spring Boot API 서버
│       ├── src/main/
│       │   ├── kotlin/com/finsight/api/
│       │   │   ├── account/          # 계좌 API
│       │   │   │   └── AccountController.kt
│       │   │   ├── codef/            # CODEF 토큰 관리
│       │   │   │   └── CodefTokenService.kt
│       │   │   ├── config/           # 설정 (Security, Beans)
│       │   │   │   ├── AppBeans.kt
│       │   │   │   └── SecurityConfig.kt
│       │   │   ├── forecast/         # AI 예측 API
│       │   │   │   └── ForecastController.kt
│       │   │   ├── user/             # 사용자 & OAuth2
│       │   │   │   ├── UserController.kt
│       │   │   │   └── UserService.kt
│       │   │   └── ApiApplication.kt
│       │   └── resources/
│       │       ├── application.yml
│       │       └── application-test.yml
│       ├── src/test/
│       └── build.gradle.kts
│
├── libs/
│   ├── common-core/                   # 공통 유틸리티
│   │   └── src/main/kotlin/com/finsight/common/
│   │       └── Logging.kt
│   ├── common-domain/                 # 도메인 모델
│   │   └── src/main/kotlin/com/finsight/domain/
│   │       ├── account/Account.kt
│   │       ├── codef/Codef.kt
│   │       ├── forecast/Forecast.kt
│   │       └── user/User.kt
│   └── common-infra/                  # 외부 API 클라이언트
│       └── src/main/kotlin/com/finsight/infra/
│           ├── codef/CodefClient.kt
│           └── forecast/ForecastClient.kt
│
├── .github/
│   ├── workflows/build.yml           # CI/CD
│   ├── ISSUE_TEMPLATE/
│   └── PULL_REQUEST_TEMPLATE.md
│
├── gradle/
│   └── wrapper/
├── scripts/
│   └── bootstrap_repo.ps1            # 저장소 초기화 스크립트
│
├── build.gradle.kts                  # Root Gradle 설정
├── settings.gradle.kts               # 멀티모듈 설정
├── gradle.properties
├── gradlew / gradlew.bat
├── .env.example                      # 환경변수 예시
└── README.md
```


---

## 🧠 예측 모델 개요

| 모델 | 특징 |
|------|------|
| Prophet | 시계열 트렌드 기반 예측 (휴일 효과 반영 가능) |
| LSTM | 순환신경망 기반의 거래 시퀀스 예측 |
| Gradient Boosting | 특징 기반 회귀 모델 (비시계열 포함) |

---

## 📅 개발 로드맵

| 단계 | 목표 | 기간 |
|------|------|------|
| 1단계 | OpenBanking 연동 및 거래내역 저장 | 2025.10 |
| 2단계 | ML 모델 API 연동 (Python Flask or FastAPI) | 2025.11 |
| 3단계 | Airflow 자동화 + Docker Compose 통합 | 2025.12 |
| 4단계 | 리포트/대시보드 UI 연동 (선택) | 2026.01 |

---

## 📜 License
MIT License
