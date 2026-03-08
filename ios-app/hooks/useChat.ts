import { useEffect, useCallback, useRef } from 'react';
import { useChatStore, ChatMessage } from '../store/chatStore';
import { chatApi } from '../services/api';
import { socketService } from '../services/socketService';

const PAGE_SIZE = 30;

export function useChat(entityId: string | null) {
  const {
    messagesByEntity,
    isLoadingHistory,
    setMessages,
    appendMessage,
    prependMessages,
    setLoadingHistory,
    markRead,
    incrementUnread,
  } = useChatStore();

  const messages = entityId ? (messagesByEntity[entityId] ?? []) : [];
  const oldestIdRef = useRef<string | null>(null);

  // Load initial chat history
  const loadHistory = useCallback(async () => {
    if (!entityId) return;
    setLoadingHistory(true);
    try {
      const res = await chatApi.getHistory(entityId, { limit: PAGE_SIZE });
      const msgs: ChatMessage[] = res.data.messages ?? [];
      setMessages(entityId, msgs);
      if (msgs.length > 0) {
        oldestIdRef.current = msgs[0].id;
      }
      markRead(entityId);
    } catch (error) {
      console.error('[CHAT] Failed to load history:', error);
    } finally {
      setLoadingHistory(false);
    }
  }, [entityId, setMessages, setLoadingHistory, markRead]);

  // Load older messages (pagination)
  const loadMore = useCallback(async () => {
    if (!entityId || !oldestIdRef.current || isLoadingHistory) return;
    setLoadingHistory(true);
    try {
      const res = await chatApi.getHistory(entityId, {
        before: oldestIdRef.current,
        limit: PAGE_SIZE,
      });
      const older: ChatMessage[] = res.data.messages ?? [];
      if (older.length > 0) {
        prependMessages(entityId, older);
        oldestIdRef.current = older[0].id;
      }
    } catch (error) {
      console.error('[CHAT] Failed to load more:', error);
    } finally {
      setLoadingHistory(false);
    }
  }, [entityId, isLoadingHistory, prependMessages, setLoadingHistory]);

  // Send text message
  const sendMessage = useCallback(
    async (text: string) => {
      if (!entityId || !text.trim()) return;
      try {
        await chatApi.speak({ entityId, message: text });
      } catch (error) {
        console.error('[CHAT] Failed to send message:', error);
        throw error;
      }
    },
    [entityId]
  );

  // Real-time incoming messages via Socket.IO
  useEffect(() => {
    const unsubscribe = socketService.on<ChatMessage>('chat:message', (msg) => {
      if (entityId && msg.entityId === entityId) {
        appendMessage(msg);
        markRead(entityId);
      } else if (msg.entityId) {
        // Message for another entity — increment unread
        incrementUnread(msg.entityId);
      }
    });
    return unsubscribe;
  }, [entityId, appendMessage, markRead, incrementUnread]);

  // Load history when entityId changes
  useEffect(() => {
    if (entityId) {
      loadHistory();
    }
  }, [entityId, loadHistory]);

  return {
    messages,
    isLoadingHistory,
    loadHistory,
    loadMore,
    sendMessage,
  };
}
