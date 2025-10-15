"""
LLM Provider 추상화
여러 LLM을 쉽게 교체할 수 있도록 전략 패턴 적용
"""
from abc import ABC, abstractmethod
from typing import Optional
import os


class LLMProvider(ABC):
    """LLM Provider 추상 클래스"""

    @abstractmethod
    def analyze(self, prompt: str, max_tokens: int = 4096, temperature: float = 0.7) -> str:
        """
        프롬프트를 받아 LLM 응답을 반환

        Args:
            prompt: 분석 요청 프롬프트
            max_tokens: 최대 토큰 수
            temperature: 창의성 (0.0 ~ 1.0)

        Returns:
            LLM 응답 텍스트
        """
        pass

    @abstractmethod
    def get_name(self) -> str:
        """Provider 이름 반환"""
        pass

    @abstractmethod
    def is_available(self) -> bool:
        """사용 가능 여부 확인"""
        pass


class GeminiProvider(LLMProvider):
    """Google Gemini Provider (무료!)"""

    def __init__(self, api_key: Optional[str] = None):
        self.api_key = api_key or os.getenv("GEMINI_API_KEY")
        self.client = None

        if self.api_key:
            try:
                import google.generativeai as genai
                genai.configure(api_key=self.api_key)
                self.client = genai.GenerativeModel('gemini-2.5-flash')
                print(f"✅ Gemini initialized (model: gemini-2.5-flash)")
            except Exception as e:
                print(f"❌ Gemini initialization failed: {e}")

    def analyze(self, prompt: str, max_tokens: int = 4096, temperature: float = 0.7) -> str:
        if not self.client:
            raise Exception("Gemini client not initialized")

        generation_config = {
            "temperature": temperature,
            "max_output_tokens": max_tokens,
        }

        response = self.client.generate_content(
            prompt,
            generation_config=generation_config
        )

        return response.text

    def get_name(self) -> str:
        return "Google Gemini 2.5 Flash"

    def is_available(self) -> bool:
        return self.client is not None


class AnthropicProvider(LLMProvider):
    """Anthropic Claude Provider"""

    def __init__(self, api_key: Optional[str] = None, model: str = "claude-sonnet-4-20250514"):
        self.api_key = api_key or os.getenv("ANTHROPIC_API_KEY")
        self.model = model
        self.client = None

        if self.api_key:
            try:
                import anthropic
                self.client = anthropic.Anthropic(api_key=self.api_key)
                print(f"✅ Anthropic initialized (model: {model})")
            except Exception as e:
                print(f"❌ Anthropic initialization failed: {e}")

    def analyze(self, prompt: str, max_tokens: int = 4096, temperature: float = 0.7) -> str:
        if not self.client:
            raise Exception("Anthropic client not initialized")

        response = self.client.messages.create(
            model=self.model,
            max_tokens=max_tokens,
            temperature=temperature,
            messages=[
                {"role": "user", "content": prompt}
            ]
        )

        return response.content[0].text

    def get_name(self) -> str:
        return f"Anthropic {self.model}"

    def is_available(self) -> bool:
        return self.client is not None


class OpenAIProvider(LLMProvider):
    """OpenAI GPT Provider"""

    def __init__(self, api_key: Optional[str] = None, model: str = "gpt-4o-mini"):
        self.api_key = api_key or os.getenv("OPENAI_API_KEY")
        self.model = model
        self.client = None

        if self.api_key:
            try:
                import openai
                self.client = openai.OpenAI(api_key=self.api_key)
                print(f"✅ OpenAI initialized (model: {model})")
            except Exception as e:
                print(f"❌ OpenAI initialization failed: {e}")

    def analyze(self, prompt: str, max_tokens: int = 4096, temperature: float = 0.7) -> str:
        if not self.client:
            raise Exception("OpenAI client not initialized")

        response = self.client.chat.completions.create(
            model=self.model,
            messages=[
                {"role": "user", "content": prompt}
            ],
            max_tokens=max_tokens,
            temperature=temperature
        )

        return response.choices[0].message.content

    def get_name(self) -> str:
        return f"OpenAI {self.model}"

    def is_available(self) -> bool:
        return self.client is not None


class LLMProviderFactory:
    """LLM Provider Factory - 환경변수로 선택"""

    @staticmethod
    def create_provider() -> Optional[LLMProvider]:
        """
        환경변수 LLM_PROVIDER에 따라 적절한 Provider 반환
        우선순위: LLM_PROVIDER 설정 > Gemini > Anthropic > OpenAI
        """
        provider_type = os.getenv("LLM_PROVIDER", "gemini").lower()

        # 명시적 선택
        if provider_type == "gemini":
            provider = GeminiProvider()
            if provider.is_available():
                return provider

        elif provider_type == "anthropic":
            provider = AnthropicProvider()
            if provider.is_available():
                return provider

        elif provider_type == "openai":
            provider = OpenAIProvider()
            if provider.is_available():
                return provider

        # Fallback: 사용 가능한 첫 번째 Provider 사용
        print(f"⚠️  {provider_type} not available, trying alternatives...")

        for ProviderClass in [GeminiProvider, AnthropicProvider, OpenAIProvider]:
            provider = ProviderClass()
            if provider.is_available():
                print(f"✅ Using fallback: {provider.get_name()}")
                return provider

        print("❌ No LLM provider available")
        return None