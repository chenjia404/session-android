package org.thoughtcrime.securesms.home

import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityFollowBinding
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.et.ETFollowManagerAdapter
import org.thoughtcrime.securesms.util.StatusBarUtil


/**
 * Created by Yaakov on
 * Describe:
 */
@AndroidEntryPoint
class ETFollowActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityFollowBinding


    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityFollowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtil.setStatusColor(this, true, true, getColorFromAttr(R.attr.settingCardColor))
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
    }

    override fun initViews() {
        val adapter = ETFollowManagerAdapter(this)
        binding.viewpager.adapter = adapter
        val mediator = TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, pos ->
            tab.text = when (pos) {
                0 -> getString(R.string.following)
                1 -> getString(R.string.followers)
                else -> throw IllegalStateException()
            }
        }
        mediator.attach()
    }
}