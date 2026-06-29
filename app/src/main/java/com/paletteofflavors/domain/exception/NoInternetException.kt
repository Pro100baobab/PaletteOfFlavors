package com.paletteofflavors.domain.exception


class NoInternetException(
    message: String = "No internet connection available",
    cause: Throwable? = null
) : RuntimeException(message, cause)