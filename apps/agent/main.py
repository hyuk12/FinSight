"""
FinSight AI Agent - 소비 패턴 분석 서버
멀티 LLM 지원 (Gemini, Anthropic, OpenAI)
"""
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Dict, Any
from datetime import datetime
import os
import json

# 환경 변수 로드
from dotenv import load_dotenv
load_dotenv()

# LLM Provider
from llm_providers import LLMProviderFactory

app = FastAPI(
    title="FinSight Agent API",
    description="멀티 LLM 기반 소비 패턴 분석 엔진",
    version="0.3.0"
)

# LLM Provider 초기화
llm_provider = LLMProviderFactory.create_provider()

if llm_provider:
    print(f"🤖 LLM Provider: {llm_provider.get_name()}")
else:
    print("⚠️  No LLM provider available. LLM analysis will be disabled.")

# ===== 데이터 모델 =====

class Transaction(BaseModel):
    id: str
    date: str
    amount: int
    category: str
    merchant: str
    description: str

class AnalysisRequest(BaseModel):
    userId: str
    userName: str
    transactions: List[Transaction]
    month: str

class AnalysisResult(BaseModel):
    userId: str
    month: str
    nickname: str
    topCategories: List[Dict[str, Any]]
    insights: List[str]
    advice: List[str]
    totalAmount: int
    generatedAt: str

# ===== 분석 로직 =====

def analyze_simple(request: AnalysisRequest) -> AnalysisResult:
    """간단한 통계 기반 분석 (LLM 없이)"""
    # 카테고리별 집계
    category_sum = {}
    for tx in request.transactions:
        category_sum[tx.category] = category_sum.get(tx.category, 0) + tx.amount

    total = sum(category_sum.values())

    # 상위 3개 카테고리
    top_3 = sorted(category_sum.items(), key=lambda x: x[1], reverse=True)[:3]
    top_categories = [
        {
            "category": cat,
            "amount": amt,
            "percentage": round(amt / total * 100, 1) if total > 0 else 0
        }
        for cat, amt in top_3
    ]

    # 간단한 성향 판단
    if top_categories and top_categories[0]["category"] == "카페":
        nickname = "카페 탐험가 ☕"
    elif any(cat["category"] == "온라인쇼핑" for cat in top_categories):
        nickname = "디지털 쇼퍼 🛒"
    elif any(cat["category"] == "구독" for cat in top_categories):
        nickname = "구독 마니아 📱"
    else:
        nickname = "균형잡힌 소비자 ⚖️"

    return AnalysisResult(
        userId=request.userId,
        month=request.month,
        nickname=nickname,
        topCategories=top_categories,
        insights=[
            f"이번 달 총 {len(request.transactions)}건의 거래가 발생했어요",
            f"가장 많이 소비한 카테고리는 '{top_categories[0]['category']}'입니다" if top_categories else "거래 내역이 없습니다",
            f"총 소비액은 {total:,}원이에요"
        ],
        advice=[
            "다음 달도 현재 패턴을 유지하면 좋을 것 같아요",
            "예산 관리를 위해 고정비를 먼저 확인해보세요",
            "소비 카테고리가 분산되어 있어 균형잡힌 소비 패턴이에요"
        ],
        totalAmount=total,
        generatedAt=datetime.now().isoformat()
    )


