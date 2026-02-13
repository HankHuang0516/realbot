package com.hank.clawlive.billing;

/**
 * Represents the current subscription state for UI display
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u000b\n\u0002\u0010\u0007\n\u0002\b\u000e\b\u0086\b\u0018\u00002\u00020\u0001B7\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0003\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\t\u0010\u0019\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\tH\u00c6\u0003J;\u0010\u001e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\tH\u00c6\u0001J\u0013\u0010\u001f\u001a\u00020\u00032\b\u0010 \u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010!\u001a\u00020\u0005H\u00d6\u0001J\t\u0010\"\u001a\u00020\tH\u00d6\u0001R\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\r\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b\r\u0010\fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\fR\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0010\u001a\u00020\t8F\u00a2\u0006\u0006\u001a\u0004\b\u0011\u0010\u000fR\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0014\u001a\u00020\u00158F\u00a2\u0006\u0006\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0013\u00a8\u0006#"}, d2 = {"Lcom/hank/clawlive/billing/SubscriptionState;", "", "isPremium", "", "usageToday", "", "usageLimit", "canSendMessage", "subscriptionPrice", "", "(ZIIZLjava/lang/String;)V", "getCanSendMessage", "()Z", "isLimitReached", "getSubscriptionPrice", "()Ljava/lang/String;", "usageDisplay", "getUsageDisplay", "getUsageLimit", "()I", "usageProgress", "", "getUsageProgress", "()F", "getUsageToday", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class SubscriptionState {
    private final boolean isPremium = false;
    private final int usageToday = 0;
    private final int usageLimit = 0;
    private final boolean canSendMessage = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String subscriptionPrice = null;
    
    public SubscriptionState(boolean isPremium, int usageToday, int usageLimit, boolean canSendMessage, @org.jetbrains.annotations.NotNull()
    java.lang.String subscriptionPrice) {
        super();
    }
    
    public final boolean isPremium() {
        return false;
    }
    
    public final int getUsageToday() {
        return 0;
    }
    
    public final int getUsageLimit() {
        return 0;
    }
    
    public final boolean getCanSendMessage() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSubscriptionPrice() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUsageDisplay() {
        return null;
    }
    
    public final float getUsageProgress() {
        return 0.0F;
    }
    
    public final boolean isLimitReached() {
        return false;
    }
    
    public SubscriptionState() {
        super();
    }
    
    public final boolean component1() {
        return false;
    }
    
    public final int component2() {
        return 0;
    }
    
    public final int component3() {
        return 0;
    }
    
    public final boolean component4() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hank.clawlive.billing.SubscriptionState copy(boolean isPremium, int usageToday, int usageLimit, boolean canSendMessage, @org.jetbrains.annotations.NotNull()
    java.lang.String subscriptionPrice) {
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