package com.sajda.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajda.app.domain.model.PrayerName
import com.sajda.app.ui.component.HeroCard
import com.sajda.app.ui.component.MetadataChip
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.theme.surfaceContainerHigh
import com.sajda.app.ui.theme.surfaceContainerLow
import com.sajda.app.ui.theme.surfaceContainerLowest
import com.sajda.app.ui.viewmodel.PrayerTimeViewModel
import com.sajda.app.util.DateTimeUtils
import com.sajda.app.util.currentDayName
import com.sajda.app.util.currentGregorianSummary
import com.sajda.app.util.currentHijriSummary
import com.sajda.app.util.displayName
import com.sajda.app.util.displayNameRes
import com.sajda.app.util.isEnglish
import java.time.LocalDate

@Composable
fun PrayerTimeScreen(
    viewModel: PrayerTimeViewModel,
    onOpenWeeklySchedule: () -> Unit,
    onOpenQibla: () -> Unit,
    onOpenLocationSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = state.settings
    val isEnglish = settings.appLanguage.isEnglish()
    val today = LocalDate.now()
    val prayerTime = state.todayPrayerTime
    val nextPrayer = prayerTime?.let(DateTimeUtils::nextPrayer)
    val countdown = prayerTime?.let(DateTimeUtils::countdownClockToNextPrayer) ?: "--:--:--"
    val detailed = prayerTime?.let {
        com.sajda.app.util.PrayerTimeCalculator.calculateDetailedPrayerTimes(
            date = LocalDate.parse(it.date),
            latitude = it.latitude,
            longitude = it.longitude,
            locationName = it.locationName,
            calculationMethod = settings.prayerCalculationMethod,
            asrMadhhab = settings.asrMadhhab
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 150.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "NurApp",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = settings.locationName.trim().ifBlank {
                            if (isEnglish) "Location not active" else "Lokasi belum aktif"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${currentHijriSummary(settings.appLanguage, today)} - ${currentDayName(settings.appLanguage, today)}, ${currentGregorianSummary(settings.appLanguage, today)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = if (isEnglish) "Change" else "Ubah Lokasi",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.28f))
                        .clickable(onClick = onOpenLocationSettings)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }

        item {
            androidx.compose.material3.Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEnglish) "COUNTDOWN" else "HITUNG MUNDUR",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "LIVE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (isEnglish) "Next Adhan" else "Adzan Berikutnya",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.8.sp
                            )
                            val nextPrayerName = nextPrayer?.first?.displayNameRes()?.let {
                                androidx.compose.ui.res.stringResource(it)
                            } ?: androidx.compose.ui.res.stringResource(PrayerName.ASR.displayNameRes())
                            Text(
                                text = nextPrayerName,
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = nextPrayer?.second ?: "--:--",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = countdown,
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.weeklyPrayerTimes.take(5), key = { it.date }) { item ->
                    val localDate = LocalDate.parse(item.date)
                    val active = item.date == prayerTime?.date
                    Column(
                        modifier = Modifier
                            .width(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (active) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentDayName(settings.appLanguage, localDate).take(3).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = localDate.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        item {
            SanctuaryCard {
                detailed?.let {
                    PrayerRow(
                        label = if (isEnglish) "Imsak" else "Imsak",
                        value = it.imsak,
                        enabled = settings.imsakAdzanEnabled,
                        onToggle = { viewModel.toggleImsak(it) }
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                    )
                }
                PrayerRow(
                    label = androidx.compose.ui.res.stringResource(PrayerName.FAJR.displayNameRes()),
                    value = prayerTime?.fajr ?: "--:--",
                    enabled = settings.fajrAdzanEnabled,
                    highlighted = (nextPrayer?.first == PrayerName.FAJR),
                    onToggle = { viewModel.togglePrayer(PrayerName.FAJR, it) }
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                )
                detailed?.let {
                    PrayerRow(
                        label = if (isEnglish) "Sunrise" else "Terbit",
                        value = it.sunrise,
                        enabled = settings.sunriseAdzanEnabled,
                        onToggle = { viewModel.toggleSunrise(it) }
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                    )
                }
                PrayerRow(
                    label = androidx.compose.ui.res.stringResource(PrayerName.DHUHR.displayNameRes()),
                    value = prayerTime?.dhuhr ?: "--:--",
                    enabled = settings.dhuhrAdzanEnabled,
                    highlighted = (nextPrayer?.first == PrayerName.DHUHR),
                    onToggle = { viewModel.togglePrayer(PrayerName.DHUHR, it) }
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                )
                PrayerRow(
                    label = androidx.compose.ui.res.stringResource(PrayerName.ASR.displayNameRes()),
                    value = prayerTime?.asr ?: "--:--",
                    enabled = settings.asrAdzanEnabled,
                    highlighted = (nextPrayer?.first == PrayerName.ASR),
                    onToggle = { viewModel.togglePrayer(PrayerName.ASR, it) }
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                )
                PrayerRow(
                    label = androidx.compose.ui.res.stringResource(PrayerName.MAGHRIB.displayNameRes()),
                    value = prayerTime?.maghrib ?: "--:--",
                    enabled = settings.maghribAdzanEnabled,
                    highlighted = (nextPrayer?.first == PrayerName.MAGHRIB),
                    onToggle = { viewModel.togglePrayer(PrayerName.MAGHRIB, it) }
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                )
                PrayerRow(
                    label = androidx.compose.ui.res.stringResource(PrayerName.ISHA.displayNameRes()),
                    value = prayerTime?.isha ?: "--:--",
                    enabled = settings.ishaAdzanEnabled,
                    highlighted = (nextPrayer?.first == PrayerName.ISHA),
                    onToggle = { viewModel.togglePrayer(PrayerName.ISHA, it) }
                )
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isEnglish) {
                            "Method: ${settings.prayerCalculationMethod.label} • Madhhab: ${settings.asrMadhhab.label}"
                        } else {
                            "Metode: ${settings.prayerCalculationMethod.label} • Madzhab: ${settings.asrMadhhab.label}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(onClick = onOpenQibla)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Rounded.Explore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEnglish) "Qibla" else "Kiblat",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1
                        )
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(onClick = onOpenWeeklySchedule)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEnglish) "Weekly" else "Mingguan",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerRow(
    label: String,
    value: String,
    enabled: Boolean,
    highlighted: Boolean = false,
    onToggle: ((Boolean) -> Unit)?
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (highlighted) colors.secondary.copy(alpha = 0.08f) else Color.Transparent)
            .border(
                width = if (highlighted) 1.dp else 0.dp,
                color = if (highlighted) colors.secondary.copy(alpha = 0.20f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 14.dp, end = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (highlighted) colors.secondary else Color.Transparent)
                )

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (highlighted) colors.secondary.copy(alpha = 0.12f) else colors.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = if (onToggle != null) Icons.Rounded.NotificationsActive else Icons.Rounded.Mosque,
                        contentDescription = null,
                        tint = if (highlighted) colors.secondary else colors.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (highlighted) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (highlighted) colors.secondary else colors.onSurface
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (highlighted) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (highlighted) colors.secondary else colors.onSurface
                )
                if (onToggle != null) {
                    Switch(
                        checked = enabled,
                        onCheckedChange = onToggle,
                        modifier = Modifier.size(width = 52.dp, height = 32.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(52.dp))
                }
            }
        }
    }
}
