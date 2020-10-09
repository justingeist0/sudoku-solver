package com.fantasmaplasma.sudokusolver

import com.fantasmaplasma.sudokusolver.Cell.Companion.SQRT_SIZE

class Repository {
    private val actions: MutableList<Action> = mutableListOf()
    private var actionIdx = -1

    private var selectedRow = -1
    private var selectedCol = -1

    var undoBtnEnabled = false
        private set
    var redoBtnEnabled = false
        private set
    var deleteBtnEnabled = false
        private set

    val board: Board = Board()

    fun setBlankBoard() {
        if(board.canResetNonBoldCells()) return
        resetActions()
    }

    fun resetActions() {
        undoBtnEnabled = false
        redoBtnEnabled = false
        deleteBtnEnabled = false
        actions.clear()
        actionIdx = -1
    }

    fun handleInput(number: Int) {
        if (selectedRow == -1 || selectedCol == -1) return
        val cell = board.getCell(selectedRow, selectedCol)
        val prevValue = cell.value
        if (prevValue != number) {
            cell.value = number
            board.checkConflictingCells(cell, prevValue)
            val cellIdx = board.getIdx(selectedRow, selectedCol)
            if (actionIdx < 0 || actions[actionIdx].idx != cellIdx) {
                actionIdx++
                actions.add(actionIdx, Action(prevValue, cellIdx))
                updateButtonsEnabled()
            }
            cell.isBold = false
        } else {
            return
        }
        deleteBtnEnabled = true
    }

    fun updateSelectedCell(row: Int, col: Int) {
        selectedRow = row
        selectedCol = col

        if (row == -1 || col == -1) {
            deleteBtnEnabled = false
            return
        }

        val cell = board.getCell(selectedRow, selectedCol)
        deleteBtnEnabled =
            cell.value > 0
    }

    fun undo() {
        val undoAction = actions[actionIdx]
        val cell = board.cells[undoAction.idx]
        val prevValue = cell.value
        cell.value = undoAction.valuePosted
        undoAction.valuePosted = prevValue
        board.checkConflictingCells(cell, prevValue)
        actionIdx--
        updateButtonsEnabled()
    }

    fun redo() {
        actionIdx++
        val redoAction = actions[actionIdx]
        val cell = board.cells[redoAction.idx]
        val prevValue = cell.value
        cell.value = redoAction.valuePosted
        redoAction.valuePosted = prevValue
        board.checkConflictingCells(cell, prevValue)
        updateButtonsEnabled()
    }

    fun delete() {
        val cellIdx = board.getIdx(selectedRow, selectedCol)
        val cell = board.cells[cellIdx]
        actionIdx++
        actions.add(actionIdx, Action(cell.value, cellIdx))
        board.cells.forEach { conflictingCell ->
            val r = conflictingCell.row
            val c = conflictingCell.col
            if (r == selectedRow || c == selectedCol ||
                r / SQRT_SIZE == selectedRow / SQRT_SIZE && c / SQRT_SIZE == selectedCol / SQRT_SIZE) {
                if (conflictingCell.value == cell.value) { //Check if this delete makes less conflicting cells
                    conflictingCell.conflictingCells--
                }
            }
        }
        cell.conflictingCells = 0
        cell.value = 0
        updateButtonsEnabled()
    }

    fun solveBoard(ensureActive: () -> Unit, updateCells: () -> Unit) {
        board.checkIfBeingSolved()
        val boardValues = Array(board.cells.size) {
                i -> board.cells[i].value
        }
        Board.solve(boardValues) { idx, green ->
            ensureActive()
            board.cells[idx].apply {
                isGreen = green
                value = boardValues[idx]
                conflictingCells = 0
                isBold = false
            }
            updateCells()
        }
    }

    private fun updateButtonsEnabled() {
        undoBtnEnabled =
            actionIdx >= 0
        redoBtnEnabled =
            actionIdx < actions.size - 1
        val cell = getSelectedCell()
        deleteBtnEnabled =
            cell != null && cell.value > 0
    }

    private fun getSelectedCell(): Cell? {
        return if (selectedRow == -1 || selectedCol == -1) null
        else board.getCell(selectedRow, selectedCol)
    }

    fun resetColors() {
        board.cells.forEach {
            it.isGreen = null
        }
    }

    fun clearBoardIfFullWithNoBoldCells() {
        board.clearIfFullWithNoBoldCells()
    }

    inner class Action(var valuePosted: Int, val idx: Int)

}