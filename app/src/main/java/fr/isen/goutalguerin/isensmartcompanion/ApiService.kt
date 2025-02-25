package fr.isen.goutalguerin.isensmartcompanion

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("events.json")
    fun getEvents(): Call<List<Event>>

   // @GET("events/{id}")
   //    fun getEventById(eventId: String): Call<Event>

}