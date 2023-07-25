package org.thoughtcrime.securesms.home.web3

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.ConsoleMessage
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.just.agentweb.AgentWeb
import com.just.agentweb.DefaultWebClient
import com.just.agentweb.WebChromeClient
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.databinding.ActivityDappWebBinding
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.home.DaoViewModel
import org.thoughtcrime.securesms.util.Logger


@AndroidEntryPoint
class DAppWebActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityDappWebBinding

    private val viewModel by viewModels<DaoViewModel>()

    private lateinit var agentWeb: AgentWeb

    companion object {

        // Request codes
        const val FINISH = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityDappWebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.statusBarColor = getColor(network.qki.messenger.R.color.color623BB3)
        initView()
    }

    override fun onPause() {
        agentWeb.webLifeCycle.onPause()
        super.onPause()
    }

    override fun onResume() {
        agentWeb.webLifeCycle.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        agentWeb.webLifeCycle.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (agentWeb.handleKeyEvent(keyCode, event)) {
            true
        } else super.onKeyDown(keyCode, event)
    }

    override fun finish() {
        super.finish()
        val intent = Intent()
        setResult(RESULT_OK, intent)
        //overridePendingTransition(0, 0)
    }

    private fun initView() {
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar ?: return
        actionBar.title = ""
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        val upArrow = ContextCompat.getDrawable(this, network.qki.messenger.R.drawable.abc_ic_ab_back_material)
        if (upArrow != null) {
            upArrow.setColorFilter(ContextCompat.getColor(this, network.qki.messenger.R.color.white), PorterDuff.Mode.SRC_ATOP)
            if (supportActionBar != null) {
                supportActionBar!!.setHomeAsUpIndicator(upArrow)
            }
        }

        val providerJs = viewModel.loadProviderJs()
        val initJs = viewModel.loadInitJs(1, "https://eth.llamarpc.com")
        val commonBuilder = AgentWeb.with(this)
            .setAgentWebParent(binding.webContainer, LinearLayout.LayoutParams(-1, -1))
            .useDefaultIndicator(getColorFromAttr(network.qki.messenger.R.attr.mainColor), 0)
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
                    binding.tvName.text = title
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
                viewModel.wallet,
                agentWeb.webCreator.webView,
                viewModel
            )
        )
        agentWeb.urlLoader.loadUrl("https://app.ethtweet.io/")
    }
}
