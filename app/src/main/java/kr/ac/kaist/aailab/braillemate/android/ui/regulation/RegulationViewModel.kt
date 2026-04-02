package kr.ac.kaist.aailab.braillemate.android.ui.regulation

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
class RegulationViewModel @Inject constructor(
    private val regulationRepository: RegulationRepository
) : ViewModel() {

    private val _selectedSection = MutableStateFlow<String?>(null)
    val selectedSection: StateFlow<String?> = _selectedSection

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val sections: StateFlow<List<String>> = regulationRepository.getSections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val regulations: StateFlow<List<RegulationEntity>> = _selectedSection
        .filterNotNull()
        .flatMapLatest { regulationRepository.getRegulationsBySection(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchResults: StateFlow<List<RegulationEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.length >= 2) regulationRepository.searchRegulations(query)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectSection(section: String) {
        _selectedSection.value = section
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    // Detail
    private val _selectedRegulation = MutableStateFlow<RegulationEntity?>(null)
    val selectedRegulation: StateFlow<RegulationEntity?> = _selectedRegulation

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked

    fun loadRegulation(id: Int) {
        viewModelScope.launch {
            _selectedRegulation.value = regulationRepository.getRegulationById(id)
            regulationRepository.isBookmarked(id).collect { _isBookmarked.value = it }
        }
    }

    fun toggleBookmark(regulationId: Int) {
        viewModelScope.launch {
            regulationRepository.toggleBookmark(regulationId)
        }
    }
}
