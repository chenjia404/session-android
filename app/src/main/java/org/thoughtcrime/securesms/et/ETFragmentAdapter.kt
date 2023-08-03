package org.thoughtcrime.securesms.et

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ETFragmentAdapter(parentFragment: Fragment) : FragmentStateAdapter(parentFragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ETFragment().apply {
                type = position
            }

            1 -> ETFragment().apply {
                type = position
            }

            else -> throw IllegalStateException()
        }
    }

}