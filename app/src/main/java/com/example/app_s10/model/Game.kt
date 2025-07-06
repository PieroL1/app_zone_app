package com.example.app_s10.model

data class Game(
    var id: String? = "",     // Usado para la clave en la base de datos
    var nombre: String? = "",
    var genero: String? = "",
    var anio: Int? = 0
)
