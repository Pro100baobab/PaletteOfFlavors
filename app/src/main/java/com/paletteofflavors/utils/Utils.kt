package com.paletteofflavors.utils

fun verifyCode(enteredCode: String, savedCode: String): Boolean {
    return savedCode != "" && enteredCode == savedCode
}

fun maskHideChars(input: String): String {
    return when {
        input.length <= 7 -> input
        else -> input.take(5) + "*".repeat(input.length - 6) + input.takeLast(2)
    }
}