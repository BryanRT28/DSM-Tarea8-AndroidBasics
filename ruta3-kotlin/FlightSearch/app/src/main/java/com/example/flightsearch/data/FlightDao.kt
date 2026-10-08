package com.example.flightsearch.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightDao {

    // Búsqueda de aeropuertos que coincidan con el texto ingresado
    @Query("""
        SELECT * FROM airport 
        WHERE iata_code LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' 
        ORDER BY passengers DESC
    """)
    fun getAirportsByQuery(query: String): Flow<List<Airport>>

    // Obtener un aeropuerto específico por su código IATA
    @Query("SELECT * FROM airport WHERE iata_code = :iataCode LIMIT 1")
    suspend fun getAirportByIata(iataCode: String): Airport?

    // Todos los aeropuertos excepto el seleccionado (posibles destinos)
    @Query("SELECT * FROM airport WHERE iata_code != :iataCode ORDER BY passengers DESC")
    fun getAllDestinationsExcept(iataCode: String): Flow<List<Airport>>

    // Consultar todos los favoritos guardados
    @Query("SELECT * FROM favorite")
    fun getAllFavorites(): Flow<List<Favorite>>

    // Insertar favorito
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    // Eliminar favorito
    @Query("DELETE FROM favorite WHERE departure_code = :depCode AND destination_code = :destCode")
    suspend fun deleteFavoriteByRoute(depCode: String, destCode: String)
}