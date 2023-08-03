@file:Suppress("unused")

package org.thoughtcrime.securesms.util

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable


inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? {
    return getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? {
    return getParcelable(key) as? T
}