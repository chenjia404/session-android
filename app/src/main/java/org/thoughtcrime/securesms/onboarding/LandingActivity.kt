package org.thoughtcrime.securesms.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityLandingBinding
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.BaseActionBarActivity
import org.thoughtcrime.securesms.crypto.IdentityKeyUtil
import org.thoughtcrime.securesms.service.KeyCachingService
import org.thoughtcrime.securesms.util.push

class LandingActivity : BaseActionBarActivity() {

    lateinit var adapter: StartBannerAdapter
    lateinit var binding: ActivityLandingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.statusBarColor = getColorFromAttr(R.attr.mainColor)
        with(binding) {
            clCreate?.setOnClickListener { register() }
            clRecovery?.setOnClickListener { link() }
            initBanner()
        }
        IdentityKeyUtil.generateIdentityKeyPair(this)
        TextSecurePreferences.setPasswordDisabled(this, true)
        // AC: This is a temporary workaround to trick the old code that the screen is unlocked.
        KeyCachingService.setMasterSecret(applicationContext, Object())
    }

    private fun register() {
        val intent = Intent(this, RegisterActivity::class.java)
        push(intent)
    }

    private fun link() {
        val intent = Intent(this, LinkDeviceActivity::class.java)
        push(intent)
    }

    private fun initBanner() {
        binding.tvDesc.text = getString(R.string.banner_desc_1)
        val resIds: MutableList<StartBanner> = ArrayList()
        resIds.add(StartBanner(R.drawable.ic_banner_1, R.string.app_name))
        resIds.add(StartBanner(R.drawable.ic_banner_2, R.string.banner_desc_2_1))
        resIds.add(StartBanner(R.drawable.ic_banner_3, R.string.banner_desc_3_1))
        adapter = StartBannerAdapter()
        binding.banner
            ?.setIndicatorVisibility(View.GONE)
            ?.setLifecycleRegistry(lifecycle)
            ?.setAdapter(adapter)
            ?.registerOnPageChangeCallback(object : OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    when (position) {
                        0 -> {
                            binding.tvDesc.text = getString(R.string.banner_desc_1)
                        }

                        1 -> {
                            binding.tvDesc.text = getString(R.string.banner_desc_2)
                        }

                        2 -> {
                            binding.tvDesc.text = getString(R.string.banner_desc_3)
                        }

                        else -> {}
                    }
                }


            })
            ?.create(resIds)
    }

}