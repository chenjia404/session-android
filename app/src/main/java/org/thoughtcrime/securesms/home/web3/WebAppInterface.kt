package org.thoughtcrime.securesms.home.web3

import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONObject
import org.thoughtcrime.securesms.et.Wallet
import org.thoughtcrime.securesms.home.DaoViewModel
import org.thoughtcrime.securesms.util.Logger
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric

class WebAppInterface(
    private val wallet: Wallet,
    private val webView: WebView,
    private val viewModel: DaoViewModel
) {

    @JavascriptInterface
    fun postMessage(json: String): String {
        Logger.d(json)
        val obj = JSONObject(json)
        val id = obj.getLong("id")
        val method = DAppMethod.fromValue(obj.getString("name"))
        val network = obj.getString("network")
        Logger.d(method.name)
        when (method) {
            DAppMethod.REQUESTACCOUNTS -> {
                webView?.post {
                    Logger.d("address = ${wallet.address}")
                    viewModel.setAddress(webView, wallet.address)
                    viewModel.sendAddress(webView, id, wallet.address)
                }
            }

            DAppMethod.SIGNMESSAGE -> {
                val data = extractMessage(obj)
                if (network == "ethereum")
                    handleSignMessage(id, data, addPrefix = false)
            }

            DAppMethod.SIGNPERSONALMESSAGE -> {
                val data = extractMessage(obj)
                handleSignMessage(id, data, addPrefix = true)
            }

            DAppMethod.SIGNTYPEDMESSAGE -> {
                val data = extractMessage(obj)
                val raw = extractRaw(obj)
            }

            DAppMethod.SIGNTRANSACTION -> {

            }

            else -> {
                Logger.e("$method not implemented")
            }
        }
        return ""
    }

    private fun extractMessage(json: JSONObject): ByteArray {
        val param = json.getJSONObject("object")
        val data = param.getString("data")
        return Numeric.hexStringToByteArray(data)
    }

    private fun extractRaw(json: JSONObject): String {
        val param = json.getJSONObject("object")
        return param.getString("raw")
    }

    private fun handleSignMessage(id: Long, data: ByteArray, addPrefix: Boolean) {
        webView.sendResult(
            "ethereum",
            signEthereumMessage(wallet, data, addPrefix),
            id
        )
    }

    private fun signEthereumMessage(wallet: Wallet, message: ByteArray, addPrefix: Boolean): String {
        var data = message
        if (addPrefix) {
            val messagePrefix = "\u0019Ethereum Signed Message:\n"
            val prefix = (messagePrefix + message.size).toByteArray()
            val result = ByteArray(prefix.size + message.size)
            System.arraycopy(prefix, 0, result, 0, prefix.size)
            System.arraycopy(message, 0, result, prefix.size, message.size)
            data = result
        }
        val s = String(data, Charsets.UTF_8)
        val signMessage = Sign.signMessage(
            s.toByteArray(),
            ECKeyPair.create(Numeric.toBigInt(wallet.pk))
        )
        return getSignature(signMessage)
    }

    private fun getSignature(signatureData: Sign.SignatureData): String {
        val sig = ByteArray(65)
        System.arraycopy(signatureData.r, 0, sig, 0, 32)
        System.arraycopy(signatureData.s, 0, sig, 32, 32)
        System.arraycopy(signatureData.v, 0, sig, 64, signatureData.v.size)
        return Numeric.toHexString(sig)
    }


}
