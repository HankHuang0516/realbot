package com.hank.clawlive.ui.chat;

/**
 * Adapter for chat messages with dual ViewHolder types (sent/received)
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u0000 \u00102\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00030\u0001:\u0004\u000f\u0010\u0011\u0012B\u0005\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0006H\u0016J\u0018\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u00032\u0006\u0010\u0007\u001a\u00020\u0006H\u0016J\u0018\u0010\u000b\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u0006H\u0016\u00a8\u0006\u0013"}, d2 = {"Lcom/hank/clawlive/ui/chat/ChatAdapter;", "Landroidx/recyclerview/widget/ListAdapter;", "Lcom/hank/clawlive/data/local/database/ChatMessage;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "()V", "getItemViewType", "", "position", "onBindViewHolder", "", "holder", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "ChatDiffCallback", "Companion", "ReceivedMessageViewHolder", "SentMessageViewHolder", "app_debug"})
public final class ChatAdapter extends androidx.recyclerview.widget.ListAdapter<com.hank.clawlive.data.local.database.ChatMessage, androidx.recyclerview.widget.RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SENT = 0;
    private static final int VIEW_TYPE_RECEIVED = 1;
    @org.jetbrains.annotations.NotNull()
    public static final com.hank.clawlive.ui.chat.ChatAdapter.Companion Companion = null;
    
    public ChatAdapter() {
        super(null);
    }
    
    @java.lang.Override()
    public int getItemViewType(int position) {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public androidx.recyclerview.widget.RecyclerView.ViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    androidx.recyclerview.widget.RecyclerView.ViewHolder holder, int position) {
    }
    
    /**
     * DiffUtil callback for efficient list updates
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00022\u0006\u0010\u0007\u001a\u00020\u0002H\u0016J\u0018\u0010\b\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00022\u0006\u0010\u0007\u001a\u00020\u0002H\u0016\u00a8\u0006\t"}, d2 = {"Lcom/hank/clawlive/ui/chat/ChatAdapter$ChatDiffCallback;", "Landroidx/recyclerview/widget/DiffUtil$ItemCallback;", "Lcom/hank/clawlive/data/local/database/ChatMessage;", "()V", "areContentsTheSame", "", "oldItem", "newItem", "areItemsTheSame", "app_debug"})
    public static final class ChatDiffCallback extends androidx.recyclerview.widget.DiffUtil.ItemCallback<com.hank.clawlive.data.local.database.ChatMessage> {
        
        public ChatDiffCallback() {
            super();
        }
        
        @java.lang.Override()
        public boolean areItemsTheSame(@org.jetbrains.annotations.NotNull()
        com.hank.clawlive.data.local.database.ChatMessage oldItem, @org.jetbrains.annotations.NotNull()
        com.hank.clawlive.data.local.database.ChatMessage newItem) {
            return false;
        }
        
        @java.lang.Override()
        public boolean areContentsTheSame(@org.jetbrains.annotations.NotNull()
        com.hank.clawlive.data.local.database.ChatMessage oldItem, @org.jetbrains.annotations.NotNull()
        com.hank.clawlive.data.local.database.ChatMessage newItem) {
            return false;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/hank/clawlive/ui/chat/ChatAdapter$Companion;", "", "()V", "VIEW_TYPE_RECEIVED", "", "VIEW_TYPE_SENT", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    /**
     * ViewHolder for received messages (left-aligned, gray bubble)
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/hank/clawlive/ui/chat/ChatAdapter$ReceivedMessageViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "itemView", "Landroid/view/View;", "(Landroid/view/View;)V", "tvAvatar", "Landroid/widget/TextView;", "tvEntityName", "tvMessage", "tvTime", "bind", "", "message", "Lcom/hank/clawlive/data/local/database/ChatMessage;", "app_debug"})
    public static final class ReceivedMessageViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvAvatar = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvEntityName = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvMessage = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvTime = null;
        
        public ReceivedMessageViewHolder(@org.jetbrains.annotations.NotNull()
        android.view.View itemView) {
            super(null);
        }
        
        public final void bind(@org.jetbrains.annotations.NotNull()
        com.hank.clawlive.data.local.database.ChatMessage message) {
        }
    }
    
    /**
     * ViewHolder for sent messages (right-aligned, green bubble)
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/hank/clawlive/ui/chat/ChatAdapter$SentMessageViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "itemView", "Landroid/view/View;", "(Landroid/view/View;)V", "tvMessage", "Landroid/widget/TextView;", "tvTargets", "tvTime", "bind", "", "message", "Lcom/hank/clawlive/data/local/database/ChatMessage;", "app_debug"})
    public static final class SentMessageViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvMessage = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvTime = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvTargets = null;
        
        public SentMessageViewHolder(@org.jetbrains.annotations.NotNull()
        android.view.View itemView) {
            super(null);
        }
        
        public final void bind(@org.jetbrains.annotations.NotNull()
        com.hank.clawlive.data.local.database.ChatMessage message) {
        }
    }
}