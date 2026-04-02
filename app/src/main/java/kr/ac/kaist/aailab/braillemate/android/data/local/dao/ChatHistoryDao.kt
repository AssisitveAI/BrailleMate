package kr.ac.kaist.aailab.braillemate.android.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kr.ac.kaist.aailab.braillemate.android.data.local.entity.ChatMessageEntity

@Dao
interface ChatHistoryDao {

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessages(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT DISTINCT sessionId FROM chat_messages ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<String>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC LIMIT 1")
    suspend fun getFirstMessage(sessionId: String): ChatMessageEntity?

    @Insert
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAll()
}
