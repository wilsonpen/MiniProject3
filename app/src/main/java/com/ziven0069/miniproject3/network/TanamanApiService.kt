package com.ziven0069.miniproject3.network

import com.ziven0069.miniproject3.model.Tanaman
import com.ziven0069.miniproject3.model.OpStatus
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "https://apimobpromobil-production.up.railway.app/api/"
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface TanamanApiService {
    @GET("tanaman")
    suspend fun getTanaman(
        @Header("Authorization") userId: String
    ): List<Tanaman>

    @Multipart
    @POST("tanaman")
    suspend fun postTanaman(
        @Header("Authorization") userId: String,
        @Part("nama_tanaman") nama_tanaman: RequestBody,
        @Part("nama_latin") namaLatin: RequestBody,
        @Part image: MultipartBody.Part
    ): OpStatus


    @Multipart
    @POST("tanaman/{id}")
    suspend fun updateTanaman(
        @Header("Authorization") userId: String,
        @Part("nama_tanaman") nama: RequestBody,
        @Part("nama_latin") namaLatin: RequestBody,
        @Part image: MultipartBody.Part,
        @Path("id") id: String
    ): OpStatus

    @DELETE("tanaman/{id}")
    suspend fun deleteTanaman(
        @Header("Authorization") userId: String,
        @Path("id") id: String
    ): OpStatus
}

object TanamanApi {
    val service: TanamanApiService by lazy {
        retrofit.create(TanamanApiService::class.java)
    }
    fun getTanamanUrl(imageId: String): String {
        return "${BASE_URL.replace("/api/","/")}storage/$imageId"
    }
}
enum class ApiStatus { LOADING, SUCCESS, FAILED}