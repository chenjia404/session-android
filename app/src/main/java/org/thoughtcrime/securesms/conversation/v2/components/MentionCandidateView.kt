package org.thoughtcrime.securesms.conversation.v2.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import network.qki.messenger.databinding.ViewMentionCandidateBinding
import org.session.libsession.messaging.mentions.Mention
import org.thoughtcrime.securesms.groups.OpenGroupManager
import org.thoughtcrime.securesms.mms.GlideRequests

class MentionCandidateView : LinearLayout {
    private lateinit var binding: ViewMentionCandidateBinding
    var mentionCandidate = Mention("", "")
        set(newValue) { field = newValue; update() }
    var glide: GlideRequests? = null
    var openGroupServer: String? = null
    var openGroupRoom: String? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { initialize() }

    private fun initialize() {
        binding = ViewMentionCandidateBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private fun update() = with(binding) {
        mentionCandidateNameTextView.text = mentionCandidate.displayName
        profilePictureView.root.publicKey = mentionCandidate.publicKey
        profilePictureView.root.displayName = mentionCandidate.displayName
        profilePictureView.root.additionalPublicKey = null
        profilePictureView.root.glide = glide!!
        profilePictureView.root.update()
        if (openGroupServer != null && openGroupRoom != null) {
            val isUserModerator = OpenGroupManager.isUserModerator(context, "$openGroupRoom.$openGroupServer", mentionCandidate.publicKey)
            moderatorIconImageView.visibility = if (isUserModerator) View.VISIBLE else View.GONE
        } else {
            moderatorIconImageView.visibility = View.GONE
        }
    }
}