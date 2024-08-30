package com.example.neo2048

class GameLogic {
    var score = 0
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

    fun moveLeft() {
        val initialBoard = board.map { it.copyOf() }

        for (i in 0 until boardSize) {
            val newRow = compressLeftOrUp(board[i])
            mergeLeftOrUp(newRow)
            board[i] = compressLeftOrUp(newRow)
        }
        if (boardChanged(initialBoard, board)) {
            addNewTile()
        }
    }

    fun moveRight() {
        val initialBoard = board.map { it.copyOf() }

        for (i in 0 until boardSize) {
            val newRow = compressRightOrDown(board[i])
            mergeRightOrDown(newRow)
            board[i] = compressRightOrDown(newRow)
        }
        if (boardChanged(initialBoard, board)) {
            addNewTile()
        }
    }

    fun moveUp() {
        val initialBoard = board.map { it.copyOf() }

        for (j in 0 until boardSize) {
            val column = Array(boardSize) { i -> board[i][j] }
            val newColumn = compressLeftOrUp(column)
            mergeLeftOrUp(newColumn)
            val finalColumn = compressLeftOrUp(newColumn)
            for (i in 0 until boardSize) {
                board[i][j] = finalColumn[i]
            }
        }
        if (boardChanged(initialBoard, board)) {
            addNewTile()
        }
    }

    fun moveDown() {
        val initialBoard = board.map { it.copyOf() }

        for (j in 0 until boardSize) {
            val column = Array(boardSize) { i -> board[i][j] }
            val newColumn = compressRightOrDown(column)
            mergeRightOrDown(newColumn)
            val finalColumn = compressRightOrDown(newColumn)
            for (i in 0 until boardSize) {
                board[i][j] = finalColumn[i]
            }
        }
        if (boardChanged(initialBoard, board)) {
            addNewTile()
        }
    }


    private fun compressLeftOrUp(row: Array<Int>): Array<Int> {
        val newRow = row.filter { it != 0 }.toMutableList()
        while (newRow.size < boardSize) {
            newRow.add(0)
        }
        return newRow.toTypedArray()
    }

    private fun compressRightOrDown(row: Array<Int>): Array<Int> {
        val newRow = row.filter { it != 0 }.toMutableList()
        while (newRow.size < boardSize) {
            newRow.add(0, 0)
        }
        return newRow.toTypedArray()
    }

    private fun mergeLeftOrUp(row: Array<Int>) {
        for (i in 0 until boardSize - 1) {
            if (row[i] != 0 && row[i] == row[i + 1]) {
                row[i] *= 2
                score += row[i]
                row[i + 1] = 0
            }
        }
    }

    private fun mergeRightOrDown(row: Array<Int>) {
        for (i in boardSize - 1 downTo  1) {
            if (row[i] != 0 && row[i] == row[i - 1]) {
                row[i] *= 2
                score += row[i]
                row[i - 1] = 0
            }
        }
    }
    private fun boardChanged(initialBoard: List<Array<Int>>, newBoard: Array<Array<Int>>): Boolean {
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


