package org.thoughtcrime.securesms.home

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityUserEtEditBinding
import org.greenrobot.eventbus.EventBus
import org.session.libsession.utilities.SSKEnvironment
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.et.ETViewModel
import org.thoughtcrime.securesms.et.User
import org.thoughtcrime.securesms.et.UserUpdateEvent
import org.thoughtcrime.securesms.mediasend.Media
import org.thoughtcrime.securesms.mediasend.MediaSelectActivity
import org.thoughtcrime.securesms.mediasend.MediaSendActivity
import org.thoughtcrime.securesms.permissions.Permissions
import org.thoughtcrime.securesms.util.GlideHelper
import org.thoughtcrime.securesms.util.StatusBarUtil
import org.thoughtcrime.securesms.util.parcelable
import org.thoughtcrime.securesms.util.toastOnUi


/**
 * Created by Yaakov on
 * Describe:
 */
class ETEditUserActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityUserEtEditBinding
    private val viewModel by viewModels<ETViewModel>()

    var user: User? = null
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityUserEtEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtil.setStatusColor(this, false, TextSecurePreferences.CLASSIC_DARK != TextSecurePreferences.getThemeStyle(this), getColorFromAttr(R.attr.chatsToolbarColor))
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.apply {
                val medias = getParcelableArrayListExtra<Media>(MediaSendActivity.EXTRA_MEDIA)
                if (!medias.isNullOrEmpty()) {
                    viewModel.uploadFile({}, {}, medias[0])
                }
            }
        }
        user = intent.parcelable(ETUserCenterActivity.KEY_USER)
        user?.let {

        } ?: finish()
    }

    override fun initViews() {
        binding.tvAppName.text = getString(R.string.edit)
        binding.ivAvatarUpload.setOnClickListener {
            Permissions.with(this@ETEditUserActivity)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withPermanentDenialDialog(getString(R.string.AttachmentManager_signal_requires_the_external_storage_permission_in_order_to_attach_photos_videos_or_audio))
                .withRationaleDialog(getString(R.string.ConversationActivity_to_send_photos_and_video_allow_signal_access_to_storage), R.drawable.ic_baseline_photo_library_24)
                .onAllGranted {
                    var intent = Intent(this@ETEditUserActivity, MediaSelectActivity::class.java)
                    resultLauncher.launch(intent)
                }
                .execute()
        }
        binding.tvSave.setOnClickListener {
            val nickname = binding.etContent.text.toString().trim()
            if (nickname.toByteArray().size > SSKEnvironment.ProfileManagerProtocol.Companion.NAME_PADDED_LENGTH) {
                toastOnUi(R.string.activity_settings_display_name_too_long_error)
                return@setOnClickListener
            }
            viewModel.updateUser({ showLoading() }, { hideLoading() }, user?.Avatar ?: "", nickname, user?.Desc ?: "", user?.Sex ?: "", (System.currentTimeMillis() / 1000).toString())
        }
        updateUI(user)
    }

    override fun initObserver() {
        viewModel.ipfsLiveData.observe(this) {
            user?.Avatar = it?.url
            updateUI(user)
        }
        viewModel.updateUserStatusLiveData.observe(this) {
            if (it) {
                EventBus.getDefault().post(UserUpdateEvent(user))
                finish()
            }
        }
    }

    private fun updateUI(user: User?) {
        with(binding) {
            user?.let {
                GlideHelper.showImage(
                    ivAvatar, user.Avatar ?: "", 100, R.drawable.ic_pic_default_round, R.drawable.ic_pic_default_round
                )
                etContent.setText(user.Nickname.toString())
            }

        }
    }

}