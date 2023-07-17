package org.thoughtcrime.securesms.conversation.v2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityUserBinding
import org.greenrobot.eventbus.EventBus
import org.session.libsession.messaging.messages.control.ExpirationTimerUpdate
import org.session.libsession.messaging.sending_receiving.MessageSender
import org.session.libsession.messaging.sending_receiving.leave
import org.session.libsession.snode.SnodeAPI
import org.session.libsession.utilities.GroupUtil
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.UserEvent
import org.session.libsession.utilities.getColorFromAttr
import org.session.libsession.utilities.recipients.Recipient
import org.session.libsignal.utilities.toHexString
import org.thoughtcrime.securesms.ApplicationContext
import org.thoughtcrime.securesms.ExpirationDialog
import org.thoughtcrime.securesms.MuteDialog
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.conversation.v2.menus.ConversationMenuHelper
import org.thoughtcrime.securesms.conversation.v2.utilities.NotificationUtils
import org.thoughtcrime.securesms.database.GroupDatabase
import org.thoughtcrime.securesms.database.RecipientDatabase
import org.thoughtcrime.securesms.dependencies.DatabaseComponent
import org.thoughtcrime.securesms.groups.EditClosedGroupActivity
import org.thoughtcrime.securesms.mms.GlideApp
import org.thoughtcrime.securesms.util.sendToClip
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class UserActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityUserBinding

    @Inject
    lateinit var groupDb: GroupDatabase

    @Inject
    lateinit var recipientDb: RecipientDatabase

    private val userViewModel by viewModels<UserViewModel>()

    private lateinit var recipient: Recipient

    private val glide by lazy { GlideApp.with(this) }

    companion object {
        // Extras
        const val RECIPIENT_ID = "recipient_id"

    }

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.statusBarColor = getColorFromAttr(R.attr.settingBgColor)
        val threadID = intent.getLongExtra(RECIPIENT_ID, -1)
        recipient = DatabaseComponent.get(this).threadDatabase().getRecipientForThreadId(threadID) ?: return
        initView()

    }


    override fun onDestroy() {
        super.onDestroy()
    }

    private fun initView() {
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar ?: return
        actionBar.title = ""
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        binding.profilePictureView.root.isLarge = true
        binding.profilePictureView.root.glide = glide
        binding.profilePictureView.root.update(recipient)
        binding.tvName.text = when {
            recipient.isLocalNumber -> getString(R.string.note_to_self)
            recipient.isGroupRecipient -> getString(R.string.group_setting)
            else -> recipient.toShortString()
        }
        binding.tvGroupName.text = when {
            recipient.isLocalNumber -> getString(R.string.note_to_self)
            else -> recipient.toShortString()
        }
        if (recipient.isGroupRecipient) {
            binding.tvGroupName.isVisible = true
            binding.llSessionId.isVisible = false
            binding.rlEditGroup.isVisible = true
            binding.rlLeaveGroup.isVisible = true
            binding.rlNotification.isVisible = true
            binding.rlBlock.isVisible = false
        } else {
            binding.tvGroupName.isVisible = false
            binding.llSessionId.isVisible = true
            binding.rlEditGroup.isVisible = false
            binding.rlLeaveGroup.isVisible = false
            binding.rlNotification.isVisible = false
            binding.rlBlock.isVisible = true
            binding.publicKeyTextView.text = TextSecurePreferences.getLocalNumber(this)
            binding.publicKeyTextView.setOnClickListener {
                sendToClip(TextSecurePreferences.getLocalNumber(this))
            }
        }
        if (recipient.isMuted) {
            binding.tvMute.text = getString(R.string.conversation_muted__unmute)
        } else {
            binding.tvMute.text = getString(R.string.mute)
        }
        if (recipient.isContactRecipient) {
            if (recipient.isBlocked) {
                binding.tvBlock.text = getString(R.string.ConversationActivity_unblock)
            } else {
                binding.tvBlock.text = getString(R.string.recipient_preferences__block)
            }
        }
        binding.rlAllMedia.setOnClickListener {
            ConversationMenuHelper.showAllMedia(this, recipient)
        }
        binding.rlSearch.setOnClickListener {
            EventBus.getDefault().post(UserEvent(UserEvent.SEARCH, recipient))
            finish()
        }
        binding.rlExpiration.setOnClickListener {
            showExpiringMessagesDialog(recipient)
        }
        binding.rlMute.setOnClickListener {
            if (recipient.isMuted) {
                unmute(this, recipient)
            } else {
                mute(this, recipient)
            }
        }
        binding.rlBlock.setOnClickListener {
            if (recipient.isBlocked) {
                unblock(recipient)
            } else {
                block(recipient)
            }
        }
        binding.rlEditGroup.setOnClickListener {
            editClosedGroup(this, recipient)
        }
        binding.rlLeaveGroup.setOnClickListener {
            leaveClosedGroup(this, recipient)
        }
        binding.rlNotification.setOnClickListener {
            setNotifyType(this, recipient)
        }
    }

    private fun showExpiringMessagesDialog(thread: Recipient) {
        if (thread.isClosedGroupRecipient) {
            val group = groupDb.getGroup(thread.address.toGroupString()).orNull()
            if (group?.isActive == false) {
                return
            }
        }
        ExpirationDialog.show(this, thread.expireMessages) { expirationTime: Int ->
            recipientDb.setExpireMessages(thread, expirationTime)
            val message = ExpirationTimerUpdate(expirationTime)
            message.recipient = thread.address.serialize()
            message.sentTimestamp = SnodeAPI.nowWithOffset
            val expiringMessageManager = ApplicationContext.getInstance(this).expiringMessageManager
            expiringMessageManager.setExpirationTimer(message)
            MessageSender.send(message, thread.address)
            EventBus.getDefault().post(UserEvent(UserEvent.EXPIRATION, recipient))
            finish()
        }
    }

    private fun unmute(context: Context, thread: Recipient) {
        DatabaseComponent.get(context).recipientDatabase().setMuted(thread, 0)
        finish()
    }

    private fun mute(context: Context, thread: Recipient) {
        MuteDialog.show(ContextThemeWrapper(context, context.theme)) { until: Long ->
            DatabaseComponent.get(context).recipientDatabase().setMuted(thread, until)
            finish()
        }
    }

    fun unblock(thread: Recipient) {
        if (!thread.isContactRecipient) {
            return
        }
        EventBus.getDefault().post(UserEvent(UserEvent.UNBLOCK, recipient))
        finish()
    }

    fun block(thread: Recipient) {
        if (!thread.isContactRecipient) {
            return
        }
        EventBus.getDefault().post(UserEvent(UserEvent.BLOCK, recipient))
        finish()
    }

    private fun editClosedGroup(context: Context, thread: Recipient) {
        if (!thread.isClosedGroupRecipient) {
            return
        }
        val intent = Intent(context, EditClosedGroupActivity::class.java)
        val groupID: String = thread.address.toGroupString()
        intent.putExtra(EditClosedGroupActivity.groupIDKey, groupID)
        context.startActivity(intent)
    }

    private fun leaveClosedGroup(context: Context, thread: Recipient) {
        if (!thread.isClosedGroupRecipient) {
            return
        }
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.resources.getString(R.string.ConversationActivity_leave_group))
        builder.setCancelable(true)
        val group = DatabaseComponent.get(context).groupDatabase().getGroup(thread.address.toGroupString()).orNull()
        val admins = group.admins
        val sessionID = TextSecurePreferences.getLocalNumber(context)
        val isCurrentUserAdmin = admins.any { it.toString() == sessionID }
        val message = if (isCurrentUserAdmin) {
            "Because you are the creator of this group it will be deleted for everyone. This cannot be undone."
        } else {
            context.resources.getString(R.string.ConversationActivity_are_you_sure_you_want_to_leave_this_group)
        }
        builder.setMessage(message)
        builder.setPositiveButton(R.string.yes) { _, _ ->
            var groupPublicKey: String?
            var isClosedGroup: Boolean
            try {
                groupPublicKey = GroupUtil.doubleDecodeGroupID(thread.address.toString()).toHexString()
                isClosedGroup = DatabaseComponent.get(context).lokiAPIDatabase().isClosedGroup(groupPublicKey)
            } catch (e: IOException) {
                groupPublicKey = null
                isClosedGroup = false
            }
            try {
                if (isClosedGroup) {
                    MessageSender.leave(groupPublicKey!!, true)
                } else {
                    Toast.makeText(context, R.string.ConversationActivity_error_leaving_group, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, R.string.ConversationActivity_error_leaving_group, Toast.LENGTH_LONG).show()
            }
        }
        builder.setNegativeButton(R.string.no, null)
        builder.show()
    }

    private fun setNotifyType(context: Context, thread: Recipient) {
        NotificationUtils.showNotifyDialog(context, thread) { notifyType ->
            DatabaseComponent.get(context).recipientDatabase().setNotifyType(thread, notifyType)
        }
    }
}
