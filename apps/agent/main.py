# apps/agent/main.py (업데이트 버전)
"""
FinSight AI Agent - Multi-Agent System
"""
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Dict, Any
from datetime import datetime
import os

from dotenv import load_dotenv
load_dotenv()

from llm_providers import LLMProviderFactory
from ai_agent_system import (
    MultiAgentSystem,
    DataAgent,
    AnalyzerAgent,
    ReporterAgent,
    NotificationAgent
)

app = FastAPI(
    title="FinSight Multi-Agent System",
    description="자율적으로 협업하는 AI Agent 시스템",
    version="0.4.0"
)

# LLM Provider 초기화
llm_provider = LLMProviderFactory.create_provider()

if llm_provider:
    print(f"🤖 LLM Provider: {llm_provider.get_name()}")

    # Multi-Agent System 초기화
    agent_system = MultiAgentSystem(llm_provider)
    print(f"✅ Multi-Agent System initialized with {len(agent_system.agents)} agents")
else:
    agent_system = None
    print("⚠️  LLM not available. Multi-Agent features will be disabled.")


# ===== 기존 모델 (유지) =====
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


# ===== 새로운 모델 (Multi-Agent용) =====
class AgentRequest(BaseModel):
    """사용자의 자연어 요청"""
    user_id: str
    request: str
    # 예: "10월 소비 분석해서 이메일로 보내줘"
    # 예: "카페 지출이 너무 많은데 줄일 방법 알려줘"
    # 예: "예산 대비 소비 현황 리포트 만들어줘"


class AgentResponse(BaseModel):
    user_id: str
    request: str
    workflow: Dict[str, Any]
    results: List[Dict[str, Any]]
    execution_time: float
    status: str


# ===== 기존 엔드포인트 (유지) =====
@app.get("/")
def root():
    return {
        "service": "FinSight Multi-Agent System",
        "status": "running",
        "version": "0.4.0",
        "llm_provider": llm_provider.get_name() if llm_provider else None,
        "agents": [
            "Orchestrator",
            "DataAgent",
            "AnalyzerAgent",
            "ReporterAgent",
            "NotificationAgent"
        ] if agent_system else []
    }


@app.get("/agents")
def list_agents():
    """사용 가능한 Agent 목록"""
    if not agent_system:
        raise HTTPException(status_code=503, detail="Agent system not available")

    return {
        "agents": [
            {
                "name": agent.name,
                "role": agent.role,
                "tools": [tool.name for tool in agent.tools]
            }
            for agent in agent_system.agents
        ]
    }


# ===== 새로운 Multi-Agent 엔드포인트 =====
@app.post("/agent/execute")
async def execute_agent_request(request: AgentRequest) -> AgentResponse:
    """
    자연어 요청을 Multi-Agent 시스템으로 처리

    예시:
    - "10월 소비 분석해서 이메일로 보내줘"
    - "카페 지출 줄이는 방법 알려줘"
    - "이번 달 예산 현황 리포트 만들어"
    """
    if not agent_system:
        raise HTTPException(
            status_code=503,
            detail="Multi-Agent system not available. Please configure LLM provider."
        )

    import time
    start_time = time.time()

    try:
        # Multi-Agent System 실행
        result = agent_system.execute(request.request)

        execution_time = time.time() - start_time

        return AgentResponse(
            user_id=request.user_id,
            request=request.request,
            workflow=result.get("workflow", {}),
            results=result.get("results", []),
            execution_time=execution_time,
            status="success"
        )

    except Exception as e:
        execution_time = time.time() - start_time

        return AgentResponse(
            user_id=request.user_id,
            request=request.request,
            workflow={},
            results=[{
                "error": str(e),
                "type": type(e).__name__
            }],
            execution_time=execution_time,
            status="failed"
        )


@app.post("/agent/execute-simple")
async def execute_simple_agent(request: AgentRequest):
    """
    단일 Agent 테스트 (간단한 버전)
    """
    if not agent_system:
        raise HTTPException(status_code=503, detail="Agent system not available")

    # 간단히 DataAgent만 실행
    result = agent_system.data_agent.process(
        f"사용자 {request.user_id}의 거래내역을 가져와주세요"
    )

    return {
        "user_id": request.user_id,
        "agent": "DataAgent",
        "result": result
    }


# ===== 기존 분석 엔드포인트 (유지) =====
@app.post("/analyze")
async def analyze(request: AnalysisRequest):
    """기존 통계 기반 분석"""
    # ... 기존 코드 유지 ...
    pass


@app.post("/analyze-with-llm")
async def analyze_with_llm_endpoint(request: AnalysisRequest):
    """기존 LLM 분석"""
    # ... 기존 코드 유지 ...
    pass


if __name__ == "__main__":
    import uvicorn
    print("🚀 Starting FinSight Multi-Agent System...")
    print(f"🤖 LLM: {llm_provider.get_name() if llm_provider else 'Disabled'}")
    print(f"🔧 Agents: {len(agent_system.agents) if agent_system else 0}")
    uvicorn.run(app, host="0.0.0.0", port=8000)