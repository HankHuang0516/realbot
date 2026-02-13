package com.hank.clawlive.data.local.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Boolean;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@SuppressWarnings({"unchecked", "deprecation"})
public final class ChatMessageDao_Impl implements ChatMessageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ChatMessage> __insertionAdapterOfChatMessage;

  private final Converters __converters = new Converters();

  private final SharedSQLiteStatement __preparedStmtOfMarkSynced;

  private final SharedSQLiteStatement __preparedStmtOfPruneOldMessages;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public ChatMessageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfChatMessage = new EntityInsertionAdapter<ChatMessage>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `chat_messages` (`id`,`text`,`timestamp`,`isFromUser`,`messageType`,`source`,`targetEntityIds`,`fromEntityId`,`fromEntityName`,`fromEntityCharacter`,`deduplicationKey`,`isSynced`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChatMessage entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getText() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getText());
        }
        statement.bindLong(3, entity.getTimestamp());
        final int _tmp = entity.isFromUser() ? 1 : 0;
        statement.bindLong(4, _tmp);
        final String _tmp_1 = __converters.fromMessageType(entity.getMessageType());
        if (_tmp_1 == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, _tmp_1);
        }
        if (entity.getSource() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getSource());
        }
        if (entity.getTargetEntityIds() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getTargetEntityIds());
        }
        if (entity.getFromEntityId() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getFromEntityId());
        }
        if (entity.getFromEntityName() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getFromEntityName());
        }
        if (entity.getFromEntityCharacter() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getFromEntityCharacter());
        }
        if (entity.getDeduplicationKey() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getDeduplicationKey());
        }
        final int _tmp_2 = entity.isSynced() ? 1 : 0;
        statement.bindLong(12, _tmp_2);
      }
    };
    this.__preparedStmtOfMarkSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE chat_messages SET isSynced = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfPruneOldMessages = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        DELETE FROM chat_messages\n"
                + "        WHERE id NOT IN (SELECT id FROM chat_messages ORDER BY timestamp DESC LIMIT ?)\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM chat_messages";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ChatMessage message, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfChatMessage.insertAndReturnId(message);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<ChatMessage> messages,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfChatMessage.insert(messages);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markSynced(final long messageId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkSynced.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, messageId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkSynced.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object pruneOldMessages(final int keepCount,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfPruneOldMessages.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, keepCount);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfPruneOldMessages.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ChatMessage>> getRecentMessages(final int limit) {
    final String _sql = "SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_messages"}, new Callable<List<ChatMessage>>() {
      @Override
      @NonNull
      public List<ChatMessage> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfIsFromUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isFromUser");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTargetEntityIds = CursorUtil.getColumnIndexOrThrow(_cursor, "targetEntityIds");
          final int _cursorIndexOfFromEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "fromEntityId");
          final int _cursorIndexOfFromEntityName = CursorUtil.getColumnIndexOrThrow(_cursor, "fromEntityName");
          final int _cursorIndexOfFromEntityCharacter = CursorUtil.getColumnIndexOrThrow(_cursor, "fromEntityCharacter");
          final int _cursorIndexOfDeduplicationKey = CursorUtil.getColumnIndexOrThrow(_cursor, "deduplicationKey");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final List<ChatMessage> _result = new ArrayList<ChatMessage>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatMessage _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpText;
            if (_cursor.isNull(_cursorIndexOfText)) {
              _tmpText = null;
            } else {
              _tmpText = _cursor.getString(_cursorIndexOfText);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final boolean _tmpIsFromUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFromUser);
            _tmpIsFromUser = _tmp != 0;
            final MessageType _tmpMessageType;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfMessageType)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfMessageType);
            }
            _tmpMessageType = __converters.toMessageType(_tmp_1);
            final String _tmpSource;
            if (_cursor.isNull(_cursorIndexOfSource)) {
              _tmpSource = null;
            } else {
              _tmpSource = _cursor.getString(_cursorIndexOfSource);
            }
            final String _tmpTargetEntityIds;
            if (_cursor.isNull(_cursorIndexOfTargetEntityIds)) {
              _tmpTargetEntityIds = null;
            } else {
              _tmpTargetEntityIds = _cursor.getString(_cursorIndexOfTargetEntityIds);
            }
            final Integer _tmpFromEntityId;
            if (_cursor.isNull(_cursorIndexOfFromEntityId)) {
              _tmpFromEntityId = null;
            } else {
              _tmpFromEntityId = _cursor.getInt(_cursorIndexOfFromEntityId);
            }
            final String _tmpFromEntityName;
            if (_cursor.isNull(_cursorIndexOfFromEntityName)) {
              _tmpFromEntityName = null;
            } else {
              _tmpFromEntityName = _cursor.getString(_cursorIndexOfFromEntityName);
            }
            final String _tmpFromEntityCharacter;
            if (_cursor.isNull(_cursorIndexOfFromEntityCharacter)) {
              _tmpFromEntityCharacter = null;
            } else {
              _tmpFromEntityCharacter = _cursor.getString(_cursorIndexOfFromEntityCharacter);
            }
            final String _tmpDeduplicationKey;
            if (_cursor.isNull(_cursorIndexOfDeduplicationKey)) {
              _tmpDeduplicationKey = null;
            } else {
              _tmpDeduplicationKey = _cursor.getString(_cursorIndexOfDeduplicationKey);
            }
            final boolean _tmpIsSynced;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_2 != 0;
            _item = new ChatMessage(_tmpId,_tmpText,_tmpTimestamp,_tmpIsFromUser,_tmpMessageType,_tmpSource,_tmpTargetEntityIds,_tmpFromEntityId,_tmpFromEntityName,_tmpFromEntityCharacter,_tmpDeduplicationKey,_tmpIsSynced);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ChatMessage>> getMessagesAscending(final int limit) {
    final String _sql = "SELECT * FROM chat_messages ORDER BY timestamp ASC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_messages"}, new Callable<List<ChatMessage>>() {
      @Override
      @NonNull
      public List<ChatMessage> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfIsFromUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isFromUser");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTargetEntityIds = CursorUtil.getColumnIndexOrThrow(_cursor, "targetEntityIds");
          final int _cursorIndexOfFromEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "fromEntityId");
          final int _cursorIndexOfFromEntityName = CursorUtil.getColumnIndexOrThrow(_cursor, "fromEntityName");
          final int _cursorIndexOfFromEntityCharacter = CursorUtil.getColumnIndexOrThrow(_cursor, "fromEntityCharacter");
          final int _cursorIndexOfDeduplicationKey = CursorUtil.getColumnIndexOrThrow(_cursor, "deduplicationKey");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final List<ChatMessage> _result = new ArrayList<ChatMessage>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatMessage _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpText;
            if (_cursor.isNull(_cursorIndexOfText)) {
              _tmpText = null;
            } else {
              _tmpText = _cursor.getString(_cursorIndexOfText);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final boolean _tmpIsFromUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFromUser);
            _tmpIsFromUser = _tmp != 0;
            final MessageType _tmpMessageType;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfMessageType)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfMessageType);
            }
            _tmpMessageType = __converters.toMessageType(_tmp_1);
            final String _tmpSource;
            if (_cursor.isNull(_cursorIndexOfSource)) {
              _tmpSource = null;
            } else {
              _tmpSource = _cursor.getString(_cursorIndexOfSource);
            }
            final String _tmpTargetEntityIds;
            if (_cursor.isNull(_cursorIndexOfTargetEntityIds)) {
              _tmpTargetEntityIds = null;
            } else {
              _tmpTargetEntityIds = _cursor.getString(_cursorIndexOfTargetEntityIds);
            }
            final Integer _tmpFromEntityId;
            if (_cursor.isNull(_cursorIndexOfFromEntityId)) {
              _tmpFromEntityId = null;
            } else {
              _tmpFromEntityId = _cursor.getInt(_cursorIndexOfFromEntityId);
            }
            final String _tmpFromEntityName;
            if (_cursor.isNull(_cursorIndexOfFromEntityName)) {
              _tmpFromEntityName = null;
            } else {
              _tmpFromEntityName = _cursor.getString(_cursorIndexOfFromEntityName);
            }
            final String _tmpFromEntityCharacter;
            if (_cursor.isNull(_cursorIndexOfFromEntityCharacter)) {
              _tmpFromEntityCharacter = null;
            } else {
              _tmpFromEntityCharacter = _cursor.getString(_cursorIndexOfFromEntityCharacter);
            }
            final String _tmpDeduplicationKey;
            if (_cursor.isNull(_cursorIndexOfDeduplicationKey)) {
              _tmpDeduplicationKey = null;
            } else {
              _tmpDeduplicationKey = _cursor.getString(_cursorIndexOfDeduplicationKey);
            }
            final boolean _tmpIsSynced;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_2 != 0;
            _item = new ChatMessage(_tmpId,_tmpText,_tmpTimestamp,_tmpIsFromUser,_tmpMessageType,_tmpSource,_tmpTargetEntityIds,_tmpFromEntityId,_tmpFromEntityName,_tmpFromEntityCharacter,_tmpDeduplicationKey,_tmpIsSynced);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ChatMessage>> getMessagesForEntity(final int entityId, final int limit) {
    final String _sql = "\n"
            + "        SELECT * FROM chat_messages\n"
            + "        WHERE targetEntityIds LIKE '%' || ? || '%'\n"
            + "           OR fromEntityId = ?\n"
            + "        ORDER BY timestamp DESC\n"
            + "        LIMIT ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, entityId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, entityId);
    _argIndex = 3;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_messages"}, new Callable<List<ChatMessage>>() {
      @Override
      @NonNull
      public List<ChatMessage> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfIsFromUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isFromUser");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTargetEntityIds = CursorUtil.getColumnIndexOrThrow(_cursor, "targetEntityIds");
          final int _cursorIndexOfFromEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "fromEntityId");
          final int _cursorIndexOfFromEntityName = CursorUtil.getColumnIndexOrThrow(_cursor, "fromEntityName");
          final int _cursorIndexOfFromEntityCharacter = CursorUtil.getColumnIndexOrThrow(_cursor, "fromEntityCharacter");
          final int _cursorIndexOfDeduplicationKey = CursorUtil.getColumnIndexOrThrow(_cursor, "deduplicationKey");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final List<ChatMessage> _result = new ArrayList<ChatMessage>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatMessage _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpText;
            if (_cursor.isNull(_cursorIndexOfText)) {
              _tmpText = null;
            } else {
              _tmpText = _cursor.getString(_cursorIndexOfText);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final boolean _tmpIsFromUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFromUser);
            _tmpIsFromUser = _tmp != 0;
            final MessageType _tmpMessageType;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfMessageType)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfMessageType);
            }
            _tmpMessageType = __converters.toMessageType(_tmp_1);
            final String _tmpSource;
            if (_cursor.isNull(_cursorIndexOfSource)) {
              _tmpSource = null;
            } else {
              _tmpSource = _cursor.getString(_cursorIndexOfSource);
            }
            final String _tmpTargetEntityIds;
            if (_cursor.isNull(_cursorIndexOfTargetEntityIds)) {
              _tmpTargetEntityIds = null;
            } else {
              _tmpTargetEntityIds = _cursor.getString(_cursorIndexOfTargetEntityIds);
            }
            final Integer _tmpFromEntityId;
            if (_cursor.isNull(_cursorIndexOfFromEntityId)) {
              _tmpFromEntityId = null;
            } else {
              _tmpFromEntityId = _cursor.getInt(_cursorIndexOfFromEntityId);
            }
            final String _tmpFromEntityName;
            if (_cursor.isNull(_cursorIndexOfFromEntityName)) {
              _tmpFromEntityName = null;
            } else {
              _tmpFromEntityName = _cursor.getString(_cursorIndexOfFromEntityName);
            }
            final String _tmpFromEntityCharacter;
            if (_cursor.isNull(_cursorIndexOfFromEntityCharacter)) {
              _tmpFromEntityCharacter = null;
            } else {
              _tmpFromEntityCharacter = _cursor.getString(_cursorIndexOfFromEntityCharacter);
            }
            final String _tmpDeduplicationKey;
            if (_cursor.isNull(_cursorIndexOfDeduplicationKey)) {
              _tmpDeduplicationKey = null;
            } else {
              _tmpDeduplicationKey = _cursor.getString(_cursorIndexOfDeduplicationKey);
            }
            final boolean _tmpIsSynced;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_2 != 0;
            _item = new ChatMessage(_tmpId,_tmpText,_tmpTimestamp,_tmpIsFromUser,_tmpMessageType,_tmpSource,_tmpTargetEntityIds,_tmpFromEntityId,_tmpFromEntityName,_tmpFromEntityCharacter,_tmpDeduplicationKey,_tmpIsSynced);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ChatMessage>> getUserMessages(final int limit) {
    final String _sql = "SELECT * FROM chat_messages WHERE isFromUser = 1 ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_messages"}, new Callable<List<ChatMessage>>() {
      @Override
      @NonNull
      public List<ChatMessage> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfIsFromUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isFromUser");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTargetEntityIds = CursorUtil.getColumnIndexOrThrow(_cursor, "targetEntityIds");
          final int _cursorIndexOfFromEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "fromEntityId");
          final int _cursorIndexOfFromEntityName = CursorUtil.getColumnIndexOrThrow(_cursor, "fromEntityName");
          final int _cursorIndexOfFromEntityCharacter = CursorUtil.getColumnIndexOrThrow(_cursor, "fromEntityCharacter");
          final int _cursorIndexOfDeduplicationKey = CursorUtil.getColumnIndexOrThrow(_cursor, "deduplicationKey");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final List<ChatMessage> _result = new ArrayList<ChatMessage>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatMessage _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpText;
            if (_cursor.isNull(_cursorIndexOfText)) {
              _tmpText = null;
            } else {
              _tmpText = _cursor.getString(_cursorIndexOfText);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final boolean _tmpIsFromUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFromUser);
            _tmpIsFromUser = _tmp != 0;
            final MessageType _tmpMessageType;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfMessageType)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfMessageType);
            }
            _tmpMessageType = __converters.toMessageType(_tmp_1);
            final String _tmpSource;
            if (_cursor.isNull(_cursorIndexOfSource)) {
              _tmpSource = null;
            } else {
              _tmpSource = _cursor.getString(_cursorIndexOfSource);
            }
            final String _tmpTargetEntityIds;
            if (_cursor.isNull(_cursorIndexOfTargetEntityIds)) {
              _tmpTargetEntityIds = null;
            } else {
              _tmpTargetEntityIds = _cursor.getString(_cursorIndexOfTargetEntityIds);
            }
            final Integer _tmpFromEntityId;
            if (_cursor.isNull(_cursorIndexOfFromEntityId)) {
              _tmpFromEntityId = null;
            } else {
              _tmpFromEntityId = _cursor.getInt(_cursorIndexOfFromEntityId);
            }
            final String _tmpFromEntityName;
            if (_cursor.isNull(_cursorIndexOfFromEntityName)) {
              _tmpFromEntityName = null;
            } else {
              _tmpFromEntityName = _cursor.getString(_cursorIndexOfFromEntityName);
            }
            final String _tmpFromEntityCharacter;
            if (_cursor.isNull(_cursorIndexOfFromEntityCharacter)) {
              _tmpFromEntityCharacter = null;
            } else {
              _tmpFromEntityCharacter = _cursor.getString(_cursorIndexOfFromEntityCharacter);
            }
            final String _tmpDeduplicationKey;
            if (_cursor.isNull(_cursorIndexOfDeduplicationKey)) {
              _tmpDeduplicationKey = null;
            } else {
              _tmpDeduplicationKey = _cursor.getString(_cursorIndexOfDeduplicationKey);
            }
            final boolean _tmpIsSynced;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_2 != 0;
            _item = new ChatMessage(_tmpId,_tmpText,_tmpTimestamp,_tmpIsFromUser,_tmpMessageType,_tmpSource,_tmpTargetEntityIds,_tmpFromEntityId,_tmpFromEntityName,_tmpFromEntityCharacter,_tmpDeduplicationKey,_tmpIsSynced);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getLastMessages(final int limit,
      final Continuation<? super List<ChatMessage>> $completion) {
    final String _sql = "SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ChatMessage>>() {
      @Override
      @NonNull
      public List<ChatMessage> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfIsFromUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isFromUser");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTargetEntityIds = CursorUtil.getColumnIndexOrThrow(_cursor, "targetEntityIds");
          final int _cursorIndexOfFromEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "fromEntityId");
          final int _cursorIndexOfFromEntityName = CursorUtil.getColumnIndexOrThrow(_cursor, "fromEntityName");
          final int _cursorIndexOfFromEntityCharacter = CursorUtil.getColumnIndexOrThrow(_cursor, "fromEntityCharacter");
          final int _cursorIndexOfDeduplicationKey = CursorUtil.getColumnIndexOrThrow(_cursor, "deduplicationKey");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final List<ChatMessage> _result = new ArrayList<ChatMessage>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatMessage _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpText;
            if (_cursor.isNull(_cursorIndexOfText)) {
              _tmpText = null;
            } else {
              _tmpText = _cursor.getString(_cursorIndexOfText);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final boolean _tmpIsFromUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFromUser);
            _tmpIsFromUser = _tmp != 0;
            final MessageType _tmpMessageType;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfMessageType)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfMessageType);
            }
            _tmpMessageType = __converters.toMessageType(_tmp_1);
            final String _tmpSource;
            if (_cursor.isNull(_cursorIndexOfSource)) {
              _tmpSource = null;
            } else {
              _tmpSource = _cursor.getString(_cursorIndexOfSource);
            }
            final String _tmpTargetEntityIds;
            if (_cursor.isNull(_cursorIndexOfTargetEntityIds)) {
              _tmpTargetEntityIds = null;
            } else {
              _tmpTargetEntityIds = _cursor.getString(_cursorIndexOfTargetEntityIds);
            }
            final Integer _tmpFromEntityId;
            if (_cursor.isNull(_cursorIndexOfFromEntityId)) {
              _tmpFromEntityId = null;
            } else {
              _tmpFromEntityId = _cursor.getInt(_cursorIndexOfFromEntityId);
            }
            final String _tmpFromEntityName;
            if (_cursor.isNull(_cursorIndexOfFromEntityName)) {
              _tmpFromEntityName = null;
            } else {
              _tmpFromEntityName = _cursor.getString(_cursorIndexOfFromEntityName);
            }
            final String _tmpFromEntityCharacter;
            if (_cursor.isNull(_cursorIndexOfFromEntityCharacter)) {
              _tmpFromEntityCharacter = null;
            } else {
              _tmpFromEntityCharacter = _cursor.getString(_cursorIndexOfFromEntityCharacter);
            }
            final String _tmpDeduplicationKey;
            if (_cursor.isNull(_cursorIndexOfDeduplicationKey)) {
              _tmpDeduplicationKey = null;
            } else {
              _tmpDeduplicationKey = _cursor.getString(_cursorIndexOfDeduplicationKey);
            }
            final boolean _tmpIsSynced;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_2 != 0;
            _item = new ChatMessage(_tmpId,_tmpText,_tmpTimestamp,_tmpIsFromUser,_tmpMessageType,_tmpSource,_tmpTargetEntityIds,_tmpFromEntityId,_tmpFromEntityName,_tmpFromEntityCharacter,_tmpDeduplicationKey,_tmpIsSynced);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object countByDeduplicationKey(final String key,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM chat_messages WHERE deduplicationKey = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (key == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, key);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object existsByDeduplicationKey(final String key,
      final Continuation<? super Boolean> $completion) {
    final String _sql = "SELECT EXISTS(SELECT 1 FROM chat_messages WHERE deduplicationKey = ?)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (key == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, key);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp == null ? null : _tmp != 0;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Integer>> getDistinctEntityIds() {
    final String _sql = "\n"
            + "        SELECT DISTINCT fromEntityId FROM chat_messages\n"
            + "        WHERE fromEntityId IS NOT NULL\n"
            + "        ORDER BY fromEntityId\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_messages"}, new Callable<List<Integer>>() {
      @Override
      @NonNull
      public List<Integer> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<Integer> _result = new ArrayList<Integer>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Integer _item;
            if (_cursor.isNull(0)) {
              _item = null;
            } else {
              _item = _cursor.getInt(0);
            }
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getLastEntityMessage(final int entityId,
      final Continuation<? super ChatMessage> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM chat_messages\n"
            + "        WHERE fromEntityId = ? AND messageType = 'ENTITY_RESPONSE'\n"
            + "        ORDER BY timestamp DESC\n"
            + "        LIMIT 1\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, entityId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ChatMessage>() {
      @Override
      @Nullable
      public ChatMessage call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfIsFromUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isFromUser");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTargetEntityIds = CursorUtil.getColumnIndexOrThrow(_cursor, "targetEntityIds");
          final int _cursorIndexOfFromEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "fromEntityId");
          final int _cursorIndexOfFromEntityName = CursorUtil.getColumnIndexOrThrow(_cursor, "fromEntityName");
          final int _cursorIndexOfFromEntityCharacter = CursorUtil.getColumnIndexOrThrow(_cursor, "fromEntityCharacter");
          final int _cursorIndexOfDeduplicationKey = CursorUtil.getColumnIndexOrThrow(_cursor, "deduplicationKey");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final ChatMessage _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpText;
            if (_cursor.isNull(_cursorIndexOfText)) {
              _tmpText = null;
            } else {
              _tmpText = _cursor.getString(_cursorIndexOfText);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final boolean _tmpIsFromUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFromUser);
            _tmpIsFromUser = _tmp != 0;
            final MessageType _tmpMessageType;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfMessageType)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfMessageType);
            }
            _tmpMessageType = __converters.toMessageType(_tmp_1);
            final String _tmpSource;
            if (_cursor.isNull(_cursorIndexOfSource)) {
              _tmpSource = null;
            } else {
              _tmpSource = _cursor.getString(_cursorIndexOfSource);
            }
            final String _tmpTargetEntityIds;
            if (_cursor.isNull(_cursorIndexOfTargetEntityIds)) {
              _tmpTargetEntityIds = null;
            } else {
              _tmpTargetEntityIds = _cursor.getString(_cursorIndexOfTargetEntityIds);
            }
            final Integer _tmpFromEntityId;
            if (_cursor.isNull(_cursorIndexOfFromEntityId)) {
              _tmpFromEntityId = null;
            } else {
              _tmpFromEntityId = _cursor.getInt(_cursorIndexOfFromEntityId);
            }
            final String _tmpFromEntityName;
            if (_cursor.isNull(_cursorIndexOfFromEntityName)) {
              _tmpFromEntityName = null;
            } else {
              _tmpFromEntityName = _cursor.getString(_cursorIndexOfFromEntityName);
            }
            final String _tmpFromEntityCharacter;
            if (_cursor.isNull(_cursorIndexOfFromEntityCharacter)) {
              _tmpFromEntityCharacter = null;
            } else {
              _tmpFromEntityCharacter = _cursor.getString(_cursorIndexOfFromEntityCharacter);
            }
            final String _tmpDeduplicationKey;
            if (_cursor.isNull(_cursorIndexOfDeduplicationKey)) {
              _tmpDeduplicationKey = null;
            } else {
              _tmpDeduplicationKey = _cursor.getString(_cursorIndexOfDeduplicationKey);
            }
            final boolean _tmpIsSynced;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_2 != 0;
            _result = new ChatMessage(_tmpId,_tmpText,_tmpTimestamp,_tmpIsFromUser,_tmpMessageType,_tmpSource,_tmpTargetEntityIds,_tmpFromEntityId,_tmpFromEntityName,_tmpFromEntityCharacter,_tmpDeduplicationKey,_tmpIsSynced);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getMessageCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM chat_messages";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
