# 💰 FinSight - AI 소비 분석 에이전트

> 멀티 LLM Agent를 활용한 개인화된 소비 패턴 분석 및 인사이트 제공 서비스

---

## 🎯 프로젝트 개요

**FinSight**는 CODEF Open Banking API로 수집한 실제 거래내역을 **여러 LLM Agent** (Gemini, Claude, GPT)가 분석하여,  
사용자의 소비 성향과 인사이트를 도출하고 **매월 자동으로 리포트를 발송**하는 시스템입니다.

### 핵심 차별점
- ✅ **멀티 LLM 지원**: Gemini (무료) / Claude / GPT 중 선택 가능
- ✅ **전략 패턴**: LLM을 쉽게 교체할 수 있는 확장 가능한 구조
- ✅ "홈카페 마니아", "디지털 노마드" 같은 **AI 성향 분석**
- ✅ LLM의 추론 능력을 활용한 **개인화된 조언**

---

## 🤖 지원 LLM

| LLM | 비용 | 속도 | 품질 | 추천 |
|-----|------|------|------|------|
| **Google Gemini 1.5 Flash** | ✅ **무료!** | ⚡ 빠름 | ⭐⭐⭐⭐ | 개발/학습 |
| **Anthropic Claude Sonnet 4** | 💰 유료 | 🐢 느림 | ⭐⭐⭐⭐⭐ | 프로덕션 |
| **OpenAI GPT-4o-mini** | 💰 유료 | ⚡ 빠름 | ⭐⭐⭐⭐ | 프로덕션 |

---

## 🚀 빠른 시작 (Gemini 무료 버전)

### 1. Google Gemini API Key 발급 (무료!)

1. https://aistudio.google.com/app/apikey 접속
2. "Create API Key" 버튼 클릭
3. 기존 프로젝트 선택 또는 새로 생성
4. API 키 복사

**무료 한도:**
- 하루 1,500회 요청 (충분!)
- 월 45,000회 = 45,000명 분석 가능
- 크레딧 카드 등록 불필요

### 2. 환경 설정

```bash
cd apps/agent

# .env 파일 생성
cat > .env << 'EOF'
# LLM Provider 선택
LLM_PROVIDER=gemini

# Google Gemini API Key (무료!)
GEMINI_API_KEY=여기에_발급받은_키_입력
EOF
```

### 3. Python 의존성 설치

```bash
# 가상환경 생성
python3 -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 의존성 설치
pip install -r requirements.txt
```

### 4. Agent 서버 실행

```bash
python main.py
```

정상 실행 시:
```
🚀 Starting FinSight Agent Server...
✅ Gemini initialized (model: gemini-1.5-flash)
🤖 LLM Provider: Google Gemini 1.5 Flash
INFO:     Uvicorn running on http://0.0.0.0:8000
```

### 5. Spring Boot 실행 & 테스트

```bash
# 새 터미널에서
./gradlew :apps:api:bootRun

# 브라우저 접속
open http://localhost:8080/test.html
```

---

## 🔄 LLM 전환하기

### Gemini → Claude로 전환

```bash
# apps/agent/.env 수정
LLM_PROVIDER=anthropic
ANTHROPIC_API_KEY=sk-ant-api03-xxxxx

# 서버 재시작
python main.py
```

### Gemini → GPT로 전환

```bash
# apps/agent/.env 수정
LLM_PROVIDER=openai
OPENAI_API_KEY=sk-xxxxx

# 서버 재시작
python main.py
```

### 자동 Fallback

API 키가 없으면 자동으로 다른 LLM으로 전환됩니다:

```bash
# 우선순위: Gemini > Anthropic > OpenAI
# 모두 없으면 LLM 비활성화 (통계 분석만 사용)
```

---

## 📊 LLM 비교 분석

### Google Gemini 1.5 Flash
**장점:**
- ✅ 완전 무료 (하루 1,500회)
- ✅ 빠른 응답 속도 (1-3초)
- ✅ 크레딧 카드 불필요
- ✅ 학습/개발에 최적

**단점:**
- ⚠️ Claude보다 품질 약간 낮음
- ⚠️ 한국어 이해도가 약간 떨어질 수 있음

### Anthropic Claude Sonnet 4
**장점:**
- ✅ 최고 품질의 분석
- ✅ 뛰어난 한국어 이해
- ✅ 맥락 이해 능력 우수

**단점:**
- 💰 비용: $3/1M input, $15/1M output
- 💰 $5 크레딧 필요 (약 250회)

### OpenAI GPT-4o-mini
**장점:**
- ✅ 균형잡힌 품질/속도
- ✅ 저렴한 비용 (Claude의 1/10)
- ✅ 안정적인 성능

