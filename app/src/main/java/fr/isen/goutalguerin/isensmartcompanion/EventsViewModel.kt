package fr.isen.goutalguerin.isensmartcompanion

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class EventsState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class EventsViewModel : ViewModel() {
    private val _events = MutableStateFlow(EventsState())
    val events: StateFlow<EventsState> = _events.asStateFlow()

    init {
        fetchEvents()
    }

    //pourrécupérer la liste des événements
    private fun fetchEvents() {
        _events.value = EventsState(isLoading = true)

        ApiClient.instance.getEvents().enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful && response.body() != null) {
                    _events.value = EventsState(events = response.body()!!)
                } else {
                    _events.value = EventsState(error = "Erreur serveur : ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                _events.value = EventsState(error = "Erreur réseau : ${t.message}")
            }
        })
    }

}