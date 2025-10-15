# apps/agent/ai_agent_system.py
"""
AI Agent System - 자율적으로 도구를 사용하고 협업하는 멀티 에이전트
"""
from typing import List, Dict, Any, Optional
from dataclasses import dataclass
from enum import Enum
import json


# ===== Agent 메시지 프로토콜 =====
class MessageType(Enum):
    REQUEST = "request"      # 작업 요청
    RESPONSE = "response"    # 작업 결과
    QUERY = "query"          # 정보 질의
    NOTIFICATION = "notification"  # 알림


@dataclass
class AgentMessage:
    """Agent 간 통신 메시지"""
    sender: str              # 발신자 Agent 이름
    receiver: str            # 수신자 Agent 이름
    msg_type: MessageType
    content: Dict[str, Any]
    context: Dict[str, Any] = None


# ===== Tool 정의 =====
class Tool:
    """Agent가 사용할 수 있는 도구"""

    def __init__(self, name: str, description: str, parameters: Dict[str, Any]):
        self.name = name
        self.description = description
        self.parameters = parameters

    def execute(self, **kwargs) -> Any:
        """도구 실행 (하위 클래스에서 구현)"""
        raise NotImplementedError

    def to_dict(self) -> Dict[str, Any]:
        """LLM에게 보여줄 도구 스펙"""
        return {
            "name": self.name,
            "description": self.description,
            "parameters": self.parameters
        }


# ===== 구체적인 Tool 구현 =====
class FetchTransactionsTool(Tool):
    """거래내역 조회 도구"""

    def __init__(self, codef_client):
        super().__init__(
            name="fetch_transactions",
            description="CODEF API를 통해 사용자의 거래내역을 조회합니다",
            parameters={
                "user_id": "사용자 ID",
                "month": "조회할 월 (YYYY-MM)",
                "account_id": "(선택) 특정 계좌만 조회"
            }
        )
        self.codef_client = codef_client

    def execute(self, user_id: str, month: str, account_id: Optional[str] = None) -> Dict[str, Any]:
        """실제 CODEF API 호출"""
        print(f"🔧 Tool: fetch_transactions(user_id={user_id}, month={month})")

        # 실제로는 CODEF API 호출
        # 지금은 더미 데이터 반환
        return {
            "user_id": user_id,
            "month": month,
            "transactions": [
                {"date": "2025-10-01", "merchant": "스타벅스", "amount": 4500, "category": "카페"},
                {"date": "2025-10-05", "merchant": "쿠팡", "amount": 89000, "category": "온라인쇼핑"},
                {"date": "2025-10-10", "merchant": "넷플릭스", "amount": 17000, "category": "구독"},
            ],
            "total_count": 3
        }


class AnalyzeSpendingTool(Tool):
    """소비 패턴 분석 도구"""

    def __init__(self, llm_provider):
        super().__init__(
            name="analyze_spending",
            description="거래내역을 분석하여 소비 패턴과 인사이트를 도출합니다",
            parameters={
                "transactions": "분석할 거래내역 리스트",
                "analysis_type": "분석 유형 (pattern|budget|forecast)"
            }
        )
        self.llm = llm_provider

    def execute(self, transactions: List[Dict], analysis_type: str = "pattern") -> Dict[str, Any]:
        print(f"🔧 Tool: analyze_spending(count={len(transactions)}, type={analysis_type})")

        # 간단한 통계 분석
        total = sum(tx["amount"] for tx in transactions)
        categories = {}
        for tx in transactions:
            cat = tx["category"]
            categories[cat] = categories.get(cat, 0) + tx["amount"]

        return {
            "total_amount": total,
            "categories": categories,
            "transaction_count": len(transactions),
            "top_category": max(categories, key=categories.get) if categories else None
        }


class GenerateReportTool(Tool):
    """리포트 생성 도구"""

    def __init__(self):
        super().__init__(
            name="generate_report",
            description="분석 결과를 바탕으로 HTML 리포트를 생성합니다",
            parameters={
                "analysis": "분석 결과",
                "format": "리포트 형식 (html|pdf|markdown)"
            }
        )

    def execute(self, analysis: Dict, format: str = "html") -> Dict[str, Any]:
        print(f"🔧 Tool: generate_report(format={format})")

        html = f"""
        <!DOCTYPE html>
        <html>
        <head><title>소비 분석 리포트</title></head>
        <body>
            <h1>💰 소비 분석 리포트</h1>
            <p>총 소비액: {analysis.get('total_amount', 0):,}원</p>
            <p>거래 건수: {analysis.get('transaction_count', 0)}건</p>
            <h2>카테고리별 소비</h2>
            <ul>
                {''.join(f"<li>{k}: {v:,}원</li>" for k, v in analysis.get('categories', {}).items())}
            </ul>
        </body>
        </html>
        """

        return {
            "format": format,
            "content": html,
            "size": len(html)
        }


