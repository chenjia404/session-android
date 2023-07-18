package org.thoughtcrime.securesms

import android.content.Intent
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import network.qki.messenger.R

/**
 * Created by Yaakov on
 * Describe:
 */
abstract class BaseFragment<VM : ViewModel>(@LayoutRes layoutID: Int) : Fragment(layoutID) {
    protected abstract val viewModel: VM


    private val defaultSessionRequestCode: Int
        get() = 42

    fun Fragment.push(intent: Intent, isForResult: Boolean = false) {
        if (isForResult) {
            startActivityForResult(intent, defaultSessionRequestCode)
        } else {
            startActivity(intent)
        }
        activity?.overridePendingTransition(R.anim.slide_from_right, R.anim.fade_scale_out)
    }

    fun Fragment.show(intent: Intent, isForResult: Boolean = false) {
        if (isForResult) {
            startActivityForResult(intent, defaultSessionRequestCode)
        } else {
            startActivity(intent)
        }
        activity?.overridePendingTransition(R.anim.slide_from_bottom, R.anim.fade_scale_out)
    }

    fun stopRefreshing(swipeRefreshLayout: SwipeRefreshLayout) {
        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
        }
    }

}