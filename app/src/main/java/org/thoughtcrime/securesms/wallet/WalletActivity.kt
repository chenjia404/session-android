package org.thoughtcrime.securesms.wallet

import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityWalletBinding
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.util.StatusBarUtil

@AndroidEntryPoint
class WalletActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityWalletBinding

    private val viewModel by viewModels<WalletViewModel>()

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtil.setStatusColor(this, true, false, R.color.core_white)
        initView()

    }


    private fun initView() {
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        with(binding) {

        }

    }


}
