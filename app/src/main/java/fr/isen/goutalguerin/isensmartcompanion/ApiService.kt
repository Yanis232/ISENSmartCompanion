package fr.isen.goutalguerin.isensmartcompanion

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("events.json")
    fun getEvents(): Call<List<Event>>

    @GET("events/{id}.json")
    fun getEventById(@Path("id") eventId: String): Call<Event>

}