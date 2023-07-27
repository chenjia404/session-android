package org.thoughtcrime.securesms.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.FragmentMeBinding
import org.session.libsession.utilities.TextSecurePreferences
import org.thoughtcrime.securesms.BaseFragment
import org.thoughtcrime.securesms.et.ET
import org.thoughtcrime.securesms.et.ETDetailActivity
import org.thoughtcrime.securesms.et.ETFragment
import org.thoughtcrime.securesms.et.ETMeAdapter
import org.thoughtcrime.securesms.et.ETPublishActivity
import org.thoughtcrime.securesms.et.MeViewModel
import org.thoughtcrime.securesms.et.User
import org.thoughtcrime.securesms.util.GlideHelper
import org.thoughtcrime.securesms.util.sendToClip
import org.thoughtcrime.securesms.util.viewbindingdelegate.viewBinding
import java.lang.Float.max


/**
 * Created by Yaakov on
 * Describe:
 */
@AndroidEntryPoint
class MeFragment : BaseFragment<MeViewModel>(R.layout.fragment_me) {

    private val binding by viewBinding(FragmentMeBinding::bind)
    override val viewModel by viewModels<MeViewModel>()

    private var isFirst: Boolean = true
    private val adapter: ETMeAdapter = ETMeAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
        initData()
    }

    private fun initView() {
        with(binding) {
            appBarLayout.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout, verticalOffset ->
                swipeRefreshLayout.isEnabled = verticalOffset >= 0
                //val calcRange = appBarLayout.totalScrollRange / 2f
                val calcRange = appBarLayout.totalScrollRange * 0.1
                val calcOffset = max(0f, (kotlin.math.abs(verticalOffset) - calcRange).toFloat())
                val offsetPercent = 1 - (calcOffset / calcRange)
               // Logger.d("offsetPercent = $offsetPercent")
                var alpha = if (offsetPercent <= 1) {
                    (255 * offsetPercent).toInt()
                } else {
                    1
                }
                llWallet.alpha = alpha.toFloat()
                llName.alpha = (1 - alpha).toFloat()
            })
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
            adapter.loadMoreModule.setOnLoadMoreListener {
                viewModel.loadETFollow({}, {})
            }
            adapter.loadMoreModule.isAutoLoadMore = true
            adapter.loadMoreModule.isEnableLoadMoreIfNotFullPage = false
            adapter.setOnItemClickListener { adapter, _, position ->
                val et = adapter.data[position] as ET
                val intent = Intent(context, ETDetailActivity::class.java)
                intent.putExtra(ETFragment.KEY_ET, et)
                show(intent)
            }
            adapter.addChildClickViewIds(R.id.llForward)
            adapter.setOnItemChildClickListener { adapter, v, position ->
                when (v.id) {
                    R.id.llForward -> {
                        val et = adapter.data[position] as ET
                        val intent = Intent(context, ETPublishActivity::class.java)
                        intent.putExtra(ETFragment.KEY_ET, et)
                        show(intent)
                    }

                    else -> {

                    }
                }
            }
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.cursor = ""
                loadData()
            }
            tvId.setOnClickListener {
                requireContext().sendToClip(TextSecurePreferences.getLocalNumber(requireContext()))
            }
            ivSetting.setOnClickListener {
                val intent = Intent(context, SettingActivity::class.java)
                show(intent)
            }
            ivCard.setOnClickListener {
                val intent = Intent(context, CardActivity::class.java)
                show(intent)
            }

        }
    }

    private fun initData() {
        loadData()
    }

    private fun initObserver() {
        viewModel.userInfoLiveData.observe(viewLifecycleOwner) {
            if (it?.user != null) {
                updateUI(it.user)
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
    }

    private fun loadData() {
        viewModel.loadUserInfo({
            if (isFirst) {
                // showLoading()
            }
        }, {
            isFirst = false
            //hideLoading()
            stopRefreshing(binding.swipeRefreshLayout)
        }, viewModel.wallet.address)
        viewModel.loadETFollow({}, {})
    }

    private fun updateUI(user: User) {
        with(binding) {
            GlideHelper.showImage(
                ivAvatar,
                user?.Avatar ?: "",
                100,
                R.drawable.ic_pic_default_round,
                R.drawable.ic_pic_default_round
            )
            tvName.text = user.Nickname
            GlideHelper.showImage(
                ivTitleAvatar,
                user?.Avatar ?: "",
                100,
                R.drawable.ic_pic_default_round,
                R.drawable.ic_pic_default_round
            )
            tvTitleName.text = user.Nickname
            tvId.text = TextSecurePreferences.getLocalNumber(requireContext())
            tvFollowNum.text = "${user.FollowCount}"
            tvFollowerNum.text = "${user.FansCount}"
        }
    }


}