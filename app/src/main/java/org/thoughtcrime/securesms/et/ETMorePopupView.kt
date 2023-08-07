package org.thoughtcrime.securesms.et

import android.content.Context
import com.lxj.xpopup.core.AttachPopupView
import network.qki.messenger.R
import network.qki.messenger.databinding.LayoutPopupMoreBinding

/**
 * Created by Yaakov on
 * Describe:
 */
class ETMorePopupView(
    context: Context, private val onSelect: (position: Int) -> Unit
) : AttachPopupView(context) {

    lateinit var binding: LayoutPopupMoreBinding

    override fun getImplLayoutId(): Int {
        return R.layout.layout_popup_more
    }

    override fun onCreate() {
        super.onCreate()
        binding = LayoutPopupMoreBinding.bind(popupImplView)
        binding.root.setOnClickListener {
            onSelect.invoke(0)
            dismiss()
        }
    }
}