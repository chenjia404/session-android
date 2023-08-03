package org.thoughtcrime.securesms.home

import android.content.Context
import com.hjq.language.MultiLanguages
import com.lxj.xpopup.core.BottomPopupView
import network.qki.messenger.R
import network.qki.messenger.databinding.LayoutLanguagePopupBinding
import java.util.Locale

/**
 * Created by Yaakov on
 * Describe:
 */
class LanguagePopupView(
    context: Context,
    private val onSelect: (position: Int) -> Unit
) :
    BottomPopupView(context) {

    lateinit var binding: LayoutLanguagePopupBinding

    override fun getImplLayoutId(): Int {
        return R.layout.layout_language_popup
    }

    override fun onCreate() {
        super.onCreate()
        binding = LayoutLanguagePopupBinding.bind(popupImplView)
        with(binding) {
            if (MultiLanguages.isSystemLanguage()) {
                tvSimple.setTextColor(R.color.color3E66FB)
            } else {
                when (MultiLanguages.getAppLanguage()) {
                    Locale.CHINA -> tvSimple.setTextColor(R.color.color3E66FB)
                    Locale.TAIWAN -> tvTraditional.setTextColor(R.color.color3E66FB)
                    Locale.ENGLISH -> tvEnglish.setTextColor(R.color.color3E66FB)
                }
            }
            tvSimple.setOnClickListener {
                var start = System.currentTimeMillis()
                onSelect.invoke(0)
                dismiss()

            }
            tvTraditional.setOnClickListener {
                onSelect.invoke(1)
                dismiss()
            }
            tvEnglish.setOnClickListener {
                onSelect.invoke(2)
                dismiss()
            }
            tvSystem.setOnClickListener {
                onSelect.invoke(3)
                dismiss()
            }
        }

    }
}