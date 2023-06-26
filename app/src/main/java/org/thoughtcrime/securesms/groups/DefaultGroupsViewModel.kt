package org.thoughtcrime.securesms.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import network.qki.messenger.BuildConfig
import nl.komponents.kovenant.functional.map
import org.session.libsession.messaging.open_groups.OpenGroupApi
import org.thoughtcrime.securesms.util.State

typealias DefaultGroups = List<OpenGroupApi.DefaultGroup>
typealias GroupState = State<DefaultGroups>

class DefaultGroupsViewModel : ViewModel() {

    init {
        OpenGroupApi.defaultServer = BuildConfig.defaultServer
        OpenGroupApi.defaultServerPublicKey =BuildConfig.defaultServerPublicKey
        OpenGroupApi.getDefaultServerCapabilities().map {
            OpenGroupApi.getDefaultRoomsIfNeeded()
        }
    }

    val defaultRooms = OpenGroupApi.defaultRooms.map<DefaultGroups, GroupState> {
        State.Success(it)
    }.onStart {
        emit(State.Loading)
    }.asLiveData()
}