class SendNotificationTool(Tool):
    """알림 발송 도구"""

    def __init__(self):
        super().__init__(
            name="send_notification",
            description="사용자에게 이메일/슬랙/카카오톡 알림을 보냅니다",
            parameters={
                "user_id": "사용자 ID",
                "channel": "알림 채널 (email|slack|kakao)",
                "message": "알림 메시지"
            }
        )

    def execute(self, user_id: str, channel: str, message: str) -> Dict[str, Any]:
        print(f"🔧 Tool: send_notification(user={user_id}, channel={channel})")

        # 실제로는 각 채널 API 호출
        return {
            "status": "sent",
            "channel": channel,
            "user_id": user_id,
            "sent_at": "2025-10-15T12:00:00Z"
        }


# ===== Base AI Agent =====
class BaseAIAgent:
    """AI Agent 기본 클래스"""

    def __init__(self, name: str, role: str, llm_provider, tools: List[Tool] = None):
        self.name = name
        self.role = role
        self.llm = llm_provider
        self.tools = tools or []
        self.memory = []  # 대화 기록
        self.context = {}  # 공유 컨텍스트

    def add_tool(self, tool: Tool):
        """도구 추가"""
        self.tools.append(tool)

    def get_tools_description(self) -> str:
        """도구 목록을 LLM에게 설명"""
        if not self.tools:
            return "사용 가능한 도구가 없습니다."

        descriptions = []
        for tool in self.tools:
            desc = f"- {tool.name}: {tool.description}\n"
            desc += f"  Parameters: {json.dumps(tool.parameters, ensure_ascii=False)}"
            descriptions.append(desc)

        return "\n".join(descriptions)

    def execute_tool(self, tool_name: str, **kwargs) -> Any:
        """도구 실행"""
        for tool in self.tools:
            if tool.name == tool_name:
                return tool.execute(**kwargs)

        raise ValueError(f"Tool not found: {tool_name}")

    def think(self, task: str) -> Dict[str, Any]:
        """
        ReAct 패턴: Reason (생각) + Act (행동)
        LLM이 필요한 도구를 선택하고 사용
        """
        print(f"\n🤔 [{self.name}] Thinking about: {task}")

        # LLM에게 작업과 도구를 설명
        prompt = f"""당신은 {self.role}입니다.

현재 작업: {task}

사용 가능한 도구:
{self.get_tools_description()}

작업을 완료하기 위한 계획을 세우고, 필요한 도구를 순서대로 사용하세요.

다음 JSON 형식으로 응답하세요:
{{
  "reasoning": "작업을 어떻게 수행할지 설명",
  "actions": [
    {{
      "tool": "도구 이름",
      "parameters": {{"param1": "value1"}},
      "reason": "왜 이 도구를 사용하는지"
    }}
  ]
}}
"""

        try:
            response = self.llm.analyze(prompt, max_tokens=2048, temperature=0.3)

            # JSON 추출
            if "```json" in response:
                response = response.split("```json")[1].split("```")[0].strip()
            elif "```" in response:
                response = response.split("```")[1].split("```")[0].strip()

            plan = json.loads(response)

            print(f"📋 Plan: {plan['reasoning']}")

            return plan

        except Exception as e:
            print(f"❌ Planning failed: {e}")
            return {
                "reasoning": "계획 수립 실패",
                "actions": []
            }

    def act(self, plan: Dict[str, Any]) -> Dict[str, Any]:
        """계획에 따라 도구를 실행"""
        results = []

        for action in plan.get("actions", []):
            tool_name = action.get("tool")
            parameters = action.get("parameters", {})

            print(f"⚡ [{self.name}] Executing: {tool_name}")

            try:
                result = self.execute_tool(tool_name, **parameters)
                results.append({
                    "tool": tool_name,
                    "status": "success",
                    "result": result
                })
            except Exception as e:
                print(f"❌ Tool execution failed: {e}")
                results.append({
                    "tool": tool_name,
                    "status": "failed",
                    "error": str(e)
                })

        return {
            "reasoning": plan.get("reasoning"),
            "results": results
        }

    def process(self, task: str) -> Dict[str, Any]:
        """작업 처리: Think → Act"""
        plan = self.think(task)
        return self.act(plan)


