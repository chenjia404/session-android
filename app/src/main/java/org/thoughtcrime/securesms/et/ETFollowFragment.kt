package org.thoughtcrime.securesms.et

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import network.qki.messenger.R
import network.qki.messenger.databinding.FragmentEtFollowBinding
import org.thoughtcrime.securesms.BaseFragment
import org.thoughtcrime.securesms.util.viewbindingdelegate.viewBinding

/**
 * Created by Yaakov on
 * Describe:
 */
class ETFollowFragment : BaseFragment<ETViewModel>(R.layout.fragment_et_follow) {

    private val binding by viewBinding(FragmentEtFollowBinding::bind)
    override val viewModel by viewModels<ETViewModel>()

    private val adapter by lazy {
        ETFollowAdapter().apply {
            this.type = this@ETFollowFragment.type
        }
    }

    // 0 Following 1 Followers
    var type: Int? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
        initData()
    }

    private fun initView() {
        binding.apply {
            stateLayout.showProgressView()
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
            adapter.loadMoreModule.setOnLoadMoreListener {
                initData()
            }
            adapter.loadMoreModule.isAutoLoadMore = true
            adapter.loadMoreModule.isEnableLoadMoreIfNotFullPage = false
            adapter.addChildClickViewIds(R.id.llOpt)
            adapter.setOnItemChildClickListener { adapter, v, position ->
                when (v.id) {
                    R.id.llOpt -> {
                        val user = adapter.data[position] as User
                        if (user.IsFollow == true) {
                            viewModel.cancelFollow({}, {}, user.UserAddress ?: "")
                        } else {
                            viewModel.follow({}, {}, user.UserAddress ?: "")
                        }
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
        if (type == 0) {
            loadFollowing()
        } else {
            loadFollowers()
        }
    }

    private fun initObserver() {
        viewModel.followLiveData.observe(viewLifecycleOwner) {
            stopRefreshing(binding.swipeRefreshLayout)
            if (viewModel.page == 1) {
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
                viewModel.page = viewModel.page + 1
            }
            if (adapter.data.isNullOrEmpty()) {
                binding.stateLayout.showEmptyView(R.drawable.ic_statelayout_empty, getString(R.string.no_data), false) {}
            } else {
                binding.stateLayout.showContentView()
            }
        }
        viewModel.followStatusLiveData.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.page = 1
                if (type == 0) {
                    loadFollowing()
                } else {
                    loadFollowers()
                }
            }

        }
    }

    private fun loadFollowing() {
        viewModel.loadFollowing({}, {
            stopRefreshing(binding.swipeRefreshLayout)
        })
    }

    private fun loadFollowers() {
        viewModel.loadFollowers({}, {
            stopRefreshing(binding.swipeRefreshLayout)
        })
    }


}