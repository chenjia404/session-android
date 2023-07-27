package org.thoughtcrime.securesms.et

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import network.qki.messenger.R
import network.qki.messenger.databinding.ItemEtCommentBinding
import org.thoughtcrime.securesms.util.GlideHelper

class ETDetailAdapter : BaseQuickAdapter<Comment, BaseViewHolder>(R.layout.item_et_comment), LoadMoreModule {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        ItemEtCommentBinding.bind(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: Comment) {
        item.let {
            ItemEtCommentBinding.bind(holder.itemView)?.apply {
                tvUserName.text = it.UserInfo?.Nickname
                tvContent.text = it.Content
                GlideHelper.showImage(
                    view = ivAvatar,
                    url = it.UserInfo?.Avatar ?: "",
                    roundRadius = 100,
                    placeHolder = R.drawable.ic_pic_default_round,
                    errorHolder = R.drawable.ic_pic_default_round
                )
            }
        }

    }


}