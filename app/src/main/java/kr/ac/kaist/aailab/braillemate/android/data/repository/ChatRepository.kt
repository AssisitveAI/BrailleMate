package kr.ac.kaist.aailab.braillemate.android.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kr.ac.kaist.aailab.braillemate.android.data.local.dao.ChatHistoryDao
import kr.ac.kaist.aailab.braillemate.android.data.local.entity.ChatMessageEntity
import kr.ac.kaist.aailab.braillemate.android.di.dataStore
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatHistoryDao: ChatHistoryDao,
    @ApplicationContext private val context: Context
) {
    companion object {
        private val API_KEY_PREF = stringPreferencesKey("gemini_api_key")

        private const val SYSTEM_PROMPT = """당신은 '한국 점자 규정'(문화체육관광부고시 제2024-0005호, 2024년 개정)에 기반하여 
점자에 관한 전문적인 컨설팅을 제공하는 AI 점자 전문가입니다.

[역할]
- 점역교정사와 점자 학습자의 질문에 정확하고 상세하게 답변합니다.
- 답변 시 반드시 관련 규정 조항(제X항)을 인용합니다.
- 점자 표기 예시를 함께 제공합니다.

[규정 범위]
- 한국 점자 표기의 기본 원칙 (제1항~제8항)
- 한글 점자: 자모(제1장), 약자와 약어(제2장), 옛 글자(제3장), 로마자와 그리스 문자(제4장), 숫자와 기호(제5장), 문장 부호(제6장)
- 수학 점자: 수학 일반, 문자와 식, 정수론·대수, 기하, 함수, 미적분, 집합과 명제, 확률과 통계
- 과학 점자
- 한국 음악 점자: 정간보 기호, 오선보 기호
- 서양 음악 점자: 기본 기호, 성악, 악기별 기호
- [부록 1] 외국어 점자 (영어, 독일어, 프랑스어, 스페인어, 중국어, 일본어, 러시아어, 아랍어, 베트남어)
- [부록 2] 국제음성기호 점자

[핵심 규정 요약]
1. 한국 점자는 6점(세로3×가로2) 조합, 63가지 점형으로 구성
2. 점 번호: 왼쪽 위→아래 1,2,3점 / 오른쪽 위→아래 4,5,6점
3. 풀어쓰기 방식
4. 자음 14개, 모음 21개 별도 점형 배정
5. 약자: 가,나,다,마,바,사,자,카,타,파,하 등 + 억,언,얼,연,열,영,옥,온,옹,운,울,은,을,인,'것'
6. 약어: 그래서, 그러나, 그러면, 그러므로, 그런데, 그리고, 그리하여
7. 된소리표(6점): ㄲ,ㄸ,ㅃ,ㅆ,ㅉ 표기
8. 'ㅇ'이 첫소리일 때 표기하지 않음

[응답 형식]
- 관련 규정 조항을 명시: 예) "제13항에 따르면..."
- 점자 표기 예시 제공 시 한글과 점자를 나란히 표시
- 불확실한 내용은 명시적으로 밝혀주세요
- 친절하고 전문적인 어조를 유지하세요"""
    }

    private var generativeModel: GenerativeModel? = null

    private suspend fun getApiKey(): String? {
        return context.dataStore.data.first()[API_KEY_PREF]
    }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { prefs ->
            prefs[API_KEY_PREF] = apiKey
        }
        generativeModel = null // Reset model to use new key
    }

    fun getApiKeyFlow(): Flow<String?> {
        return context.dataStore.data.map { prefs -> prefs[API_KEY_PREF] }
    }

    private suspend fun getOrCreateModel(): GenerativeModel? {
        if (generativeModel != null) return generativeModel
        val apiKey = getApiKey() ?: return null
        generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.3f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 2048
            },
            systemInstruction = content { text(SYSTEM_PROMPT) }
        )
        return generativeModel
    }

    suspend fun sendMessage(
        sessionId: String,
        userMessage: String
    ): Result<String> {
        return try {
            val model = getOrCreateModel()
                ?: return Result.failure(Exception("API 키가 설정되지 않았습니다. 설정에서 Gemini API 키를 입력해 주세요."))

            // Save user message
            chatHistoryDao.insertMessage(
                ChatMessageEntity(
                    sessionId = sessionId,
                    role = "user",
                    content = userMessage
                )
            )

            // Get chat history for context
            val history = chatHistoryDao.getMessages(sessionId).first()
            val chat = model.startChat(
                history = history.dropLast(1).map { msg ->
                    content(role = if (msg.role == "user") "user" else "model") {
                        text(msg.content)
                    }
                }
            )

            val response = chat.sendMessage(userMessage)
            val responseText = response.text ?: "응답을 생성할 수 없습니다."

            // Save assistant message
            chatHistoryDao.insertMessage(
                ChatMessageEntity(
                    sessionId = sessionId,
                    role = "assistant",
                    content = responseText
                )
            )

            Result.success(responseText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMessages(sessionId: String): Flow<List<ChatMessageEntity>> {
        return chatHistoryDao.getMessages(sessionId)
    }

    fun getAllSessions(): Flow<List<String>> {
        return chatHistoryDao.getAllSessions()
    }

    fun createNewSession(): String = UUID.randomUUID().toString()

    suspend fun deleteSession(sessionId: String) {
        chatHistoryDao.deleteSession(sessionId)
    }
}
