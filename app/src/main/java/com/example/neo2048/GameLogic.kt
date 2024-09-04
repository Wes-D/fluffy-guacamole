package com.example.neo2048

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

class GameLogic {
    var score by mutableIntStateOf(0)
    private val boardSize = 4
    private var uniqueIdCounter = 0
    private var board by mutableStateOf(Array(boardSize) { Array(boardSize) { 0 } })
    var tiles by mutableStateOf(emptyList<TileMovement>())
        private set

    init {
        resetGame()
    }

    fun resetGame() {
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                board[i][j] = 0
            }
        }
        addNewTile()
        addNewTile()

        tiles = getInitialTileMovements()
    }

    // Helper function to create initial TileMovement list based on the board's current state
    private fun getInitialTileMovements(): List<TileMovement> {
        val initialTiles = mutableListOf<TileMovement>()
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                val value = board[i][j]
                if (value != 0) {
                    initialTiles.add(
                        TileMovement(
                            id = generateUniqueId(),
                            value = value,
                            oldX = i, // Initial position is the same as the current position
                            oldY = j,
                            newX = i,
                            newY = j,
                            isNew = true, // Treat initial tiles as new
                            isMerged = false
                        )
                    )
                }
            }
        }
        return initialTiles
    }

    private fun generateUniqueId(): Int {
        uniqueIdCounter += 1
        return uniqueIdCounter
    }

    private fun addNewTile(): Pair<Int, Int>? {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                if (board[i][j] == 0) emptyCells.add(Pair(i, j))
            }
        }
        if (emptyCells.isNotEmpty()) {
            val (x, y) = emptyCells.random()
            board[x][y] = if (Math.random() < 0.9) 2 else 4
            return Pair(x, y) // Return the coordinates of the newly added tile
        }
        return null
    }


    fun moveLeft() {
        val initialBoard = board.map { it.copyOf() }.toTypedArray()
        val movementInfo = mutableListOf<TileMovement>()

        for (i in 0 until boardSize) {
            val row = board[i] // Instead of a column, process each row
            val processedResult = processRowLeft(row) // Use processRowLeft

            // Update board and track movements
            for (j in 0 until boardSize) {
                val oldValue = board[i][j]
                val newValue = processedResult.newColumn[j]
                board[i][j] = newValue

                // Track only if movement or change happened
                if (newValue != 0) {
                    movementInfo.add(
                        TileMovement(
                            id = generateUniqueId(),
                            value = newValue,
                            oldX = i, // Row index stays the same
                            oldY = j, // Previous position in the row
                            newX = i, // Still in the same row
                            newY = processedResult.positions[j], // New position in the row
                            isNew = processedResult.isNew[j],
                            isMerged = processedResult.isMerged[j]
                        )
                    )
                }
            }
        }

        if (boardChanged(initialBoard, board)) {
            // Add new tile after the move and get its position
            val newTilePos = addNewTile()

            // If a new tile was added, add it to the movement info
            if (newTilePos != null) {
                movementInfo.add(
                    TileMovement(
                        id = generateUniqueId(),
                        value = board[newTilePos.first][newTilePos.second],
                        oldX = newTilePos.first, // New tile starts in its new position
                        oldY = newTilePos.second,
                        newX = newTilePos.first,
                        newY = newTilePos.second,
                        isNew = true,
                        isMerged = false
                    )
                )
            }

            // Update tiles with movement information for animations
            tiles = movementInfo
            logBoardState(board)
        }
    }


    fun moveRight() {
        val initialBoard = board.map { it.copyOf() }.toTypedArray()
        val movementInfo = mutableListOf<TileMovement>()

        for (i in 0 until boardSize) {
            val row = board[i] // Instead of a column, process each row
            val processedResult = processRowRight(row) // Use processRowRight

            // Update board and track movements
            for (j in 0 until boardSize) {
                val oldValue = board[i][j]
                val newValue = processedResult.newColumn[j]
                board[i][j] = newValue

                // Track only if movement or change happened
                if (newValue != 0) {
                    movementInfo.add(
                        TileMovement(
                            id = generateUniqueId(),
                            value = newValue,
                            oldX = i, // Row index stays the same
                            oldY = j, // Previous position in the row
                            newX = i, // Still in the same row
                            newY = processedResult.positions[j], // New position in the row
                            isNew = processedResult.isNew[j],
                            isMerged = processedResult.isMerged[j]
                        )
                    )
                }
            }
        }

        if (boardChanged(initialBoard, board)) {
            // Add new tile after the move and get its position
            val newTilePos = addNewTile()

            // If a new tile was added, add it to the movement info
            if (newTilePos != null) {
                movementInfo.add(
                    TileMovement(
                        id = generateUniqueId(),
                        value = board[newTilePos.first][newTilePos.second],
                        oldX = newTilePos.first, // New tile starts in its new position
                        oldY = newTilePos.second,
                        newX = newTilePos.first,
                        newY = newTilePos.second,
                        isNew = true,
                        isMerged = false
                    )
                )
            }

            // Update tiles with movement information for animations
            tiles = movementInfo
            logBoardState(board)
        }
    }


    fun moveUp() {
        val initialBoard = board.map { it.copyOf() }.toTypedArray()
        val movementInfo = mutableListOf<TileMovement>()

        for (j in 0 until boardSize) {
            val column = Array(boardSize) { i -> board[i][j] }
            val processedResult = processColumnUp(column)

            // Update board and track movements
            for (i in 0 until boardSize) {
                val oldValue = board[i][j]
                val newValue = processedResult.newColumn[i]
                board[i][j] = newValue

                if (newValue != 0) {
                        movementInfo.add(
                            TileMovement(
                                id = generateUniqueId(),
                                value = newValue,
                                oldX = i,
                                oldY = j,
                                newX = processedResult.positions[i],
                                newY = j,
                                isNew = processedResult.isNew[i],
                                isMerged = processedResult.isMerged[i]
                            )
                        )
                }
            }
        }

        if (boardChanged(initialBoard, board)) {

            // Add new tile after the move and get its position
            val newTilePos = addNewTile()

            if (newTilePos != null) {
                movementInfo.add(
                    TileMovement(
                        id = generateUniqueId(),
                        value = board[newTilePos.first][newTilePos.second],
                        oldX = newTilePos.first, // New tile starts in its new position
                        oldY = newTilePos.second,
                        newX = newTilePos.first,
                        newY = newTilePos.second,
                        isNew = true,
                        isMerged = false
                    )
                )
            }
            // Update tiles with movement information
            tiles = movementInfo
            logBoardState(board)
        }
    }

    fun moveDown() {
        val initialBoard = board.map { it.copyOf() }.toTypedArray()
        val movementInfo = mutableListOf<TileMovement>()

        for (j in 0 until boardSize) {
            val column = Array(boardSize) { i -> board[i][j] }
            val processedResult = processColumnDown(column)

            // Update board and track movements
            for (i in 0 until boardSize) {
                val oldValue = board[i][j]
                val newValue = processedResult.newColumn[i]
                board[i][j] = newValue

                if (newValue != 0) {
                    movementInfo.add(
                        TileMovement(
                            id = generateUniqueId(),
                            value = newValue,
                            oldX = i,
                            oldY = j,
                            newX = processedResult.positions[i],
                            newY = j,
                            isNew = processedResult.isNew[i],
                            isMerged = processedResult.isMerged[i]
                        )
                    )
                }
            }
        }

        if (boardChanged(initialBoard, board)) {

            // Add new tile after the move and get its position
            val newTilePos = addNewTile()

            if (newTilePos != null) {
                movementInfo.add(
                    TileMovement(
                        id = generateUniqueId(),
                        value = board[newTilePos.first][newTilePos.second],
                        oldX = newTilePos.first, // New tile starts in its new position
                        oldY = newTilePos.second,
                        newX = newTilePos.first,
                        newY = newTilePos.second,
                        isNew = true,
                        isMerged = false
                    )
                )
            }
            // Update tiles with movement information
            tiles = movementInfo
            logBoardState(board)
        }
    }

    private fun processColumnUp(column: Array<Int>): ProcessedResult {
        val newColumn = column.filter { it != 0 }.toMutableList() // Compress non-zero values
        val positions = Array(column.size) { it } // Initialize positions to their original index
        val isNew = Array(column.size) { false } // Initially, no tiles are new
        val isMerged = Array(column.size) { false } // Initially, no tiles are merged

        for (i in 0 until newColumn.size - 1) {
            if (newColumn[i] == newColumn[i + 1]) {
                newColumn[i] *= 2 // Merge tiles
                newColumn[i + 1] = 0 // Mark the next tile as merged
                isMerged[i] = true // Mark this tile as a merged tile
                isMerged[i + 1] = true // Mark the merged tile
            }
        }

        // Remove the zero values after merging
        val finalColumn = newColumn.filter { it != 0 }.toMutableList()

        // Update positions and fill the rest of the column with zeros
        for (i in finalColumn.indices) {
            positions[i] = i // Set the new position for the moved tiles
        }
        while (finalColumn.size < column.size) {
            finalColumn.add(0)
        }

        return ProcessedResult(
            newColumn = finalColumn.toTypedArray(),
            positions = positions,
            isNew = isNew,
            isMerged = isMerged
        )
    }

    private fun processColumnDown(column: Array<Int>): ProcessedResult {
        val newColumn = column.filter { it != 0 }.toMutableList() // Compress non-zero values
        val positions = Array(column.size) { it } // Initialize positions to their original index
        val isNew = Array(column.size) { false } // Initially, no tiles are new
        val isMerged = Array(column.size) { false } // Initially, no tiles are merged

        // Process the column from bottom to top (reverse order)
        for (i in newColumn.size - 1 downTo 1) {
            if (newColumn[i] == newColumn[i - 1]) {
                newColumn[i] *= 2 // Merge tiles
                newColumn[i - 1] = 0 // Mark the next tile as merged
                isMerged[i] = true // Mark this tile as a merged tile
                isMerged[i - 1] = true // Mark the merged tile
            }
        }

        // Remove the zero values after merging
        val finalColumn = newColumn.filter { it != 0 }.toMutableList()

        // Update positions for the final column values, working bottom-to-top
        for (i in finalColumn.indices) {
            positions[finalColumn.size - 1 - i] = column.size - 1 - i // New position
        }

        // Fill the rest of the column with zeros
        while (finalColumn.size < column.size) {
            finalColumn.add(0, 0)
        }

        return ProcessedResult(
            newColumn = finalColumn.toTypedArray(),
            positions = positions,
            isNew = isNew,
            isMerged = isMerged
        )
    }

    private fun processRowLeft(row: Array<Int>): ProcessedResult {
        val newRow = row.filter { it != 0 }.toMutableList() // Compress non-zero values
        val positions = Array(row.size) { it } // Initialize positions to their original index
        val isNew = Array(row.size) { false } // Initially, no tiles are new
        val isMerged = Array(row.size) { false } // Initially, no tiles are merged

        // Process the row from left to right
        for (i in 0 until newRow.size - 1) {
            if (newRow[i] == newRow[i + 1]) {
                newRow[i] *= 2 // Merge tiles
                newRow[i + 1] = 0 // Mark the next tile as merged
                isMerged[i] = true // Mark this tile as a merged tile
                isMerged[i + 1] = true // Mark the merged tile
            }
        }

        // Remove the zero values after merging
        val finalRow = newRow.filter { it != 0 }.toMutableList()

        // Update positions for the final row values
        for (i in finalRow.indices) {
            positions[i] = i // New position
        }

        // Fill the rest of the row with zeros
        while (finalRow.size < row.size) {
            finalRow.add(0)
        }

        return ProcessedResult(
            newColumn = finalRow.toTypedArray(),
            positions = positions,
            isNew = isNew,
            isMerged = isMerged
        )
    }

    private fun processRowRight(row: Array<Int>): ProcessedResult {
        val newRow = row.filter { it != 0 }.toMutableList() // Compress non-zero values
        val positions = Array(row.size) { it } // Initialize positions to their original index
        val isNew = Array(row.size) { false } // Initially, no tiles are new
        val isMerged = Array(row.size) { false } // Initially, no tiles are merged

        // Process the row from right to left (reverse order)
        for (i in newRow.size - 1 downTo 1) {
            if (newRow[i] == newRow[i - 1]) {
                newRow[i] *= 2 // Merge tiles
                newRow[i - 1] = 0 // Mark the previous tile as merged
                isMerged[i] = true // Mark this tile as a merged tile
                isMerged[i - 1] = true // Mark the merged tile
            }
        }

        // Remove the zero values after merging
        val finalRow = newRow.filter { it != 0 }.toMutableList()

        // Update positions for the final row values, working right-to-left
        for (i in finalRow.indices) {
            positions[row.size - 1 - i] = row.size - 1 - i // New position
        }

        // Fill the rest of the row with zeros
        while (finalRow.size < row.size) {
            finalRow.add(0, 0)
        }

        return ProcessedResult(
            newColumn = finalRow.toTypedArray(),
            positions = positions,
            isNew = isNew,
            isMerged = isMerged
        )
    }



    private fun boardChanged(initialBoard: Array<Array<Int>>, newBoard: Array<Array<Int>>): Boolean {
        for (i in 0 until boardSize) {
            if (!initialBoard[i].contentEquals(newBoard[i])) {
                return true
            }
        }
        return false
    }

    fun isGameOver(): Boolean {
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                if (board[i][j] == 0) return false // Empty cell
                if (i < boardSize - 1 && board[i][j] == board[i + 1][j]) return false // Vertical merge
                if (j < boardSize - 1 && board[i][j] == board[i][j + 1]) return false // Horizontal merge
            }
        }
        return true
    }
}



private fun logBoardState(board: Array<Array<Int>>) {
    board.forEach { row ->
        println(row.joinToString(" ") { it.toString() })
    }
    println("------")
}


