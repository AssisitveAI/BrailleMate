package kr.ac.kaist.aailab.braillemate.android.ui.regulation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kr.ac.kaist.aailab.braillemate.android.R
import kr.ac.kaist.aailab.braillemate.android.data.local.entity.RegulationEntity
import kotlinx.serialization.json.Json
import kr.ac.kaist.aailab.braillemate.android.data.repository.BrailleExample

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegulationListScreen(
    section: String? = null,
    onNavigateBack: () -> Unit,
    onRegulationClick: (Int) -> Unit,
    viewModel: RegulationViewModel = hiltViewModel()
) {
    val regulations by viewModel.regulations.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    LaunchedEffect(section) {
        section?.let { viewModel.selectSection(it) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = 48.dp)) {
        TopAppBar(
            title = {
                Text(
                    section ?: stringResource(R.string.regulation_title),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                }
            }
        )

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text(stringResource(R.string.regulation_search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "지우기")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        val displayList = if (searchQuery.length >= 2) searchResults else regulations

        // Group by chapter
        val grouped = displayList.groupBy { it.chapter }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            grouped.forEach { (chapter, articles) ->
                item {
                    Text(
                        text = chapter,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                items(articles) { regulation ->
                    RegulationListItem(
                        regulation = regulation,
                        onClick = { onRegulationClick(regulation.id) }
                    )
                }
            }

            if (displayList.isEmpty() && searchQuery.length >= 2) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.regulation_no_results))
                    }
                }
            }
        }
    }
}

@Composable
private fun RegulationListItem(
    regulation: RegulationEntity,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                regulation.articleNumber,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            Text(
                regulation.content.take(100),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall
            )
        },
        overlineContent = regulation.subSection?.let {
            { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary) }
        },
        trailingContent = {
            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp))
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
    HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegulationDetailScreen(
    regulationId: Int,
    onNavigateBack: () -> Unit,
    viewModel: RegulationViewModel = hiltViewModel()
) {
    val regulation by viewModel.selectedRegulation.collectAsStateWithLifecycle()
    val isBookmarked by viewModel.isBookmarked.collectAsStateWithLifecycle()
    val json = remember { Json { ignoreUnknownKeys = true } }

    LaunchedEffect(regulationId) {
        viewModel.loadRegulation(regulationId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = 48.dp)) {
        TopAppBar(
            title = {
                Text(
                    regulation?.articleNumber ?: "",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                }
            },
            actions = {
                IconButton(onClick = { regulation?.let { viewModel.toggleBookmark(it.id) } }) {
                    Icon(
                        if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isBookmarked) "북마크 제거" else "북마크 추가",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )

        regulation?.let { reg ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Breadcrumb
                item {
                    Text(
                        text = "${reg.section} > ${reg.chapter}" +
                                (reg.subSection?.let { " > $it" } ?: ""),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Content
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = reg.articleNumber,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = reg.content,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // Examples
                reg.examples?.let { examplesJson ->
                    try {
                        val examples = json.decodeFromString<List<BrailleExample>>(examplesJson)
                        if (examples.isNotEmpty()) {
                            item {
                                Text(
                                    "예시",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        examples.forEach { example ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = example.text,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = example.braille,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 2.dp),
                                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) { }
                }

                // Notes
                reg.notes?.let { notes ->
                    if (notes.isNotBlank()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            "비고",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = notes,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
