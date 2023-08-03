package org.thoughtcrime.securesms

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
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
    private var loadingWindow: PopupWindow? = null

    private val defaultSessionRequestCode: Int
        get() = 42

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLoading()
    }

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

    open fun initLoading() {
        val loadingView = layoutInflater.inflate(R.layout.layout_circle_progress, null)
        loadingWindow = PopupWindow(loadingView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        loadingWindow?.isFocusable = true
        loadingWindow?.isClippingEnabled = false
        loadingWindow?.setBackgroundDrawable(ColorDrawable())
    }

    open fun showLoading() {
        if (loadingWindow != null) {
            val decorView = requireActivity().window.decorView
            if (decorView.windowToken != null) {
                loadingWindow?.showAtLocation(requireActivity().window.decorView, Gravity.CENTER, 0, 0)
            } else {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (decorView.windowToken != null) {
                        loadingWindow?.showAtLocation(requireActivity().window.decorView, Gravity.CENTER, 0, 0)
                    }
                }, 200)
            }
        }
    }

    open fun hideLoading() {
        if (loadingWindow != null) {
            loadingWindow?.dismiss()
        }
    }

}