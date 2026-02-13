package com.hank.clawlive.data.remote;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\r\u001a\n \u000f*\u0004\u0018\u00010\u000e0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lcom/hank/clawlive/data/remote/NetworkModule;", "", "()V", "BASE_URL", "", "api", "Lcom/hank/clawlive/data/remote/ClawApiService;", "getApi", "()Lcom/hank/clawlive/data/remote/ClawApiService;", "loggingInterceptor", "Lokhttp3/logging/HttpLoggingInterceptor;", "okHttpClient", "Lokhttp3/OkHttpClient;", "retrofit", "Lretrofit2/Retrofit;", "kotlin.jvm.PlatformType", "app_release"})
public final class NetworkModule {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String BASE_URL = "https://eclaw.up.railway.app/";
    @org.jetbrains.annotations.NotNull()
    private static final okhttp3.logging.HttpLoggingInterceptor loggingInterceptor = null;
    @org.jetbrains.annotations.NotNull()
    private static final okhttp3.OkHttpClient okHttpClient = null;
    private static final retrofit2.Retrofit retrofit = null;
    @org.jetbrains.annotations.NotNull()
    private static final com.hank.clawlive.data.remote.ClawApiService api = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.hank.clawlive.data.remote.NetworkModule INSTANCE = null;
    
    private NetworkModule() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hank.clawlive.data.remote.ClawApiService getApi() {
        return null;
    }
}