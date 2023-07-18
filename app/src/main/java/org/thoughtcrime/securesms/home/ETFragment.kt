package org.thoughtcrime.securesms.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.FragmentEtBinding
import org.thoughtcrime.securesms.BaseFragment
import org.thoughtcrime.securesms.util.viewbindingdelegate.viewBinding

/**
 * Created by Yaakov on
 * Describe:
 */
@AndroidEntryPoint
class ETFragment : BaseFragment<ETViewModel>(R.layout.fragment_et) {

    private val binding by viewBinding(FragmentEtBinding::bind)
    override val viewModel by viewModels<ETViewModel>()

    private val adapter: ETAdapter = ETAdapter()

    // 0 Following 1 Explore
    var type: Int? = null


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
            adapter.setOnItemClickListener { adapter, view, position ->

            }
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.cursor = ""
                viewModel.loadET()
            }

        }
    }

    private fun initData() {
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