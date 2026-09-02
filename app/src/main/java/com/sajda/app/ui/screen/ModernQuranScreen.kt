package com.sajda.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.AudioDownloadMode
import com.sajda.app.domain.model.QuranReadingMode
import com.sajda.app.domain.model.QuranReciter
import com.sajda.app.domain.model.Surah
import com.sajda.app.ui.component.ArabicVerseText
import com.sajda.app.ui.component.MetadataChip
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.component.formatStorageSize
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import com.sajda.app.ui.theme.surfaceContainerHigh
import com.sajda.app.ui.theme.surfaceContainerLow
import com.sajda.app.ui.theme.surfaceContainerLowest
import com.sajda.app.ui.viewmodel.QuranViewModel
import com.sajda.app.ui.viewmodel.SettingsViewModel
import com.sajda.app.util.audioBundleSizeBytes
import com.sajda.app.util.hasAnyDownloadedAudio
import com.sajda.app.util.hasDownloadedAudioFor
import com.sajda.app.util.isEnglish
import com.sajda.app.util.AppTranslations

private enum class QuranFilter { ALL, MAKKIYAH, MADANIYAH }

private enum class ReaderMode { MUSHAF, TRANSLATION, TAFSIR }

@Composable
fun ModernQuranScreen(
    viewModel: QuranViewModel,
    settingsViewModel: SettingsViewModel,
    onPlayAudio: (Surah) -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenTafsir: (Surah, com.sajda.app.domain.model.Ayat) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isEnglish = state.appLanguage.isEnglish()
    val selectedReciter = state.selectedQuranReciter
    var pendingDownloadSurah by remember { mutableStateOf<Surah?>(null) }
    var downloadMode by remember { mutableStateOf(state.audioDownloadMode) }
    var wifiOnlyDownload by remember { mutableStateOf(state.wifiOnlyAudioDownloads) }

    androidx.compose.runtime.LaunchedEffect(pendingDownloadSurah) {
        if (pendingDownloadSurah != null) {
            downloadMode = state.audioDownloadMode
            wifiOnlyDownload = state.wifiOnlyAudioDownloads
        }
    }

    pendingDownloadSurah?.let { surah ->
        AudioDownloadOptionsDialog(
            appLanguage = state.appLanguage,
            selectedReciter = selectedReciter,
            mode = downloadMode,
            wifiOnly = wifiOnlyDownload,
            onModeChange = { downloadMode = it },
            onWifiOnlyChange = { wifiOnlyDownload = it },
            onDismiss = { pendingDownloadSurah = null },
            onConfirm = {
                viewModel.downloadAudio(surah, downloadMode, wifiOnlyDownload)
                pendingDownloadSurah = null
            }
        )
    }
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(QuranFilter.ALL) }
    var readerMode by rememberSaveable(state.selectedSurah?.number) { mutableStateOf(ReaderMode.TRANSLATION) }

    if (state.isLoading && state.surahList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val filteredSurah = remember(state.surahList, query, filter) {
        state.surahList.filter { surah ->
            val matchesQuery = query.isBlank() ||
                surah.transliteration.contains(query, ignoreCase = true) ||
                surah.translation.contains(query, ignoreCase = true) ||
                surah.englishTranslation.contains(query, ignoreCase = true) ||
                surah.nameArabic.contains(query)
            val matchesFilter = when (filter) {
                QuranFilter.ALL -> true
                QuranFilter.MAKKIYAH -> surah.revelationPlace.contains("meccan", true) || surah.revelationPlace.contains("mak", true)
                QuranFilter.MADANIYAH -> surah.revelationPlace.contains("medinan", true) || surah.revelationPlace.contains("mad", true)
            }
            matchesQuery && matchesFilter
        }
    }

    if (state.selectedSurah == null) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Al-Qur'an",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onOpenSearch) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onOpenBookmarks) {
                            Icon(
                                imageVector = Icons.Rounded.Bookmark,
                                contentDescription = "Bookmarks",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item {
                state.errorMessage?.let { message ->
                    QuranEmptyStateCard(
                        title = if (isEnglish) "Audio status" else "Status audio",
                        message = message
                    )
                }
            }

            item {
                SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            ),
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        placeholder = {
                            Text(
                                text = if (isEnglish) "Search surah or meaning..." else "Cari surah atau arti...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(androidx.compose.ui.res.stringResource(com.sajda.app.R.string.all), filter == QuranFilter.ALL) { filter = QuranFilter.ALL }
                        FilterChip("Makkiyah", filter == QuranFilter.MAKKIYAH) { filter = QuranFilter.MAKKIYAH }
                        FilterChip("Madaniyah", filter == QuranFilter.MADANIYAH) { filter = QuranFilter.MADANIYAH }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        QuranReciter.entries.forEach { reciter ->
                            QuranChoiceChip(
                                label = reciter.title,
                                selected = selectedReciter == reciter,
                                onClick = { settingsViewModel.setSelectedQuranReciter(reciter) }
                            )
                        }
                    }
                }
            }

            if (filteredSurah.isEmpty()) {
                item {
                    QuranEmptyStateCard(
                        title = if (isEnglish) "No surah found" else "Surah tidak ditemukan",
                        message = if (isEnglish) {
                            "Try another keyword or open the full search page."
                        } else {
                            "Coba kata kunci lain atau buka halaman pencarian penuh."
                        }
                    )
                }
            }

            items(filteredSurah, key = { it.number }) { surah ->
                val downloadState = state.downloadStates[surah.number]
                val hasSelectedAudio = surah.hasDownloadedAudioFor(selectedReciter)
                val hasOfflineBundle = surah.hasAnyDownloadedAudio()
                val estimatedSize = surah.audioBundleSizeBytes(
                    mode = state.audioDownloadMode,
                    selectedReciter = selectedReciter
                )

                SanctuaryCard(
                    modifier = Modifier.clickable { viewModel.openSurah(surah) },
                    elevated = true,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = surah.number.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = surah.transliteration,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${resolveSurahTranslation(state.appLanguage, surah.translation, surah.englishTranslation)} • ${surah.totalVerses} ${androidx.compose.ui.res.stringResource(com.sajda.app.R.string.verses)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val sizeLabel = if (hasOfflineBundle) {
                                if (isEnglish) "Offline" else "Offline"
                            } else {
                                formatStorageSize(estimatedSize)
                            }
                            Text(
                                text = sizeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = surah.nameArabic,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (downloadState?.isDownloading == true) {
                                CircularProgressIndicator(
                                    progress = downloadState.progress / 100f,
                                    modifier = Modifier.size(32.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp
                                )
                            } else if (hasOfflineBundle) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                            .clickable { onPlayAudio(surah) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Headphones,
                                            contentDescription = "Play",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .clickable { viewModel.deleteAudio(surah.number) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                        .clickable { pendingDownloadSurah = surah },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Download,
                                        contentDescription = "Download",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (downloadState?.isDownloading == true) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${downloadState.progress}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    } else {
        val surah = state.selectedSurah ?: return
        var memorizationMode by rememberSaveable { mutableStateOf(false) }
        val bookmarkMap = remember(state.bookmarks, surah.number) {
            state.bookmarks.filter { it.surahNumber == surah.number }.associateBy { it.ayatNumber }
        }
        val hasSelectedAudio = surah.hasDownloadedAudioFor(selectedReciter)
        val hasOfflineBundle = surah.hasAnyDownloadedAudio()
        val downloadState = state.downloadStates[surah.number]
        val estimatedSize = surah.audioBundleSizeBytes(
            mode = state.audioDownloadMode,
            selectedReciter = selectedReciter
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SanctuaryCard(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = viewModel::closeSurah,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            ) {
                                Icon(Icons.Rounded.ChevronLeft, contentDescription = "Back")
                            }
                            Column {
                                Text(
                                    text = surah.transliteration,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${surah.totalVerses} ${androidx.compose.ui.res.stringResource(com.sajda.app.R.string.verses)} | ${surah.revelationPlace.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = { memorizationMode = !memorizationMode },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (memorizationMode) MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerLow)
                            ) {
                                Icon(
                                    imageVector = if (memorizationMode) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = "Mode Hafalan",
                                    tint = if (memorizationMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (hasOfflineBundle) {
                                IconButton(
                                    onClick = { onPlayAudio(surah) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                ) {
                                    Icon(Icons.Rounded.Headphones, contentDescription = "Play", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { viewModel.deleteAudio(surah.number) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            } else {
                                IconButton(
                                    onClick = { pendingDownloadSurah = surah },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                ) {
                                    Icon(Icons.Rounded.Download, contentDescription = "Download", tint = MaterialTheme.colorScheme.primary)
                                }
                                Text(
                                    text = formatStorageSize(estimatedSize),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = surah.nameArabic,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        QuranReciter.entries.forEach { reciter ->
                            QuranChoiceChip(
                                label = reciter.title,
                                selected = selectedReciter == reciter,
                                onClick = { settingsViewModel.setSelectedQuranReciter(reciter) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(4.dp)
                    ) {
                        FilterChip(androidx.compose.ui.res.stringResource(com.sajda.app.R.string.mushaf), readerMode == ReaderMode.MUSHAF) {
                            readerMode = ReaderMode.MUSHAF
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterChip(androidx.compose.ui.res.stringResource(com.sajda.app.R.string.translation), readerMode == ReaderMode.TRANSLATION) {
                            readerMode = ReaderMode.TRANSLATION
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterChip("Tafsir", readerMode == ReaderMode.TAFSIR) {
                            readerMode = ReaderMode.TAFSIR
                        }
                    }

                    if (downloadState?.isDownloading == true) {
                        LinearProgressIndicator(
                            progress = downloadState.progress / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (surah.number != 9) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ArabicVerseText(
                            text = "بِسْمِ ٱللّٰهِ ٱلرَّحْمٰنِ ٱلرَّحِيمِ",
                            fontSize = 28,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            items(state.ayatList, key = { it.id }) { ayat ->
                val bookmarked = bookmarkMap[ayat.ayatNumber] != null
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (bookmarked) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLowest
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = if (bookmarked) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.recordLastRead(ayat) }
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (bookmarked) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ayat.ayatNumber.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(onClick = { viewModel.toggleBookmark(ayat) }) {
                                Icon(
                                    imageVector = if (bookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (bookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onOpenTafsir(surah, ayat) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = "Tafsir",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    ArabicVerseText(
                        text = ayat.textArabic,
                        fontSize = if (readerMode == ReaderMode.MUSHAF) state.arabicFontSize + 4 else state.arabicFontSize
                    )

                    if (state.showTransliteration && state.quranReadingMode != QuranReadingMode.ARABIC_ONLY && ayat.transliteration.isNotBlank()) {
                        Text(
                            text = ayat.transliteration,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 22.sp
                        )
                    }

                    if (readerMode != ReaderMode.MUSHAF && state.quranReadingMode != QuranReadingMode.ARABIC_ONLY && !memorizationMode) {
                        Text(
                            text = resolveAyatTranslation(
                                appLanguage = state.appLanguage,
                                indonesian = ayat.translation,
                                english = ayat.englishTranslation
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 26.sp
                        )
                    }

                    if (readerMode == ReaderMode.TAFSIR) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.open_full_tafsir),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .clickable { onOpenTafsir(surah, ayat) }
                                .padding(vertical = 8.dp, horizontal = 14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioDownloadOptionsDialog(
    appLanguage: AppLanguage,
    selectedReciter: QuranReciter,
    mode: AudioDownloadMode,
    wifiOnly: Boolean,
    onModeChange: (AudioDownloadMode) -> Unit,
    onWifiOnlyChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val isEnglish = appLanguage.isEnglish()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.download_audio),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.choose_a_download_package),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DownloadOptionRow(
                    label = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.active_reciter_only_selectedreciter_titl),
                    selected = mode == AudioDownloadMode.SELECTED_RECITER_ONLY,
                    onClick = { onModeChange(AudioDownloadMode.SELECTED_RECITER_ONLY) }
                )
                DownloadOptionRow(
                    label = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.all_reciters),
                    selected = mode == AudioDownloadMode.ALL_RECITERS,
                    onClick = { onModeChange(AudioDownloadMode.ALL_RECITERS) }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.download_via_wi_fi_only),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.avoid_large_downloads_over_mobile_data),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = wifiOnly, onCheckedChange = onWifiOnlyChange)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(if (isEnglish) "Download" else "Unduh", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isEnglish) "Cancel" else "Batal", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

private fun resolveAyatTranslation(
    appLanguage: com.sajda.app.domain.model.AppLanguage,
    indonesian: String,
    english: String
): String {
    val fallbackEnglish = english.ifBlank { indonesian }
    return when (appLanguage) {
        com.sajda.app.domain.model.AppLanguage.INDONESIAN -> indonesian
        com.sajda.app.domain.model.AppLanguage.ENGLISH -> fallbackEnglish
        else -> AppTranslations.translate(fallbackEnglish, appLanguage)
    }
}

private fun resolveSurahTranslation(
    appLanguage: com.sajda.app.domain.model.AppLanguage,
    indonesian: String,
    english: String
): String {
    val fallbackEnglish = english.ifBlank { indonesian }
    return when (appLanguage) {
        com.sajda.app.domain.model.AppLanguage.INDONESIAN -> indonesian
        com.sajda.app.domain.model.AppLanguage.ENGLISH -> fallbackEnglish
        else -> AppTranslations.translate(fallbackEnglish, appLanguage)
    }
}

@Composable
private fun DownloadOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "filterChipTextColor"
    )
    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
        label = "filterChipBgColor"
    )
    
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = textColor,
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun QuranChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = if (selected) colors.primary else colors.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) colors.primary.copy(alpha = 0.08f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) colors.primary else colors.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
private fun QuranEmptyStateCard(
    title: String,
    message: String
) {
    SanctuaryCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
