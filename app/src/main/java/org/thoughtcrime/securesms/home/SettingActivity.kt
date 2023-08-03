package org.thoughtcrime.securesms.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.hjq.language.MultiLanguages
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivitySettingBinding
import org.session.libsession.utilities.CacheDataManager
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.preferences.NotificationSettingsActivity
import org.thoughtcrime.securesms.preferences.PrivacySettingsActivity
import org.thoughtcrime.securesms.util.ThemeState
import org.thoughtcrime.securesms.util.push
import org.thoughtcrime.securesms.util.themeState
import org.thoughtcrime.securesms.util.toastOnUi
import java.util.Locale
import javax.inject.Inject


/**
 * Created by Yaakov on
 * Describe:
 */
@AndroidEntryPoint
class SettingActivity : PassphraseRequiredActionBarActivity() {

    private val viewModel by viewModels<SettingViewModel>()

    private lateinit var binding: ActivitySettingBinding

    private var currentTheme: ThemeState? = null

    @Inject
    lateinit var textSecurePreferences: TextSecurePreferences

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.statusBarColor = getColorFromAttr(R.attr.chatsToolbarColor)
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        with(binding) {
            updateUI()
            llTheme.setOnClickListener {
                var position = when (textSecurePreferences.themeState().theme) {
                    R.style.Classic_Light -> 0
                    R.style.Classic_Dark -> 1
                    else -> 0
                }
                if (textSecurePreferences.getFollowSystemSettings()) {
                    position = 2
                }
                XPopup.Builder(this@SettingActivity)
                    .asCustom(ThemePopupView(this@SettingActivity, position, { position ->
                        val currentBase =
                            if (currentTheme?.theme == R.style.Classic_Dark || currentTheme?.theme == R.style.Classic_Light) R.style.Classic else R.style.Ocean
                        val (mappedStyle, newBase) = when (position) {
                            0 -> TextSecurePreferences.CLASSIC_LIGHT to R.style.Classic
                            1 -> TextSecurePreferences.CLASSIC_DARK to R.style.Classic
                            else -> throw NullPointerException("Invalid style for view")
                        }
                        viewModel.setNewStyle(mappedStyle)
                        viewModel.setNewFollowSystemSettings(false)
                        if (currentBase != newBase) {
                            if (newBase == R.style.Ocean) {
                                viewModel.setNewAccent(R.style.PrimaryBlue)
                            } else if (newBase == R.style.Classic) {
                                viewModel.setNewAccent(R.style.PrimaryGreen)
                            }
                        }
                    }, {
                        viewModel.setNewFollowSystemSettings(true)
                    }))
                    .show()
            }
            llLanguage.setOnClickListener {
                XPopup.Builder(this@SettingActivity)
                    .asCustom(LanguagePopupView(this@SettingActivity) { position ->
                        var restart = when (position) {
                            0 -> MultiLanguages.setAppLanguage(this@SettingActivity, Locale.CHINA)
                            1 -> MultiLanguages.setAppLanguage(this@SettingActivity, Locale.TAIWAN)
                            2 -> MultiLanguages.setAppLanguage(this@SettingActivity, Locale.ENGLISH)
                            3 -> MultiLanguages.clearAppLanguage(this@SettingActivity)
                            else -> false
                        }
                        if (restart) {
                            recreate()
                        }
                        updateUI()
                    })
                    .show()
            }
            llCache.setOnClickListener {
                XPopup.Builder(this@SettingActivity)
                    .asCustom(TipPopupView(this@SettingActivity, getString(R.string.tips), getString(R.string.clean_cache_tip), getString(R.string.ok), "") { position ->
                        when (position) {
                            0 -> {
                                CacheDataManager.clearAllCache(this@SettingActivity)
                                tvCache.text = "0MB"
                            }
                        }

                    })
                    .show()

            }
            llPrivacy.setOnClickListener {
                val intent = Intent(this@SettingActivity, PrivacySettingsActivity::class.java)
                push(intent)
            }
            llNotify.setOnClickListener {
                val intent = Intent(this@SettingActivity, NotificationSettingsActivity::class.java)
                push(intent)
            }
            llReadReceipt.setOnClickListener {
                switchReadReceipt.isChecked = !switchReadReceipt.isChecked
                textSecurePreferences.setReadReceiptsEnabled(switchReadReceipt.isChecked)
            }
            llTrim.setOnClickListener {
                if (textSecurePreferences.isThreadLengthTrimmingEnabled()) {
                    switchTrim.isChecked = !switchTrim.isChecked
                    textSecurePreferences.setThreadLengthTrimmingEnabled(switchTrim.isChecked)
                } else {
                    XPopup.Builder(this@SettingActivity)
                        .asCustom(TipPopupView(this@SettingActivity, getString(R.string.tips), getString(R.string.preferences_chats__message_trimming_summary), getString(R.string.ok), "") { position ->
                            when (position) {
                                0 -> {
                                    switchTrim.isChecked = !switchTrim.isChecked
                                    textSecurePreferences.setThreadLengthTrimmingEnabled(switchTrim.isChecked)
                                }
                            }

                        })
                        .show()
                }
            }
            llTypingIndicators.setOnClickListener {
                switchTypingIndicators.isChecked = !switchTypingIndicators.isChecked
                textSecurePreferences.setTypingIndicatorsEnabled(switchTypingIndicators.isChecked)
            }
            llLinkPreview.setOnClickListener {
                switchLinkPreview.isChecked = !switchLinkPreview.isChecked
                textSecurePreferences.setLinkPreviewsEnabled(switchLinkPreview.isChecked)
            }
            llCall.setOnClickListener {
                if (textSecurePreferences.isCallNotificationsEnabled()) {
                    switchCall.isChecked = !switchCall.isChecked
                    textSecurePreferences.setCallNotificationsEnabled(switchCall.isChecked)
                } else {
                    XPopup.Builder(this@SettingActivity)
                        .asCustom(TipPopupView(this@SettingActivity, getString(R.string.tips), getString(R.string.preferences__allow_access_voice_video), getString(R.string.ok), "") { position ->
                            when (position) {
                                0 -> {
                                    switchCall.isChecked = !switchCall.isChecked
                                    textSecurePreferences.setCallNotificationsEnabled(switchCall.isChecked)
                                }
                            }

                        })
                        .show()
                }
            }
            llInvite.setOnClickListener {
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                val invitation = getString(R.string.invitation) + "${textSecurePreferences.getLocalNumber()} "
                intent.putExtra(Intent.EXTRA_TEXT, invitation)
                intent.type = "text/plain"
                val chooser =
                    Intent.createChooser(intent, getString(R.string.activity_settings_invite_button_title))
                startActivity(chooser)
            }
            llAbout.setOnClickListener {
                toastOnUi("todo")
            }
            llWebsite.setOnClickListener {
                toastOnUi("todo")
            }
            tvExit.setOnClickListener {
                XPopup.Builder(this@SettingActivity)
                    .asCustom(TipPopupView(this@SettingActivity, getString(R.string.exit), getString(R.string.tip_exit), getString(R.string.ok), "") { position ->
                        when (position) {
                            0 -> {
                                viewModel.clearAllData()
                            }
                        }

                    })
                    .show()
            }
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.themeLiveData.observe(this) { themeState ->
            val (theme, accent, followSystem) = themeState
            updateUI()
            if (currentTheme != null && currentTheme != themeState) {
                recreate()
            } else {
                currentTheme = themeState
            }
        }
        lifecycleScope.launchWhenResumed {
            viewModel.uiState.collectLatest { themeState ->
                val (theme, accent, followSystem) = themeState
                updateUI()
                if (currentTheme != null && currentTheme != themeState) {
                    recreate()
                } else {
                    currentTheme = themeState
                }
            }
        }
    }

