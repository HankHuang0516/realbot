package com.hank.clawlive.data.model

data class AgentStatus(
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
}

enum class CharacterType {
    LOBSTER
}

enum class CharacterState {
    IDLE,       // Default state
    BUSY,       // Working on something
    EATING,     // Eating noodles/food
    SLEEPING,   // Night time or inactive
    EXCITED     // New task or user interaction
}
