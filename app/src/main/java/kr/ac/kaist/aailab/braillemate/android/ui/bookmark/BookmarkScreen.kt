package kr.ac.kaist.aailab.braillemate.android.ui.bookmark

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kr.ac.kaist.aailab.braillemate.android.R
import kr.ac.kaist.aailab.braillemate.android.data.local.entity.RegulationEntity
import kr.ac.kaist.aailab.braillemate.android.data.repository.RegulationRepository
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val regulationRepository: RegulationRepository
) : ViewModel() {
    val bookmarkedRegulations: StateFlow<List<RegulationEntity>> =
        regulationRepository.getBookmarkedRegulations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(
    onRegulationClick: (Int) -> Unit,
    viewModel: BookmarkViewModel = hiltViewModel()
) {
    val bookmarks by viewModel.bookmarkedRegulations.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(top = 48.dp)) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    stringResource(R.string.bookmark_title),
                    fontWeight = FontWeight.Bold
                )
            }
        )

        if (bookmarks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        stringResource(R.string.bookmark_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(bookmarks) { regulation ->
                    ListItem(
                        headlineContent = {
                            Text(regulation.articleNumber, fontWeight = FontWeight.Medium)
                        },
                        supportingContent = {
                            Text(
                                regulation.content.take(80),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        overlineContent = {
                            Text(
                                "${regulation.section} > ${regulation.chapter}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        leadingContent = {
                            Icon(
                                Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.clickable { onRegulationClick(regulation.id) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                }
            }
        }
    }
}
