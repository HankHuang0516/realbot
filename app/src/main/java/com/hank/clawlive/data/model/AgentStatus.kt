package com.hank.clawlive.data.model

data class AgentStatus(
    val character: CharacterType = CharacterType.LOBSTER,
    val state: CharacterState = CharacterState.IDLE,
    val message: String = "Loading...",
    val batteryLevel: Int = 100,
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class CharacterType {
    LOBSTER, PIG
}

enum class CharacterState {
    IDLE,       // Default state
    BUSY,       // Working on something
    EATING,     // Eating noodles/food
    SLEEPING,   // Night time or inactive
    EXCITED     // New task or user interaction
}
