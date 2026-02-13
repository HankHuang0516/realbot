package com.hank.clawlive.data.model

/**
 * Status for a single entity in multi-entity mode.
 * Similar to AgentStatus but includes entityId.
 */
data class EntityStatus(
    val entityId: Int = 0,
    val name: String? = null, // Optional name set by bot (max 20 chars)
    val character: String = "LOBSTER",
    val state: CharacterState = CharacterState.IDLE,
    val message: String = "Loading...",
    val parts: Map<String, Float>? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isBound: Boolean = false // Server returns this field
) {
    // All characters are now LOBSTER type (PIG removed)
    val baseShape: CharacterType
        get() = CharacterType.LOBSTER

    /**
     * Convert to legacy AgentStatus for backward compatibility
     */
    fun toAgentStatus(): AgentStatus = AgentStatus(
        name = name,
        character = character,
        state = state,
        message = message,
        parts = parts,
        lastUpdated = lastUpdated
    )

    companion object {
        /**
         * Create EntityStatus from AgentStatus
         */
        fun fromAgentStatus(agentStatus: AgentStatus, entityId: Int = 0): EntityStatus = EntityStatus(
            entityId = entityId,
            name = agentStatus.name,
            character = agentStatus.character,
            state = agentStatus.state,
            message = agentStatus.message,
            parts = agentStatus.parts,
            lastUpdated = agentStatus.lastUpdated,
            isBound = agentStatus.isBound
        )
    }
}
