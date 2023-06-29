package org.thoughtcrime.securesms.util

import android.text.TextUtils
import android.util.Log
import network.qki.messenger.BuildConfig

/**
 * Created by Author on 2020/4/26
 */
object Logger {
    private const val APP_TAG = "mcnk"
    private val functionName: String?
        private get() {
            val sts = Thread.currentThread().stackTrace
            if (sts != null) {
                for (st in sts) {
                    if (st.isNativeMethod) {
                        continue
                    }
                    if (st.className == Thread::class.java.name) {
                        continue
                    }
                    if (st.className == Logger::class.java.name) {
                        continue
                    }
                    return "[ Thread:" + Thread.currentThread().name + ", at " + st.className + "." + st.methodName + "(" + st.fileName + ":" + st.lineNumber + ")" + " ]"
                }
            }
            return null
        }

    fun v(msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.v(APP_TAG, getMsgFormat(msg))
        }
    }

    fun v(tag: String?, msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, getMsgFormat(msg))
        }
    }

    fun d(msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.d(APP_TAG, getMsgFormat(msg))
        }
    }

    fun d(tag: String?, msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, getMsgFormat(msg))
        }
    }

    fun i(msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.i(APP_TAG, getMsgFormat(msg))
        }
    }

    fun i(tag: String?, msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, getMsgFormat(msg))
        }
    }

    fun w(msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.w(APP_TAG, getMsgFormat(msg))
        }
    }

    fun w(tag: String?, msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, getMsgFormat(msg))
        }
    }

    fun e(msg: String?) {
        Log.e(APP_TAG, getMsgFormat(msg))
    }

    fun e(e: Exception?) {
        if (null != e && TextUtils.isEmpty(e.message)) {
            Log.e(APP_TAG, getMsgFormat(e.message))
        }
    }

    fun e(tag: String?, msg: String?) {
        Log.e(tag, getMsgFormat(msg))
    }

    private fun getMsgFormat(msg: String?): String {
        return "$msg ==> $functionName"
    }
}