package org.thoughtcrime.securesms.et

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ETFollowManagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ETFollowFragment().apply {
                type = position
            }

            1 -> ETFollowFragment().apply {
                type = position
            }

            else -> throw IllegalStateException()
        }
    }

}