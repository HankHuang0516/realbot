package com.hank.clawlive.data.remote;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\bf\u0018\u00002\u00020\u0001J.\u0010\u0002\u001a\u00020\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0003\u0010\u0006\u001a\u00020\u00072\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u0005H\u00a7@\u00a2\u0006\u0002\u0010\tJ\u001a\u0010\n\u001a\u00020\u000b2\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u0005H\u00a7@\u00a2\u0006\u0002\u0010\fJ\u0018\u0010\r\u001a\u00020\u00032\b\b\u0001\u0010\u000e\u001a\u00020\u000fH\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u0018\u0010\u0011\u001a\u00020\u00122\b\b\u0001\u0010\u000e\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u0014J$\u0010\u0015\u001a\u00020\u00162\u0014\b\u0001\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u0018H\u00a7@\u00a2\u0006\u0002\u0010\u0019J)\u0010\u001a\u001a\u00020\u00162\u0019\b\u0001\u0010\u0017\u001a\u0013\u0012\u0004\u0012\u00020\u0005\u0012\t\u0012\u00070\u0001\u00a2\u0006\u0002\b\u001b0\u0018H\u00a7@\u00a2\u0006\u0002\u0010\u0019J)\u0010\u001c\u001a\u00020\u001d2\u0019\b\u0001\u0010\u0017\u001a\u0013\u0012\u0004\u0012\u00020\u0005\u0012\t\u0012\u00070\u0001\u00a2\u0006\u0002\b\u001b0\u0018H\u00a7@\u00a2\u0006\u0002\u0010\u0019J$\u0010\u001e\u001a\u00020\u00162\u0014\b\u0001\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u0018H\u00a7@\u00a2\u0006\u0002\u0010\u0019J$\u0010\u001f\u001a\u00020\u00162\u0014\b\u0001\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u0018H\u00a7@\u00a2\u0006\u0002\u0010\u0019\u00a8\u0006 "}, d2 = {"Lcom/hank/clawlive/data/remote/ClawApiService;", "", "getAgentStatus", "Lcom/hank/clawlive/data/model/AgentStatus;", "deviceId", "", "entityId", "", "appVersion", "(Ljava/lang/String;ILjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllEntities", "Lcom/hank/clawlive/data/model/MultiEntityResponse;", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getDeviceStatus", "request", "Lcom/hank/clawlive/data/model/DeviceStatusRequest;", "(Lcom/hank/clawlive/data/model/DeviceStatusRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "registerDevice", "Lcom/hank/clawlive/data/model/RegisterResponse;", "Lcom/hank/clawlive/data/model/RegisterRequest;", "(Lcom/hank/clawlive/data/model/RegisterRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "removeEntity", "Lcom/hank/clawlive/data/model/ApiResponse;", "body", "", "(Ljava/util/Map;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "removeEntityByDevice", "Lkotlin/jvm/JvmSuppressWildcards;", "sendClientMessage", "Lcom/hank/clawlive/data/model/ClientMessageResponse;", "sendFeedback", "wakeUpAgent", "app_debug"})
public abstract interface ClawApiService {
    
    @retrofit2.http.GET(value = "api/status")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAgentStatus(@retrofit2.http.Query(value = "deviceId")
    @org.jetbrains.annotations.NotNull()
    java.lang.String deviceId, @retrofit2.http.Query(value = "entityId")
    int entityId, @retrofit2.http.Query(value = "appVersion")
    @org.jetbrains.annotations.Nullable()
    java.lang.String appVersion, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.hank.clawlive.data.model.AgentStatus> $completion);
    
    @retrofit2.http.POST(value = "api/device/register")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object registerDevice(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.hank.clawlive.data.model.RegisterRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.hank.clawlive.data.model.RegisterResponse> $completion);
    
    @retrofit2.http.POST(value = "api/device/status")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getDeviceStatus(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.hank.clawlive.data.model.DeviceStatusRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.hank.clawlive.data.model.AgentStatus> $completion);
    
    @retrofit2.http.POST(value = "api/wakeup")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object wakeUpAgent(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.hank.clawlive.data.model.ApiResponse> $completion);
    
    @retrofit2.http.POST(value = "api/client/speak")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object sendClientMessage(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.Object> body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.hank.clawlive.data.model.ClientMessageResponse> $completion);
    
    @retrofit2.http.GET(value = "api/entities")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAllEntities(@retrofit2.http.Query(value = "deviceId")
    @org.jetbrains.annotations.Nullable()
    java.lang.String deviceId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.hank.clawlive.data.model.MultiEntityResponse> $completion);
    
    @retrofit2.http.HTTP(method = "DELETE", path = "api/entity", hasBody = true)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object removeEntity(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.hank.clawlive.data.model.ApiResponse> $completion);
    
    @retrofit2.http.HTTP(method = "DELETE", path = "api/device/entity", hasBody = true)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object removeEntityByDevice(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.Object> body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.hank.clawlive.data.model.ApiResponse> $completion);
    
    @retrofit2.http.POST(value = "api/feedback")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object sendFeedback(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.hank.clawlive.data.model.ApiResponse> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}