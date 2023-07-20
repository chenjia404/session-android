package org.thoughtcrime.securesms.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.FragmentEtBinding
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsignal.crypto.MnemonicCodec
import org.session.libsignal.utilities.hexEncodedPrivateKey
import org.thoughtcrime.securesms.BaseFragment
import org.thoughtcrime.securesms.conversation.v2.ETDetailActivity
import org.thoughtcrime.securesms.crypto.IdentityKeyUtil
import org.thoughtcrime.securesms.crypto.MnemonicUtilities
import org.thoughtcrime.securesms.util.viewbindingdelegate.viewBinding
import javax.inject.Inject

/**
 * Created by Yaakov on
 * Describe:
 */
@AndroidEntryPoint
class ETFragment : BaseFragment<ETViewModel>(R.layout.fragment_et) {

    private val binding by viewBinding(FragmentEtBinding::bind)
    override val viewModel by viewModels<ETViewModel>()

    @Inject
    lateinit var textSecurePreferences: TextSecurePreferences

    private val adapter: ETAdapter = ETAdapter()

    // 0 Following 1 Explore
    var type: Int? = null

    companion object {
        // Extras
        const val KEY_ET = "et"

    }

    private val mnemonic by lazy {
        var hexEncodedSeed = IdentityKeyUtil.retrieve(requireContext(), IdentityKeyUtil.LOKI_SEED)
        if (hexEncodedSeed == null) {
            hexEncodedSeed = IdentityKeyUtil.getIdentityKeyPair(requireContext()).hexEncodedPrivateKey // Legacy account
        }
        val loadFileContents: (String) -> String = { fileName ->
            MnemonicUtilities.loadFileContents(requireContext(), fileName)
        }
        if (hexEncodedSeed.length == 64 && textSecurePreferences.isImportByPk()) {
            hexEncodedSeed
        } else {
            MnemonicCodec(loadFileContents).encode(hexEncodedSeed!!, MnemonicCodec.Language.Configuration.english)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
        initData()
    }

    private fun initView() {
        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
            adapter.loadMoreModule.setOnLoadMoreListener { viewModel.loadET() }
            adapter.loadMoreModule.isAutoLoadMore = true
            adapter.loadMoreModule.isEnableLoadMoreIfNotFullPage = false
            adapter.setOnItemClickListener { adapter, _, position ->
                val et = adapter.data[position] as ET
                val intent = Intent(context, ETDetailActivity::class.java)
                intent.putExtra(KEY_ET, et)
                show(intent)
            }
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.cursor = ""
                viewModel.loadET()
            }

        }
    }

    private fun initData() {
        viewModel.login(mnemonic)
        viewModel.loadET()
    }

    private fun initObserver() {
        viewModel.etsLiveData.observe(viewLifecycleOwner) {
            stopRefreshing(binding.swipeRefreshLayout)
            if (viewModel.cursor.isEmpty()) {
                adapter.data.clear()
            }
            adapter.loadMoreModule.isEnableLoadMore = true
            if (it.isNullOrEmpty()) {
                adapter.loadMoreModule.loadMoreEnd()
            } else {
                adapter.loadMoreModule.loadMoreComplete()
                adapter.addData(it)
            }
        }
    }


}