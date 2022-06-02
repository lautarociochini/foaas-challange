package exceptions

import kotlin.RuntimeException

open class BaseException(override val message: String, val code: String = "bad.request") :
    RuntimeException() {
    companion object {
        const val MESSAGE = "message"
        const val CODE = "code"
    }

    open fun getMessageMap(): HashMap<String, String> {
        return hashMapOf(MESSAGE to message, CODE to code)
    }
}