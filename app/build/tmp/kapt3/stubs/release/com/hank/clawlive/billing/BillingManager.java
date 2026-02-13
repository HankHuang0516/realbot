package com.hank.clawlive.billing;

/**
 * Manages Google Play Billing for subscription purchases.
 * Handles connection, purchase, acknowledgment, and restoration.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0006\u0018\u0000 &2\u00020\u0001:\u0001&B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017H\u0002J\u0006\u0010\u0018\u001a\u00020\u0015J\u0006\u0010\u0019\u001a\u00020\u0015J\u000e\u0010\u001a\u001a\u00020\u00152\u0006\u0010\u001b\u001a\u00020\u001cJ \u0010\u001d\u001a\u00020\u00152\u0006\u0010\u001e\u001a\u00020\u001f2\u000e\u0010 \u001a\n\u0012\u0004\u0012\u00020\u0017\u0018\u00010!H\u0016J\b\u0010\"\u001a\u00020\u0015H\u0002J\b\u0010#\u001a\u00020\u0015H\u0002J\u0006\u0010$\u001a\u00020\u0015J\b\u0010%\u001a\u00020\u0015H\u0002R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00070\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\'"}, d2 = {"Lcom/hank/clawlive/billing/BillingManager;", "Lcom/android/billingclient/api/PurchasesUpdatedListener;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "_subscriptionState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/hank/clawlive/billing/SubscriptionState;", "billingClient", "Lcom/android/billingclient/api/BillingClient;", "productDetails", "Lcom/android/billingclient/api/ProductDetails;", "scope", "Lkotlinx/coroutines/CoroutineScope;", "subscriptionState", "Lkotlinx/coroutines/flow/StateFlow;", "getSubscriptionState", "()Lkotlinx/coroutines/flow/StateFlow;", "usageManager", "Lcom/hank/clawlive/data/local/UsageManager;", "acknowledgePurchase", "", "purchase", "Lcom/android/billingclient/api/Purchase;", "connectToBillingService", "destroy", "launchPurchaseFlow", "activity", "Landroid/app/Activity;", "onPurchasesUpdated", "billingResult", "Lcom/android/billingclient/api/BillingResult;", "purchases", "", "queryProductDetails", "querySubscriptionStatus", "refreshState", "updateState", "Companion", "app_release"})
public final class BillingManager implements com.android.billingclient.api.PurchasesUpdatedListener {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SUBSCRIPTION_ID = "e_claw_premium";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "BillingManager";
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.hank.clawlive.billing.BillingManager instance;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hank.clawlive.data.local.UsageManager usageManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.hank.clawlive.billing.SubscriptionState> _subscriptionState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.hank.clawlive.billing.SubscriptionState> subscriptionState = null;
    @org.jetbrains.annotations.NotNull()
    private com.android.billingclient.api.BillingClient billingClient;
    @org.jetbrains.annotations.Nullable()
    private com.android.billingclient.api.ProductDetails productDetails;
    @org.jetbrains.annotations.NotNull()
    public static final com.hank.clawlive.billing.BillingManager.Companion Companion = null;
    
    public BillingManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.hank.clawlive.billing.SubscriptionState> getSubscriptionState() {
        return null;
    }
    
    /**
     * Connect to Google Play Billing service
     */
    public final void connectToBillingService() {
    }
    
    /**
     * Query current subscription status
     */
    private final void querySubscriptionStatus() {
    }
    
    /**
     * Query product details for display
     */
    private final void queryProductDetails() {
    }
    
    /**
     * Launch subscription purchase flow
     */
    public final void launchPurchaseFlow(@org.jetbrains.annotations.NotNull()
    android.app.Activity activity) {
    }
    
    /**
     * Handle purchase updates from Google Play
     */
    @java.lang.Override()
    public void onPurchasesUpdated(@org.jetbrains.annotations.NotNull()
    com.android.billingclient.api.BillingResult billingResult, @org.jetbrains.annotations.Nullable()
    java.util.List<? extends com.android.billingclient.api.Purchase> purchases) {
    }
    
    /**
     * Acknowledge purchase to prevent refund
     */
    private final void acknowledgePurchase(com.android.billingclient.api.Purchase purchase) {
    }
    
    /**
     * Update subscription state for UI
     */
    private final void updateState() {
    }
    
    /**
     * Refresh state (call when UI is shown)
     */
    public final void refreshState() {
    }
    
    /**
     * Clean up resources
     */
    public final void destroy() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\b\u001a\u00020\u00072\u0006\u0010\t\u001a\u00020\nR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/hank/clawlive/billing/BillingManager$Companion;", "", "()V", "SUBSCRIPTION_ID", "", "TAG", "instance", "Lcom/hank/clawlive/billing/BillingManager;", "getInstance", "context", "Landroid/content/Context;", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.hank.clawlive.billing.BillingManager getInstance(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
    }
}