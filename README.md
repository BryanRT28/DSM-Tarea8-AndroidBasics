# Desarrollo de Software Móvil - Tarea 8 (Android Basics with Compose)

Repositorio que contiene las soluciones prácticas correspondientes a la **Unidad 6: Persistencia de datos locales**, implementando **Room Database** y **Preferences DataStore** con arquitectura moderna en Android.

---

## Proyectos Desarrollados

###  Ruta 1: `ruta1-kotlin/Inventory`
- **Descripción:** Aplicación para la gestión de inventario y stock de productos utilizando una base de datos SQLite gestionada con Room.
- **Aspectos Técnicos:**
  - Definición de Entidades (`@Entity`) con claves primarias autogeneradas.
  - Implementación de Data Access Object (`ItemDao`) con operaciones reactivas (`Flow<List<Item>>`) y funciones de suspensión (`suspend`).
  - Patrón Singleton en la clase `InventoryDatabase` con `@Volatile` y bloque `synchronized`.
  - Inyección manual de dependencias mediante `AppContainer` y desacoplamiento con `OfflineItemsRepository`.
  - UI declarativa en **Jetpack Compose** con estado inmutable mediante `StateFlow`.

###  Ruta 2: `ruta2-kotlin/FlightSearch`
- **Descripción:** Buscador de vuelos y destinos en tiempo real que integra una base de datos SQLite pre-poblada y almacenamiento de configuraciones de usuario.
- **Aspectos Técnicos:**
  - Carga de base de datos pre-cargada desde el directorio de activos (`assets/database/flight_search.db`) mediante `.createFromAsset()`.
  - Consultas de autocompletado y búsqueda relacional con operadores `LIKE` en Room.
  - Persistencia de la última consulta de búsqueda del usuario mediante **Preferences DataStore** (`preferencesDataStore` y `stringPreferencesKey`).
  - Gestión de favoritos (alta y baja de rutas aéreas) guardados de forma local y reactiva.
  - Integración reactiva en el ViewModel con operadores `combine` y `flatMapLatest`.

---

## Tecnologías Utilizadas

- **Lenguaje:** Kotlin
- **Toolkit UI:** Jetpack Compose (Material 3)
- **Persistencia Relacional:** Android Jetpack Room
- **Persistencia Clave-Valor:** Android Jetpack Preferences DataStore
- **Concurrencia y Reactividad:** Kotlin Coroutines & Flow
- **Arquitectura:** MVVM, Clean Architecture por capas y Patrón Repositorio
