package com.sajda.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import androidx.media3.common.util.UnstableApi
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.util.AppLocaleManager
import com.sajda.app.util.AppTranslations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltAndroidApp
@UnstableApi
class SajdaApplication : Application() {
    private val appScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        AppTranslations.init(this)
        appScope.launch {
            runCatching {
                val language = PreferencesDataStore(this@SajdaApplication).settingsFlow.first().appLanguage
                AppLocaleManager.apply(language)
            }
        }
    }
}

