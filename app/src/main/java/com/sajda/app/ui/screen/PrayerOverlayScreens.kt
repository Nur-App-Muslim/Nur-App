package com.sajda.app.ui.screen

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.PrayerName
import com.sajda.app.domain.model.PrayerTime
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.ui.component.HeroCard
import com.sajda.app.ui.component.MetadataChip
import com.sajda.app.ui.component.SajdaTopAction
import com.sajda.app.ui.component.SajdaTopBar
import com.sajda.app.ui.component.SanctuaryCard
import com.sajda.app.ui.theme.surfaceContainerHigh
import com.sajda.app.util.DateTimeUtils
import com.sajda.app.util.PrayerTimeCalculator
import com.sajda.app.util.displayName
import com.sajda.app.util.displayNameRes
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeeklyPrayerScheduleScreen(
    weeklyPrayerTimes: List<PrayerTime>,
    monthlyPrayerTimes: List<PrayerTime>,
    settings: UserSettings,
    onBack: () -> Unit
) {
    var selectedRangeDays by rememberSaveable { mutableIntStateOf(7) }
    val prayerTimes = if (selectedRangeDays == 30) monthlyPrayerTimes else weeklyPrayerTimes

    OverlayShell(
        title = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.prayer_schedule),
        subtitle = if (selectedRangeDays == 30) {
            androidx.compose.ui.res.stringResource(com.sajda.app.R.string.next_30_days)
        } else {
            androidx.compose.ui.res.stringResource(com.sajda.app.R.string.next_7_days)
        },
        onBack = onBack
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChoiceChip(
                label = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.str_7_days),
                selected = selectedRangeDays == 7,
                onClick = { selectedRangeDays = 7 }
            )
            ChoiceChip(
                label = androidx.compose.ui.res.stringResource(com.sajda.app.R.string.str_30_days),
                selected = selectedRangeDays == 30,
                onClick = { selectedRangeDays = 30 }
            )
        }

        prayerTimes.forEach { prayerTime ->
            val details = PrayerTimeCalculator.calculateDetailedPrayerTimes(
                date = LocalDate.parse(prayerTime.date),
                latitude = prayerTime.latitude,
                longitude = prayerTime.longitude,
                locationName = prayerTime.locationName,
                calculationMethod = settings.prayerCalculationMethod,
                asrMadhhab = settings.asrMadhhab
            )
            SanctuaryCard {
                Text(
                    text = DateTimeUtils.formatDateLabel(prayerTime.date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (settings.appLanguage == AppLanguage.INDONESIAN) "Imsak: ${details.imsak} • Terbit: ${details.sunrise}" else "Imsak: ${details.imsak} • Sunrise: ${details.sunrise}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    WeeklyPrayerSlot(androidx.compose.ui.res.stringResource(PrayerName.FAJR.displayNameRes()), prayerTime.fajr)
                    WeeklyPrayerSlot(androidx.compose.ui.res.stringResource(PrayerName.DHUHR.displayNameRes()), prayerTime.dhuhr)
                    WeeklyPrayerSlot(androidx.compose.ui.res.stringResource(PrayerName.ASR.displayNameRes()), prayerTime.asr)
                    WeeklyPrayerSlot(androidx.compose.ui.res.stringResource(PrayerName.MAGHRIB.displayNameRes()), prayerTime.maghrib)
                    WeeklyPrayerSlot(androidx.compose.ui.res.stringResource(PrayerName.ISHA.displayNameRes()), prayerTime.isha)
                }
            }
        }
    }
}

