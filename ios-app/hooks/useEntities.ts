import { useEffect, useCallback } from 'react';
import { useEntityStore } from '../store/entityStore';
import { useAuthStore } from '../store/authStore';
import { deviceApi } from '../services/api';
import { socketService } from '../services/socketService';
import { EntityUpdate } from '../services/socketService';

const POLL_INTERVAL = 5000; // 5 seconds, same as Android Wallpaper Service

export function useEntities() {
  const { deviceId } = useAuthStore();
  const { entities, isLoading, setEntities, updateEntity, setLoading } = useEntityStore();

  const fetchEntities = useCallback(async () => {
    if (!deviceId) return;
    try {
      const res = await deviceApi.getEntities(deviceId);
      setEntities(res.data.entities ?? []);
    } catch (error) {
      console.error('[ENTITY] Failed to fetch entities:', error);
    }
  }, [deviceId, setEntities]);

  // Initial fetch + polling (matches Android's 5s interval)
  useEffect(() => {
    if (!deviceId) return;

    setLoading(true);
    fetchEntities().finally(() => setLoading(false));

    const interval = setInterval(fetchEntities, POLL_INTERVAL);
    return () => clearInterval(interval);
  }, [deviceId, fetchEntities, setLoading]);

  // Real-time updates via Socket.IO
  useEffect(() => {
    const unsubscribe = socketService.on<EntityUpdate>('entity:update', (data) => {
      if (data.entityId) {
        updateEntity(data.entityId, data);
      }
    });
    return unsubscribe;
  }, [updateEntity]);

  return { entities, isLoading, refetch: fetchEntities };
}
