package org.thoughtcrime.securesms.home

import android.os.Bundle
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityAboutBinding
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.et.User
import org.thoughtcrime.securesms.util.DeviceUtils
import org.thoughtcrime.securesms.util.StatusBarUtil
import org.thoughtcrime.securesms.util.openUrl


/**
 * Created by Yaakov on
 * Describe:
 */
class AboutActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityAboutBinding

    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtil.setStatusColor(this, false, TextSecurePreferences.CLASSIC_DARK != TextSecurePreferences.getThemeStyle(this), getColorFromAttr(R.attr.chatsToolbarColor))
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
    }

    override fun initViews() {
        with(binding) {
            tvAppName.text = getString(R.string.about)
            if (TextSecurePreferences.CLASSIC_DARK == TextSecurePreferences.getThemeStyle(this@AboutActivity)) {
                ivAvatar.setImageResource(R.drawable.ic_logo_2_2)
            } else {
                ivAvatar.setImageResource(R.drawable.ic_logo_2_1)
            }
            tvVersion.text = "V${DeviceUtils.getVerName(this@AboutActivity)}"
            llTwitter.setOnClickListener {
                openUrl("")
            }
            llWebsite.setOnClickListener {
                openUrl("https://ethmessenger.app")
            }
        }


    }


}