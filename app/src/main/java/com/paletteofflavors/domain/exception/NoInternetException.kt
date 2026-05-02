package com.paletteofflavors.domain.exception

/** Исключение, выбрасываемое при отсутствии интернет-соединения */
class NoInternetException(
    message: String = "No internet connection available",
    cause: Throwable? = null
) : RuntimeException(message, cause)