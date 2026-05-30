package com.paletteofflavors.data.remote.api.imgBB

import com.paletteofflavors.BuildConfig
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

interface ImgBBService {
    @FormUrlEncoded
    @POST("1/upload")
    suspend fun uploadImage(
        @Query("key") key: String = BuildConfig.IMGBB_KEY,
        @Field("image") imageBase64: String
    ): Response<ImgBBResponse>
}
