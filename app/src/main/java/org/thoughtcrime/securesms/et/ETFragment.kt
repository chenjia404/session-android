package org.thoughtcrime.securesms.et

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.FragmentEtBinding
import network.qki.messenger.databinding.LayoutStatelayoutEmptyBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.session.libsession.utilities.TextSecurePreferences
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

    var isLogin: Boolean = false

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
                initData()
            }
            adapter.loadMoreModule.isAutoLoadMore = true
            adapter.loadMoreModule.isEnableLoadMoreIfNotFullPage = false
            adapter.setOnItemClickListener { adapter, _, position ->
                val et = adapter.data[position] as ET
                val intent = Intent(context, ETDetailActivity::class.java)
                intent.putExtra(KEY_ET, et)
                show(intent)
            }
            // empty
            val emptyViewBinding = LayoutStatelayoutEmptyBinding.inflate(LayoutInflater.from(context), root, false)
            adapter.headerWithEmptyEnable = true
            adapter.setEmptyView(emptyViewBinding.root)
            adapter.addChildClickViewIds(R.id.llFavorite, R.id.llForward)
            adapter.setOnItemChildClickListener { adapter, v, position ->
                val et = adapter.data[position] as ET
                when (v.id) {
                    R.id.llForward -> {
                        val intent = Intent(context, ETPublishActivity::class.java)
                        intent.putExtra(KEY_ET, et)
                        show(intent)
                    }

                    R.id.llFavorite -> {
                        viewModel.like({}, {}, et)
                    }

                    else -> {

                    }
                }
            }
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.cursor = ""
                initData()
            }

        }
    }

    private fun initData() {
        if (type == 0 && !isLogin) {
            viewModel.login()
        }
        if (type == 0) {
            loadET()
        } else {
            loadETFollow()
        }
    }

    private fun initObserver() {
        viewModel.userLiveData.observe(viewLifecycleOwner) {
            isLogin = true
            val localNickname = TextSecurePreferences.getProfileName(requireContext())
            if (!TextUtils.isEmpty(localNickname) && !localNickname.equals(it?.Nickname)) {
                viewModel.updateUser({ }, { }, it?.Avatar ?: "", localNickname ?: "", it?.Desc ?: "", it?.Sex ?: "", (System.currentTimeMillis() / 1000).toString())
            }
        }
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
        viewModel.likeLiveData.observe(viewLifecycleOwner) {
            var ets = (adapter.data as List<ET>).toMutableList()
            ets.forEachIndexed { index, et ->
                if (et.TwAddress.equals(it.TwAddress, true)) {
                    ets[index] = it
                    adapter.notifyItemChanged(index)
                }
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

    private fun loadETFollow() {
        viewModel.loadETFollow({
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