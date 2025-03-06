package fr.isen.goutalguerin.isensmartcompanion

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Event(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("date") val date: String,
    @SerializedName("location") val location: String,
    @SerializedName("category") val category: String,
    val isNotificationEnabled: Boolean = false
) : Parcelable

//Représenter un événement avec ses différentes propriétés (id, titre, description, date, lieu, catégorie).
