package org.thoughtcrime.securesms.home

import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.flexbox.FlexboxLayout
import network.qki.messenger.R
import network.qki.messenger.databinding.ItemEtAttachBinding
import network.qki.messenger.databinding.ItemEtBinding
import org.thoughtcrime.securesms.util.GlideHelper
import org.thoughtcrime.securesms.util.formatMedias

class ETAdapter : BaseQuickAdapter<ET, BaseViewHolder>(R.layout.item_et), LoadMoreModule {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        ItemEtBinding.bind(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: ET) {
        item.let {
            ItemEtBinding.bind(holder.itemView)?.apply {
                tvUserName.text = it.UserInfo.Nickname
                tvContent.text = it.Content
                ivFavorite.isSelected = it.isTwLike
                tvFavoriteNum.text = "${it.LikeCount}"
                tvCommentNum.text = "${it.CommentCount}"
                tvForwardNum.text = "${it.ForwardCount}"
                GlideHelper.showImage(
                    view = ivAvatar,
                    url = it.UserInfo.Avatar,
                    roundRadius = 100,
                    placeHolder = R.drawable.ic_pic_default_round,
                    errorHolder = R.drawable.ic_pic_default_round
                )
                flexbox.removeAllViews()
                it.Attachment.trim()?.let { it ->
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
            }
        }

    }


}