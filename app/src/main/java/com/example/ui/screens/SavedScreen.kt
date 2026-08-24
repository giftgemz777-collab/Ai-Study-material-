package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SavedItemEntity
import com.example.data.model.SavedItemType
import com.example.ui.StudyViewModel
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun SavedScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val savedItems by viewModel.allSavedItems.collectAsState()
    var selectedCategory by remember { mutableStateOf<SavedItemType?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    val filteredItems = remember(savedItems, selectedCategory, searchQuery) {
        savedItems.filter { item ->
            val matchesCategory = selectedCategory == null || item.type == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.content.contains(searchQuery, ignoreCase = true) ||
                    item.subject.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("saved_screen"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Saved Bookmarks & Notes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search saved notes, MCQs, viva questions...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("saved_search_field"),
                shape = RoundedCornerShape(14.dp),
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Clear")
                        }
                    }
                }
            )
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All (${savedItems.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == SavedItemType.NOTE_SUMMARY,
                        onClick = { selectedCategory = SavedItemType.NOTE_SUMMARY },
                        label = { Text("Notes & Summaries") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == SavedItemType.MCQ_QUESTION,
                        onClick = { selectedCategory = SavedItemType.MCQ_QUESTION },
                        label = { Text("MCQs") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == SavedItemType.VIVA_QUESTION,
                        onClick = { selectedCategory = SavedItemType.VIVA_QUESTION },
                        label = { Text("Viva Questions") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == SavedItemType.CHAT_QA,
                        onClick = { selectedCategory = SavedItemType.CHAT_QA },
                        label = { Text("Chat Q&A") }
                    )
                }
            }
        }

        if (filteredItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BookmarkBorder,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                        Text(
                            text = "No saved items found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Bookmark notes, MCQs, or AI answers from the Study tools to review them anytime offline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        } else {
            items(filteredItems, key = { it.id }) { item ->
                SavedItemCard(
                    item = item,
                    onDelete = { viewModel.deleteSavedItem(item.id) },
                    onCopy = { clipboardManager.setText(AnnotatedString("${item.title}\n\n${item.content}")) }
                )
            }
        }
    }
}

@Composable
fun SavedItemCard(
    item: SavedItemEntity,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val (badgeText, badgeColor, badgeIcon) = when (item.type) {
        SavedItemType.NOTE_SUMMARY -> Triple("Summary", SlateSecondary, Icons.AutoMirrored.Filled.MenuBook)
        SavedItemType.MCQ_QUESTION -> Triple("Practice MCQ", AmberTertiary, Icons.Filled.Quiz)
        SavedItemType.VIVA_QUESTION -> Triple("Viva Question", PurpleAccent, Icons.Filled.School)
        SavedItemType.CHAT_QA -> Triple("AI Assistant Q&A", IndigoPrimary, Icons.Filled.AutoAwesome)
        SavedItemType.FLASHCARD -> Triple("Flashcard", SuccessGreen, Icons.Filled.Style)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = badgeIcon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(14.dp))
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }

                Row {
                    IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = ErrorRed)
                    }
                }
            }

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (item.subtitle.isNotBlank()) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                onClick = { expanded = !expanded },
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = if (expanded) item.content else item.content.take(150) + if (item.content.length > 150) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (item.content.length > 150) {
                        Text(
                            text = if (expanded) "Show less ▲" else "Read full text ▼",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
