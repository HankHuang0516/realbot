import React, { useState, useEffect, useCallback } from 'react';
import { View, ScrollView, StyleSheet, Alert } from 'react-native';
import {
  Text,
  Button,
  TextInput,
  Chip,
  Card,
  IconButton,
  Dialog,
  Portal,
  useTheme,
  ActivityIndicator,
  Snackbar,
  FAB,
} from 'react-native-paper';
import { useTranslation } from 'react-i18next';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useEntityStore } from '../../store/entityStore';
import { missionApi } from '../../services/api';
import { socketService, VarsApprovalRequest } from '../../services/socketService';

type TabKey = 'todo' | 'missions' | 'done' | 'notes' | 'skills' | 'souls' | 'rules' | 'variables';

interface MissionItem {
  id: string;
  title: string;
  status: 'todo' | 'mission' | 'done';
  createdAt: number;
}

interface Dashboard {
  todos: MissionItem[];
  missions: MissionItem[];
  done: MissionItem[];
  notes: Array<{ id: string; content: string }>;
  skills: Array<{ id: string; name: string; description: string }>;
  souls: Array<{ id: string; name: string; personality: string }>;
  rules: Array<{ id: string; content: string; priority: number }>;
  version: number;
}

const TABS: { key: TabKey; i18nKey: string }[] = [
  { key: 'todo', i18nKey: 'mission.todo' },
  { key: 'missions', i18nKey: 'mission.missions' },
  { key: 'done', i18nKey: 'mission.done' },
  { key: 'notes', i18nKey: 'mission.notes' },
  { key: 'skills', i18nKey: 'mission.skills' },
  { key: 'souls', i18nKey: 'mission.souls' },
  { key: 'rules', i18nKey: 'mission.rules' },
  { key: 'variables', i18nKey: 'mission.variables' },
];

