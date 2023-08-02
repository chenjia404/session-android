package org.thoughtcrime.securesms.et

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.google.android.flexbox.FlexboxLayout
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.util.SmartGlideImageLoader
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityEtPublishBinding
import network.qki.messenger.databinding.ItemEtAttachBinding
import network.qki.messenger.databinding.ItemPublishAttachBinding
import org.greenrobot.eventbus.EventBus
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.constants.AppConst
import org.thoughtcrime.securesms.net.network.IpfsResponse
import org.thoughtcrime.securesms.permissions.Permissions
import org.thoughtcrime.securesms.util.GlideHelper
import org.thoughtcrime.securesms.util.Logger
import org.thoughtcrime.securesms.util.dateDifferenceDesc
import org.thoughtcrime.securesms.util.formatMediaUrl
import org.thoughtcrime.securesms.util.formatMedias
import org.thoughtcrime.securesms.util.parcelable
import java.util.Date

@AndroidEntryPoint
class ETPublishActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityEtPublishBinding

    private val viewModel by viewModels<ETPublishViewModel>()

    var et: ET? = null
    private var attaches = arrayListOf<String>()

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityEtPublishBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        window?.statusBarColor = getColorFromAttr(R.attr.chatsToolbarColor)
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            val clipData = data?.clipData
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    viewModel.uploadFile({}, {}, uri)
                }
            } else {
                data?.data?.let {
                    viewModel.uploadFile({}, {}, it)
                }
            }

        }
        et = intent.parcelable(ETFragment.KEY_ET)
    }


    override fun initViews() {
        super.initViews()
        initForwardLayout()
        binding.apply {
            etContent.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    val data = s.toString().trim()
                    tvCount.text = "${data.length}/10000"
                }

            })
            etContent.setOnTouchListener { v, event ->
                if (event?.action == MotionEvent.ACTION_DOWN) {
                    v?.parent?.requestDisallowInterceptTouchEvent(true)
                }
                if (event?.action == MotionEvent.ACTION_MOVE) {
                    v?.parent?.requestDisallowInterceptTouchEvent(true)
                }
                if (event?.action == MotionEvent.ACTION_UP) {
                    v?.parent?.requestDisallowInterceptTouchEvent(false)
                }
                false
            }
            tvCancel.setOnClickListener {
                finish()
            }
            tvPublish.setOnClickListener {
                var data = etContent.text.toString().trim()
                viewModel.publish({
                    showLoading()
                }, {
                    hideLoading()
                }, data, attaches.joinToString(","), et?.TwAddress ?: "")
                Logger.d("attaches = ${attaches.joinToString(",")}")
            }
            ivUpload.setOnClickListener {
                Permissions.with(this@ETPublishActivity).request(Manifest.permission.CAMERA).onAnyResult {
                    var intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*,video/*"
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    resultLauncher.launch(intent)
                }.execute()
            }
        }
    }

    override fun initData() {

    }

    override fun initObserver() {
        viewModel.publishStatusLiveData.observe(this) {
            if (it) {
                EventBus.getDefault().post(RefreshEvent(et))
                finish()
            }
        }
        viewModel.ipfsLiveData.observe(this) {
            if (it != null && !it.Hash.isNullOrBlank()) {
                updateFlowLayout(it)
            }
        }
    }

    private fun initForwardLayout() {
        if (et != null) {
            binding.layoutForward.rootForward.isVisible = true
            binding.layoutForward.tvUserName.text = et?.UserInfo?.Nickname
            binding.layoutForward.tvContent.text = et?.Content
            binding.layoutForward.tvTime.text = "${Date(et?.CreatedAt?.toLong()?.times(1000) ?: System.currentTimeMillis()).dateDifferenceDesc()}"
            GlideHelper.showImage(
                binding.layoutForward.ivAvatar,
                et?.UserInfo?.Avatar ?: "",
                100,
                R.drawable.ic_pic_default_round,
                R.drawable.ic_pic_default_round
            )
            binding.layoutForward.flexbox.removeAllViews()
            et?.Attachment?.trim()?.let { it ->
                val medias = it.formatMedias()
                val urls = it.formatMediaUrl()
                if (!medias.isNullOrEmpty()) {
                    for (i in medias.indices) {
                        val media = medias[i]
                        val attachBinding = ItemEtAttachBinding.inflate(LayoutInflater.from(this), binding.layoutForward.root, false)
                        attachBinding.ivAttach.setOnClickListener {
                            showGallery(attachBinding.ivAttach, i, urls)
                        }
                        binding.layoutForward.flexbox.addView(attachBinding.root)
                        val lp = attachBinding.root.layoutParams as FlexboxLayout.LayoutParams
                        lp.flexBasisPercent = 0.3f
                        GlideHelper.showImage(
                            attachBinding.ivAttach,
                            media.url,
                            8,
                            R.drawable.ic_pic_default,
                            R.drawable.ic_pic_default
                        )
                        if (i >= 8 && medias.size > 9) {
                            attachBinding.ivAttach.foreground = getDrawable(R.drawable.shape_pic_foreground)
                            attachBinding.tvNum.isVisible = true
                            attachBinding.tvNum.text = "+${medias.size - 9}"
                            break
                        } else {
                            attachBinding.ivAttach.foreground = null
                            attachBinding.tvNum.isVisible = false
                        }
                    }
                }
            }
        } else {
            binding.layoutForward.rootForward.isVisible = false
        }
    }

    private fun updateFlowLayout(ipfs: IpfsResponse) {
        val attachBinding = ItemPublishAttachBinding.inflate(LayoutInflater.from(this), binding.flexbox, false)
        binding.flexbox.addView(attachBinding.root)
        val lp = attachBinding.root.layoutParams as FlexboxLayout.LayoutParams
        lp.flexBasisPercent = 0.3f
        GlideHelper.showImage(
            attachBinding.ivAttach,
            "${AppConst.URLS.IPFS_SCAN}/${ipfs.Hash}",
            8,
            R.drawable.ic_pic_default,
            R.drawable.ic_pic_default
        )
        attaches.add("${AppConst.URLS.IPFS_SCAN}/${ipfs.Hash}?filename=${ipfs.Name}")
        attachBinding.ivDel.setOnClickListener {
            binding.flexbox.removeView(attachBinding.root)
            attaches.remove("${AppConst.URLS.IPFS_SCAN}/${ipfs.Hash}?filename=${ipfs.Name}")
        }
    }

    private fun showGallery(imageView: ImageView, position: Int, urls: List<String>) {
        XPopup.Builder(this)
            .isTouchThrough(true)
            .asImageViewer(imageView, position, urls, false, true, -1, -1, 0, false, Color.rgb(32, 36, 46), { popupView, i ->

            }, SmartGlideImageLoader(), null)
            .show()

    }
}
