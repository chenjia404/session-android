package org.thoughtcrime.securesms.et

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.FragmentEtBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
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

    private var isFirst: Boolean = true

    // 0 Following 1 Explore
    var type: Int? = null

    companion object {
        // Extras
        const val KEY_ET = "et"

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
        initView()
        initObserver()
        initData()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun initView() {
        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
            adapter.loadMoreModule.setOnLoadMoreListener {
                loadET()
            }
            adapter.loadMoreModule.isAutoLoadMore = true
            adapter.loadMoreModule.isEnableLoadMoreIfNotFullPage = false
            adapter.setOnItemClickListener { adapter, _, position ->
                val et = adapter.data[position] as ET
                val intent = Intent(context, ETDetailActivity::class.java)
                intent.putExtra(KEY_ET, et)
                show(intent)
            }
            adapter.addChildClickViewIds(R.id.llForward)
            adapter.setOnItemChildClickListener { adapter, v, position ->
                when (v.id) {
                    R.id.llForward -> {
                        val et = adapter.data[position] as ET
                        val intent = Intent(context, ETPublishActivity::class.java)
                        intent.putExtra(KEY_ET, et)
                        show(intent)
                    }

                    else -> {

                    }
                }
            }
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.cursor = ""
                loadET()
            }

        }
    }

    private fun initData() {
        viewModel.login()
        loadET()
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
            if (!it.isNullOrEmpty()) {
                viewModel.cursor = it?.last()?.Cursor ?: ""
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RefreshEvent) {
        viewModel.cursor = ""

    }

    private fun loadET() {
        viewModel.loadET({
//            if (isFirst) {
//                showLoading()
//            }
        }, {
//            isFirst = false
//            hideLoading()
            stopRefreshing(binding.swipeRefreshLayout)
        })
    }


}