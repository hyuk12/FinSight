"""
FinSight AI Agent - 소비 패턴 분석 서버
"""
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Dict, Any
from datetime import datetime

app = FastAPI(
    title="FinSight Agent API",
    description="LLM 기반 소비 패턴 분석 엔진",
    version="0.1.0"
)

# ===== 데이터 모델 (camelCase로 통일) =====

class Transaction(BaseModel):
    id: str
    date: str
    amount: int
    category: str
    merchant: str
    description: str

class AnalysisRequest(BaseModel):
    userId: str  # camelCase!
    userName: str
    transactions: List[Transaction]
    month: str

class AnalysisResult(BaseModel):
    userId: str  # camelCase!
    month: str
    nickname: str
    topCategories: List[Dict[str, Any]]
    insights: List[str]
    advice: List[str]
    totalAmount: int
    generatedAt: str

# ===== Agent 로직 =====

def analyze_simple(request: AnalysisRequest) -> AnalysisResult:
    """
    임시 분석 로직 (추후 LLM으로 교체)
    """
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

# ===== API 엔드포인트 =====

@app.get("/")
def root():
    return {
        "service": "FinSight Agent",
        "status": "running",
        "version": "0.1.0"
    }

@app.get("/health")
def health():
    return {"status": "healthy"}

@app.post("/analyze", response_model=AnalysisResult)
async def analyze(request: AnalysisRequest):
    """
    거래내역 분석 API
    """
    try:
        print(f"📊 분석 요청 받음: userId={request.userId}, 거래건수={len(request.transactions)}")
        result = analyze_simple(request)
        print(f"✅ 분석 완료: {result.nickname}")
        return result
    except Exception as e:
        print(f"❌ 분석 에러: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/analyze-with-llm", response_model=AnalysisResult)
async def analyze_with_llm(request: AnalysisRequest):
    """
    LLM 기반 분석 (Phase 2에서 구현)
    """
    raise HTTPException(
        status_code=501,
        detail="LLM analysis not implemented yet. Use /analyze endpoint."
    )

if __name__ == "__main__":
    import uvicorn
    print("🚀 Starting FinSight Agent Server...")
    uvicorn.run(app, host="0.0.0.0", port=8000)