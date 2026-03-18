import { create } from 'zustand';

export interface RichEmbed {
  title?: string;
  description?: string;
  color?: string;
  url?: string;
  thumbnail?: string;
  fields?: Array<{ name: string; value: string; inline?: boolean }>;
}

export interface RichButton {
  label: string;
  action: 'url' | 'callback';
  value: string;
}

export interface RichQuickReply {
  label: string;
  value: string;
}

export interface RichContent {
  embeds?: RichEmbed[];
  buttons?: RichButton[];
  quickReplies?: RichQuickReply[];
}

export interface ChatMessage {
  id: string;
  entityId: string;
  sender: 'user' | 'bot';
  content: string;
  timestamp: number;
  mediaUrl?: string;
  mediaType?: 'image' | 'audio' | 'video';
  isRead?: boolean;
  richContent?: RichContent;
}

interface ChatState {
  messagesByEntity: Record<string, ChatMessage[]>;
  unreadCounts: Record<string, number>;
  isLoadingHistory: boolean;

  // Actions
  setMessages: (entityId: string, messages: ChatMessage[]) => void;
  appendMessage: (message: ChatMessage) => void;
  prependMessages: (entityId: string, messages: ChatMessage[]) => void;
  markRead: (entityId: string) => void;
  setLoadingHistory: (loading: boolean) => void;
  incrementUnread: (entityId: string) => void;
  clearMessages: (entityId: string) => void;
}

export const useChatStore = create<ChatState>((set) => ({
  messagesByEntity: {},
  unreadCounts: {},
  isLoadingHistory: false,

  setMessages: (entityId, messages) =>
    set((state) => ({
      messagesByEntity: { ...state.messagesByEntity, [entityId]: messages },
    })),

  appendMessage: (message) =>
    set((state) => {
      const existing = state.messagesByEntity[message.entityId] ?? [];
      return {
        messagesByEntity: {
          ...state.messagesByEntity,
          [message.entityId]: [...existing, message],
        },
      };
    }),

  prependMessages: (entityId, messages) =>
    set((state) => {
      const existing = state.messagesByEntity[entityId] ?? [];
      return {
        messagesByEntity: {
          ...state.messagesByEntity,
          [entityId]: [...messages, ...existing],
        },
      };
    }),

  markRead: (entityId) =>
    set((state) => ({
      unreadCounts: { ...state.unreadCounts, [entityId]: 0 },
    })),

  setLoadingHistory: (isLoadingHistory) => set({ isLoadingHistory }),

  incrementUnread: (entityId) =>
    set((state) => ({
      unreadCounts: {
        ...state.unreadCounts,
        [entityId]: (state.unreadCounts[entityId] ?? 0) + 1,
      },
    })),

  clearMessages: (entityId) =>
    set((state) => {
      const updated = { ...state.messagesByEntity };
      delete updated[entityId];
      return { messagesByEntity: updated };
    }),
}));
