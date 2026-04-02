package kr.ac.kaist.aailab.braillemate.android.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kr.ac.kaist.aailab.braillemate.android.data.local.entity.ChatMessageEntity
import kr.ac.kaist.aailab.braillemate.android.data.repository.ChatRepository
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _sessionId = MutableStateFlow(chatRepository.createNewSession())
    val sessionId: StateFlow<String> = _sessionId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val messages: StateFlow<List<ChatMessageEntity>> = _sessionId
        .flatMapLatest { chatRepository.getMessages(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasApiKey: StateFlow<Boolean> = chatRepository.getApiKeyFlow()
        .map { !it.isNullOrBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun sendMessage(message: String) {
        if (message.isBlank() || _isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = chatRepository.sendMessage(_sessionId.value, message)
            result.onFailure { e ->
                _error.value = e.message ?: "알 수 없는 오류가 발생했습니다."
            }

            _isLoading.value = false
        }
    }

    fun startNewSession() {
        _sessionId.value = chatRepository.createNewSession()
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
