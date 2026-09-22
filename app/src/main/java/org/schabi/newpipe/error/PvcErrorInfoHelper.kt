package org.schabi.newpipe.error

import android.util.Log
import org.schabi.newpipe.pvc.feature.logcat.PvcLogcatDumper
import org.schabi.newpipe.error.ErrorInfo.Companion.throwableListToStringList
import org.schabi.newpipe.error.ErrorInfo.Companion.throwableToStringList
import org.schabi.newpipe.extractor.pvc.AttachException

object PvcErrorInfoHelper {

    /**
     * dump [ErrorInfo] stack traces via Log.e.
     *
     * - The traces need to be dumped early for [PvcLogcatDumper] to actually catch
     *   them. In the current NewPipe implementation the traces will be dumped
     *   to logcat only if the [ErrorActivity] is already started. That is way too late and
     *   also user dependent.
     * - If the [PvcLogcatDumper] is enabled we disable the dumping in [ErrorActivity]
     */
    fun logStackTraces(stackTraces: Array<String>): Array<String> {
        logIfPvcLocatDumperIsEnabled(stackTraces)
        return stackTraces
    }

    fun logStackTraces(throwable: Throwable): Array<String> {
        val stackTraces: Array<String> = throwableToStringList(throwable)
        logDataIfAttachException(throwable)
        logIfPvcLocatDumperIsEnabled(stackTraces)
        return stackTraces
    }

    fun logStackTraces(throwables: List<Throwable>): Array<String> {
        val stackTraces: Array<String> = throwableListToStringList(throwables)
        throwables.forEach { throwable ->
            logDataIfAttachException(throwable)
        }
        logIfPvcLocatDumperIsEnabled(stackTraces)
        return stackTraces
    }

    /**
     * if the exception is [AttachException] we dump its user given data to the Logger.
     */
    private fun logDataIfAttachException(throwable: Throwable) {
        if (throwable is AttachException) {
            throwable.exceptionData.forEach { data ->
                val stackTrace = throwable.stackTrace
                if (stackTrace.isNotEmpty()) {
                    val element = stackTrace[0]
                    val className = element.className.substringAfterLast(".")
                    Log.e(
                        "AttachExceptionData",
                        "[$className.${element.methodName}() line:${element.lineNumber}] DATA: $data"
                    )
                } else {
                    Log.e(
                        "AttachExceptionData",
                        "DATA: $data"
                    )
                }
            }
        }
    }

    private fun logIfPvcLocatDumperIsEnabled(stackTraces: Array<String>): Array<String> {
        if (!PvcLogcatDumper.isLogcatDumperEnabled()) return stackTraces

        // print stack trace once again for debugging:
        var count = 0 // how many traces do we have
        stackTraces.forEach {
            Log.e("${count++}_${PvcLogcatDumper::class.simpleName}", it)
        }

        return stackTraces
    }
}
