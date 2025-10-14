# 💰 FinSight - AI 소비 분석 에이전트

> LLM Agent를 활용한 개인화된 소비 패턴 분석 및 인사이트 제공 서비스

---

## 🎯 프로젝트 개요

**FinSight**는 CODEF Open Banking API로 수집한 실제 거래내역을 LLM Agent가 분석하여,  
사용자의 소비 성향과 인사이트를 도출하고 **매월 자동으로 리포트를 발송**하는 시스템입니다.

### 핵심 차별점
- ❌ 단순 통계 집계가 아닌 **AI 기반 맥락 이해**
- ✅ "홈카페 마니아", "디지털 노마드" 같은 **성향 분석**
- ✅ LLM의 추론 능력을 활용한 **개인화된 조언**
- ✅ 완전 자동화된 **월별 리포트 발송**

---

## 🏗️ 아키텍처

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│   CODEF     │─────▶│  Spring Boot │─────▶│   Python    │
│ (거래내역)   │      │   API 서버    │      │ Agent 서버   │
└─────────────┘      └──────┬───────┘      └──────┬──────┘
                            │                     │
                            ▼                     ▼
                     ┌──────────────┐      ┌─────────────┐
                     │ PostgreSQL   │      │  Claude API │
                     │  (거래 저장)  │      │ (LLM 분석)  │
                     └──────────────┘      └─────────────┘
                            │
                            ▼
                     ┌──────────────┐
                     │ 알림 발송     │
                     │ Email/SMS/톡 │
                     └──────────────┘
```

### 주요 컴포넌트

1. **Spring Boot API** (Kotlin)
    - CODEF 거래내역 수집 및 저장
    - 월별 배치 스케줄러
    - 알림 발송 관리

2. **Agent 서버** (Python + FastAPI)
    - LangChain 기반 분석 Agent
    - 프롬프트 엔지니어링
    - 소비 성향 분류 및 인사이트 생성

3. **알림 시스템**
    - 이메일 (SendGrid/AWS SES)
    - SMS (Twilio/Aligo)
    - 카카오톡 알림톡

---

## 🚀 빠른 시작

### 1. 환경 설정

```bash
# .env 파일 생성
cp .env.example .env

# 필수 환경변수 설정
# - CODEF_CLIENT_ID, CODEF_CLIENT_SECRET
# - ANTHROPIC_API_KEY (Claude API)
# - 알림 서비스 API 키
```

### 2. API 서버 실행

```bash
./gradlew :apps:api:bootRun
```

### 3. Agent 서버 실행

```bash
cd apps/agent
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```

### 4. 테스트

```bash
# 거래내역 수집
curl http://localhost:8080/api/transactions/sync

# 분석 요청
curl http://localhost:8080/api/analysis/trigger
```

---

## 📊 분석 결과 예시

```
========================================
💰 홍길동님의 10월 소비 리포트
========================================

🎯 당신의 소비 성향: "테크 얼리어답터"

📊 주요 소비 카테고리
1. 온라인 쇼핑: 680,000원 (42%)
2. 구독 서비스: 290,000원 (18%)
3. 카페/디저트: 250,000원 (15%)

✨ 이번 달 특이사항
- AI 관련 구독 서비스 3개 신규 가입
- 평소 대비 전자기기 지출 200% 증가
- 주말 카페 방문 패턴 감소 (재택 증가 추정)

💡 다음 달 조언
- 중복 구독 서비스 정리 권장 (월 50,000원 절약 가능)
- 최근 구매한 기기 활용도를 높이는 것이 우선
- 홈카페 장비 투자 고려 (3개월 회수 가능)
========================================
```

---

## 🛠️ 기술 스택

| 영역 | 기술 |
|------|------|
| Backend | Kotlin, Spring Boot 3.5 |
| AI Agent | Python 3.11, FastAPI, LangChain |
| LLM | Claude 4 (Anthropic) |
| Database | PostgreSQL 16 |
| Auth | OAuth2 (Google) |
| Banking API | CODEF |
| Notification | SendGrid, Twilio, Kakao |
| Scheduler | Spring @Scheduled |
| DevOps | Docker Compose, GitHub Actions |

---

## 📁 프로젝트 구조

```
FinSight/
├── apps/
│   ├── api/                    # Spring Boot API
│   │   ├── controller/
│   │   ├── service/
│   │   └── scheduler/
│   └── agent/                  # Python Agent (신규)
│       ├── main.py
│       ├── agents/
│       └── prompts/
├── libs/
│   ├── common-core/
│   ├── common-domain/
│   └── common-infra/
└── docker-compose.yml
```

---

## 📅 개발 로드맵

| 단계 | 기능 | 상태 |
|------|------|------|
| ✅ Phase 0 | Spring Boot + CODEF 연동 | 완료 |
| 🔄 Phase 1 | Agent 서버 구축 | 진행중 |
| 📋 Phase 2 | 월별 자동 분석 배치 | 예정 |
| 📋 Phase 3 | 알림 시스템 통합 | 예정 |
| 📋 Phase 4 | 프로덕션 배포 | 예정 |

---

## 🧪 테스트 계정

개발/테스트용 더미 데이터를 제공합니다:

```bash
# 더미 거래내역 생성
curl -X POST http://localhost:8080/api/dev/generate-dummy-transactions
```

---

## 📝 License

MIT License