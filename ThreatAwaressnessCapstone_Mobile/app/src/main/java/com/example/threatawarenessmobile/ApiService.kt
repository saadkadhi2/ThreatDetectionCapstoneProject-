package com.example.threatawarenessmobile

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// http://192.168.0.33:5000/
// chris 172.20.10.14
//private const val url = "http://10.157.37.35:5000/"
//private const val url = "http://172.20.10.2:5000/"
private const val url = "http://172.20.10.14:5000/"
private val retrofit = Retrofit.Builder()
    .baseUrl(url)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val dataService = retrofit.create(ApiService::class.java)
interface ApiService {
    @GET("status")
    fun getWarning(): Call<WarningResponse>
}