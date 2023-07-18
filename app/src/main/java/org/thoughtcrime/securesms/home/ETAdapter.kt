package org.thoughtcrime.securesms.home

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import network.qki.messenger.R
import network.qki.messenger.databinding.ItemEtBinding
import org.thoughtcrime.securesms.util.GlideHelper

class ETAdapter : BaseQuickAdapter<ET, BaseViewHolder>(R.layout.item_et), LoadMoreModule {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        ItemEtBinding.bind(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: ET) {
        item?.let {
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
                    placeHolder = R.drawable.ic_launcher_round,
                    errorHolder = R.drawable.ic_launcher_round
                )
            }
        }

    }


}