package com.hank.clawlive.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.hank.clawlive.data.local.UsageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume

/**
 * Manages Google Play Billing for subscription purchases.
 * Handles connection, purchase, acknowledgment, and restoration.
 */
class BillingManager(private val context: Context) : PurchasesUpdatedListener {

    companion object {
        const val SUBSCRIPTION_ID = "e_claw_premium"
        private const val TAG = "BillingManager"

        @Volatile
        private var instance: BillingManager? = null

        fun getInstance(context: Context): BillingManager {
            return instance ?: synchronized(this) {
                instance ?: BillingManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.Main)
    private val usageManager = UsageManager.getInstance(context)

    private val _subscriptionState = MutableStateFlow(SubscriptionState())
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private var productDetails: ProductDetails? = null

    init {
        connectToBillingService()
    }

    /**
     * Connect to Google Play Billing service
     */
    fun connectToBillingService() {
        if (billingClient.isReady) {
            querySubscriptionStatus()
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.tag(TAG).d("Billing service connected")
                    querySubscriptionStatus()
                    queryProductDetails()
                } else {
                    Timber.tag(TAG).e("Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Timber.tag(TAG).w("Billing service disconnected")
                // Reconnect on next operation
            }
        })
    }

    /**
     * Query current subscription status
     */
    private fun querySubscriptionStatus() {
        scope.launch {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val hasActiveSubscription = purchases.any { purchase ->
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        purchase.products.contains(SUBSCRIPTION_ID)
                    }

                    usageManager.isPremium = hasActiveSubscription
                    updateState()

                    // Acknowledge any unacknowledged purchases
                    purchases.filter { !it.isAcknowledged }.forEach { purchase ->
                        acknowledgePurchase(purchase)
                    }

                    Timber.tag(TAG).d("Subscription status: $hasActiveSubscription")
                }
            }
        }
    }

    /**
     * Query product details for display
     */
    private fun queryProductDetails() {
        scope.launch {
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(SUBSCRIPTION_ID)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    productDetails = productDetailsList.firstOrNull()
                    productDetails?.let {
                        val price = it.subscriptionOfferDetails
                            ?.firstOrNull()
                            ?.pricingPhases
                            ?.pricingPhaseList
                            ?.firstOrNull()
                            ?.formattedPrice ?: ""
                        
                        _subscriptionState.value = _subscriptionState.value.copy(
                            subscriptionPrice = price
                        )
                        Timber.tag(TAG).d("Product details loaded: $price")
                    }
                }
            }
        }
    }

    /**
     * Launch subscription purchase flow
     */
    fun launchPurchaseFlow(activity: Activity) {
        val details = productDetails
        if (details == null) {
            Timber.tag(TAG).e("Product details not available")
            return
        }

        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken == null) {
            Timber.tag(TAG).e("Offer token not available")
            return
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val result = billingClient.launchBillingFlow(activity, billingFlowParams)
        Timber.tag(TAG).d("Launch purchase flow result: ${result.responseCode}")
    }

    /**
     * Handle purchase updates from Google Play
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        if (!purchase.isAcknowledged) {
                            acknowledgePurchase(purchase)
                        }
                        if (purchase.products.contains(SUBSCRIPTION_ID)) {
                            usageManager.isPremium = true
                            updateState()
                        }
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Timber.tag(TAG).d("Purchase cancelled by user")
            }
            else -> {
                Timber.tag(TAG).e("Purchase failed: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Acknowledge purchase to prevent refund
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        scope.launch {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient.acknowledgePurchase(params) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.tag(TAG).d("Purchase acknowledged")
                }
            }
        }
    }

    /**
     * Update subscription state for UI
     */
    private fun updateState() {
        _subscriptionState.value = SubscriptionState(
            isPremium = usageManager.isPremium,
            usageToday = usageManager.dailyMessageCount,
            usageLimit = UsageManager.FREE_TIER_LIMIT,
            canSendMessage = usageManager.canUseMessage(),
            subscriptionPrice = _subscriptionState.value.subscriptionPrice
        )
    }

    /**
     * Refresh state (call when UI is shown)
     */
    fun refreshState() {
        querySubscriptionStatus()
        updateState()
    }

    /**
     * Clean up resources
     */
    fun destroy() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
