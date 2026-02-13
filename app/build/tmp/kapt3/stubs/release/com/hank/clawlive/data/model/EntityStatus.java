package com.hank.clawlive.data.model;

/**
 * Status for a single entity in multi-entity mode.
 * Similar to AgentStatus but includes entityId.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u001c\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\b\u0018\u0000 22\u00020\u0001:\u00012Be\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\u0005\u0012\u0016\b\u0002\u0010\n\u001a\u0010\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\f\u0018\u00010\u000b\u0012\b\b\u0002\u0010\r\u001a\u00020\u000e\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0010\u00a2\u0006\u0002\u0010\u0011J\t\u0010#\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010$\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010%\u001a\u00020\u0005H\u00c6\u0003J\t\u0010&\u001a\u00020\bH\u00c6\u0003J\t\u0010\'\u001a\u00020\u0005H\u00c6\u0003J\u0017\u0010(\u001a\u0010\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\f\u0018\u00010\u000bH\u00c6\u0003J\t\u0010)\u001a\u00020\u000eH\u00c6\u0003J\t\u0010*\u001a\u00020\u0010H\u00c6\u0003Ji\u0010+\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\u00052\u0016\b\u0002\u0010\n\u001a\u0010\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\f\u0018\u00010\u000b2\b\b\u0002\u0010\r\u001a\u00020\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u0010H\u00c6\u0001J\u0013\u0010,\u001a\u00020\u00102\b\u0010-\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010.\u001a\u00020\u0003H\u00d6\u0001J\u0006\u0010/\u001a\u000200J\t\u00101\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\u0012\u001a\u00020\u00138F\u00a2\u0006\u0006\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\u000f\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u001aR\u0011\u0010\r\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0011\u0010\t\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0017R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0017R\u001f\u0010\n\u001a\u0010\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\f\u0018\u00010\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010 R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\"\u00a8\u00063"}, d2 = {"Lcom/hank/clawlive/data/model/EntityStatus;", "", "entityId", "", "name", "", "character", "state", "Lcom/hank/clawlive/data/model/CharacterState;", "message", "parts", "", "", "lastUpdated", "", "isBound", "", "(ILjava/lang/String;Ljava/lang/String;Lcom/hank/clawlive/data/model/CharacterState;Ljava/lang/String;Ljava/util/Map;JZ)V", "baseShape", "Lcom/hank/clawlive/data/model/CharacterType;", "getBaseShape", "()Lcom/hank/clawlive/data/model/CharacterType;", "getCharacter", "()Ljava/lang/String;", "getEntityId", "()I", "()Z", "getLastUpdated", "()J", "getMessage", "getName", "getParts", "()Ljava/util/Map;", "getState", "()Lcom/hank/clawlive/data/model/CharacterState;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "equals", "other", "hashCode", "toAgentStatus", "Lcom/hank/clawlive/data/model/AgentStatus;", "toString", "Companion", "app_release"})
public final class EntityStatus {
    private final int entityId = 0;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String name = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String character = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hank.clawlive.data.model.CharacterState state = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String message = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.Map<java.lang.String, java.lang.Float> parts = null;
    private final long lastUpdated = 0L;
    private final boolean isBound = false;
    @org.jetbrains.annotations.NotNull()
    public static final com.hank.clawlive.data.model.EntityStatus.Companion Companion = null;
    
    public EntityStatus(int entityId, @org.jetbrains.annotations.Nullable()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String character, @org.jetbrains.annotations.NotNull()
    com.hank.clawlive.data.model.CharacterState state, @org.jetbrains.annotations.NotNull()
    java.lang.String message, @org.jetbrains.annotations.Nullable()
    java.util.Map<java.lang.String, java.lang.Float> parts, long lastUpdated, boolean isBound) {
        super();
    }
    
    public final int getEntityId() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCharacter() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hank.clawlive.data.model.CharacterState getState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getMessage() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.Map<java.lang.String, java.lang.Float> getParts() {
        return null;
    }
    
    public final long getLastUpdated() {
        return 0L;
    }
    
    public final boolean isBound() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hank.clawlive.data.model.CharacterType getBaseShape() {
        return null;
    }
    
    /**
     * Convert to legacy AgentStatus for backward compatibility
     */
    @org.jetbrains.annotations.NotNull()
    public final com.hank.clawlive.data.model.AgentStatus toAgentStatus() {
        return null;
    }
    
    public EntityStatus() {
        super();
    }
    
    public final int component1() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hank.clawlive.data.model.CharacterState component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.Map<java.lang.String, java.lang.Float> component6() {
        return null;
    }
    
    public final long component7() {
        return 0L;
    }
    
    public final boolean component8() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hank.clawlive.data.model.EntityStatus copy(int entityId, @org.jetbrains.annotations.Nullable()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String character, @org.jetbrains.annotations.NotNull()
    com.hank.clawlive.data.model.CharacterState state, @org.jetbrains.annotations.NotNull()
    java.lang.String message, @org.jetbrains.annotations.Nullable()
    java.util.Map<java.lang.String, java.lang.Float> parts, long lastUpdated, boolean isBound) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\b\u00a8\u0006\t"}, d2 = {"Lcom/hank/clawlive/data/model/EntityStatus$Companion;", "", "()V", "fromAgentStatus", "Lcom/hank/clawlive/data/model/EntityStatus;", "agentStatus", "Lcom/hank/clawlive/data/model/AgentStatus;", "entityId", "", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * Create EntityStatus from AgentStatus
         */
        @org.jetbrains.annotations.NotNull()
        public final com.hank.clawlive.data.model.EntityStatus fromAgentStatus(@org.jetbrains.annotations.NotNull()
        com.hank.clawlive.data.model.AgentStatus agentStatus, int entityId) {
            return null;
        }
    }
}