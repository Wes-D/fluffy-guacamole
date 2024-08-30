package com.example.neo2048

class GameLogic {
    private val boardSize = 4
    var board: Array<Array<Int>> = Array(boardSize) { Array(boardSize) { 0 } }

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

    // Add movement functions here (left, right, up, down)
}
