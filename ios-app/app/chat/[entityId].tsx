import React, { useState, useRef, useCallback } from 'react';
import {
  View,
  FlatList,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
  TouchableOpacity,
  Alert,
} from 'react-native';
import {
  Text,
  TextInput,
  IconButton,
  useTheme,
  ActivityIndicator,
  Snackbar,
} from 'react-native-paper';
import { useLocalSearchParams, useNavigation } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { SafeAreaView } from 'react-native-safe-area-context';
import * as ImagePicker from 'expo-image-picker';
import { useChat } from '../../hooks/useChat';
import { useEntityStore } from '../../store/entityStore';
import { chatApi } from '../../services/api';
import { ChatMessage } from '../../store/chatStore';

function ChatBubble({ message, isMe }: { message: ChatMessage; isMe: boolean }) {
  const theme = useTheme();
  const time = new Date(message.timestamp).toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <View style={[styles.bubbleRow, isMe ? styles.rowRight : styles.rowLeft]}>
      <View
        style={[
          styles.bubble,
          isMe
            ? { backgroundColor: theme.colors.primary }
            : { backgroundColor: theme.colors.surfaceVariant },
        ]}
      >
        {message.mediaType === 'image' && message.mediaUrl ? (
          <Text style={{ color: isMe ? 'white' : theme.colors.onSurfaceVariant }}>
            📷 Photo
          </Text>
        ) : message.mediaType === 'audio' ? (
          <Text style={{ color: isMe ? 'white' : theme.colors.onSurfaceVariant }}>
            🎙 Voice Message
          </Text>
        ) : (
          <Text style={{ color: isMe ? 'white' : theme.colors.onSurface }}>
            {message.content}
          </Text>
        )}
        <Text
          style={[
            styles.timestamp,
            { color: isMe ? 'rgba(255,255,255,0.7)' : theme.colors.onSurfaceVariant },
          ]}
        >
          {time}
        </Text>
      </View>
    </View>
  );
}

export default function ChatScreen() {
  const { entityId } = useLocalSearchParams<{ entityId: string }>();
  const { t } = useTranslation();
  const theme = useTheme();
  const navigation = useNavigation();
  const { entities } = useEntityStore();
  const entity = entities.find((e) => e.entityId === entityId) ?? null;

  const { messages, isLoadingHistory, loadMore, sendMessage } = useChat(entityId ?? null);

  const [inputText, setInputText] = useState('');
  const [isSending, setIsSending] = useState(false);
  const [snack, setSnack] = useState('');
  const flatListRef = useRef<FlatList>(null);

  // Set header title
  React.useLayoutEffect(() => {
    if (entity) {
      navigation.setOptions({ title: entity.name, headerShown: true });
    }
  }, [entity, navigation]);

  const handleSend = async () => {
    const text = inputText.trim();
    if (!text || isSending) return;
    setInputText('');
    setIsSending(true);
    try {
      await sendMessage(text);
    } catch {
      setSnack(t('chat.failed_to_send'));
    } finally {
      setIsSending(false);
    }
  };

  const handleAttachPhoto = async () => {
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      quality: 0.8,
    });
    if (!result.canceled && result.assets[0] && entityId) {
      const asset = result.assets[0];
      const formData = new FormData();
      formData.append('file', {
        uri: asset.uri,
        type: 'image/jpeg',
        name: 'photo.jpg',
      } as unknown as Blob);
      try {
        await chatApi.uploadMedia(entityId, formData);
      } catch {
        setSnack(t('errors.server'));
      }
    }
  };

  const handleTakePhoto = async () => {
    const perm = await ImagePicker.requestCameraPermissionsAsync();
    if (!perm.granted) {
      Alert.alert(t('errors.unknown'), 'Camera permission denied');
      return;
    }
    const result = await ImagePicker.launchCameraAsync({ quality: 0.8 });
    if (!result.canceled && result.assets[0] && entityId) {
      const asset = result.assets[0];
      const formData = new FormData();
      formData.append('file', {
        uri: asset.uri,
        type: 'image/jpeg',
        name: 'photo.jpg',
      } as unknown as Blob);
      try {
        await chatApi.uploadMedia(entityId, formData);
      } catch {
        setSnack(t('errors.server'));
      }
    }
  };

  const renderItem = useCallback(
    ({ item }: { item: ChatMessage }) => (
      <ChatBubble message={item} isMe={item.sender === 'user'} />
    ),
    []
  );

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: theme.colors.background }} edges={['bottom']}>
      <KeyboardAvoidingView
        style={{ flex: 1 }}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        keyboardVerticalOffset={90}
      >
        {/* Messages */}
        {isLoadingHistory && messages.length === 0 ? (
          <View style={styles.centered}>
            <ActivityIndicator />
          </View>
        ) : (
          <FlatList
            ref={flatListRef}
            data={messages}
            keyExtractor={(item) => item.id}
            renderItem={renderItem}
            onEndReached={loadMore}
            onEndReachedThreshold={0.1}
            contentContainerStyle={styles.messageList}
            inverted={false}
            ListEmptyComponent={
              <View style={styles.centered}>
                <Text variant="bodyLarge">
                  {t('chat.no_messages_desc', { name: entity?.name ?? '' })}
                </Text>
              </View>
            }
          />
        )}

        {/* Input bar */}
        <View style={[styles.inputBar, { backgroundColor: theme.colors.surface }]}>
          <IconButton icon="camera" onPress={handleTakePhoto} size={22} />
          <IconButton icon="image" onPress={handleAttachPhoto} size={22} />
          <TextInput
            mode="outlined"
            placeholder={t('chat.placeholder')}
            value={inputText}
            onChangeText={setInputText}
            style={styles.input}
            dense
            multiline
            maxLength={2000}
            onSubmitEditing={handleSend}
          />
          <IconButton
            icon="send"
            onPress={handleSend}
            disabled={!inputText.trim() || isSending}
            loading={isSending}
            iconColor={theme.colors.primary}
            size={24}
          />
        </View>
      </KeyboardAvoidingView>

      <Snackbar visible={!!snack} onDismiss={() => setSnack('')} duration={2000}>
        {snack}
      </Snackbar>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 32 },
  messageList: { padding: 12, paddingBottom: 8 },
  bubbleRow: { marginVertical: 4, maxWidth: '80%' },
  rowLeft: { alignSelf: 'flex-start' },
  rowRight: { alignSelf: 'flex-end' },
  bubble: {
    borderRadius: 18,
    paddingHorizontal: 14,
    paddingVertical: 8,
    gap: 4,
  },
  timestamp: { fontSize: 10, alignSelf: 'flex-end', marginTop: 2 },
  inputBar: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    paddingHorizontal: 4,
    paddingVertical: 6,
    borderTopWidth: StyleSheet.hairlineWidth,
    borderTopColor: 'rgba(0,0,0,0.1)',
  },
  input: { flex: 1, maxHeight: 120 },
});
