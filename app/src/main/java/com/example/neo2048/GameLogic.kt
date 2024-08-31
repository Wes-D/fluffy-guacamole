package com.example.neo2048

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

class GameLogic {
    var score by mutableIntStateOf(0)
    private val boardSize = 4
    var board by mutableStateOf(Array(boardSize) { Array(boardSize) { 0 } })

    // This is the dummy state to force a recomposition
    var forceRecompose by mutableStateOf(false)

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
    }

    private fun addNewTile() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                if (board[i][j] == 0) emptyCells.add(Pair(i, j))
            }
        }
        if (emptyCells.isNotEmpty()) {
            val (x, y) = emptyCells.random()
            board[x][y] = if (Math.random() < 0.9) 2 else 4
        }
    }

    fun moveLeft() {
        val initialBoard = board.map { it.copyOf() }.toTypedArray()

        for (i in 0 until boardSize) {
            board[i] = processRowLeftOrUp(board[i])
        }

        if (boardChanged(initialBoard, board)) {
            logBoardState(board)
            board = board.copyOf() // Create a new array instance to trigger recomposition
            addNewTile()
        }
    }

    fun moveRight() {
        val initialBoard = board.map { it.copyOf() }.toTypedArray()

        for (i in 0 until boardSize) {
            board[i] = processRowRightOrDown(board[i])
        }

        if (boardChanged(initialBoard, board)) {
            logBoardState(board)
            board = board.copyOf()
            addNewTile()
        }
    }

    fun moveUp() {
        val initialBoard = board.map { it.copyOf() }.toTypedArray()

        for (j in 0 until boardSize) {
            val column = Array(boardSize) { i -> board[i][j] }
            val newColumn = processRowLeftOrUp(column)
            for (i in 0 until boardSize) {
                board[i][j] = newColumn[i]
            }
        }

        if (boardChanged(initialBoard, board)) {
            logBoardState(board)
            board = board.copyOf()
            addNewTile()
        }
    }

    fun moveDown() {
        val initialBoard = board.map { it.copyOf() }.toTypedArray()

        for (j in 0 until boardSize) {
            val column = Array(boardSize) { i -> board[i][j] }
            val newColumn = processRowRightOrDown(column)
            for (i in 0 until boardSize) {
                board[i][j] = newColumn[i]
            }
        }

        if (boardChanged(initialBoard, board)) {
            logBoardState(board)
            board = board.copyOf()
            addNewTile()
        }
    }

    // Combined compression and merging logic for left/up
    private fun processRowLeftOrUp(row: Array<Int>): Array<Int> {
        val newRow = row.filter { it != 0 }.toMutableList()
        for (i in 0 until newRow.size - 1) {
            if (newRow[i] == newRow[i + 1]) {
                newRow[i] *= 2
                score += newRow[i]
                newRow[i + 1] = 0
            }
        }
        val finalRow = newRow.filter { it != 0 }.toMutableList()
        while (finalRow.size < boardSize) {
            finalRow.add(0)
        }
        return finalRow.toTypedArray()
    }

    // Combined compression and merging logic for right/down
    private fun processRowRightOrDown(row: Array<Int>): Array<Int> {
        val newRow = row.filter { it != 0 }.toMutableList()
        for (i in newRow.size - 1 downTo 1) {
            if (newRow[i] == newRow[i - 1]) {
                newRow[i] *= 2
                score += newRow[i]
                newRow[i - 1] = 0
            }
        }
        val finalRow = newRow.filter { it != 0 }.toMutableList()
        while (finalRow.size < boardSize) {
            finalRow.add(0, 0)
        }
        return finalRow.toTypedArray()
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

    fun getTiles(): List<Tile> {
        val tiles = mutableListOf<Tile>()
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                val value = board[i][j]
                if (value != 0) {
                    tiles.add(Tile(value, i, j))
                }
            }
        }
        return tiles
    }
}



private fun logBoardState(board: Array<Array<Int>>) {
    board.forEach { row ->
        println(row.joinToString(" ") { it.toString() })
    }
    println("------")
}


