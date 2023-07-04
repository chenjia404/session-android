package org.thoughtcrime.securesms.preferences

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import network.qki.messenger.R
import network.qki.messenger.databinding.DialogProxyBinding
import org.session.libsession.utilities.TextSecurePreferences
import org.thoughtcrime.securesms.conversation.v2.utilities.BaseDialog

class ProxyDialog(
    private val type: Int,
    private val onNext: (data: String) -> Unit,
    private val onCancel: () -> Unit
) :
    BaseDialog() {

    override fun setContentView(builder: AlertDialog.Builder) {
        val binding = DialogProxyBinding.inflate(LayoutInflater.from(requireContext()))
        binding.tvTitle.text = if (type == 0) "Https Proxy" else "Socks5 Proxy"
        binding.tvSubTitle.text =
            if (type == 0) requireContext().getString(R.string.https_proxy_desc) else requireContext().getString(
                R.string.socks5_proxy_desc
            )
        binding.etContent.hint =
            if (type == 0) requireContext().getString(R.string.please_enter_content) else "127.0.0.1:1080"
        val proxy =
            if (type == 0) TextSecurePreferences.getHttpsProxy(requireContext()) else TextSecurePreferences.getSocks5Proxy(
                requireContext()
            )
        binding.etContent.setText(proxy)
        binding.tvCancel.setOnClickListener {
            onCancel.invoke()
            dismiss()
        }
        binding.tvOk.setOnClickListener {
            val data = binding.etContent.text.toString()
            onNext.invoke(data)
            dismiss()
        }
        isCancelable = false
        builder.setView(binding.root)
    }
}