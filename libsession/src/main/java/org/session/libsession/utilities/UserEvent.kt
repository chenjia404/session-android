package org.session.libsession.utilities

import org.session.libsession.utilities.recipients.Recipient

data class UserEvent(val type: Int, val recipient: Recipient) {
    companion object {
        const val EXPIRATION = 0
        const val BLOCK = 1
        const val UNBLOCK = 2
        const val SEARCH = 3
    }
}