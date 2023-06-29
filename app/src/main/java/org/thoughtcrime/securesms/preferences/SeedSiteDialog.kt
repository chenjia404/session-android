package org.thoughtcrime.securesms.preferences

import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import network.qki.messenger.R
import network.qki.messenger.databinding.DialogSeedSiteBinding
import org.thoughtcrime.securesms.conversation.v2.utilities.BaseDialog
import org.thoughtcrime.securesms.util.isValidURL

class SeedSiteDialog(private val onNext: (data: String) -> Unit) : BaseDialog() {

    override fun setContentView(builder: AlertDialog.Builder) {
        val binding = DialogSeedSiteBinding.inflate(LayoutInflater.from(requireContext()))
        binding.tvCancel.setOnClickListener { dismiss() }
        binding.tvOk.setOnClickListener {
            val data = binding.etContent.text.toString()
            if (TextUtils.isEmpty(data)) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.content_not_empty),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (data.isValidURL()) {
                    onNext.invoke(data)
                    dismiss()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enter_the_correct_url),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
        builder.setView(binding.root)
    }
}