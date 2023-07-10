package org.thoughtcrime.securesms.home

import androidx.fragment.app.viewModels
import network.qki.messenger.R
import org.thoughtcrime.securesms.BaseFragment

/**
 * Created by Yaakov on
 * Describe:
 */
class ChatsFragment : BaseFragment<ChatsViewModel>(R.layout.fragment_chats) {

    override val viewModel by viewModels<ChatsViewModel>()


}