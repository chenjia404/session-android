package org.thoughtcrime.securesms.home

import android.app.Application
import androidx.annotation.StyleRes
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.session.libsession.utilities.TextSecurePreferences
import org.thoughtcrime.securesms.ApplicationContext
import org.thoughtcrime.securesms.BaseViewModel
import org.thoughtcrime.securesms.util.ConfigurationMessageUtilities
import org.thoughtcrime.securesms.util.ThemeState
import org.thoughtcrime.securesms.util.themeState
import org.thoughtcrime.securesms.util.toastOnUi
import javax.inject.Inject

/**
 * Created by Yaakov on
 * Describe:
 */
@HiltViewModel
class SettingViewModel @Inject constructor(private val textSecurePreferences: TextSecurePreferences, application: Application) : BaseViewModel(application) {

    val themeLiveData = MutableLiveData<ThemeState>()

    private val _uiState = MutableStateFlow(textSecurePreferences.themeState())
    val uiState: StateFlow<ThemeState> = _uiState

    fun setNewAccent(@StyleRes newAccentColorStyle: Int) {
        textSecurePreferences.setAccentColorStyle(newAccentColorStyle)
        _uiState.value = textSecurePreferences.themeState()
    }

    fun setNewStyle(newThemeStyle: String) {
        textSecurePreferences.setThemeStyle(newThemeStyle)
        _uiState.value = textSecurePreferences.themeState()
    }

    fun setNewFollowSystemSettings(followSystemSettings: Boolean) {
        textSecurePreferences.setFollowSystemSettings(followSystemSettings)
        _uiState.value = textSecurePreferences.themeState()
    }

    fun clearAllData() {
        execute {
            ConfigurationMessageUtilities.forceSyncConfigurationNowIfNeeded(context).get()
            ApplicationContext.getInstance(context).clearAllData(false)
        }.onSuccess {
        }.onError {
            context.toastOnUi(it.message)
        }
    }
}