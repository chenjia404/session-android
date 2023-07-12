package org.thoughtcrime.securesms.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Created by Yaakov on
 * Describe:
 */
class ManagerViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    private val fragments: MutableList<Fragment> = ArrayList()

    init {
        fragments.add(ChatsFragment())
        fragments.add(ChatsFragment())
        fragments.add(SettingFragment())
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    override fun getItemCount(): Int {
        return fragments.size
    }
}
