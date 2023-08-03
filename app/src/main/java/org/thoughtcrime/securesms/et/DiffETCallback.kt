package org.thoughtcrime.securesms.et

import androidx.recyclerview.widget.DiffUtil
import org.thoughtcrime.securesms.util.Logger

/**
 * Created by Yaakov on
 * Describe:
 */
class DiffETCallback : DiffUtil.ItemCallback<ET>() {
    override fun areItemsTheSame(oldItem: ET, newItem: ET): Boolean {
        return oldItem.TwAddress.equals(newItem.TwAddress, true)
    }

    override fun areContentsTheSame(oldItem: ET, newItem: ET): Boolean {
        Logger.d("isTwLike = ${oldItem.isTwLike == newItem.isTwLike}")
        return oldItem.isTwLike == newItem.isTwLike
    }
}