# ===== 구체적인 AI Agent 구현 =====
class OrchestratorAgent(BaseAIAgent):
    """전체 작업을 조율하는 마스터 Agent"""

    def __init__(self, llm_provider):
        super().__init__(
            name="Orchestrator",
            role="전체 프로세스를 관리하고 다른 Agent들에게 작업을 분배하는 조율자",
            llm_provider=llm_provider
        )

    def orchestrate(self, user_request: str, available_agents: List[BaseAIAgent]) -> Dict[str, Any]:
        """사용자 요청을 분석하고 Agent들에게 작업 할당"""
        print(f"\n🎯 [Orchestrator] Received request: {user_request}")

        # LLM에게 전체 계획 요청
        agent_list = "\n".join([f"- {a.name}: {a.role}" for a in available_agents])

        prompt = f"""당신은 AI Agent 시스템의 조율자입니다.

사용자 요청: {user_request}

사용 가능한 Agent:
{agent_list}

이 요청을 처리하기 위한 전체 워크플로우를 계획하세요.

다음 JSON 형식으로 응답:
{{
  "workflow": [
    {{
      "step": 1,
      "agent": "Agent 이름",
      "task": "구체적인 작업 내용",
      "dependencies": []
    }}
  ],
  "expected_outcome": "최종 결과물 설명"
}}
"""

        try:
            response = self.llm.analyze(prompt, max_tokens=2048, temperature=0.3)

            if "```json" in response:
                response = response.split("```json")[1].split("```")[0].strip()

            workflow = json.loads(response)

            print(f"📋 Workflow: {len(workflow.get('workflow', []))} steps")

            return workflow

        except Exception as e:
            print(f"❌ Orchestration failed: {e}")
            return {"workflow": [], "expected_outcome": "실패"}


class DataAgent(BaseAIAgent):
    """데이터 수집 전문 Agent"""

    def __init__(self, llm_provider, codef_client=None):
        super().__init__(
            name="DataAgent",
            role="CODEF API를 통해 사용자의 금융 데이터를 수집하는 전문가",
            llm_provider=llm_provider
        )

        # 도구 추가
        self.add_tool(FetchTransactionsTool(codef_client))


class AnalyzerAgent(BaseAIAgent):
    """분석 전문 Agent"""

    def __init__(self, llm_provider):
        super().__init__(
            name="AnalyzerAgent",
            role="거래내역을 분석하여 소비 패턴과 인사이트를 도출하는 분석가",
            llm_provider=llm_provider
        )

        self.add_tool(AnalyzeSpendingTool(llm_provider))


class ReporterAgent(BaseAIAgent):
    """리포트 생성 전문 Agent"""

    def __init__(self, llm_provider):
        super().__init__(
            name="ReporterAgent",
            role="분석 결과를 시각적으로 아름다운 리포트로 만드는 디자이너",
            llm_provider=llm_provider
        )

        self.add_tool(GenerateReportTool())


class NotificationAgent(BaseAIAgent):
    """알림 발송 전문 Agent"""

    def __init__(self, llm_provider):
        super().__init__(
            name="NotificationAgent",
            role="사용자에게 다양한 채널로 알림을 보내는 커뮤니케이터",
            llm_provider=llm_provider
        )

        self.add_tool(SendNotificationTool())


# ===== Multi-Agent System =====
class MultiAgentSystem:
    """여러 AI Agent를 관리하는 시스템"""

    def __init__(self, llm_provider):
        self.llm = llm_provider

        # Agent 초기화
        self.orchestrator = OrchestratorAgent(llm_provider)
        self.data_agent = DataAgent(llm_provider, codef_client=None)
        self.analyzer_agent = AnalyzerAgent(llm_provider)
        self.reporter_agent = ReporterAgent(llm_provider)
        self.notification_agent = NotificationAgent(llm_provider)

        self.agents = [
            self.data_agent,
            self.analyzer_agent,
            self.reporter_agent,
            self.notification_agent
        ]

    def execute(self, user_request: str) -> Dict[str, Any]:
        """사용자 요청 실행"""
        print(f"\n{'='*60}")
        print(f"🚀 Multi-Agent System Starting")
        print(f"{'='*60}")

        # 1. Orchestrator가 계획 수립
        workflow = self.orchestrator.orchestrate(user_request, self.agents)

        # 2. 각 Agent가 순차적으로 작업 수행
        results = []
        shared_context = {}

        for step in workflow.get("workflow", []):
            agent_name = step.get("agent")
            task = step.get("task")

            # Agent 찾기
            agent = next((a for a in self.agents if a.name == agent_name), None)

            if agent:
                print(f"\n{'─'*60}")
                print(f"Step {step.get('step')}: {agent_name}")
                print(f"Task: {task}")
                print(f"{'─'*60}")

                # 이전 단계 결과를 컨텍스트로 전달
                task_with_context = task
                if shared_context:
                    task_with_context += f"\n\n이전 단계 결과:\n{json.dumps(shared_context, ensure_ascii=False, indent=2)}"

                result = agent.process(task_with_context)

                # 결과를 공유 컨텍스트에 저장
                shared_context[agent_name] = result
                results.append({
                    "step": step.get("step"),
                    "agent": agent_name,
                    "result": result
                })

        print(f"\n{'='*60}")
        print(f"✅ Multi-Agent System Completed")
        print(f"{'='*60}\n")

        return {
            "workflow": workflow,
            "results": results,
            "shared_context": shared_context
        }