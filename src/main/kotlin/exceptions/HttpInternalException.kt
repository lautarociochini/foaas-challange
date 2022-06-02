package exceptions

class HttpInternalException(override val message: String, code: String) : BaseException(message, code)