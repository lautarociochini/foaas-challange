package exceptions

class DeserializationException(message: String) :
    BaseException(
        message,
        "deserialization_error"
    )