def analyze_with_llm(request: AnalysisRequest) -> AnalysisResult:
    """LLM 기반 심층 분석"""
    if not llm_provider:
        raise HTTPException(
            status_code=503,
            detail={
                "error": "LLM_NOT_AVAILABLE",
                "message": "LLM 서비스를 사용할 수 없습니다.",
                "solution": "GEMINI_API_KEY, ANTHROPIC_API_KEY, 또는 OPENAI_API_KEY를 설정해주세요."
            }
        )

    # 거래내역을 텍스트로 포맷팅
    transactions_text = "\n".join([
        f"- {tx.date} | {tx.category} | {tx.merchant} | {tx.amount:,}원 | {tx.description}"
        for tx in request.transactions
    ])

    total_amount = sum(tx.amount for tx in request.transactions)

    # 프롬프트 구성
    prompt = f"""당신은 개인 재무 분석 전문가입니다. 사용자의 소비 패턴을 분석하여 맞춤형 인사이트와 조언을 제공해주세요.

📊 분석 대상
- 사용자: {request.userName}님
- 분석 월: {request.month}
- 총 거래 건수: {len(request.transactions)}건
- 총 소비액: {total_amount:,}원

💳 거래 내역:
{transactions_text}

---

다음 형식의 JSON으로 분석 결과를 생성해주세요:

{{
  "nickname": "사용자의 소비 성향을 나타내는 창의적인 별명 (예: 테크 얼리어답터, 홈카페 마스터, 미니멀 라이프 실천가)",
  "topCategories": [
    {{
      "category": "카테고리명",
      "amount": 금액(숫자),
      "percentage": 비율(소수점 1자리)
    }}
  ],
  "insights": [
    "거래 패턴에서 발견한 특이사항이나 트렌드 (3-5개)",
    "구체적인 수치나 비교를 포함해주세요"
  ],
  "advice": [
    "실용적이고 구체적인 재무 조언 (3-5개)",
    "다음 달 소비 계획이나 절약 팁"
  ]
}}

**요구사항:**
1. 별명은 구체적이고 개성있게 (이모지 포함 가능)
2. 인사이트는 데이터 기반의 구체적인 관찰
3. 조언은 실행 가능한 액션 아이템
4. 친근하고 공감적인 톤 유지
5. 반드시 유효한 JSON 형식으로만 응답

JSON만 출력하고 다른 설명은 하지 마세요."""

    try:
        # LLM 호출
        print(f"🤖 Calling {llm_provider.get_name()}...")

        response_text = llm_provider.analyze(
            prompt=prompt,
            max_tokens=4096,
            temperature=0.7
        )

        print(f"📥 Response length: {len(response_text)} chars")

        # JSON 추출 (markdown 코드블록 제거)
        if response_text.startswith("```"):
            response_text = response_text.split("```")[1]
            if response_text.startswith("json"):
                response_text = response_text[4:].strip()

        analysis_data = json.loads(response_text)

        # 검증 및 기본값 설정
        nickname = analysis_data.get("nickname", "지혜로운 소비자 🎯")
        top_categories = analysis_data.get("topCategories", [])
        insights = analysis_data.get("insights", ["분석 데이터를 수집하고 있어요"])
        advice = analysis_data.get("advice", ["다음 달 분석을 기대해주세요"])

        return AnalysisResult(
            userId=request.userId,
            month=request.month,
            nickname=nickname,
            topCategories=top_categories,
            insights=insights,
            advice=advice,
            totalAmount=total_amount,
            generatedAt=datetime.now().isoformat()
        )

    except json.JSONDecodeError as e:
        print(f"❌ JSON parsing error: {e}")
        print(f"Raw response: {response_text[:500]}")
        # fallback to simple analysis
        return analyze_simple(request)

    except Exception as e:
        print(f"❌ LLM error: {e}")
        error_msg = str(e).lower()

        # 크레딧 부족 에러 처리
        if "credit balance is too low" in error_msg or "insufficient_quota" in error_msg:
            raise HTTPException(
                status_code=402,
                detail={
                    "error": "INSUFFICIENT_CREDITS",
                    "message": f"{llm_provider.get_name()} 크레딧이 부족합니다.",
                    "solution": "다른 LLM으로 전환하거나 크레딧을 충전해주세요.",
                    "fallback": "'/api/analysis/test' 엔드포인트를 사용하면 무료 통계 분석을 이용할 수 있습니다."
                }
            )

        raise HTTPException(
            status_code=500,
            detail={
                "error": "LLM_ANALYSIS_FAILED",
                "message": f"분석 중 오류가 발생했습니다: {str(e)}",
                "provider": llm_provider.get_name()
            }
        )


# ===== API 엔드포인트 =====

@app.get("/")
def root():
    return {
        "service": "FinSight Agent",
        "status": "running",
        "version": "0.3.0",
        "llm_provider": llm_provider.get_name() if llm_provider else None,
        "llm_enabled": llm_provider is not None
    }

@app.get("/health")
def health():
    return {
        "status": "healthy",
        "llm_provider": llm_provider.get_name() if llm_provider else None,
        "llm_available": llm_provider is not None
    }

@app.post("/analyze", response_model=AnalysisResult)
async def analyze(request: AnalysisRequest):
    """간단한 통계 기반 분석 (빠름)"""
    try:
        print(f"📊 분석 요청: userId={request.userId}, 거래건수={len(request.transactions)}")
        result = analyze_simple(request)
        print(f"✅ 분석 완료: {result.nickname}")
        return result
    except Exception as e:
        print(f"❌ 분석 에러: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/analyze-with-llm", response_model=AnalysisResult)
async def analyze_with_llm_endpoint(request: AnalysisRequest):
    """LLM 기반 심층 분석 (느리지만 고품질)"""
    try:
        print(f"🤖 LLM 분석 요청: userId={request.userId}, 거래건수={len(request.transactions)}")
        result = analyze_with_llm(request)
        print(f"✅ LLM 분석 완료: {result.nickname}")
        return result
    except HTTPException:
        raise
    except Exception as e:
        print(f"❌ LLM 분석 에러: {e}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    print("🚀 Starting FinSight Agent Server...")
    print(f"🤖 LLM Status: {llm_provider.get_name() if llm_provider else 'Disabled'}")
    uvicorn.run(app, host="0.0.0.0", port=8000)