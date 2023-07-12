package org.thoughtcrime.securesms.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.cash.copper.flow.observeQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import org.thoughtcrime.securesms.BaseViewModel
import org.thoughtcrime.securesms.database.DatabaseContentProviders
import org.thoughtcrime.securesms.database.ThreadDatabase
import org.thoughtcrime.securesms.database.model.ThreadRecord
import java.lang.ref.WeakReference
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(application: Application, private val threadDb: ThreadDatabase): BaseViewModel(application) {

    private val executor = viewModelScope + SupervisorJob()
    private var lastContext: WeakReference<Context>? = null
    private var updateJobs: MutableList<Job> = mutableListOf()

    private val _conversations = MutableLiveData<List<ThreadRecord>>()
    val conversations: LiveData<List<ThreadRecord>> = _conversations

    private val listUpdateChannel = Channel<Unit>(capacity = Channel.CONFLATED)

    fun tryUpdateChannel() = listUpdateChannel.trySend(Unit)

    fun getObservable(context: Context): LiveData<List<ThreadRecord>> {
        // If the context has changed (eg. the activity gets recreated) then
        // we need to cancel the old executors and recreate them to prevent
        // the app from triggering extra updates when data changes
        if (context != lastContext?.get()) {
            lastContext = WeakReference(context)
            updateJobs.forEach { it.cancel() }
            updateJobs.clear()

            updateJobs.add(
                executor.launch(Dispatchers.IO) {
                    context.contentResolver
                        .observeQuery(DatabaseContentProviders.ConversationList.CONTENT_URI)
                        .onEach { listUpdateChannel.trySend(Unit) }
                        .collect()
                }
            )
            updateJobs.add(
                executor.launch(Dispatchers.IO) {
                    for (update in listUpdateChannel) {
                        threadDb.approvedConversationList.use { openCursor ->
                            val reader = threadDb.readerFor(openCursor)
                            val threads = mutableListOf<ThreadRecord>()
                            while (true) {
                                threads += reader.next ?: break
                            }
                            withContext(Dispatchers.Main) {
                                _conversations.value = threads
                            }
                        }
                    }
                }
            )
        }
        return conversations
    }
}