package org.thoughtcrime.securesms.home

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.lxj.xpopup.impl.FullScreenPopupView
import network.qki.messenger.R
import network.qki.messenger.databinding.LayoutPopupShareBinding
import java.util.Locale


/**
 * Created by Yaakov on
 * Describe:
 */
class SharePopupView(context: Context) : FullScreenPopupView(context) {

    lateinit var binding: LayoutPopupShareBinding

    override fun getImplLayoutId(): Int {
        return R.layout.layout_popup_share
    }

    override fun onCreate() {
        super.onCreate()
        binding = LayoutPopupShareBinding.bind(popupImplView)
        with(binding) {
            var resId = if ("zh".equals(Locale.getDefault().language, true)) {
                R.drawable.ic_share_z
            } else {
                R.drawable.ic_share_e
            }
            ivImg.setImageResource(resId)
            root.setOnClickListener {
                dismiss()
            }
            shareFile(resId)
        }
    }

    private fun shareFile(resId: Int) {
        val uri = Uri.parse(
            ((ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                    + context.resources.getResourcePackageName(resId)) + "/"
                    + context.resources.getResourceTypeName(resId)) + "/"
                    + context.resources.getResourceEntryName(resId)
        )
        val share = Intent(Intent.ACTION_SEND)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        share.putExtra(Intent.EXTRA_STREAM, uri)
        share.type = "image/*"
        share.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(share, context.getString(R.string.share)))
    }
}