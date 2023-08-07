package org.thoughtcrime.securesms.et

import android.os.Bundle
import android.text.TextUtils
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityReportBinding
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.util.StatusBarUtil
import org.thoughtcrime.securesms.util.toastOnUi


/**
 * Created by Yaakov on
 * Describe:
 */
class ETReportActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityReportBinding

    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtil.setStatusColor(this, false, TextSecurePreferences.CLASSIC_DARK != TextSecurePreferences.getThemeStyle(this), getColorFromAttr(R.attr.chatsToolbarColor))
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
    }

    override fun initViews() {
        binding.tvAppName.text = getString(R.string.report)
        binding.tvOk.setOnClickListener {
            val content = binding.etContent.text.toString().trim()
            if (TextUtils.isEmpty(content)) {
                toastOnUi(R.string.content_not_empty)
            } else {
                binding.tvOk.postDelayed({
                    toastOnUi(getString(R.string.report_success))
                }, 2000)
            }
        }
    }


}