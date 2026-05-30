package com.paletteofflavors.data.remote.api.imgBB

import kotlinx.serialization.Serializable

@Serializable
data class ImgBBResponse(
    val data: ImgBBData? = null,
    val success: Boolean,
    val status: Int
)

@Serializable
data class ImgBBData(
    val id: String,
    val title: String,
    val url_viewer: String,
    val url: String,
    val display_url: String,
    val width: String,
    val height: String,
    val size: String,
    val time: String,
    val expiration: String,
    val image: ImgBBImage,
    val thumb: ImgBBImage,
    val medium: ImgBBImage? = null,
    val delete_url: String
)

@Serializable
data class ImgBBImage(
    val filename: String,
    val name: String,
    val mime: String,
    val extension: String,
    val url: String
)
