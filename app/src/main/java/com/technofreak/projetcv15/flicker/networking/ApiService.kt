package com.technofreak.projetcv15.flicker.networking

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.technofreak.projetcv15.flicker.FlickerPhotoModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://www.flickr.com/"
private const val CONNECTION_TIMEOUT_MS: Long = 10
private val retrofit = Retrofit.Builder()
    .addConverterFactory(
        GsonConverterFactory.create(
            GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()
        )
    )  .client(
        OkHttpClient.Builder().connectTimeout(
            CONNECTION_TIMEOUT_MS,
            TimeUnit.SECONDS
        ).addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }).build()
    )
    .baseUrl(BASE_URL)
    .build()


interface ApiService {
    @GET("services/rest/?")
    suspend fun fetchImages(
        @Query("method") method: String, @Query("api_key") api_key: String,
        @Query("format") format: String, @Query("nojsoncallback") nojsoncallback: Int,
        @Query("page") page: Int, @Query("text") text: String?
    ): FlickerPhotoModel
}

object FlickerApi {
    val retrofitService : ApiService by lazy {
        retrofit.create(ApiService::class.java) }
}