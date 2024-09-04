package com.example.neo2048

data class TileMovement(
    val id: Int,
    val value: Int,
    val oldX: Int,
    val oldY: Int,
    val newX: Int,
    val newY: Int,
    val isNew: Boolean = false,
    val isMerged: Boolean = false
)

