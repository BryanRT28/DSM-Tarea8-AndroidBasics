package com.example.flightsearch.data

import kotlinx.coroutines.flow.Flow

interface FlightRepository {
    fun getAirportsByQuery(query: String): Flow<List<Airport>>
    suspend fun getAirportByIata(iataCode: String): Airport?
    fun getAllDestinationsExcept(iataCode: String): Flow<List<Airport>>
    fun getAllFavorites(): Flow<List<Favorite>>
    suspend fun insertFavorite(favorite: Favorite)
    suspend fun removeFavorite(departureCode: String, destinationCode: String)
}

class OfflineFlightRepository(
    private val flightDao: FlightDao
) : FlightRepository {

    override fun getAirportsByQuery(query: String): Flow<List<Airport>> =
        flightDao.getAirportsByQuery(query)

    override suspend fun getAirportByIata(iataCode: String): Airport? =
        flightDao.getAirportByIata(iataCode)

    override fun getAllDestinationsExcept(iataCode: String): Flow<List<Airport>> =
        flightDao.getAllDestinationsExcept(iataCode)

    override fun getAllFavorites(): Flow<List<Favorite>> =
        flightDao.getAllFavorites()

    override suspend fun insertFavorite(favorite: Favorite) =
        flightDao.insertFavorite(favorite)

    override suspend fun removeFavorite(departureCode: String, destinationCode: String) =
        flightDao.deleteFavoriteByRoute(departureCode, destinationCode)
}