**단점:**
- 💰 비용: $0.15/1M input, $0.60/1M output
- 💰 신규 가입자는 $5 무료 크레딧

---

## 🛠️ 기술 스택

| 영역 | 기술 |
|------|------|
| Backend | Kotlin, Spring Boot 3.5 |
| AI Agent | Python 3.11, FastAPI |
| LLM | **Gemini (무료)** / Claude / GPT |
| 패턴 | Strategy Pattern (전략 패턴) |
| Database | PostgreSQL 16 (예정) |
| Auth | OAuth2 (Google) |
| Banking API | CODEF |

---

## 📁 프로젝트 구조

```
FinSight/
├── apps/
│   ├── api/                    # Spring Boot API
│   │   ├── controller/
│   │   ├── service/
│   │   └── config/
│   └── agent/                  # Python Agent
│       ├── main.py            # FastAPI 서버
│       ├── llm_providers.py   # ✨ LLM 추상화
│       ├── requirements.txt
│       └── .env
├── libs/
│   ├── common-core/
│   ├── common-domain/
│   └── common-infra/
│       └── agent/
└── .env.example
```

---

## 🧪 API 테스트

```bash
# 서버 정보 확인
curl http://localhost:8000/

# 예상 출력:
# {
#   "service": "FinSight Agent",
#   "llm_provider": "Google Gemini 1.5 Flash",
#   "llm_enabled": true
# }

# 통계 분석 (무료)
curl -X POST http://localhost:8080/api/analysis/test

# LLM 분석 (Gemini 무료!)
curl -X POST http://localhost:8080/api/analysis/test-llm
```

---

## 🔑 API Key 발급 가이드

### 🆓 Google Gemini (무료, 추천!)

1. https://aistudio.google.com/app/apikey 접속
2. "Create API Key" 버튼
3. 프로젝트 선택 또는 생성
4. 완료! 크레딧 카드 불필요

**무료 한도:**
- 하루 1,500회 요청
- 월 45,000회
- RPM (분당): 15회
- 충분히 개발/학습 가능!

### 💰 Anthropic Claude (유료)

1. https://console.anthropic.com/
2. API Keys > Create Key
3. **Billing에서 크레딧 구매 필요** ($5~)
4. $5 = 약 250회 분석

### 💰 OpenAI GPT (유료)

1. https://platform.openai.com/api-keys
2. Create new secret key
3. 신규 가입자는 $5 무료 크레딧
4. GPT-4o-mini 추천 (저렴)

---

## 🐛 트러블슈팅

### "No LLM provider available" 에러

```bash
# .env 파일 확인
cat apps/agent/.env

# API 키 확인
# GEMINI_API_KEY가 올바르게 설정되었는지 확인

# 서버 재시작
python main.py
```

### Gemini API 에러

```bash
# 일일 한도 초과 시
# 24시간 대기 또는 다른 LLM으로 전환

# API 키 오류 시
# https://aistudio.google.com/app/apikey 에서 재발급
```

### LLM 전환이 안 될 때

```bash
# 의존성 재설치
pip install --upgrade google-generativeai anthropic openai

# .env 파일 권한 확인
chmod 600 .env
```

---

## 💡 비용 최적화 팁

### 개발/학습 단계
```bash
LLM_PROVIDER=gemini  # 무료!
```

### MVP/테스트 단계
```bash
LLM_PROVIDER=openai  # GPT-4o-mini 저렴
# 또는 Gemini 무료 버전 계속 사용
```

### 프로덕션 단계
```bash
LLM_PROVIDER=anthropic  # Claude 최고 품질
# 또는 사용량에 따라 Gemini/GPT 혼용
```

---

## 📅 개발 로드맵

| 단계 | 기능 | 상태 |
|------|------|------|
| ✅ Phase 0 | Spring Boot + CODEF 연동 | 완료 |
| ✅ Phase 1 | **멀티 LLM Agent 구축** | **완료** ✨ |
| 🔄 Phase 2 | PostgreSQL 연동 + 실거래 분석 | 진행중 |
| 📋 Phase 3 | 월별 자동 배치 스케줄러 | 예정 |
| 📋 Phase 4 | 알림 시스템 (Email/SMS) | 예정 |
| 📋 Phase 5 | 프로덕션 배포 | 예정 |

---

## 📝 License

MIT License

---

## 🙏 Credits

- **LLM**: Google Gemini (무료) / Anthropic Claude / OpenAI GPT
- **Backend**: Spring Boot, Kotlin
- **Agent**: FastAPI, Python
- **Banking API**: CODEF