package com.paletteofflavors.data.remote.api.googleScript

import com.paletteofflavors.BuildConfig
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded


interface GoogleScriptService {
    @POST(BuildConfig.GOOGLE_SCRIPT_URL)
    @FormUrlEncoded
    fun executeScript(
        @Field("email") email: String
    ): Call<String>
}