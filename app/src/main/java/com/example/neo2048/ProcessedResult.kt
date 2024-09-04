package com.example.neo2048

data class ProcessedResult(
    val newColumn: Array<Int>, // The new values in the column after processing
    val positions: Array<Int>, // The new positions of each tile in the column
    val isNew: Array<Boolean>, // Whether each tile is a newly added tile
    val isMerged: Array<Boolean> // Whether each tile was the result of a merge
)
