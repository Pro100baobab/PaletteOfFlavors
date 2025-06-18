package com.paletteofflavors.data.remote.API.GoogleScript

import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded


interface GoogleScriptService {
    @POST("https://script.google.com/macros/s/AKfycbyMdw5WXBcXs13Igoi3wE5PTR3OszGlqsyUH4F3n4c5w0Ntqm2heBVx3n9L2L6rS2Hw/exec")
    @FormUrlEncoded
    fun executeScript(
        @Field("email") email: String
    ): Call<String>
}