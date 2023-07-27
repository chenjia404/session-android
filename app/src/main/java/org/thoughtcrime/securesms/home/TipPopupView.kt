package org.thoughtcrime.securesms.home

import android.content.Context
import com.lxj.xpopup.core.CenterPopupView
import network.qki.messenger.R
import network.qki.messenger.databinding.LayoutTipPopupBinding

/**
 * Created by Yaakov on
 * Describe:
 */
class TipPopupView(
    context: Context,
    private val title: String,
    private val content: String,
    private val btn: String = context.getString(R.string.ok),
    private val sub: String,
    private val onSelect: (position: Int) -> Unit
) :
    CenterPopupView(context) {

    lateinit var binding: LayoutTipPopupBinding

    override fun getImplLayoutId(): Int {
        return R.layout.layout_tip_popup
    }

    override fun onCreate() {
        super.onCreate()
        binding = LayoutTipPopupBinding.bind(popupImplView)
        with(binding) {
            tvTitle.text = title
            tvContent.text = content
            tvOk.text = btn
            tvSub.text = sub
            tvOk.setOnClickListener {
                onSelect.invoke(0)
                dismiss()
            }
            tvSub.setOnClickListener {
                onSelect.invoke(1)
                dismiss()
            }
        }

    }
}