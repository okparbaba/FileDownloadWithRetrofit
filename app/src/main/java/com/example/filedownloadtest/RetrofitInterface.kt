package com.example.filedownloadtest

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming

interface RetrofitInterface {
    @GET("big_buck_bunny.mp4")
    @Streaming
    fun downloadFile(): Call<ResponseBody>
}