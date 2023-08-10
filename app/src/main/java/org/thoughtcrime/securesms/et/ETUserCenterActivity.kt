package org.thoughtcrime.securesms.et

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityUserEtBinding
import network.qki.messenger.databinding.LayoutStatelayoutEmptyBinding
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.home.SettingActivity
import org.thoughtcrime.securesms.util.GlideHelper
import org.thoughtcrime.securesms.util.StatusBarUtil
import org.thoughtcrime.securesms.util.parcelable
import org.thoughtcrime.securesms.util.show
import java.lang.Float
import kotlin.Boolean
import kotlin.getValue
import kotlin.let
import kotlin.math.abs
import kotlin.with


/**
 * Created by Yaakov on
 * Describe:
 */
@AndroidEntryPoint
class ETUserCenterActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityUserEtBinding
    private val viewModel by viewModels<ETViewModel>()

    private var isFirst: Boolean = true
    private val adapter: ETMeAdapter = ETMeAdapter()

    var user: User? = null

    companion object {
        // Extras
        const val KEY_USER = "user"

    }

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityUserEtBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtil.setStatusColor(this, true, false, getColorFromAttr(R.attr.settingCardColor))
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        user = intent.parcelable(KEY_USER)
        user?.let {

        } ?: finish()
    }

    override fun initViews() {
        with(binding) {
            appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                swipeRefreshLayout.isEnabled = verticalOffset >= 0
                //val calcRange = appBarLayout.totalScrollRange / 2f
                val calcRange = appBarLayout.totalScrollRange * 0.1
                val calcOffset = Float.max(0f, (abs(verticalOffset) - calcRange).toFloat())
                val offsetPercent = 1 - (calcOffset / calcRange)
                // Logger.d("offsetPercent = $offsetPercent")
                var alpha = if (offsetPercent <= 1) {
                    (255 * offsetPercent).toInt()
                } else {
                    1
                }
                tvTitleName.alpha = (1 - alpha).toFloat()
            })
            recyclerView.layoutManager = LinearLayoutManager(this@ETUserCenterActivity)
            recyclerView.adapter = adapter
            adapter.loadMoreModule.setOnLoadMoreListener {
                viewModel.loadETFollow({}, {})
            }
            adapter.loadMoreModule.isAutoLoadMore = true
            adapter.loadMoreModule.isEnableLoadMoreIfNotFullPage = false
            adapter.setOnItemClickListener { adapter, _, position ->
                val et = adapter.data[position] as ET
                val intent = Intent(this@ETUserCenterActivity, ETDetailActivity::class.java)
                intent.putExtra(ETFragment.KEY_ET, et)
                show(intent)
            }
            adapter.addChildClickViewIds(R.id.llFavorite, R.id.llForward)
            adapter.setOnItemChildClickListener { adapter, v, position ->
                val et = adapter.data[position] as ET
                when (v.id) {
                    R.id.llForward -> {
                        val intent = Intent(this@ETUserCenterActivity, ETPublishActivity::class.java)
                        intent.putExtra(ETFragment.KEY_ET, et)
                        show(intent)
                    }

                    R.id.llFavorite -> {
                        viewModel.like({
                            et.isTwLike = !et.isTwLike
                            if (et.isTwLike) {
                                et.LikeCount = et.LikeCount?.plus(1)
                            } else {
                                et.LikeCount = et.LikeCount?.minus(1)
                            }
                            adapter.notifyItemChanged(position)
                        }, {}, et)
                    }

                    else -> {

                    }
                }
            }

            // empty
            val emptyViewBinding = LayoutStatelayoutEmptyBinding.inflate(LayoutInflater.from(this@ETUserCenterActivity), root, false)
            adapter.headerWithEmptyEnable = true
            adapter.setEmptyView(emptyViewBinding.root)
            emptyViewBinding.clOpt.setOnClickListener {
                val intent = Intent(this@ETUserCenterActivity, ETPublishActivity::class.java)
                show(intent)
            }
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.cursor = ""
                loadData()
            }
            ivSetting.setOnClickListener {
                val intent = Intent(this@ETUserCenterActivity, SettingActivity::class.java)
                show(intent)
            }
            tvFollow.setOnClickListener {
                if (user?.IsFollow == true) {
                    viewModel.cancelFollow({}, {}, user?.UserAddress ?: "")
                } else {
                    viewModel.follow({}, {}, user?.UserAddress ?: "")
                }

            }

        }
    }

    override fun initData() {
        showLoading()
        loadData()
    }

    override fun initObserver() {
        viewModel.userInfoLiveData.observe(this) {
            hideLoading()
            stopRefreshing(binding.swipeRefreshLayout)
            if (it?.user != null) {
                user = it.user
                updateUI(it.user)
            }
        }
        viewModel.etsLiveData.observe(this) {
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
        viewModel.followStatusLiveData.observe(this) {
            viewModel.loadUserInfo({}, {}, user?.UserAddress ?: "")
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
        }, user?.UserAddress ?: "")
        viewModel.loadETTimeline({}, {}, user?.UserAddress ?: "")
    }

    private fun updateUI(user: User) {
        with(binding) {
            val userJson = TextSecurePreferences.getUser(this@ETUserCenterActivity)
            val localUser = Gson().fromJson(userJson, User::class.java)
            if (user.UserAddress.equals(localUser.UserAddress, true)) {
                tvFollow.isVisible = false
                tvId.isVisible = true
                tvId.text = TextSecurePreferences.getLocalNumber(this@ETUserCenterActivity)
            } else {
                tvFollow.isVisible = true
                tvId.isVisible = false
                tvFollow.text = if (user.IsFollow == true) {
                    getString(R.string.unfollow)
                } else {
                    getString(R.string.follow)
                }
            }
            GlideHelper.showImage(
                ivAvatar,
                user?.Avatar ?: "",
                100,
                R.drawable.ic_pic_default_round,
                R.drawable.ic_pic_default_round
            )
            tvTitleName.text = user.Nickname
            tvName.text = user.Nickname
            tvFollowNum.text = "${user.FollowCount}"
            tvFollowerNum.text = "${user.FansCount}"
            tvFollow.backgroundTintList = if (user.IsFollow == true) {
                getColorStateList(R.color.color7A7B7D)
            } else {
                getColorStateList(R.color.color3E66FB)
            }
        }
    }
}