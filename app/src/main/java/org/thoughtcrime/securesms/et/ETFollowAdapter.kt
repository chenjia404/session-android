package org.thoughtcrime.securesms.et

import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import network.qki.messenger.R
import network.qki.messenger.databinding.ItemEtFollowBinding
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.util.GlideHelper
import org.thoughtcrime.securesms.util.formatAddress

class ETFollowAdapter : BaseQuickAdapter<User, BaseViewHolder>(R.layout.item_et_follow), LoadMoreModule {

    var type: Int? = null

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        ItemEtFollowBinding.bind(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: User) {
        item.let {
            ItemEtFollowBinding.bind(holder.itemView)?.apply {
                ivAdd.isVisible = type != 0
                if (type == 0) {
                    tvOpt.setTextColor(context.getColorFromAttr(R.attr.thirdTextColor))
                    tvOpt.text = context.getString(R.string.following)
                } else {
                    if (item.IsFollow == true) {
                        tvOpt.setTextColor(context.getColorFromAttr(R.attr.thirdTextColor))
                        tvOpt.text = context.getString(R.string.friend)
                        ivAdd.isVisible = false
                    } else {
                        tvOpt.setTextColor(context.getColor(R.color.color3E66FB))
                        tvOpt.text = context.getString(R.string.follow)
                        ivAdd.isVisible = true
                    }
                }
                tvUserName.text = item.Nickname
                tvAddress.text = it.UserAddress?.formatAddress()
                GlideHelper.showImage(
                    view = ivAvatar,
                    url = item.Avatar ?: "",
                    roundRadius = 100,
                    placeHolder = R.drawable.ic_pic_default_round,
                    errorHolder = R.drawable.ic_pic_default_round
                )
            }
        }

    }


}