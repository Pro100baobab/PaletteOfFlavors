package com.paletteofflavors.domain.model

data class User(
    val fullName: String,
    val username: String,
    val email: String,
    val phoneNumber: String,
    val passwordHash: Int
)
