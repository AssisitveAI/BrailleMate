package kr.ac.kaist.aailab.braillemate.android.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kr.ac.kaist.aailab.braillemate.android.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToRegulation: () -> Unit,
    onNavigateToRegulationSection: (String) -> Unit,
    onNavigateToRegulationDetail: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val sections by viewModel.sections.collectAsStateWithLifecycle()
    val isInitialized by viewModel.isInitialized.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Hero Section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(24.dp)
                    .padding(top = 48.dp)
            ) {
                Column {
                    Text(
                        text = "BrailleMate",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.home_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                stringResource(R.string.home_search_hint),
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "지우기",
                                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            focusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                            cursorColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                }
            }
        }

        // Search Results
        if (searchQuery.length >= 2) {
            if (searchResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.regulation_no_results),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(searchResults) { regulation ->
                    ListItem(
                        headlineContent = {
                            Text(
                                "${regulation.articleNumber}",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        supportingContent = {
                            Text(
                                regulation.content,
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
                        modifier = Modifier.clickable {
                            onNavigateToRegulationDetail(regulation.id)
                        }
                    )
                    HorizontalDivider()
                }
            }
        } else {
            // Quick Actions
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionCard(
                            title = stringResource(R.string.home_ai_consult),
                            description = stringResource(R.string.home_ai_description),
                            icon = Icons.Outlined.SmartToy,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToChat
                        )
                        QuickActionCard(
                            title = stringResource(R.string.home_regulation),
                            description = stringResource(R.string.home_regulation_description),
                            icon = Icons.Outlined.MenuBook,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToRegulation
                        )
                    }
                }
            }

            // Sections
            item {
                Text(
                    text = "규정 분류",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (!isInitialized) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("규정 데이터를 로딩 중...")
                        }
                    }
                }
            } else {
                items(sections) { section ->
                    val icon = getSectionIcon(section)
                    ListItem(
                        headlineContent = {
                            Text(section, fontWeight = FontWeight.Medium)
                        },
                        leadingContent = {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        },
                        modifier = Modifier.clickable {
                            onNavigateToRegulationSection(section)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun getSectionIcon(section: String): ImageVector {
    return when {
        "한글" in section -> Icons.Outlined.TextFields
        "수학" in section -> Icons.Outlined.Calculate
        "과학" in section -> Icons.Outlined.Science
        "한국 음악" in section -> Icons.Outlined.MusicNote
        "서양 음악" in section -> Icons.Outlined.Piano
        "외국어" in section -> Icons.Outlined.Language
        "국제음성" in section -> Icons.Outlined.RecordVoiceOver
        "기본 원칙" in section -> Icons.Outlined.Gavel
        else -> Icons.Outlined.Article
    }
}
