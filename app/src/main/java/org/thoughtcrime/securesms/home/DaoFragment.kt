package org.thoughtcrime.securesms.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.FragmentDaoBinding
import org.thoughtcrime.securesms.BaseFragment
import org.thoughtcrime.securesms.et.ETFragmentAdapter
import org.thoughtcrime.securesms.et.ETPublishActivity
import org.thoughtcrime.securesms.util.viewbindingdelegate.viewBinding

/**
 * Created by Yaakov on
 * Describe:
 */
@AndroidEntryPoint
class DaoFragment : BaseFragment<DaoViewModel>(R.layout.fragment_dao) {

    private val binding by viewBinding(FragmentDaoBinding::bind)
    override val viewModel by viewModels<DaoViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

    }


    private fun initView() {
        val adapter = ETFragmentAdapter(parentFragment = this)
        binding.viewpager.adapter = adapter
        val mediator = TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, pos ->
            tab.text = when (pos) {
                0 -> getString(R.string.following)
                1 -> getString(R.string.explore)
                else -> throw IllegalStateException()
            }
        }
        mediator.attach()
        binding.floatingActionBar.setOnClickListener {
            val intent = Intent(context, ETPublishActivity::class.java)
            show(intent)
        }
    }

}