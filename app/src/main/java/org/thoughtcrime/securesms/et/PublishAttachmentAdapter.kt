package org.thoughtcrime.securesms.et

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import network.qki.messenger.R
import network.qki.messenger.databinding.ItemPublishAttachBinding
import org.thoughtcrime.securesms.util.GlideHelper

class PublishAttachmentAdapter : BaseQuickAdapter<org.thoughtcrime.securesms.mediasend.Media, BaseViewHolder>(R.layout.item_publish_attach) {

    var type: Int? = null

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        ItemPublishAttachBinding.bind(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: org.thoughtcrime.securesms.mediasend.Media) {
        item.apply {
            ItemPublishAttachBinding.bind(holder.itemView)?.apply {
                GlideHelper.showImage(
                    ivAttach,
                    item.uri,
                    8,
                    R.drawable.ic_pic_default,
                    R.drawable.ic_pic_default
                )
            }
        }

    }


}