    private fun updateUI() {
        when (textSecurePreferences.themeState().theme) {
            R.style.Classic_Light -> binding.tvTheme.text = "Light"
            R.style.Classic_Dark -> binding.tvTheme.text = "Dark"
        }
        if (MultiLanguages.isSystemLanguage()) {
            binding.tvLanguage.text = getString(R.string.follow_system)
        } else {
            when (MultiLanguages.getAppLanguage()) {
                Locale.CHINA -> binding.tvLanguage.text = "简体中文"
                Locale.TAIWAN -> binding.tvLanguage.text = "繁体中文"
                Locale.ENGLISH -> binding.tvLanguage.text = "English"
            }
        }
        binding.tvCache.text = "${CacheDataManager.getTotalCacheSize(this)}"
        binding.switchReadReceipt.isChecked = textSecurePreferences.isReadReceiptsEnabled()
        binding.switchTrim.isChecked = textSecurePreferences.isThreadLengthTrimmingEnabled()
        binding.switchTypingIndicators.isChecked = textSecurePreferences.isTypingIndicatorsEnabled()
        binding.switchLinkPreview.isChecked = textSecurePreferences.isLinkPreviewsEnabled()
        binding.switchCall.isChecked = textSecurePreferences.isCallNotificationsEnabled()
    }


}