package org.thoughtcrime.securesms.util.coroutine

import kotlin.coroutines.cancellation.CancellationException

class ActivelyCancelException : CancellationException()