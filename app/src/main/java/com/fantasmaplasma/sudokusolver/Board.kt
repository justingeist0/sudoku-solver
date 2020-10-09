package com.fantasmaplasma.sudokusolver

import com.fantasmaplasma.sudokusolver.Cell.Companion.GRID_SIZE
import com.fantasmaplasma.sudokusolver.Cell.Companion.SQRT_SIZE

class Board {
    val cells = Array(GRID_SIZE*GRID_SIZE) { i ->
        Cell(i / GRID_SIZE, i % GRID_SIZE, 0)
    }

    fun newBoard() {
        cells.forEach {
            it.value = 0
            it.conflictingCells = 0
        }
    }

    fun getCell(row: Int, col: Int)
            = cells[getIdx(row, col)]

    fun getIdx(row: Int, col: Int)
            = row * GRID_SIZE + col

    fun getBoardCells() = cells

    fun checkConflictingCells(cell: Cell, prevValue: Int) {
        cell.conflictingCells = 0
        cells.forEach { conflictingCell ->
            val r = conflictingCell.row
            val c = conflictingCell.col
            if (r == cell.row || c == cell.col || r / SQRT_SIZE == cell.row / SQRT_SIZE && c / SQRT_SIZE == cell.col / SQRT_SIZE) {
                if (conflictingCell.value == cell.value) { //Will ALWAYS execute for selected cell
                    conflictingCell.conflictingCells++
                    cell.conflictingCells++
                } else if (conflictingCell.value == prevValue) {
                    conflictingCell.conflictingCells--
                }
            }
        }
        cell.conflictingCells -= 2 //Selected cell does not conflict with itself!
    }

    fun setNumber(row: Int, column: Int, value: Int) {
        getCell(row, column).value = value
    }

    fun isComplete(): Boolean {
        cells.forEach {
            if (it.value < 1 || it.conflictingCells > 0)
                return false
        }
        return true
    }

    fun checkIfBeingSolved() : Boolean {
        if(solveButtonPressedAlready()) {
            cells.forEach {
                if(!it.isBold) {
                    it.value = 0
                    it.isGreen = null
                }
            }
            return true
        } else {
            cells.forEach {
                if(it.value > 0 && it.isGreen == null)
                    it.isBold = true
                else
                    it.value = 0
            }
        }
        return false
    }

    private fun solveButtonPressedAlready(): Boolean {
        cells.forEach {
            if(it.isBold) return true
        }
        return false
    }

    fun canResetNonBoldCells(): Boolean {
        var boldCellsReset = false
        cells.forEach {
            if(it.isBold) {
                it.isBold = false
                boldCellsReset = true
            } else {
                it.value = 0
                it.isGreen = null
                it.conflictingCells = 0
            }
        }
        return boldCellsReset
    }

    fun clearIfFullWithNoBoldCells() {
        cells.forEach {
            if(it.isBold || it.value < 1) return
        }
        cells.forEach {
            it.value = 0
        }
    }

    companion object Solver {
        private val DIGITS_RANGE = 1..9

        fun solve(board: Array<Int>, start: Int = 0,
                  updateDisplay: (idx: Int, isGreen: Boolean?) -> Unit): Boolean {
            for (i in start until board.size) {
                if (board[i] == 0) {
                    val availableDigits = getAvailableDigits(i, board)
                    for (j in availableDigits) {
                        board[i] = j
                        updateDisplay(i, true)
                        if (solve(board, i + 1, updateDisplay)) {
                            return true
                        }
                        updateDisplay(i, false)
                        board[i] = 0
                        updateDisplay(i, null)
                    }
                    return false
                }
            }
            return true
        }

        private fun getAvailableDigits(idx: Int, board: Array<Int>) : Iterable<Int> {
            val availableDigits = mutableSetOf<Int>()
            availableDigits.addAll(DIGITS_RANGE)

            truncateDigitsUsedInRow(idx, availableDigits, board)

            if(availableDigits.size > 0)
                truncateDigitsUsedInColumn(idx, availableDigits, board)

            if(availableDigits.size > 0)
                truncateDigitsUsedInBox(idx, availableDigits, board)

            return availableDigits.asIterable().shuffled()
        }

        private fun truncateDigitsUsedInRow(idx: Int, availableDigits: MutableSet<Int>, board: Array<Int>) {
            val start = (idx / 9) * 9
            for (i in start until start+9) {
                if(board[i] != 0) {
                    availableDigits.remove(board[i])
                }
            }
        }

        private fun truncateDigitsUsedInColumn(idx: Int, availableDigits: MutableSet<Int>, board: Array<Int>) {
            val start = idx % 9
            for (i in start until GRID_SIZE*GRID_SIZE step(9)) {
                if(board[i] != 0) {
                    availableDigits.remove(board[i])
                }
            }
        }

        private fun truncateDigitsUsedInBox(idx: Int, availableDigits: MutableSet<Int>, board: Array<Int>) {
            val start = ((idx / 3) * 3) - ((idx / 9) % 3) * 9 //Top left index of 3x3 grid

            for (i in start until start+3) {
                for(j in 0..GRID_SIZE*2 step(GRID_SIZE)) {
                    if (board[i+j] != 0) {
                        availableDigits.remove(board[i+j])
                    }
                }
            }
        }
    }
}