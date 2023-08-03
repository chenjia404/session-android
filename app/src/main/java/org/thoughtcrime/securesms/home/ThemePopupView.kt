package org.thoughtcrime.securesms.home

import android.content.Context
import com.lxj.xpopup.core.BottomPopupView
import network.qki.messenger.R
import network.qki.messenger.databinding.LayoutThemePopupBinding

/**
 * Created by Yaakov on
 * Describe:
 */
class ThemePopupView(
    context: Context,
    private val  position: Int,
    private val onSelect: (position: Int) -> Unit,
    private val onSystem: () -> Unit
) :
    BottomPopupView(context) {

    lateinit var binding: LayoutThemePopupBinding

    override fun getImplLayoutId(): Int {
        return R.layout.layout_theme_popup
    }

    override fun onCreate() {
        super.onCreate()
        binding = LayoutThemePopupBinding.bind(popupImplView)
        with(binding) {
            when(position) {
                0-> tvLight.setTextColor(R.color.color3E66FB)
                1-> tvDark.setTextColor(R.color.color3E66FB)
                2-> tvSystem.setTextColor(R.color.color3E66FB)
            }
            tvLight.setOnClickListener {
                onSelect.invoke(0)
                dismiss()
            }
            tvDark.setOnClickListener {
                onSelect.invoke(1)
                dismiss()
            }
            tvSystem.setOnClickListener {
                onSystem.invoke()
                dismiss()
            }
        }

    }
}