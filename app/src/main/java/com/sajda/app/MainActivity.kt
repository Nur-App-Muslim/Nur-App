package com.sajda.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.AudioRepository
import com.sajda.app.data.repository.HadithRepository
import com.sajda.app.data.repository.TafsirRepository
import com.sajda.app.data.repository.QuranRepository
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.service.AdhanPlaybackStore
import com.sajda.app.ui.component.SajdaScreenBackground
import com.sajda.app.ui.navigation.SajdaNavGraph
import com.sajda.app.ui.navigation.Screen
import com.sajda.app.ui.theme.SajdaAppTheme
import com.sajda.app.util.AppStartupInitializer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferencesDataStore: PreferencesDataStore
    @Inject lateinit var audioRepository: AudioRepository
    @Inject lateinit var quranRepository: QuranRepository
    @Inject lateinit var hadithRepository: HadithRepository
    @Inject lateinit var tafsirRepository: TafsirRepository
    @Inject lateinit var appStartupInitializer: AppStartupInitializer

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        setupLockScreenWakeup(intent)
        val startDestination = resolveStartScreen(intent)
        updateVolumeControlStream(intent)

        lifecycleScope.launch {
            appStartupInitializer.initialize()
        }

        setContent {
            val settings by preferencesDataStore.settingsFlow.collectAsStateWithLifecycle(
                initialValue = UserSettings()
            )
            val adhanPlaybackState by AdhanPlaybackStore.state.collectAsStateWithLifecycle()
            val isDarkTheme = settings.darkMode || settings.nightMode
            
            androidx.compose.runtime.LaunchedEffect(adhanPlaybackState.isActive) {
                volumeControlStream = if (adhanPlaybackState.isActive) {
                    android.media.AudioManager.STREAM_ALARM
                } else {
                    android.media.AudioManager.STREAM_MUSIC
                }
            }

            SajdaAppTheme(darkTheme = isDarkTheme) {
                val context = LocalContext.current
                val view = LocalView.current
                val backgroundColor = MaterialTheme.colorScheme.background.toArgb()
                
                SideEffect {
                    val window = (context as android.app.Activity).window
                    window.statusBarColor = backgroundColor
                    window.navigationBarColor = backgroundColor
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
                    WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDarkTheme
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SajdaScreenBackground {
                        val resolvedStartDest = startDestination

                        SajdaNavGraph(
                            startDestination = resolvedStartDest,
                            preferencesDataStore = preferencesDataStore,
                            quranRepository = quranRepository,
                            hadithRepository = hadithRepository,
                            tafsirRepository = tafsirRepository,
                            audioRepository = audioRepository,
                            coroutineScope = lifecycleScope,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        setupLockScreenWakeup(intent)
        updateVolumeControlStream(intent)
    }

    private fun setupLockScreenWakeup(intent: android.content.Intent?) {
        val isAdhan = intent?.getBooleanExtra("is_adhan", false) == true ||
                intent?.getStringExtra(com.sajda.app.util.Constants.EXTRA_OPEN_TAB) == "prayer" ||
                AdhanPlaybackStore.state.value.isActive

        if (isAdhan) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
                val keyguardManager = getSystemService(android.content.Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
                keyguardManager?.requestDismissKeyguard(this, null)
            } else {
                @Suppress("DEPRECATION")
                window.addFlags(
                    android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                )
            }
        }
    }

    private fun updateVolumeControlStream(intent: android.content.Intent?) {
        val isAdhan = intent?.getBooleanExtra("is_adhan", false) == true || AdhanPlaybackStore.state.value.isActive
        volumeControlStream = if (isAdhan) {
            android.media.AudioManager.STREAM_ALARM
        } else {
            android.media.AudioManager.STREAM_MUSIC
        }
        if (isAdhan) {
            intent?.removeExtra("is_adhan")
        }
    }

    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        if (AdhanPlaybackStore.state.value.isActive) {
            if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                when (event.keyCode) {
                    android.view.KeyEvent.KEYCODE_VOLUME_DOWN,
                    android.view.KeyEvent.KEYCODE_VOLUME_UP,
                    android.view.KeyEvent.KEYCODE_VOLUME_MUTE,
                    android.view.KeyEvent.KEYCODE_POWER -> {
                        val stopIntent = android.content.Intent(this, com.sajda.app.service.AdzanService::class.java).apply {
                            action = com.sajda.app.util.Constants.ACTION_STOP_ADZAN
                        }
                        startService(stopIntent)
                        return true
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun resolveStartScreen(intent: android.content.Intent?): Screen {
        return when (intent?.getStringExtra(com.sajda.app.util.Constants.EXTRA_OPEN_TAB)) {
            "quran" -> Screen.Quran
            "prayer" -> Screen.Adhan
            "hadith" -> Screen.Home
            "ramadan" -> Screen.Home
            "settings", "more" -> Screen.Settings
            else -> Screen.Home
        }
    }
}
