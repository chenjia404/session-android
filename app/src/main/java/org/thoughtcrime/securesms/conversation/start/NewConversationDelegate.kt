package org.thoughtcrime.securesms.conversation.start

interface NewConversationDelegate {
    fun onNewMessageSelected()
    fun onCreateGroupSelected()
    fun onJoinCommunitySelected()
    fun onMessageRequestSelected()
    fun onContactSelected(address: String)
    fun onDialogBackPressed()
    fun onDialogClosePressed()
}