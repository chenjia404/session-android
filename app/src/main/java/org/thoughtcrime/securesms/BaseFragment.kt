package org.thoughtcrime.securesms

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel

/**
 * Created by Yaakov on
 * Describe:
 */
abstract class BaseFragment<VM : ViewModel>(@LayoutRes layoutID: Int): Fragment(layoutID) {
    protected abstract val viewModel: VM
}