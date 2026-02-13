package com.hank.clawlive.data.model;

/**
 * Response from GET /api/entities containing all active entities.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B)\u0012\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\bJ\u000f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\t\u0010\u000f\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0010\u001a\u00020\u0006H\u00c6\u0003J-\u0010\u0011\u001a\u00020\u00002\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u0006H\u00c6\u0001J\u0013\u0010\u0012\u001a\u00020\u00132\b\u0010\u0014\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0015\u001a\u00020\u0006H\u00d6\u0001J\t\u0010\u0016\u001a\u00020\u0017H\u00d6\u0001R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0007\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\n\u00a8\u0006\u0018"}, d2 = {"Lcom/hank/clawlive/data/model/MultiEntityResponse;", "", "entities", "", "Lcom/hank/clawlive/data/model/EntityStatus;", "activeCount", "", "maxEntities", "(Ljava/util/List;II)V", "getActiveCount", "()I", "getEntities", "()Ljava/util/List;", "getMaxEntities", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "toString", "", "app_release"})
public final class MultiEntityResponse {
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.hank.clawlive.data.model.EntityStatus> entities = null;
    private final int activeCount = 0;
    private final int maxEntities = 0;
    
    public MultiEntityResponse(@org.jetbrains.annotations.NotNull()
    java.util.List<com.hank.clawlive.data.model.EntityStatus> entities, int activeCount, int maxEntities) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.hank.clawlive.data.model.EntityStatus> getEntities() {
        return null;
    }
    
    public final int getActiveCount() {
        return 0;
    }
    
    public final int getMaxEntities() {
        return 0;
    }
    
    public MultiEntityResponse() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.hank.clawlive.data.model.EntityStatus> component1() {
        return null;
    }
    
    public final int component2() {
        return 0;
    }
    
    public final int component3() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hank.clawlive.data.model.MultiEntityResponse copy(@org.jetbrains.annotations.NotNull()
    java.util.List<com.hank.clawlive.data.model.EntityStatus> entities, int activeCount, int maxEntities) {
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
}