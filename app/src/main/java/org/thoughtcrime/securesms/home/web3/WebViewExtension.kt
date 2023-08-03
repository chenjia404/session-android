package org.thoughtcrime.securesms.home.web3

import android.webkit.WebView
import org.web3j.utils.Numeric

fun WebView.sendError(network: String, message: String, methodId: Long) {
    val script = "window.$network.sendError($methodId, \"$message\")"
    this.post {
        this.evaluateJavascript(script) {}
    }
}

fun WebView.sendResult(network: String, message: String, methodId: Long) {
    val script = "window.$network.sendResponse($methodId, \"$message\")"
    this.post {
        this.evaluateJavascript(script) {}
    }
}

fun WebView.sendResults(network: String, messages: List<String>, methodId: Long) {
    val message = messages.joinToString(separator = ",")
    val script = "window.$network.sendResponse($methodId, \"$message\")"
    this.post {
        this.evaluateJavascript(script) {}
    }
}

fun String.toHexByteArray(): ByteArray {
    return Numeric.hexStringToByteArray(this)
}
