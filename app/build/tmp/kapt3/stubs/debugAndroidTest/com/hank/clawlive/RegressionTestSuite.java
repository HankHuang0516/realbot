package com.hank.clawlive;

/**
 * Regression Test Suite (回歸測試套件)
 *
 * This suite runs all critical UI and functionality tests to ensure
 * no regressions are introduced during development.
 *
 * Run with:
 * ./gradlew connectedDebugAndroidTest --tests "*.RegressionTestSuite"
 */
@org.junit.runner.RunWith(value = org.junit.runners.Suite.class)
@org.junit.runners.Suite.SuiteClasses(value = {com.hank.clawlive.MainActivityUiTest.class, com.hank.clawlive.SettingsActivityUiTest.class, com.hank.clawlive.EntityManagerActivityUiTest.class, com.hank.clawlive.DebugRenderActivityUiTest.class, com.hank.clawlive.WallpaperPreviewUiTest.class, com.hank.clawlive.BilingualVerificationTest.class})
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/hank/clawlive/RegressionTestSuite;", "", "()V", "app_debugAndroidTest"})
public final class RegressionTestSuite {
    
    public RegressionTestSuite() {
        super();
    }
}