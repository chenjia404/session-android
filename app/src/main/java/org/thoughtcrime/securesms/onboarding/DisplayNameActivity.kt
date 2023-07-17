package org.thoughtcrime.securesms.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityDisplayNameBinding
import org.session.libsession.utilities.SSKEnvironment.ProfileManagerProtocol
import org.session.libsession.utilities.TextSecurePreferences
import org.thoughtcrime.securesms.ApplicationContext
import org.thoughtcrime.securesms.BaseActionBarActivity
import org.thoughtcrime.securesms.home.HomeActivity
import org.thoughtcrime.securesms.util.push

class DisplayNameActivity : BaseActionBarActivity() {
    private lateinit var binding: ActivityDisplayNameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisplayNameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        with(binding) {
            displayNameEditText.imeOptions = displayNameEditText.imeOptions or 16777216 // Always use incognito keyboard
            displayNameEditText.setOnEditorActionListener(
                OnEditorActionListener { _, actionID, event ->
                    if (actionID == EditorInfo.IME_ACTION_SEARCH ||
                        actionID == EditorInfo.IME_ACTION_DONE ||
                        (event.action == KeyEvent.ACTION_DOWN &&
                                event.keyCode == KeyEvent.KEYCODE_ENTER)
                    ) {
                        register()
                        return@OnEditorActionListener true
                    }
                    false
                })
            registerButton.setOnClickListener { register() }
        }
    }

    private fun register() {
        val displayName = binding.displayNameEditText.text.toString().trim()
        if (displayName.isEmpty()) {
            return Toast.makeText(this, R.string.activity_display_name_display_name_missing_error, Toast.LENGTH_SHORT).show()
        }
        if (displayName.toByteArray().size > ProfileManagerProtocol.Companion.NAME_PADDED_LENGTH) {
            return Toast.makeText(this, R.string.activity_display_name_display_name_too_long_error, Toast.LENGTH_SHORT).show()
        }
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.displayNameEditText.windowToken, 0)
        TextSecurePreferences.setProfileName(this, displayName)
//        val intent = Intent(this, PNModeActivity::class.java)
//        push(intent)

        TextSecurePreferences.setHasSeenWelcomeScreen(this, true)
        TextSecurePreferences.setIsUsingFCM(this, true)
        val application = ApplicationContext.getInstance(this)
        application.startPollingIfNeeded()
        application.registerForFCMIfNeeded(true)
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        push(intent)
    }
}