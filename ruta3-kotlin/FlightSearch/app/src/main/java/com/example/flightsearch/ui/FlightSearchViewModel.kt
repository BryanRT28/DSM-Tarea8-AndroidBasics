package com.example.flightsearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.flightsearch.FlightSearchApplication
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.Favorite
import com.example.flightsearch.data.FlightRepository
import com.example.flightsearch.data.FlightRow
import com.example.flightsearch.data.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FlightSearchUiState(
    val searchQuery: String = "",
    val searchResults: List<Airport> = emptyList(),
    val selectedAirport: Airport? = null,
    val destinationFlights: List<FlightRow> = emptyList(),
    val favoriteFlights: List<FlightRow> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class FlightSearchViewModel(
    private val flightRepository: FlightRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedAirport = MutableStateFlow<Airport?>(null)

    init {
        viewModelScope.launch {
            val initialQuery = userPreferencesRepository.searchQuery.first()
            _searchQuery.value = initialQuery
        }
    }

    private val favoriteFlightsStream: Flow<List<FlightRow>> = flightRepository.getAllFavorites()
        .map { favList: List<Favorite> ->
            favList.mapNotNull { fav ->
                val dep = flightRepository.getAirportByIata(fav.departureCode)
                val dest = flightRepository.getAirportByIata(fav.destinationCode)
                if (dep != null && dest != null) {
                    FlightRow(dep, dest, isFavorite = true)
                } else null
            }
        }

    val uiState: StateFlow<FlightSearchUiState> = combine(
        _searchQuery,
        _selectedAirport,
        favoriteFlightsStream
    ) { query: String, selected: Airport?, favorites: List<FlightRow> ->
        Triple(query, selected, favorites)
    }.flatMapLatest { (query, selected, favorites) ->
        when {
            selected != null -> {
                flightRepository.getAllDestinationsExcept(selected.iataCode).map { destinations: List<Airport> ->
                    val rows = destinations.map { dest ->
                        val isFav = favorites.any { fav ->
                            fav.departureAirport.iataCode == selected.iataCode &&
                                    fav.destinationAirport.iataCode == dest.iataCode
                        }
                        FlightRow(selected, dest, isFavorite = isFav)
                    }
                    FlightSearchUiState(
                        searchQuery = query,
                        selectedAirport = selected,
                        destinationFlights = rows,
                        favoriteFlights = favorites
                    )
                }
            }
            query.isNotBlank() -> {
                flightRepository.getAirportsByQuery(query).map { list: List<Airport> ->
                    FlightSearchUiState(
                        searchQuery = query,
                        searchResults = list,
                        favoriteFlights = favorites
                    )
                }
            }
            else -> {
                flowOf(
                    FlightSearchUiState(
                        searchQuery = query,
                        favoriteFlights = favorites
                    )
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = FlightSearchUiState()
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        _selectedAirport.value = null
        viewModelScope.launch {
            userPreferencesRepository.saveSearchQuery(newQuery)
        }
    }

    fun onAirportSelected(airport: Airport) {
        _selectedAirport.value = airport
    }

    fun toggleFavorite(flight: FlightRow) {
        viewModelScope.launch {
            if (flight.isFavorite) {
                flightRepository.removeFavorite(
                    flight.departureAirport.iataCode,
                    flight.destinationAirport.iataCode
                )
            } else {
                flightRepository.insertFavorite(
                    Favorite(
                        departureCode = flight.departureAirport.iataCode,
                        destinationCode = flight.destinationAirport.iataCode
                    )
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FlightSearchApplication)
                FlightSearchViewModel(
                    application.container.flightRepository,
                    application.container.userPreferencesRepository
                )
            }
        }
    }
}