package org.thoughtcrime.securesms.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.FragmentMeBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.session.libsession.utilities.TextSecurePreferences
import org.thoughtcrime.securesms.BaseFragment
import org.thoughtcrime.securesms.et.ETEditUserActivity
import org.thoughtcrime.securesms.et.ETFollowActivity
import org.thoughtcrime.securesms.et.ETUserCenterActivity
import org.thoughtcrime.securesms.et.ETViewModel
import org.thoughtcrime.securesms.et.User
import org.thoughtcrime.securesms.et.UserUpdateEvent
import org.thoughtcrime.securesms.util.GlideHelper
import org.thoughtcrime.securesms.util.sendToClip
import org.thoughtcrime.securesms.util.viewbindingdelegate.viewBinding


/**
 * Created by Yaakov on
 * Describe:
 */
@AndroidEntryPoint
class MeFragment : BaseFragment<ETViewModel>(R.layout.fragment_me) {

    private val binding by viewBinding(FragmentMeBinding::bind)
    override val viewModel by viewModels<ETViewModel>()

    private var isFirst: Boolean = true
    private var user: User? = null

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: UserUpdateEvent) {
        viewModel.cursor = ""
        loadData()
    }

    private fun initView() {
        with(binding) {
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.cursor = ""
                loadData()
            }
            tvId.setOnClickListener {
                requireContext().sendToClip(TextSecurePreferences.getLocalNumber(requireContext()))
            }
            llSetting.setOnClickListener {
                val intent = Intent(context, SettingActivity::class.java)
                show(intent)
            }
            llMoments.setOnClickListener {
                val intent = Intent(context, ETUserCenterActivity::class.java)
                intent.putExtra(ETUserCenterActivity.KEY_USER, user)
                show(intent)
            }
            llFollow.setOnClickListener {
                val intent = Intent(context, ETFollowActivity::class.java)
                show(intent)
            }
            llFollower.setOnClickListener {
                val intent = Intent(context, ETFollowActivity::class.java)
                show(intent)
            }
            llUser.setOnClickListener {
                var intent = Intent(activity, ETEditUserActivity::class.java)
                intent.putExtra(ETUserCenterActivity.KEY_USER, user)
                show(intent)
            }
            ivCard.setOnClickListener {
                val intent = Intent(context, CardActivity::class.java)
                show(intent)
            }
            llInvite.setOnClickListener {
                XPopup.Builder(context)
                    .isLightStatusBar(true)
                    .asCustom(SharePopupView(requireContext()))
                    .show()
            }
            llAbout.setOnClickListener {
                val intent = Intent(context, AboutActivity::class.java)
                show(intent)
            }
        }
    }

    private fun initData() {
        loadData()
    }

    private fun initObserver() {
        viewModel.userInfoLiveData.observe(viewLifecycleOwner) {
            stopRefreshing(binding.swipeRefreshLayout)
            if (it?.user != null) {
                user = it.user
                updateUI(it.user)
                viewModel.updateLocalUser(it.user)
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
            tvId.text = "Session ID: ${TextSecurePreferences.getLocalNumber(requireContext())}"
            tvFollowNum.text = "${user.FollowCount}"
            tvFollowerNum.text = "${user.FansCount}"
            tvMoments.text = "${user.TwCount}"
        }
    }

}