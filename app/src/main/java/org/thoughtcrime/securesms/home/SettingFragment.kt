package org.thoughtcrime.securesms.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.SparseArray
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.FragmentSettingBinding
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.all
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.successUi
import org.session.libsession.avatars.AvatarHelper
import org.session.libsession.avatars.ContactColors
import org.session.libsession.avatars.PlaceholderAvatarPhoto
import org.session.libsession.avatars.ProfileContactPhoto
import org.session.libsession.avatars.ResourceContactPhoto
import org.session.libsession.utilities.Address
import org.session.libsession.utilities.ProfileKeyUtil
import org.session.libsession.utilities.ProfilePictureUtilities
import org.session.libsession.utilities.SSKEnvironment
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.recipients.Recipient
import org.session.libsession.utilities.truncateIdForDisplay
import org.thoughtcrime.securesms.BaseFragment
import org.thoughtcrime.securesms.avatar.AvatarSelection
import org.thoughtcrime.securesms.components.ProfilePictureView
import org.thoughtcrime.securesms.messagerequests.MessageRequestsActivity
import org.thoughtcrime.securesms.mms.GlideApp
import org.thoughtcrime.securesms.mms.GlideRequests
import org.thoughtcrime.securesms.permissions.Permissions
import org.thoughtcrime.securesms.preferences.ChatSettingsActivity
import org.thoughtcrime.securesms.preferences.ClearAllDataDialog
import org.thoughtcrime.securesms.preferences.HelpSettingsActivity
import org.thoughtcrime.securesms.preferences.NotificationSettingsActivity
import org.thoughtcrime.securesms.preferences.PrivacySettingsActivity
import org.thoughtcrime.securesms.preferences.QRCodeActivity
import org.thoughtcrime.securesms.preferences.SeedDialog
import org.thoughtcrime.securesms.preferences.SeedSiteDialog
import org.thoughtcrime.securesms.preferences.appearance.AppearanceSettingsActivity
import org.thoughtcrime.securesms.profiles.ProfileMediaConstraints
import org.thoughtcrime.securesms.util.BitmapDecodingException
import org.thoughtcrime.securesms.util.BitmapUtil
import org.thoughtcrime.securesms.util.ConfigurationMessageUtilities
import org.thoughtcrime.securesms.util.Logger
import org.thoughtcrime.securesms.util.disableClipping
import org.thoughtcrime.securesms.util.sendToClip
import org.thoughtcrime.securesms.util.toastOnUi
import org.thoughtcrime.securesms.util.viewbindingdelegate.viewBinding
import splitties.systemservices.inputMethodManager
import java.io.File
import java.security.SecureRandom
import java.util.Date
import javax.inject.Inject


/**
 * Created by Yaakov on
 * Describe:
 */
@AndroidEntryPoint
class SettingFragment : BaseFragment<SettingViewModel>(R.layout.fragment_setting) {

    private val binding by viewBinding(FragmentSettingBinding::bind)
    override val viewModel by viewModels<SettingViewModel>()

    private var displayNameEditActionMode: ActionMode? = null
        set(value) {
            field = value; handleDisplayNameEditActionModeChanged()
        }
    private lateinit var glide: GlideRequests
    private var tempFile: File? = null

    @Inject
    lateinit var textSecurePreferences: TextSecurePreferences

    private val hexEncodedPublicKey: String
        get() {
            return TextSecurePreferences.getLocalNumber(requireContext())!!
        }

    companion object {
        const val updatedProfileResultCode = 1234
        private const val SCROLL_STATE = "SCROLL_STATE"
    }

