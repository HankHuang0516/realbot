package com.hank.clawlive;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0086\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010(\u001a\u00020)H\u0002J\u0010\u0010*\u001a\u00020+2\u0006\u0010,\u001a\u00020+H\u0002J\b\u0010-\u001a\u00020)H\u0002J\b\u0010.\u001a\u00020)H\u0002J\b\u0010/\u001a\u00020)H\u0002J\u0012\u00100\u001a\u00020)2\b\u00101\u001a\u0004\u0018\u000102H\u0014J\b\u00103\u001a\u00020)H\u0014J\u0010\u00104\u001a\u00020)2\u0006\u00105\u001a\u000206H\u0002J\b\u00107\u001a\u00020)H\u0002J\b\u00108\u001a\u00020)H\u0002J\b\u00109\u001a\u00020)H\u0002J\b\u0010:\u001a\u00020)H\u0002J\u0010\u0010;\u001a\u00020)2\u0006\u0010<\u001a\u00020=H\u0002J\b\u0010>\u001a\u00020)H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0013\u001a\u00020\u00148BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0017\u0010\u0018\u001a\u0004\b\u0015\u0010\u0016R\u001b\u0010\u0019\u001a\u00020\u001a8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001d\u0010\u0018\u001a\u0004\b\u001b\u0010\u001cR\u000e\u0010\u001e\u001a\u00020\u001fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010 \u001a\u00020!X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\"\u001a\u00020\u001fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010#\u001a\u00020$X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010%\u001a\u00020$X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010&\u001a\u00020\'X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006?"}, d2 = {"Lcom/hank/clawlive/SettingsActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "billingManager", "Lcom/hank/clawlive/billing/BillingManager;", "btnBack", "Landroid/widget/ImageButton;", "btnFeedback", "Lcom/google/android/material/button/MaterialButton;", "btnPrivacyPolicy", "btnSubscribe", "cardSubscription", "Lcom/google/android/material/card/MaterialCardView;", "chipGroupLanguage", "Lcom/google/android/material/chip/ChipGroup;", "chipLangEn", "Lcom/google/android/material/chip/Chip;", "chipLangSystem", "chipLangZh", "deviceManager", "Lcom/hank/clawlive/data/local/DeviceManager;", "getDeviceManager", "()Lcom/hank/clawlive/data/local/DeviceManager;", "deviceManager$delegate", "Lkotlin/Lazy;", "layoutPrefs", "Lcom/hank/clawlive/data/local/LayoutPreferences;", "getLayoutPrefs", "()Lcom/hank/clawlive/data/local/LayoutPreferences;", "layoutPrefs$delegate", "layoutPremiumBadge", "Landroid/widget/LinearLayout;", "progressUsage", "Landroid/widget/ProgressBar;", "topBar", "tvEntityCount", "Landroid/widget/TextView;", "tvUsageCount", "usageManager", "Lcom/hank/clawlive/data/local/UsageManager;", "displayAppVersion", "", "dpToPx", "", "dp", "initViews", "loadCurrentLanguage", "observeSubscriptionState", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onResume", "sendFeedback", "message", "", "setupClickListeners", "setupEdgeToEdgeInsets", "showFeedbackDialog", "updateEntityCount", "updateSubscriptionUi", "state", "Lcom/hank/clawlive/billing/SubscriptionState;", "updateUsageDisplay", "app_debug"})
public final class SettingsActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.hank.clawlive.billing.BillingManager billingManager;
    private com.hank.clawlive.data.local.UsageManager usageManager;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy layoutPrefs$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy deviceManager$delegate = null;
    private com.google.android.material.card.MaterialCardView cardSubscription;
    private android.widget.LinearLayout layoutPremiumBadge;
    private android.widget.TextView tvUsageCount;
    private android.widget.TextView tvEntityCount;
    private android.widget.ProgressBar progressUsage;
    private com.google.android.material.button.MaterialButton btnSubscribe;
    private com.google.android.material.button.MaterialButton btnFeedback;
    private com.google.android.material.button.MaterialButton btnPrivacyPolicy;
    private com.google.android.material.chip.ChipGroup chipGroupLanguage;
    private com.google.android.material.chip.Chip chipLangSystem;
    private com.google.android.material.chip.Chip chipLangEn;
    private com.google.android.material.chip.Chip chipLangZh;
    private android.widget.ImageButton btnBack;
    private android.widget.LinearLayout topBar;
    
    public SettingsActivity() {
        super();
    }
    
    private final com.hank.clawlive.data.local.LayoutPreferences getLayoutPrefs() {
        return null;
    }
    
    private final com.hank.clawlive.data.local.DeviceManager getDeviceManager() {
        return null;
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void setupEdgeToEdgeInsets() {
    }
    
    private final int dpToPx(int dp) {
        return 0;
    }
    
    @java.lang.Override()
    protected void onResume() {
    }
    
    private final void initViews() {
    }
    
    private final void setupClickListeners() {
    }
    
    private final void loadCurrentLanguage() {
    }
    
    private final void updateUsageDisplay() {
    }
    
    private final void observeSubscriptionState() {
    }
    
    private final void updateSubscriptionUi(com.hank.clawlive.billing.SubscriptionState state) {
    }
    
    private final void updateEntityCount() {
    }
    
    private final void showFeedbackDialog() {
    }
    
    private final void sendFeedback(java.lang.String message) {
    }
    
    private final void displayAppVersion() {
    }
}