package org.thoughtcrime.securesms.et

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexboxLayout
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.util.SmartGlideImageLoader
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityEtDetailBinding
import network.qki.messenger.databinding.ItemEtAttachBinding
import network.qki.messenger.databinding.LayoutEtDetailHeaderBinding
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.util.GlideHelper
import org.thoughtcrime.securesms.util.dateDifferenceDesc
import org.thoughtcrime.securesms.util.formatMediaUrl
import org.thoughtcrime.securesms.util.formatMedias
import org.thoughtcrime.securesms.util.parcelable
import org.thoughtcrime.securesms.util.show
import java.util.Date

@AndroidEntryPoint
class ETDetailActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityEtDetailBinding
    private lateinit var headerBinding: LayoutEtDetailHeaderBinding

    private val viewModel by viewModels<ETViewModel>()

    var et: ET? = null

    private val adapter: ETDetailAdapter = ETDetailAdapter()


    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityEtDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.statusBarColor = getColorFromAttr(R.attr.mainColor)
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar ?: return
        actionBar.title = ""
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        et = intent.parcelable(ETFragment.KEY_ET)
        et?.let {
            showLoading()
        } ?: finish()

    }


    override fun initViews() {
        super.initViews()
        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(this@ETDetailActivity)
            recyclerView.adapter = adapter
            initHeader()

            adapter.loadMoreModule.setOnLoadMoreListener { initData() }
            adapter.loadMoreModule.isAutoLoadMore = true
            adapter.loadMoreModule.isEnableLoadMoreIfNotFullPage = false
            adapter.setOnItemClickListener { adapter, view, position ->
                // TODO:
            }
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.page = 1
                initData()
            }

        }
    }

    override fun initData() {
        et?.TwAddress?.let {
            viewModel.loadComments(it)
        }
    }

    override fun initObserver() {
        viewModel.commentsLiveData.observe(this) {
            hideLoading()
            stopRefreshing(binding.swipeRefreshLayout)
            if (viewModel.page === 1) {
                adapter.data.clear()
            }
            adapter.loadMoreModule.isEnableLoadMore = true
            if (it.isNullOrEmpty()) {
                adapter.loadMoreModule.loadMoreEnd()
            } else {
                adapter.loadMoreModule.loadMoreComplete()
                adapter.addData(it)
            }
            viewModel.page++
        }
    }

    // initHeader
    private fun initHeader() {
        val headerView: View = layoutInflater.inflate(R.layout.layout_et_detail_header, null)
        headerBinding = LayoutEtDetailHeaderBinding.bind(headerView)
        adapter.addHeaderView(headerView)
        et?.apply {
            headerBinding.tvUserName.text = UserInfo?.Nickname
            headerBinding.tvContent.text = Content
            headerBinding.ivFavorite.isSelected = isTwLike
            headerBinding.tvFavoriteNum.text = "$LikeCount"
            if (isTwLike) {
                headerBinding.tvFavoriteNum.setTextColor(getColor(R.color.colorF03738))
            } else {
                headerBinding.tvFavoriteNum.setTextColor(getColorFromAttr(android.R.attr.textColorTertiary))
            }

            headerBinding.tvCommentNum.text = "$CommentCount"
            headerBinding.tvForwardNum.text = "$ForwardCount"
            headerBinding.tvTime.text = "${Date(CreatedAt?.toLong()?.times(1000) ?: System.currentTimeMillis()).dateDifferenceDesc()}"
            GlideHelper.showImage(
                headerBinding.ivAvatar,
                UserInfo?.Avatar ?: "",
                100,
                R.drawable.ic_pic_default_round,
                R.drawable.ic_pic_default_round
            )
            headerBinding.flexbox.removeAllViews()
            Attachment?.trim()?.let { it ->
                val medias = it.formatMedias()
                val urls = it.formatMediaUrl()
                if (!medias.isNullOrEmpty()) {
                    for (i in medias.indices) {
                        val media = medias[i]
                        val attachBinding = ItemEtAttachBinding.inflate(LayoutInflater.from(this@ETDetailActivity), headerBinding.root, false)
                        attachBinding.ivAttach.setOnClickListener {
                            showGallery(attachBinding.ivAttach, i, urls)
                        }
                        headerBinding.flexbox.addView(attachBinding.root)
                        val lp = attachBinding.root.layoutParams as FlexboxLayout.LayoutParams
                        lp.flexBasisPercent = 0.3f
                        GlideHelper.showImage(
                            attachBinding.ivAttach,
                            media.url,
                            8,
                            R.drawable.ic_pic_default,
                            R.drawable.ic_pic_default
                        )
                        attachBinding.ivAttach.foreground = null
                    }
                }
            }

            if (OriginTweet != null) {
                headerBinding.layoutForward.rootForward.isVisible = true
                headerBinding.layoutForward.tvUserName.text = OriginTweet?.UserInfo?.Nickname
                headerBinding.layoutForward.tvContent.text = OriginTweet?.Content
                headerBinding.tvTime.text = "${Date(OriginTweet?.CreatedAt?.toLong()?.times(1000) ?: System.currentTimeMillis()).dateDifferenceDesc()}"
                GlideHelper.showImage(
                    headerBinding.layoutForward.ivAvatar,
                    OriginTweet?.UserInfo?.Avatar ?: "",
                    100,
                    R.drawable.ic_pic_default_round,
                    R.drawable.ic_pic_default_round
                )
                headerBinding.layoutForward.flexbox.removeAllViews()
                OriginTweet?.Attachment?.trim()?.let { it ->
                    val medias = it.formatMedias()
                    val urls = it.formatMediaUrl()
                    if (!medias.isNullOrEmpty()) {
                        for (i in medias.indices) {
                            val media = medias[i]
                            val attachBinding = ItemEtAttachBinding.inflate(LayoutInflater.from(this@ETDetailActivity), headerBinding.layoutForward.root, false)
                            attachBinding.ivAttach.setOnClickListener {
                                showGallery(attachBinding.ivAttach, i, urls)
                            }
                            headerBinding.layoutForward.flexbox.addView(attachBinding.root)
                            val lp = attachBinding.root.layoutParams as FlexboxLayout.LayoutParams
                            lp.flexBasisPercent = 0.3f
                            GlideHelper.showImage(
                                attachBinding.ivAttach,
                                media.url,
                                8,
                                R.drawable.ic_pic_default,
                                R.drawable.ic_pic_default
                            )
                            if (i >= 8 && medias.size > 9) {
                                attachBinding.ivAttach.foreground = getDrawable(R.drawable.shape_pic_foreground)
                                attachBinding.tvNum.isVisible = true
                                attachBinding.tvNum.text = "+${medias.size - 9}"
                                break
                            } else {
                                attachBinding.ivAttach.foreground = null
                                attachBinding.tvNum.isVisible = false
                            }
                        }
                    }
                }
            } else {
                headerBinding.layoutForward.rootForward.isVisible = false
            }
            headerBinding.llComment.setOnClickListener {
                sendComment()
            }
            binding.llSend.setOnClickListener {
                sendComment()
            }
            headerBinding.llFavorite.setOnClickListener {
                viewModel.like({
                    et?.apply {
                        isTwLike = !isTwLike
                        LikeCount = if (isTwLike) {
                            LikeCount?.plus(1)
                        } else {
                            LikeCount?.minus(1)
                        }
                        headerBinding.ivFavorite.isSelected = isTwLike
                        headerBinding.tvFavoriteNum.text = "$LikeCount"
                        if (isTwLike) {
                            headerBinding.tvFavoriteNum.setTextColor(getColor(R.color.colorF03738))
                        } else {
                            headerBinding.tvFavoriteNum.setTextColor(getColorFromAttr(android.R.attr.textColorTertiary))
                        }
                    }
                }, {}, et!!)
            }
            headerBinding.llForward.setOnClickListener {
                val intent = Intent(this@ETDetailActivity, ETPublishActivity::class.java)
                intent.putExtra(ETFragment.KEY_ET, et)
                show(intent)
            }
        }
    }

    private fun sendComment() {
        XPopup.Builder(this@ETDetailActivity)
            .asCustom(ETCommentPopupView(this@ETDetailActivity, et!!) { _, content ->
                viewModel.releaseComment(et?.TwAddress ?: "", content)
            })
            .show()
    }

    private fun showGallery(imageView: ImageView, position: Int, urls: List<String>) {
        XPopup.Builder(this)
            .isTouchThrough(true)
            .asImageViewer(imageView, position, urls, false, true, -1, -1, 0, false, Color.rgb(32, 36, 46), { popupView, i ->

            }, SmartGlideImageLoader(), null)
            .show()

    }
}
