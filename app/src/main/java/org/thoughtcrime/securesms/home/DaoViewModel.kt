package org.thoughtcrime.securesms.home

import android.app.Application
import android.webkit.WebView
import org.thoughtcrime.securesms.BaseViewModel

/**
 * Created by Yaakov on
 * Describe:
 */
class DaoViewModel(application: Application) : BaseViewModel(application) {

    fun loadProviderJs(): String {
        return context.resources.openRawResource(trust.web3jprovider.R.raw.trust_min)
            .bufferedReader()
            .use { it.readText() }
    }

    fun loadInitJs(chainId: Int, rpcUrl: String): String {
        return """
        (function() {
            var config = {                
                ethereum: {
                    chainId: $chainId,
                    rpcUrl: "$rpcUrl"
                },
                solana: {
                    cluster: "mainnet-beta",
                },
                isDebug: true
            };
            trustwallet.ethereum = new trustwallet.Provider(config);
            trustwallet.solana = new trustwallet.SolanaProvider(config);
            trustwallet.postMessage = (json) => {
                window._tw_.postMessage(JSON.stringify(json));
            }
            window.ethereum = trustwallet.ethereum;
        })();
        """
    }

    fun requestAccount(webView: WebView) {
        val script = "window.ethereum.request({method: \"eth_requestAccounts\"});"
        webView.evaluateJavascript(script) {}
    }

    fun setAddress(webView: WebView, address: String) {
        val script = "window.ethereum.setAddress(\"$address\");"
        webView.evaluateJavascript(script) {}
    }

    fun sendAddress(webView: WebView, methodId: Long, address: String) {
        val script = "window.ethereum.sendResponse($methodId, [\"$address\"]);"
        webView.evaluateJavascript(script) {
        }
    }

}