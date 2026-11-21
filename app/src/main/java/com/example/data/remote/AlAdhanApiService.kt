package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class AlAdhanResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "data") val data: AlAdhanData?
)

@JsonClass(generateAdapter = true)
data class AlAdhanData(
    @Json(name = "timings") val timings: AlAdhanTimings,
    @Json(name = "date") val date: AlAdhanDateWrapper
)

@JsonClass(generateAdapter = true)
data class AlAdhanTimings(
    @Json(name = "Fajr") val fajr: String,
    @Json(name = "Sunrise") val sunrise: String,
    @Json(name = "Dhuhr") val dhuhr: String,
    @Json(name = "Asr") val asr: String,
    @Json(name = "Maghrib") val maghrib: String,
    @Json(name = "Isha") val isha: String
)

@JsonClass(generateAdapter = true)
data class AlAdhanDateWrapper(
    @Json(name = "hijri") val hijri: AlAdhanHijri
)

@JsonClass(generateAdapter = true)
data class AlAdhanHijri(
    @Json(name = "day") val day: String,
    @Json(name = "month") val month: AlAdhanHijriMonth,
    @Json(name = "year") val year: String,
    @Json(name = "weekday") val weekday: AlAdhanHijriWeekday
)

@JsonClass(generateAdapter = true)
data class AlAdhanHijriMonth(
    @Json(name = "ar") val ar: String
)

@JsonClass(generateAdapter = true)
data class AlAdhanHijriWeekday(
    @Json(name = "ar") val ar: String
)

interface AlAdhanApiService {
    @GET("v1/timings/{date}")
    suspend fun getTimings(
        @Path("date") ddMmYyyy: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int,
        @Query("school") school: Int
    ): AlAdhanResponse

    companion object {
        fun create(): AlAdhanApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .build()
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            return Retrofit.Builder()
                .baseUrl("https://api.aladhan.com/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(AlAdhanApiService::class.java)
        }
    }
}
