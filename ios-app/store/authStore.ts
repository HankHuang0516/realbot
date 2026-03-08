import { create } from 'zustand';
import * as SecureStore from 'expo-secure-store';
import { STORAGE_KEYS } from '../services/api';

interface AuthState {
  deviceId: string | null;
  deviceSecret: string | null;
  isInitialized: boolean;
  language: string;

  // Actions
  initialize: () => Promise<void>;
  setCredentials: (deviceId: string, deviceSecret: string) => Promise<void>;
  clearCredentials: () => Promise<void>;
  setLanguage: (lang: string) => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  deviceId: null,
  deviceSecret: null,
  isInitialized: false,
  language: 'system',

  initialize: async () => {
    const deviceId = await SecureStore.getItemAsync(STORAGE_KEYS.DEVICE_ID);
    const deviceSecret = await SecureStore.getItemAsync(STORAGE_KEYS.DEVICE_SECRET);
    set({ deviceId, deviceSecret, isInitialized: true });
  },

  setCredentials: async (deviceId: string, deviceSecret: string) => {
    await SecureStore.setItemAsync(STORAGE_KEYS.DEVICE_ID, deviceId);
    await SecureStore.setItemAsync(STORAGE_KEYS.DEVICE_SECRET, deviceSecret);
    set({ deviceId, deviceSecret });
  },

  clearCredentials: async () => {
    await SecureStore.deleteItemAsync(STORAGE_KEYS.DEVICE_ID);
    await SecureStore.deleteItemAsync(STORAGE_KEYS.DEVICE_SECRET);
    set({ deviceId: null, deviceSecret: null });
  },

  setLanguage: (lang: string) => set({ language: lang }),
}));
