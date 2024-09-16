package com.example.neo2048

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameLogic {
    var score by mutableIntStateOf(0)
    private val boardSize = 4
    private var uniqueIdCounter = 0
    private var board by mutableStateOf(Array(boardSize) { Array(boardSize) { 0 } })
    var tiles by mutableStateOf<List<TileMovement>>(emptyList())

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
        val newBoardRows = Array(boardSize) { Array(boardSize) { 0 } } // Changed to Array<Array<Int>>

        for (i in 0 until boardSize) {
            val row = board[i]
            val processedResult = processRowLeft(row)

            // Collect movements without updating the board yet
            for (j in 0 until boardSize) {
                val newValue = processedResult.newColumn[j]
                newBoardRows[i][j] = newValue // Store in newBoardRows as Array<Array<Int>>

                if (newValue != 0) {
                    val oldY = processedResult.positions[j] // Original column index
                    movementInfo.add(
                        TileMovement(
                            id = generateUniqueId(),
                            value = newValue,
                            oldX = i,
                            oldY = oldY,
                            newX = i,
                            newY = j,
                            isNew = false,
                            isMerged = processedResult.isMerged[j]
                        )
                    )
                }
            }
        }

        if (boardChanged(initialBoard, newBoardRows)) {
            CoroutineScope(Dispatchers.Main).launch {
                // Step 1: Trigger sliding animations
                tiles = movementInfo

                // Step 2: Delay to allow sliding animations to finish
                delay(300)

                // Step 3: Now apply merge values and update board
                for (i in 0 until boardSize) {
                    for (j in 0 until boardSize) {
                        board[i][j] = newBoardRows[i][j]
                    }
                }

                // Step 4: Trigger merge animations
                tiles = movementInfo

                // Step 5: Delay for merge animations to complete
                delay(200)

                // Step 6: Add new tile after animations
                val newTilePos = addNewTile()
                if (newTilePos != null) {
                        val newTile = TileMovement(
                            id = generateUniqueId(),
                            value = board[newTilePos.first][newTilePos.second],
                            oldX = newTilePos.first,
                            oldY = newTilePos.second,
                            newX = newTilePos.first,
                            newY = newTilePos.second,
                            isNew = true,
                            isMerged = false
                        )
                    // Update tiles to include the new tile
                    tiles = movementInfo + newTile
                    logBoardState(board)
                } else {
                    // If no new tile was added, still update the tiles to trigger recomposition
                    tiles = movementInfo.toList()
                }
            }
        }
    }

    fun moveRight() {
        val initialBoard = board.map { it.copyOf() }.toTypedArray()
        val movementInfo = mutableListOf<TileMovement>()
        val newBoardRows = Array(boardSize) { Array(boardSize) { 0 } }

        for (i in 0 until boardSize) {
            val row = board[i]
            val processedResult = processRowRight(row)

            // Collect movements without updating the board yet
            for (j in 0 until boardSize) {
                val newValue = processedResult.newColumn[j]
                newBoardRows[i][j] = newValue

                if (newValue != 0) {
                    val oldY = processedResult.positions[j] // Original column index
                    movementInfo.add(
                        TileMovement(
                            id = generateUniqueId(),
                            value = newValue,
                            oldX = i,
                            oldY = oldY,
                            newX = i,
                            newY = j,
                            isNew = false,
                            isMerged = processedResult.isMerged[j]
                        )
                    )
                }
            }
        }

        if (boardChanged(initialBoard, newBoardRows)) {
            CoroutineScope(Dispatchers.Main).launch {
                // Trigger sliding animations
                tiles = movementInfo

                // Delay to allow sliding animations to finish
                delay(300)

                // Apply merge results and update board state
                for (i in 0 until boardSize) {
                    for (j in 0 until boardSize) {
                        board[i][j] = newBoardRows[i][j]
                    }
                }

                // Trigger merge animations
                tiles = movementInfo

                // Delay for merge animations to complete
                delay(200)

                // Add new tile after animations
                val newTilePos = addNewTile()
                if (newTilePos != null) {
                    val newTile = TileMovement(
                        id = generateUniqueId(),
                        value = board[newTilePos.first][newTilePos.second],
                        oldX = newTilePos.first,
                        oldY = newTilePos.second,
                        newX = newTilePos.first,
                        newY = newTilePos.second,
                        isNew = true,
                        isMerged = false
                    )
                    // Update tiles to include the new tile
                    tiles = movementInfo + newTile
                    logBoardState(board)
                } else {
                    // If no new tile was added, still update the tiles to trigger recomposition
                    tiles = movementInfo.toList()
                }
            }
        }
    }



    fun moveUp() {
        val initialBoard = board.map { it.copyOf() }.toTypedArray()
        val movementInfo = mutableListOf<TileMovement>()
        val newBoardColumns = Array(boardSize) { Array(boardSize) { 0 } }

        for (j in 0 until boardSize) {
            val column = Array(boardSize) { i -> board[i][j] }
            val processedResult = processColumnUp(column)

            // Collect movements without updating the board yet
            for (i in 0 until boardSize) {
                val newValue = processedResult.newColumn[i]
                newBoardColumns[i][j] = newValue

                if (newValue != 0) {
                    val oldX = processedResult.positions[i] // Original row index
                    movementInfo.add(
                        TileMovement(
                            id = generateUniqueId(),
                            value = newValue,
                            oldX = oldX,
                            oldY = j,
                            newX = i,
                            newY = j,
                            isNew = false,
                            isMerged = processedResult.isMerged[i]
                        )
                    )
                }
            }
        }

        if (boardChanged(initialBoard, newBoardColumns)) {
            CoroutineScope(Dispatchers.Main).launch {
                // Trigger sliding animations
                tiles = movementInfo

                // Delay to allow sliding animations to finish
                delay(300)

                // Apply merge results and update board state
                for (i in 0 until boardSize) {
                    for (j in 0 until boardSize) {
                        board[i][j] = newBoardColumns[i][j]
                    }
                }

                // Trigger merge animations
                tiles = movementInfo

                // Delay for merge animations to complete
                delay(200)

                // Add new tile after animations
                val newTilePos = addNewTile()
                if (newTilePos != null) {
                    val newTile = TileMovement(
                        id = generateUniqueId(),
                        value = board[newTilePos.first][newTilePos.second],
                        oldX = newTilePos.first,
                        oldY = newTilePos.second,
                        newX = newTilePos.first,
                        newY = newTilePos.second,
                        isNew = true,
                        isMerged = false
                    )
                    // Update tiles to include the new tile
                    tiles = movementInfo + newTile
                    logBoardState(board)
                } else {
                    // If no new tile was added, still update the tiles to trigger recomposition
                    tiles = movementInfo.toList()
                }
            }
        }
    }

    fun moveDown() {
        val initialBoard = board.map { it.copyOf() }.toTypedArray()
        val movementInfo = mutableListOf<TileMovement>()
        val newBoardColumns = Array(boardSize) { Array(boardSize) { 0 } }

        for (j in 0 until boardSize) {
            val column = Array(boardSize) { i -> board[i][j] }
            val processedResult = processColumnDown(column)

            // Collect movements without updating the board yet
            for (i in 0 until boardSize) {
                val newValue = processedResult.newColumn[i]
                newBoardColumns[i][j] = newValue

                if (newValue != 0) {
                    val oldX = processedResult.positions[i] // Original row index
                    movementInfo.add(
                        TileMovement(
                            id = generateUniqueId(),
                            value = newValue,
                            oldX = oldX,
                            oldY = j,
                            newX = i,
                            newY = j,
                            isNew = false,
                            isMerged = processedResult.isMerged[i]
                        )
                    )
                }
            }
        }

        if (boardChanged(initialBoard, newBoardColumns)) {
            CoroutineScope(Dispatchers.Main).launch {
                // Trigger sliding animations
                tiles = movementInfo

                // Delay to allow sliding animations to finish
                delay(300)

                // Apply merge results and update board state
                for (i in 0 until boardSize) {
                    for (j in 0 until boardSize) {
                        board[i][j] = newBoardColumns[i][j]
                    }
                }

                // Trigger merge animations
                tiles = movementInfo

                // Delay for merge animations to complete
                delay(200)

                // Add new tile after animations
                val newTilePos = addNewTile()
                if (newTilePos != null) {
                    val newTile = TileMovement(
                        id = generateUniqueId(),
                        value = board[newTilePos.first][newTilePos.second],
                        oldX = newTilePos.first,
                        oldY = newTilePos.second,
                        newX = newTilePos.first,
                        newY = newTilePos.second,
                        isNew = true,
                        isMerged = false
                    )
                    // Update tiles to include the new tile
                    tiles = movementInfo + newTile
                    logBoardState(board)
                } else {
                    // If no new tile was added, still update the tiles to trigger recomposition
                    tiles = movementInfo.toList()
                }
            }
        }
    }


    private fun processColumnUp(column: Array<Int>): ProcessedResult {
        val size = column.size
        val newColumn = IntArray(size) { 0 }
        val positions = IntArray(size) { -1 }
        val isMerged = BooleanArray(size) { false }
        val isNew = BooleanArray(size) { false }

        var lastValue = 0
        var lastIndex = -1
        var insertPos = 0

        for (i in 0 until size) {
            val currentValue = column[i]
            if (currentValue != 0) {
                if (lastValue == currentValue) {
                    // Merge tiles
                    val mergedValue = lastValue * 2
                    newColumn[insertPos - 1] = mergedValue
                    isMerged[insertPos - 1] = true
                    positions[insertPos - 1] = lastIndex // Original index of the merged tile
                    lastValue = 0
                    lastIndex = -1
                } else {
                    // Move tile up
                    newColumn[insertPos] = currentValue
                    positions[insertPos] = i // Map new position to original index
                    insertPos += 1
                    lastValue = currentValue
                    lastIndex = i
                }
            }
        }

        return ProcessedResult(
            newColumn = newColumn.toTypedArray(),
            positions = positions.toTypedArray(),
            isNew = isNew.toTypedArray(),
            isMerged = isMerged.toTypedArray()
        )
    }


    private fun processColumnDown(column: Array<Int>): ProcessedResult {
        val size = column.size
        val newColumn = IntArray(size) { 0 }
        val positions = IntArray(size) { -1 }
        val isMerged = BooleanArray(size) { false }
        val isNew = BooleanArray(size) { false }

        var lastValue = 0
        var lastIndex = -1
        var insertPos = size - 1

        for (i in size - 1 downTo 0) {
            val currentValue = column[i]
            if (currentValue != 0) {
                if (lastValue == currentValue) {
                    // Merge tiles
                    val mergedValue = lastValue * 2
                    newColumn[insertPos + 1] = mergedValue
                    isMerged[insertPos + 1] = true
                    positions[insertPos + 1] = lastIndex // Original index of the merged tile
                    lastValue = 0
                    lastIndex = -1
                } else {
                    // Move tile down
                    newColumn[insertPos] = currentValue
                    positions[insertPos] = i // Map new position to original index
                    insertPos -= 1
                    lastValue = currentValue
                    lastIndex = i
                }
            }
        }

        return ProcessedResult(
            newColumn = newColumn.toTypedArray(),
            positions = positions.toTypedArray(),
            isNew = isNew.toTypedArray(),
            isMerged = isMerged.toTypedArray()
        )
    }


    private fun processRowLeft(row: Array<Int>): ProcessedResult {
        val size = row.size
        val newRow = IntArray(size) { 0 }
        val positions = IntArray(size) { -1 }
        val isMerged = BooleanArray(size) { false }
        val isNew = BooleanArray(size) { false }

        var lastValue = 0
        var lastIndex = -1
        var insertPos = 0

        for (j in 0 until size) {
            val currentValue = row[j]
            if (currentValue != 0) {
                if (lastValue == currentValue) {
                    // Merge tiles
                    val mergedValue = lastValue * 2
                    newRow[insertPos - 1] = mergedValue
                    isMerged[insertPos - 1] = true
                    positions[insertPos - 1] = lastIndex // Original index of the merged tile
                    lastValue = 0
                    lastIndex = -1
                } else {
                    // Move tile to the left
                    newRow[insertPos] = currentValue
                    positions[insertPos] = j // Map new position to original index
                    insertPos += 1
                    lastValue = currentValue
                    lastIndex = j
                }
            }
        }

        // Fill the rest of newRow with zeros (already initialized with zeros)

        return ProcessedResult(
            newColumn = newRow.toTypedArray(),
            positions = positions.toTypedArray(),
            isNew = isNew.toTypedArray(),
            isMerged = isMerged.toTypedArray()
        )
    }


    private fun processRowRight(row: Array<Int>): ProcessedResult {
        val size = row.size
        val newRow = IntArray(size) { 0 }
        val positions = IntArray(size) { -1 }
        val isMerged = BooleanArray(size) { false }
        val isNew = BooleanArray(size) { false }

        var lastValue = 0
        var lastIndex = -1
        var insertPos = size - 1

        for (j in size - 1 downTo 0) {
            val currentValue = row[j]
            if (currentValue != 0) {
                if (lastValue == currentValue) {
                    // Merge tiles
                    val mergedValue = lastValue * 2
                    newRow[insertPos + 1] = mergedValue
                    isMerged[insertPos + 1] = true
                    positions[insertPos + 1] = lastIndex // Original index of the merged tile
                    lastValue = 0
                    lastIndex = -1
                } else {
                    // Move tile to the right
                    newRow[insertPos] = currentValue
                    positions[insertPos] = j // Map new position to original index
                    insertPos -= 1
                    lastValue = currentValue
                    lastIndex = j
                }
            }
        }

        return ProcessedResult(
            newColumn = newRow.toTypedArray(),
            positions = positions.toTypedArray(),
            isNew = isNew.toTypedArray(),
            isMerged = isMerged.toTypedArray()
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


