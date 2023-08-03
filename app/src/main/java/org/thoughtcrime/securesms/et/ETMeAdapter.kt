package org.thoughtcrime.securesms.et

import android.view.LayoutInflater
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.flexbox.FlexboxLayout
import network.qki.messenger.R
import network.qki.messenger.databinding.ItemEtAttachBinding
import network.qki.messenger.databinding.ItemEtMeBinding
import org.thoughtcrime.securesms.util.GlideHelper
import org.thoughtcrime.securesms.util.formatMedias

class ETMeAdapter : BaseQuickAdapter<ET, BaseViewHolder>(R.layout.item_et_me), LoadMoreModule {


    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        ItemEtMeBinding.bind(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: ET) {
        item.let {
            ItemEtMeBinding.bind(holder.itemView)?.apply {
                view1.isInvisible = holder.adapterPosition == 0
                tvUserName.text = it.UserInfo?.Nickname
                tvContent.text = it.Content
                ivFavorite.isSelected = it.isTwLike
                tvFavoriteNum.text = "${it.LikeCount}"
                tvCommentNum.text = "${it.CommentCount}"
                tvForwardNum.text = "${it.ForwardCount}"
                flexbox.removeAllViews()
                it.Attachment?.trim()?.let { it ->
                    val medias = it.formatMedias()
                    if (!medias.isNullOrEmpty()) {
                        for (i in medias.indices) {
                            val media = medias[i]
                            val attachBinding = ItemEtAttachBinding.inflate(LayoutInflater.from(context), root, false)
                            flexbox.addView(attachBinding.root)
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
                                attachBinding.ivAttach.foreground = context.getDrawable(R.drawable.shape_pic_foreground)
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
                val originTweet = it.OriginTweet
                if (originTweet != null) {
                    layoutForward.rootForward.isVisible = true
                    layoutForward.tvUserName.text = originTweet.UserInfo?.Nickname
                    layoutForward.tvContent.text = originTweet.Content
                    GlideHelper.showImage(
                        layoutForward.ivAvatar,
                        originTweet.UserInfo?.Avatar ?: "",
                        100,
                        R.drawable.ic_pic_default_round,
                        R.drawable.ic_pic_default_round
                    )
                    layoutForward.flexbox.removeAllViews()
                    originTweet.Attachment?.trim()?.let { it ->
                        val medias = it.formatMedias()
                        if (!medias.isNullOrEmpty()) {
                            for (i in medias.indices) {
                                val media = medias[i]
                                val attachBinding = ItemEtAttachBinding.inflate(LayoutInflater.from(context), root, false)
                                layoutForward.flexbox.addView(attachBinding.root)
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
                                    attachBinding.ivAttach.foreground = context.getDrawable(R.drawable.shape_pic_foreground)
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
                    layoutForward.rootForward.isVisible = false
                }
            }
        }
    }

}