export default function MissionScreen() {
  const { t } = useTranslation();
  const theme = useTheme();
  const { entities } = useEntityStore();
  const selectedEntity = entities[0] ?? null;

  const [activeTab, setActiveTab] = useState<TabKey>('todo');
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [snack, setSnack] = useState('');

  // JIT vars approval
  const [varsRequest, setVarsRequest] = useState<VarsApprovalRequest | null>(null);

  const loadDashboard = useCallback(async () => {
    if (!selectedEntity) return;
    setIsLoading(true);
    try {
      const res = await missionApi.getDashboard(selectedEntity.entityId);
      setDashboard(res.data.dashboard);
    } catch {
      setSnack(t('errors.server'));
    } finally {
      setIsLoading(false);
    }
  }, [selectedEntity, t]);

  const saveDashboard = useCallback(async (updated: Dashboard) => {
    if (!selectedEntity || !updated) return;
    try {
      await missionApi.saveDashboard(
        selectedEntity.entityId,
        updated,
        updated.version
      );
      setDashboard(updated);
    } catch {
      setSnack(t('errors.server'));
    }
  }, [selectedEntity, t]);

  useEffect(() => {
    loadDashboard();
  }, [loadDashboard]);

  // Socket.IO: JIT vars approval request
  useEffect(() => {
    const unsubscribe = socketService.on<VarsApprovalRequest>(
      'vars:approval-request',
      (req) => {
        setVarsRequest(req);
      }
    );
    return unsubscribe;
  }, []);

  const handleVarsApproval = (approved: boolean) => {
    if (!varsRequest) return;
    socketService.approveVarsRequest(varsRequest.requestId, approved);
    setVarsRequest(null);
    setSnack(approved ? t('mission.vars_approve') : t('mission.vars_deny'));
  };

  const addTodo = async (title: string) => {
    if (!dashboard || !title.trim()) return;
    const newItem: MissionItem = {
      id: Date.now().toString(),
      title: title.trim(),
      status: 'todo',
      createdAt: Date.now(),
    };
    const updated = {
      ...dashboard,
      todos: [...dashboard.todos, newItem],
      version: dashboard.version + 1,
    };
    await saveDashboard(updated);
  };

  const markDone = async (item: MissionItem) => {
    if (!dashboard) return;
    const updated = {
      ...dashboard,
      todos: dashboard.todos.filter((t) => t.id !== item.id),
      missions: dashboard.missions.filter((m) => m.id !== item.id),
      done: [...dashboard.done, { ...item, status: 'done' as const }],
      version: dashboard.version + 1,
    };
    await saveDashboard(updated);
  };

  const renderTodos = () => {
    const items = dashboard?.todos ?? [];
    return (
      <View style={styles.tabContent}>
        {items.length === 0 ? (
          <Text variant="bodyMedium" style={{ color: theme.colors.onSurfaceVariant, textAlign: 'center' }}>
            {t('mission.no_todos')}
          </Text>
        ) : (
          items.map((item) => (
            <Card key={item.id} style={styles.itemCard}>
              <Card.Content style={styles.itemContent}>
                <Text variant="bodyLarge" style={{ flex: 1 }}>{item.title}</Text>
                <IconButton
                  icon="check"
                  size={20}
                  onPress={() => markDone(item)}
                />
              </Card.Content>
            </Card>
          ))
        )}
      </View>
    );
  };

  const renderNotes = () => {
    const notes = dashboard?.notes ?? [];
    return (
      <View style={styles.tabContent}>
        {notes.map((note) => (
          <Card key={note.id} style={styles.itemCard}>
            <Card.Content>
              <Text variant="bodyMedium">{note.content}</Text>
            </Card.Content>
          </Card>
        ))}
        {notes.length === 0 && (
          <Text variant="bodyMedium" style={{ color: theme.colors.onSurfaceVariant, textAlign: 'center' }}>
            {t('mission.no_notes')}
          </Text>
        )}
      </View>
    );
  };

  if (!selectedEntity) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
        <View style={styles.centered}>
          <Text variant="titleMedium">{t('home.no_entities')}</Text>
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]} edges={['bottom']}>
      {/* Tab bar */}
      <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.tabBar}>
        {TABS.map((tab) => (
          <Chip
            key={tab.key}
            selected={activeTab === tab.key}
            onPress={() => setActiveTab(tab.key)}
            style={styles.tabChip}
            mode={activeTab === tab.key ? 'flat' : 'outlined'}
          >
            {t(tab.i18nKey)}
          </Chip>
        ))}
      </ScrollView>

      {/* Content */}
      {isLoading ? (
        <View style={styles.centered}>
          <ActivityIndicator />
        </View>
      ) : (
        <ScrollView style={styles.content} contentContainerStyle={{ paddingBottom: 100 }}>
          {activeTab === 'todo' && renderTodos()}
          {activeTab === 'notes' && renderNotes()}
          {activeTab === 'missions' && (
            <View style={styles.tabContent}>
              {(dashboard?.missions ?? []).map((m) => (
                <Card key={m.id} style={styles.itemCard}>
                  <Card.Content style={styles.itemContent}>
                    <Text variant="bodyLarge" style={{ flex: 1 }}>{m.title}</Text>
                    <IconButton icon="check" size={20} onPress={() => markDone(m)} />
                  </Card.Content>
                </Card>
              ))}
              {(dashboard?.missions ?? []).length === 0 && (
                <Text variant="bodyMedium" style={{ color: theme.colors.onSurfaceVariant, textAlign: 'center' }}>
                  {t('mission.no_missions')}
                </Text>
              )}
            </View>
          )}
          {['souls', 'skills', 'rules', 'done', 'variables'].includes(activeTab) && (
            <View style={styles.centered}>
              <Text variant="bodyMedium" style={{ color: theme.colors.onSurfaceVariant }}>
                {t(`mission.${activeTab}`)}
              </Text>
            </View>
          )}
        </ScrollView>
      )}

      {/* Add FAB */}
      {(activeTab === 'todo' || activeTab === 'missions' || activeTab === 'notes') && (
        <FAB
          icon="plus"
          style={styles.fab}
          onPress={() => {
            Alert.prompt(
              t(`mission.add_${activeTab === 'todo' ? 'todo' : activeTab === 'notes' ? 'note' : 'mission'}`),
              undefined,
              (text) => {
                if (text) addTodo(text);
              }
            );
          }}
        />
      )}

      {/* JIT Vars Approval Dialog */}
      <Portal>
        <Dialog visible={!!varsRequest} onDismiss={() => setVarsRequest(null)}>
          <Dialog.Title>{t('mission.vars_approval_title')}</Dialog.Title>
          <Dialog.Content>
            <Text variant="bodyMedium">
              {t('mission.vars_approval_desc', {
                entityName: varsRequest?.entityName ?? '',
                varKeys: varsRequest?.varKeys.join(', ') ?? '',
              })}
            </Text>
          </Dialog.Content>
          <Dialog.Actions>
            <Button onPress={() => handleVarsApproval(false)} textColor={theme.colors.error}>
              {t('mission.vars_deny')}
            </Button>
            <Button mode="contained" onPress={() => handleVarsApproval(true)}>
              {t('mission.vars_approve')}
            </Button>
          </Dialog.Actions>
        </Dialog>
      </Portal>

      <Snackbar visible={!!snack} onDismiss={() => setSnack('')} duration={2000}>
        {snack}
      </Snackbar>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 32 },
  tabBar: { paddingHorizontal: 12, paddingVertical: 8, flexGrow: 0 },
  tabChip: { marginRight: 6 },
  content: { flex: 1 },
  tabContent: { padding: 16, gap: 8 },
  itemCard: { marginBottom: 8, borderRadius: 12 },
  itemContent: { flexDirection: 'row', alignItems: 'center' },
  fab: { position: 'absolute', bottom: 24, right: 16 },
});