@Composable
private fun WeeklyPrayerSlot(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QiblaScreen(
    prayerTime: PrayerTime?,
    appLanguage: AppLanguage,
    onBack: (() -> Unit)? = null
) {
    val direction = prayerTime?.qiblaDirection ?: 294.0
    val compassState = rememberCompassState()
    val qiblaRotation = ((direction.toFloat() - compassState.heading) + 360f) % 360f
    
    val isAligned = qiblaRotation < 3f || qiblaRotation > 357f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        SajdaTopBar(
            title = if (appLanguage == AppLanguage.INDONESIAN) "Arah Kiblat" else "Qibla Direction",
            subtitle = prayerTime?.locationName,
            leading = onBack?.let { backAction ->
                {
                    SajdaTopAction(
                        Icons.Rounded.ArrowBack,
                        if (appLanguage == AppLanguage.INDONESIAN) "Kembali" else "Back",
                        backAction
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .clip(CircleShape)
                        .background(
                            if (isAligned) MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CompassFace(
                        modifier = Modifier
                            .size(260.dp)
                            .rotate(-compassState.heading)
                    )
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.Navigation,
                        contentDescription = "Qibla Direction",
                        tint = if (isAligned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(100.dp)
                            .rotate(qiblaRotation)
                    )
                }

                Text(
                    text = "${compassState.heading.toInt()}°",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isAligned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetadataChip(
                        text = if (appLanguage == AppLanguage.INDONESIAN) "Kiblat: ${direction.toInt()}°" else "Qibla: ${direction.toInt()}°",
                        active = true
                    )
                    MetadataChip(
                        text = if (appLanguage == AppLanguage.INDONESIAN) "Heading: ${compassState.heading.toInt()}°" else "Heading: ${compassState.heading.toInt()}°"
                    )
                    MetadataChip(
                        text = when (compassState.accuracy) {
                            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> if (appLanguage == AppLanguage.INDONESIAN) "Akurasi Tinggi" else "High Accuracy"
                            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> if (appLanguage == AppLanguage.INDONESIAN) "Akurasi Sedang" else "Medium Accuracy"
                            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> if (appLanguage == AppLanguage.INDONESIAN) "Butuh Kalibrasi" else "Needs Calibration"
                            else -> if (appLanguage == AppLanguage.INDONESIAN) "Akurasi Tidak Diketahui" else "Accuracy Unavailable"
                        }
                    )
                }
            }
        }

        Text(
            text = if (compassState.isAvailable) {
                if (isAligned) {
                    if (appLanguage == AppLanguage.INDONESIAN) "Telepon Anda terarah ke Ka'bah" else "Your phone is aligned with Ka'bah"
                } else {
                    if (compassState.accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
                        if (appLanguage == AppLanguage.INDONESIAN) "Kalibrasi kompas dengan memutar ponsel membentuk angka 8" else "Calibrate the compass by turning the phone in a figure-eight pattern"
                    } else {
                        if (appLanguage == AppLanguage.INDONESIAN) "Pegang ponsel sejajar dan putar perlahan sampai jarum berwarna emas lurus ke atas" else "Hold the phone flat and turn slowly until the golden needle points straight up"
                    }
                }
            } else {
                if (appLanguage == AppLanguage.INDONESIAN) "Sensor kompas tidak tersedia di perangkat ini" else "Compass sensors are unavailable on this device"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (isAligned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isAligned) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
/*
        SanctuaryCard {
            Text(
                text = "Fallback manual",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Kalau sensor kompas lemah, hadapkan sisi atas ponsel ke ${direction.toInt()}° dari utara. Putar badan perlahan sampai heading mendekati nilai itu.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
*/

@Composable
private fun CompassFace(modifier: Modifier = Modifier) {
    val colorOnSurface = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Canvas(modifier = modifier.size(260.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f
        
        drawCircle(
            color = colorOnSurface.copy(alpha = 0.03f),
            radius = radius
        )
        drawCircle(
            color = colorOnSurface.copy(alpha = 0.12f),
            radius = radius,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
        )
        drawCircle(
            color = colorOnSurface.copy(alpha = 0.08f),
            radius = radius * 0.7f,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 1.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )
        )
        drawLine(
            color = colorOnSurface.copy(alpha = 0.1f),
            start = Offset(size.width / 2f, radius * 0.1f),
            end = Offset(size.width / 2f, size.height - radius * 0.1f),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = colorOnSurface.copy(alpha = 0.1f),
            start = Offset(radius * 0.1f, size.height / 2f),
            end = Offset(size.width - radius * 0.1f, size.height / 2f),
            strokeWidth = 1.dp.toPx()
        )
        for (angle in 0 until 360 step 15) {
            val angleRad = Math.toRadians(angle.toDouble()).toFloat()
            val isMajor = angle % 90 == 0
            val isNorth = angle == 0
            
            val tickLength = if (isNorth) 16.dp.toPx() else if (isMajor) 12.dp.toPx() else 8.dp.toPx()
            val tickWidth = if (isNorth || isMajor) 2.5.dp.toPx() else 1.dp.toPx()
            val tickColor = if (isNorth) secondaryColor else if (isMajor) primaryColor else colorOnSurface.copy(alpha = 0.25f)
            
            val startX = center.x + (radius - 4.dp.toPx() - tickLength) * Math.sin(angleRad.toDouble()).toFloat()
            val startY = center.y - (radius - 4.dp.toPx() - tickLength) * Math.cos(angleRad.toDouble()).toFloat()
            
            val endX = center.x + (radius - 4.dp.toPx()) * Math.sin(angleRad.toDouble()).toFloat()
            val endY = center.y - (radius - 4.dp.toPx()) * Math.cos(angleRad.toDouble()).toFloat()
            
            drawLine(
                color = tickColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = tickWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

private data class CompassState(
    val heading: Float,
    val isAvailable: Boolean,
    val accuracy: Int
)

@Composable
private fun rememberCompassState(): CompassState {
    val context = LocalContext.current
    val sensorManager = remember(context) {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val rotationSensor = remember(sensorManager) {
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }
    var headingDegrees by remember { mutableFloatStateOf(0f) }
    var sensorAccuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_UNRELIABLE) }

    DisposableEffect(sensorManager, rotationSensor) {
        if (rotationSensor == null) {
            return@DisposableEffect onDispose { }
        }

        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                if (azimuth < 0f) {
                    azimuth += 360f
                }
                headingDegrees = azimuth
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                sensorAccuracy = accuracy
            }
        }

        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    return CompassState(
        heading = headingDegrees,
        isAvailable = rotationSensor != null,
        accuracy = sensorAccuracy
    )
}
