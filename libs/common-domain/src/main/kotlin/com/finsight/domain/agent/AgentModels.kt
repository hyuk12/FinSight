package com.finsight.domain.agent

data class AgentTask(
    val agentName: String,
    val taskDescription: String,
    val priority: Int = 0,
    val dependencies: List<String> = emptyList()
)

data class AgentWorkflow(
    val workflowId: String,
    val userId: String,
    val tasks: List<AgentTask>,
    val status: WorkflowStatus,
    val startedAt: java.time.LocalDateTime,
    val completedAt: java.time.LocalDateTime? = null
)

enum class WorkflowStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class AgentExecutionResult(
    val agentName: String,
    val taskDescription: String,
    val status: String,
    val result: Map<String, Any>,
    val executionTime: Long,
    val error: String? = null
)