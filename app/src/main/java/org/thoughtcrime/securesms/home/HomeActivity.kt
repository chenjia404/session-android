package org.thoughtcrime.securesms.home

import android.content.BroadcastReceiver
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.azhon.appupdate.manager.DownloadManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.BuildConfig
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityHomeBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.database.GroupDatabase
import org.thoughtcrime.securesms.database.MmsSmsDatabase
import org.thoughtcrime.securesms.database.RecipientDatabase
import org.thoughtcrime.securesms.database.ThreadDatabase
import org.thoughtcrime.securesms.util.toastOnUi
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityHomeBinding
    private var broadcastReceiver: BroadcastReceiver? = null

    @Inject
    lateinit var threadDb: ThreadDatabase

    @Inject
    lateinit var mmsSmsDatabase: MmsSmsDatabase

    @Inject
    lateinit var recipientDatabase: RecipientDatabase

    @Inject
    lateinit var groupDatabase: GroupDatabase

    @Inject
    lateinit var textSecurePreferences: TextSecurePreferences


    private var navigationTitleList = arrayOf(
        R.string.activity_settings_chats_button_title,
        R.string.menu_dao,
        R.string.activity_settings_title
    )

    var navigationIconList = arrayOf(
        R.drawable.ic_chat,
        R.drawable.ic_dao,
        R.drawable.ic_setting
    )

    private var lastPressTime: Long = 0

    private val viewPagerAdapter: ManagerViewPagerAdapter by lazy {
        ManagerViewPagerAdapter(this)
    }

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?, isReady: Boolean) {
        super.onCreate(savedInstanceState, isReady)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewpager.adapter = viewPagerAdapter
        binding.viewpager.isUserInputEnabled = false
        intTabLayout()
    }

    override fun onBackPressed() {
        val now = System.currentTimeMillis()
        if (lastPressTime == 0L || now - lastPressTime > 2 * 1000) {
            toastOnUi(getString(R.string.exit_again))
            lastPressTime = now
        } else if (now - lastPressTime < 2 * 1000) super.onBackPressed()
    }


    private fun checkUpdate() {
        val client = OkHttpClient.Builder()
            .connectTimeout(30000, TimeUnit.MILLISECONDS)
            .readTimeout(35000, TimeUnit.MILLISECONDS) // 设置连接时间和读取时间
            .build() // 设置缓存
        val doRequestUrl = BuildConfig.updateServer + "update.json"
        val request = Request.Builder().url(doRequestUrl).get().build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //失败回调 回调是在子线程中，可使用Handler、post、activity.runOnUiThread()等方式在主线程中更新ui
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                //成功回调  可使用Handler、post、activity.runOnUiThread()等方式在主线程中更新ui
                //获取返回byte数组
                if (!response.isSuccessful) {
                    throw IOException("Bad response: " + response.message)
                }
                val resultData = response.body!!.string()
                try {
                    val jsonObject = JSONObject(resultData)
                    android.util.Log.d(
                        "checkUpdate",
                        "onResponse: " + jsonObject.getString("versionCode")
                    )
                    update(jsonObject)
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
            }
        })
    }

    private fun update(jsonObject: JSONObject) {
        val manager = DownloadManager.Builder(this).run {
            apkUrl(jsonObject.getString("url"))
            apkName(jsonObject.getString("versionName") + ".apk")
            smallIcon(R.mipmap.ic_launcher)
            //设置了此参数，那么内部会自动判断是否需要显示更新对话框，否则需要自己判断是否需要更新
            apkVersionCode(jsonObject.getInt("versionCode"))
            //同时下面三个参数也必须要设置
            apkVersionName(jsonObject.getString("versionName"))
            apkSize(jsonObject.getString("apkSize"))
            apkDescription(jsonObject.getString("Description"))
            //省略一些非必须参数...
            build()
        }
        manager?.download()
    }

    private fun intTabLayout() {
        binding.viewpager.adapter = viewPagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, position ->
            tab.text = getString(navigationTitleList[position])
        }.attach()
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            // 页面被选中
            override fun onTabSelected(tab: TabLayout.Tab) {
                val tvTitle = tab.customView?.findViewById<TextView>(R.id.tvTitle)
                val ivIcon = tab?.customView?.findViewById<ImageView>(R.id.ivIcon)
                tvTitle?.visibility = View.VISIBLE
                tvTitle?.setTextColor(getColorFromAttr(R.attr.mainColor))
                ivIcon?.imageTintList =
                    ColorStateList.valueOf(getColorFromAttr(R.attr.mainColor))
            }

            // 页面切换到其他
            override fun onTabUnselected(tab: TabLayout.Tab) {
                val tvTitle = tab.customView?.findViewById<TextView>(R.id.tvTitle)
                val ivIcon = tab?.customView?.findViewById<ImageView>(R.id.ivIcon)
                tvTitle?.visibility = View.GONE
                tvTitle?.setTextColor(getColorFromAttr(R.attr.reverseMainColor))
                ivIcon?.imageTintList =
                    ColorStateList.valueOf(getColorFromAttr(R.attr.reverseMainColor))
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        for (i in navigationTitleList.indices) {
            val tab = binding.tabLayout.getTabAt(i)
            tab?.setCustomView(R.layout.item_home_tab)
            val tvTitle = tab?.customView?.findViewById<TextView>(R.id.tvTitle)
            val ivIcon = tab?.customView?.findViewById<ImageView>(R.id.ivIcon)
            tvTitle?.text = getString(navigationTitleList[i])
            ivIcon?.setImageResource(navigationIconList[i])
            if (i == 0) {
                tvTitle?.visibility = View.VISIBLE
                tvTitle?.setTextColor(getColorFromAttr(R.attr.mainColor))
                ivIcon?.imageTintList =
                    ColorStateList.valueOf(getColorFromAttr(R.attr.mainColor))
            }
        }
        binding.tabLayout.getTabAt(0)?.select()
    }


}
