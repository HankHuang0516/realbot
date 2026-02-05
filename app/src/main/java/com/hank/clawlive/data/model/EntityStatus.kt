package com.hank.clawlive.data.model

/**
 * Status for a single entity in multi-entity mode.
 * Similar to AgentStatus but includes entityId.
 */
data class EntityStatus(
    val entityId: Int = 0,
    val character: CharacterType = CharacterType.LOBSTER,
    val state: CharacterState = CharacterState.IDLE,
    val message: String = "Loading...",
    val parts: Map<String, Float>? = null,
    val batteryLevel: Int = 100,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Convert to legacy AgentStatus for backward compatibility
     */
    fun toAgentStatus(): AgentStatus = AgentStatus(
        character = character,
        state = state,
        message = message,
        parts = parts,
        batteryLevel = batteryLevel,
        lastUpdated = lastUpdated
    )

    companion object {
        /**
         * Create EntityStatus from AgentStatus
         */
        fun fromAgentStatus(agentStatus: AgentStatus, entityId: Int = 0): EntityStatus = EntityStatus(
            entityId = entityId,
            character = agentStatus.character,
            state = agentStatus.state,
            message = agentStatus.message,
            parts = agentStatus.parts,
            batteryLevel = agentStatus.batteryLevel,
            lastUpdated = agentStatus.lastUpdated
        )
    }
}
