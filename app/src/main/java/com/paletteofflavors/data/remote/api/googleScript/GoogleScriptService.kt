package com.paletteofflavors.data.remote.api.googleScript

import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded


interface GoogleScriptService {
    @POST(Endpoints.GOOGLE_SCRIPT)
    @FormUrlEncoded
    fun executeScript(
        @Field("email") email: String
    ): Call<String>
}