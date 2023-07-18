package org.thoughtcrime.securesms.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.just.agentweb.AgentWeb
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.FragmentDaoBinding
import org.session.libsignal.crypto.MnemonicCodec
import org.session.libsignal.utilities.hexEncodedPrivateKey
import org.thoughtcrime.securesms.BaseFragment
import org.thoughtcrime.securesms.crypto.IdentityKeyUtil
import org.thoughtcrime.securesms.crypto.MnemonicUtilities
import org.thoughtcrime.securesms.util.viewbindingdelegate.viewBinding

/**
 * Created by Yaakov on
 * Describe:
 */
@AndroidEntryPoint
class DaoFragment : BaseFragment<DaoViewModel>(R.layout.fragment_dao) {

    private val binding by viewBinding(FragmentDaoBinding::bind)
    override val viewModel by viewModels<DaoViewModel>()

    private lateinit var agentWeb: AgentWeb

    private val mnemonic by lazy {
        var hexEncodedSeed = IdentityKeyUtil.retrieve(requireContext(), IdentityKeyUtil.LOKI_SEED)
        if (hexEncodedSeed == null) {
            hexEncodedSeed = IdentityKeyUtil.getIdentityKeyPair(requireContext()).hexEncodedPrivateKey // Legacy account
        }
        val loadFileContents: (String) -> String = { fileName ->
            MnemonicUtilities.loadFileContents(requireContext(), fileName)
        }
        if (hexEncodedSeed.length == 64) {
            hexEncodedSeed
        } else {
            MnemonicCodec(loadFileContents).encode(hexEncodedSeed!!, MnemonicCodec.Language.Configuration.english)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

    }


    private fun initView() {
        val adapter = ETFragmentAdapter(parentFragment = this)
        binding.viewpager.adapter = adapter
        val mediator = TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, pos ->
            tab.text = when (pos) {
                0 -> getString(R.string.following)
                1 -> getString(R.string.explore)
                else -> throw IllegalStateException()
            }
        }
        mediator.attach()
    }

}