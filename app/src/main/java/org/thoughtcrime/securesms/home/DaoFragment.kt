package org.thoughtcrime.securesms.home

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import com.just.agentweb.AgentWeb
import com.just.agentweb.DefaultWebClient
import com.just.agentweb.WebChromeClient
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.FragmentDaoBinding
import org.session.libsession.utilities.getColorFromAttr
import org.session.libsignal.crypto.MnemonicCodec
import org.session.libsignal.utilities.hexEncodedPrivateKey
import org.thoughtcrime.securesms.BaseFragment
import org.thoughtcrime.securesms.crypto.IdentityKeyUtil
import org.thoughtcrime.securesms.crypto.MnemonicUtilities
import org.thoughtcrime.securesms.home.web3.WebAppInterface
import org.thoughtcrime.securesms.util.Logger
import org.thoughtcrime.securesms.util.viewbindingdelegate.viewBinding

/**
 * Created by Yaakov on
 * Describe:
 */
@AndroidEntryPoint
class DaoFragment : BaseFragment<DaoViewModel>(R.layout.fragment_dao) {

    private val binding by viewBinding(FragmentDaoBinding::bind)
    override val viewModel by viewModels<DaoViewModel>()

    private lateinit var agentWeb: AgentWeb

    private val mnemonic by lazy {
        var hexEncodedSeed = IdentityKeyUtil.retrieve(requireContext(), IdentityKeyUtil.LOKI_SEED)
        if (hexEncodedSeed == null) {
            hexEncodedSeed = IdentityKeyUtil.getIdentityKeyPair(requireContext()).hexEncodedPrivateKey // Legacy account
        }
        val loadFileContents: (String) -> String = { fileName ->
            MnemonicUtilities.loadFileContents(requireContext(), fileName)
        }
        MnemonicCodec(loadFileContents).encode(hexEncodedSeed!!, MnemonicCodec.Language.Configuration.english)
    }

    // region Lifecycle
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // initView()

    }

//    override fun onPause() {
//        agentWeb.webLifeCycle.onPause()
//        super.onPause()
//    }
//
//    override fun onResume() {
//        agentWeb.webLifeCycle.onResume()
//        super.onResume()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        agentWeb.webLifeCycle.onDestroy()
//    }

    private fun initView() {
        val providerJs = viewModel.loadProviderJs()
        val initJs = viewModel.loadInitJs(1, "https://eth.llamarpc.com")
        val commonBuilder = AgentWeb.with(this)
            .setAgentWebParent(binding.webContainer, LinearLayout.LayoutParams(-1, -1))
            .useDefaultIndicator(requireContext().getColorFromAttr(R.attr.mainColor), 0)
            .interceptUnkownUrl()
            //.setMainFrameErrorView(R.layout.layout_statelayout_empty, R.id.emptyImageView)
            .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
            .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)
            .setWebChromeClient(object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                    //Logger.d("onConsoleMessage = " + consoleMessage.message())
                    return super.onConsoleMessage(consoleMessage)
                }

                override fun onReceivedTitle(view: WebView, title: String) {
                    super.onReceivedTitle(view, title)
                }

            }).setWebViewClient(object : com.just.agentweb.WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    view?.evaluateJavascript(providerJs, null)
                    view?.evaluateJavascript(initJs, null)
                    viewModel.requestAccount(agentWeb.webCreator.webView)
                }

                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler?,
                    error: SslError?
                ) {
                    // Ignore SSL certificate errors
                    handler?.proceed()
                    Logger.e(error.toString())
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    val url = request.url.toString()
                    val prefixCheck = url.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    if (prefixCheck.size > 1) {
                        val intent: Intent
                        when (prefixCheck[0]) {
                            "tel" -> {
                                intent = Intent(Intent.ACTION_DIAL)
                                intent.data = Uri.parse(url)
                                startActivity(
                                    Intent.createChooser(
                                        intent,
                                        "Call " + prefixCheck[1]
                                    )
                                )
                                return true
                            }

                            "mailto" -> {
                                intent = Intent(Intent.ACTION_SENDTO)
                                intent.data = Uri.parse(url)
                                startActivity(
                                    Intent.createChooser(
                                        intent,
                                        "Email: " + prefixCheck[1]
                                    )
                                )
                                return true
                            }

                            else -> {}
                        }
                    }
                    return false
                }
            })
        agentWeb = commonBuilder.createAgentWeb().ready().go(null)
        agentWeb.jsInterfaceHolder.addJavaObject(
            "_tw_", WebAppInterface(
                MnemonicCodec.toWallet(mnemonic),
                agentWeb.webCreator.webView,
                viewModel
            )
        )
        agentWeb.urlLoader.loadUrl("https://app.ethtweet.io/")
    }

}