package kr.ac.kaist.aailab.braillemate.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kr.ac.kaist.aailab.braillemate.android.data.local.entity.RegulationEntity
import kr.ac.kaist.aailab.braillemate.android.data.repository.RegulationRepository
import javax.inject.Inject

@OptIn(kotlinx.coroutines.FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val regulationRepository: RegulationRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    val searchResults: StateFlow<List<RegulationEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.length >= 2) {
                regulationRepository.searchRegulations(query)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sections: StateFlow<List<String>> = regulationRepository.getSections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            regulationRepository.initializeData()
            _isInitialized.value = true
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
