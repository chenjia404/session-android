package org.thoughtcrime.securesms.et

import android.content.Context
import com.lxj.xpopup.core.BottomPopupView
import network.qki.messenger.R
import network.qki.messenger.databinding.LayoutSendCommentBinding

/**
 * Created by Yaakov on
 * Describe:
 */
class ETCommentPopupView(
    context: Context,
    private val et: ET,
    private val onSend: (et: ET, string: String) -> Unit
) :
    BottomPopupView(context) {

    lateinit var binding: LayoutSendCommentBinding

    override fun getImplLayoutId(): Int {
        return R.layout.layout_send_comment
    }

    override fun onCreate() {
        super.onCreate()
        binding = LayoutSendCommentBinding.bind(popupImplView)
        binding.apply {
            etContent.hint = "Replay:${et.UserInfo?.Nickname}"
            tvSend.setOnClickListener {
                val content = etContent.text.toString().trim()
                content?.let {
                    onSend.invoke(et, it)
                    dismiss()
                }
            }
        }
    }
}