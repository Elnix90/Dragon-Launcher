/**
 * Extension for optimized logging with Timber.
 * Uses inline and lambda for performance when logging complex strings.
 */


package org.elnix.dragonlauncher.logging

import timber.log.Timber

inline fun logV(tag: LogTag, throwable: Throwable? = null, message: () -> String) {
    Timber.tag(tag.tag).v(throwable, message())
}

inline fun logD(tag: LogTag, throwable: Throwable? = null, message: () -> String) {
    Timber.tag(tag.tag).d(throwable, message())
}

inline fun logI(tag: LogTag, throwable: Throwable? = null, message: () -> String) {
    Timber.tag(tag.tag).i(throwable, message())
}

inline fun logW(tag: LogTag, throwable: Throwable? = null, message: () -> String) {
    Timber.tag(tag.tag).w(throwable,message())
}

inline fun logE(tag: LogTag, throwable: Throwable, message: () -> String) {
    Timber.tag(tag.tag).e(throwable, message())
}

inline fun logWtf(tag: LogTag, throwable: Throwable? = null, message: () -> String) {
    Timber.tag(tag.tag).wtf(throwable, message())
}

inline fun logWtf(throwable: Throwable? = null, message: () -> String) {
    Timber.tag(LogTag("WTF").tag).wtf(throwable, message())
}