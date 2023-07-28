@file:Suppress("unused")

package org.thoughtcrime.securesms.util

import android.icu.text.Collator
import android.icu.util.ULocale
import android.net.Uri
import android.text.Editable
import org.thoughtcrime.securesms.et.Media
import java.io.File
import java.lang.Character.codePointCount
import java.lang.Character.offsetByCodePoints
import java.net.URL
import java.util.*

fun String?.safeTrim() = if (this.isNullOrBlank()) null else this.trim()

fun String?.isContentScheme(): Boolean = this?.startsWith("content://") == true

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

fun String.parseToUri(): Uri {
    return if (isUri()) Uri.parse(this) else {
        Uri.fromFile(File(this))
    }
}

fun String?.isUri(): Boolean {
    this ?: return false
    return this.startsWith("file://", true) || isContentScheme()
}

fun String?.isAbsUrl() = this?.let {
    it.startsWith("http://", true) || it.startsWith("https://", true)
} ?: false

fun String?.isJson(): Boolean = this?.run {
    val str = this.trim()
    when {
        str.startsWith("{") && str.endsWith("}") -> true
        str.startsWith("[") && str.endsWith("]") -> true
        else -> false
    }
} ?: false

fun String?.isJsonObject(): Boolean = this?.run {
    val str = this.trim()
    str.startsWith("{") && str.endsWith("}")
} ?: false

fun String?.isJsonArray(): Boolean = this?.run {
    val str = this.trim()
    str.startsWith("[") && str.endsWith("]")
} ?: false

fun String?.isXml(): Boolean = this?.run {
    val str = this.trim()
    str.startsWith("<") && str.endsWith(">")
} ?: false

fun String?.isTrue(nullIsTrue: Boolean = false): Boolean {
    if (this.isNullOrBlank() || this == "null") {
        return nullIsTrue
    }
    return !this.trim().matches("(?i)^(false|no|not|0)$".toRegex())
}

fun String.splitNotBlank(vararg delimiter: String, limit: Int = 0): Array<String> = run {
    this.split(*delimiter, limit = limit).map { it.trim() }.filterNot { it.isBlank() }
        .toTypedArray()
}

fun String.splitNotBlank(regex: Regex, limit: Int = 0): Array<String> = run {
    this.split(regex, limit).map { it.trim() }.filterNot { it.isBlank() }.toTypedArray()
}

fun String.cnCompare(other: String): Int {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        Collator.getInstance(ULocale.SIMPLIFIED_CHINESE).compare(this, other)
    } else {
        java.text.Collator.getInstance(Locale.CHINA).compare(this, other)
    }
}

/**
 * 字符串所占内存大小
 */
fun String?.memorySize(): Int {
    this ?: return 0
    return 40 + 2 * length
}

/**
 * 将字符串拆分为单个字符,包含emoji
 */
fun CharSequence.toStringArray(): Array<String> {
    var codePointIndex = 0
    return try {
        Array(codePointCount(this, 0, length)) {
            val start = codePointIndex
            codePointIndex = offsetByCodePoints(this, start, 1)
            substring(start, codePointIndex)
        }
    } catch (e: Exception) {
        split("").toTypedArray()
    }
}

fun String?.isValidURL(): Boolean {
    this ?: return false
    return try {
        URL(this)
        true
    } catch (e: Exception) {
        false
    }
}

fun String.formatAddress(): String {
    if (this.isEmpty()) {
        return ""
    }
    return this.substring(0, 6) + "...." + this.substring(this.length - 6)
}

val picsList = listOf(
    "jpeg", "jpg", "gif", "png", "bmp", "webp", "svg"
)

val mediasList = listOf(
    "swf", "avi", "flv", "mpg", "rm", "mov", "wav", "asf", "3gp", "mkv", "rmvb", "mp4"
)

fun String?.formatMedias(): List<Media> = this?.run {
    val list = arrayListOf<Media>()
    val str = this.trim()
    for (attach in str.split(",")) {
        val split = attach.split(".")
        val picFilter = picsList.filter { s -> split[split.size - 1].startsWith(s, true) }
        if (!picFilter.isNullOrEmpty()) {
            list.add(Media(0, attach))
        }
        val mediaFilter = mediasList.filter { s -> split[split.size - 1].startsWith(s, true) }
        if (!mediaFilter.isNullOrEmpty()) {
            list.add(Media(1, attach))
        }
    }
    list
} ?: emptyList()

fun String?.formatMediaUrl(): List<String> = this?.run {
    val list = arrayListOf<String>()
    val str = this.trim()
    for (attach in str.split(",")) {
        val split = attach.split(".")
        val picFilter = picsList.filter { s -> split[split.size - 1].startsWith(s, true) }
        if (!picFilter.isNullOrEmpty()) {
            list.add(attach)
        }
        val mediaFilter = mediasList.filter { s -> split[split.size - 1].startsWith(s, true) }
        if (!mediaFilter.isNullOrEmpty()) {
            list.add(attach)
        }
    }
    list
} ?: emptyList()