package exceptions

class UnauthorizedException :
    BaseException(
        "You are not authorized to access this resource",
        "unauthorized_resource_error"
    )