    // region Lifecycle
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val displayName = getDisplayName()
        glide = GlideApp.with(this)
        with(binding) {
            rlQR.setOnClickListener {
                showQRCode()
            }
            setupProfilePictureView()
            ivAvatarUpload.setOnClickListener { showEditProfilePictureUI() }
            ivEdit.setOnClickListener {
                activity?.startActionMode(
                    DisplayNameEditActionModeCallback()
                )
            }
            btnGroupNameDisplay.text = displayName
            publicKeyTextView.text = hexEncodedPublicKey
            tvSessionIdCopy.setOnClickListener { copyPublicKey() }
            pathButton.setOnClickListener { showPath() }
            pathContainer.disableClipping()
            privacyButton.setOnClickListener { showPrivacySettings() }
            notificationsButton.setOnClickListener { showNotificationSettings() }
            messageRequestsButton.setOnClickListener { showMessageRequests() }
            chatsButton.setOnClickListener { showChatSettings() }
            appearanceButton.setOnClickListener { showAppearanceSettings() }
            inviteFriendButton.setOnClickListener { sendInvitation() }
            helpButton.setOnClickListener { showHelp() }
            seedButton.setOnClickListener { showSeed() }
            clearAllDataButton.setOnClickListener { clearAllData() }
            tvAddress.text = viewModel.wallet.address
            tvAddressCopy.setOnClickListener {
                requireContext().sendToClip(viewModel.wallet.address)
            }
        }
    }

    private fun getDisplayName(): String =
        TextSecurePreferences.getProfileName(requireContext()) ?: truncateIdForDisplay(
            hexEncodedPublicKey
        )

    private fun setupProfilePictureView() {
//        view.glide = glide
//        view.apply {
//            publicKey = hexEncodedPublicKey
//            displayName = getDisplayName()
//            isLarge = true
//            update()
//        }

        var publicKey = hexEncodedPublicKey
        var displayName = getDisplayName()

        val profilePicturesCache = mutableMapOf<String, String?>()
        val unknownRecipientDrawable = ResourceContactPhoto(R.drawable.ic_profile_default)
            .asDrawable(
                context,
                ContactColors.UNKNOWN_COLOR.toConversationColor(requireContext()),
                false
            )
        val unknownOpenGroupDrawable = ResourceContactPhoto(R.drawable.ic_notification)
            .asDrawable(
                context,
                ContactColors.UNKNOWN_COLOR.toConversationColor(requireContext()),
                false
            )

        if (publicKey.isNotEmpty()) {
            val recipient =
                Recipient.from(requireContext(), Address.fromSerialized(publicKey), false)
            if (profilePicturesCache.containsKey(publicKey) && profilePicturesCache[publicKey] == recipient.profileAvatar) return
            val signalProfilePicture = recipient.contactPhoto
            val avatar = (signalProfilePicture as? ProfileContactPhoto)?.avatarObject

            if (signalProfilePicture != null && avatar != "0" && avatar != "") {
                glide.clear(binding.ivAvatar)
                glide.load(signalProfilePicture)
                    .placeholder(unknownRecipientDrawable)
                    .centerCrop()
                    .error(unknownRecipientDrawable)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .circleCrop()
                    .into(binding.ivAvatar)
            } else if (recipient.isOpenGroupRecipient && recipient.groupAvatarId == null) {
                glide.clear(binding.ivAvatar)
                binding.ivAvatar.setImageDrawable(unknownOpenGroupDrawable)
            } else {
                val placeholder = PlaceholderAvatarPhoto(
                    requireContext(),
                    publicKey,
                    displayName
                )

                glide.clear(binding.ivAvatar)
                glide.load(placeholder)
                    .placeholder(unknownRecipientDrawable)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE).circleCrop().into(binding.ivAvatar)
            }
            profilePicturesCache[publicKey] = recipient.profileAvatar
        } else {
            binding.ivAvatar.setImageDrawable(null)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val scrollBundle = SparseArray<Parcelable>()
        binding.scrollView.saveHierarchyState(scrollBundle)
        outState.putSparseParcelableArray(SCROLL_STATE, scrollBundle)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AvatarSelection.REQUEST_CODE_AVATAR -> {
                Logger.d("REQUEST_CODE_AVATAR")
                if (resultCode != Activity.RESULT_OK) {
                    return
                }
                val outputFile = Uri.fromFile(File(requireContext().cacheDir, "cropped"))
                var inputFile: Uri? = data?.data
                if (inputFile == null && tempFile != null) {
                    inputFile = Uri.fromFile(tempFile)
                }
                AvatarSelection.circularCropImage(
                    requireActivity(),
                    inputFile,
                    outputFile,
                    R.string.CropImageActivity_profile_avatar
                )
            }

            AvatarSelection.REQUEST_CODE_CROP_IMAGE -> {
                Logger.d("REQUEST_CODE_CROP_IMAGE")
                if (resultCode != Activity.RESULT_OK) {
                    return
                }
                AsyncTask.execute {
                    try {
                        val profilePictureToBeUploaded = BitmapUtil.createScaledBytes(
                            requireContext(),
                            AvatarSelection.getResultUri(data),
                            ProfileMediaConstraints()
                        ).bitmap
                        Handler(Looper.getMainLooper()).post {
                            updateProfile(true, profilePictureToBeUploaded)
                        }
                    } catch (e: BitmapDecodingException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Permissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }
    // endregion

    // region Updating
    private fun handleDisplayNameEditActionModeChanged() {
        val isEditingDisplayName = this.displayNameEditActionMode !== null

        binding.btnGroupNameDisplay.visibility =
            if (isEditingDisplayName) View.INVISIBLE else View.VISIBLE
        binding.displayNameEditText.visibility =
            if (isEditingDisplayName) View.VISIBLE else View.INVISIBLE

        if (isEditingDisplayName) {
            binding.displayNameEditText.setText(binding.btnGroupNameDisplay.text)
            binding.displayNameEditText.selectAll()
            binding.displayNameEditText.requestFocus()
            inputMethodManager.showSoftInput(binding.displayNameEditText, 0)
        } else {
            inputMethodManager.hideSoftInputFromWindow(binding.displayNameEditText.windowToken, 0)
        }
    }

    private fun updateProfile(
        isUpdatingProfilePicture: Boolean,
        profilePicture: ByteArray? = null,
        displayName: String? = null
    ) {
        binding.loader.isVisible = true
        val promises = mutableListOf<Promise<*, Exception>>()
        if (displayName != null) {
            TextSecurePreferences.setProfileName(requireContext(), displayName)
        }
        val encodedProfileKey = ProfileKeyUtil.generateEncodedProfileKey(requireContext())
        if (isUpdatingProfilePicture) {
            if (profilePicture != null) {
                promises.add(
                    ProfilePictureUtilities.upload(
                        profilePicture, encodedProfileKey, requireContext()
                    )
                )
            } else {
                TextSecurePreferences.setLastProfilePictureUpload(
                    requireContext(),
                    System.currentTimeMillis()
                )
                TextSecurePreferences.setProfilePictureURL(requireContext(), null)
            }
        }
        val compoundPromise = all(promises)
        compoundPromise.successUi { // Do this on the UI thread so that it happens before the alwaysUi clause below
            if (isUpdatingProfilePicture) {
                AvatarHelper.setAvatar(
                    requireContext(),
                    Address.fromSerialized(TextSecurePreferences.getLocalNumber(requireContext())!!),
                    profilePicture
                )
                TextSecurePreferences.setProfileAvatarId(requireContext(),
                    profilePicture?.let { SecureRandom().nextInt() } ?: 0)
                TextSecurePreferences.setLastProfilePictureUpload(requireContext(), Date().time)
                ProfileKeyUtil.setEncodedProfileKey(requireContext(), encodedProfileKey)
            }
            if (profilePicture != null || displayName != null) {
                ConfigurationMessageUtilities.forceSyncConfigurationNowIfNeeded(requireContext())
            }
        }
        compoundPromise.alwaysUi {
            if (displayName != null) {
                binding.btnGroupNameDisplay.text = displayName
            }
            if (isUpdatingProfilePicture) {
                setupProfilePictureView()
            }
            binding.loader.isVisible = false
        }
    }
    // endregion

    // region Interaction

    /**
     * @return true if the update was successful.
     */
    private fun saveDisplayName(): Boolean {
        val displayName = binding.displayNameEditText.text.toString().trim()
        if (displayName.isEmpty()) {
            toastOnUi(R.string.activity_settings_display_name_missing_error)
            return false
        }
        if (displayName.toByteArray().size > SSKEnvironment.ProfileManagerProtocol.Companion.NAME_PADDED_LENGTH) {
            toastOnUi(R.string.activity_settings_display_name_too_long_error)
            return false
        }
        updateProfile(false, displayName = displayName)
        return true
    }

    private fun showQRCode() {
        val intent = Intent(requireContext(), QRCodeActivity::class.java)
        push(intent)
    }

    private fun showEditProfilePictureUI() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.activity_settings_set_display_picture)
            .setView(R.layout.dialog_change_avatar)
            .setPositiveButton(R.string.activity_settings_upload) { _, _ ->
                startAvatarSelection()
            }.setNegativeButton(R.string.cancel) { _, _ -> }.apply {
                if (TextSecurePreferences.getProfileAvatarId(context) != 0) {
                    setNeutralButton(R.string.activity_settings_remove) { _, _ -> removeAvatar() }
                }
            }.show().apply {
                val profilePic =
                    findViewById<ProfilePictureView>(R.id.profile_picture_view)?.also {
                        setupProfilePictureView()
                    }
                val pictureIcon = findViewById<View>(R.id.ic_pictures)

                val recipient =
                    Recipient.from(context, Address.fromSerialized(hexEncodedPublicKey), false)

                val photoSet =
                    (recipient.contactPhoto as ProfileContactPhoto).avatarObject !in setOf("0", "")

                profilePic?.isVisible = photoSet
                pictureIcon?.isVisible = !photoSet
            }
    }

    private fun removeAvatar() {
        updateProfile(true)
    }

    private fun startAvatarSelection() {
        // Ask for an optional camera permission.
        Permissions.with(this).request(Manifest.permission.CAMERA).onAnyResult {
            tempFile = AvatarSelection.startAvatarSelection(activity, false, true)
        }.execute()
    }

    private fun copyPublicKey() {
        requireContext().sendToClip(hexEncodedPublicKey)
    }

    private fun sharePublicKey() {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_TEXT, hexEncodedPublicKey)
        intent.type = "text/plain"
        val chooser = Intent.createChooser(intent, getString(R.string.share))
        startActivity(chooser)
    }

    private fun showPrivacySettings() {
        val intent = Intent(requireContext(), PrivacySettingsActivity::class.java)
        push(intent)
    }

    private fun showNotificationSettings() {
        val intent = Intent(requireContext(), NotificationSettingsActivity::class.java)
        push(intent)
    }

    private fun showMessageRequests() {
        val intent = Intent(requireContext(), MessageRequestsActivity::class.java)
        push(intent)
    }

    private fun showChatSettings() {
        val intent = Intent(requireContext(), ChatSettingsActivity::class.java)
        push(intent)
    }

    private fun showAppearanceSettings() {
        val intent = Intent(requireContext(), AppearanceSettingsActivity::class.java)
        push(intent)
    }

    private fun sendInvitation() {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        val invitation = getString(R.string.invitation) + "$hexEncodedPublicKey "
        intent.putExtra(Intent.EXTRA_TEXT, invitation)
        intent.type = "text/plain"
        val chooser =
            Intent.createChooser(intent, getString(R.string.activity_settings_invite_button_title))
        startActivity(chooser)
    }

    private fun showHelp() {
        val intent = Intent(requireContext(), HelpSettingsActivity::class.java)
        push(intent)
    }

    private fun showPath() {
        val intent = Intent(requireContext(), PathActivity::class.java)
        show(intent)
    }

    private fun showSeed() {
        SeedDialog().show(childFragmentManager, "Recovery Phrase Dialog")
    }

    private fun clearAllData() {
        ClearAllDataDialog().show(childFragmentManager, "Clear All Data Dialog")
    }

    private fun showSeedWebsite() {
        SeedSiteDialog {
            TextSecurePreferences.setCustomizedNodeSite(requireContext(), it)
            val site = TextSecurePreferences.getCustomizedNodeSite(requireContext())
        }.show(childFragmentManager, "show seed site")
    }

    // endregion

    private inner class DisplayNameEditActionModeCallback : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.title = getString(R.string.activity_settings_display_name_edit_text_hint)
            mode.menuInflater.inflate(R.menu.menu_apply, menu)
            displayNameEditActionMode = mode
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            binding.ivEdit.isVisible = false
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            binding.ivEdit.isVisible = true
            displayNameEditActionMode = null
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.applyButton -> {
                    if (saveDisplayName()) {
                        mode.finish()
                    }
                    return true
                }
            }
            return false;
        }
    }
}