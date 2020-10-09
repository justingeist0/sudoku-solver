package com.fantasmaplasma.sudokusolver

class Cell(
    val row: Int,
    val col: Int,
    var value: Int,
    var conflictingCells: Int = 0,
    var isBold: Boolean = false,
    var isGreen: Boolean? = null
) {
    companion object {
        const val GRID_SIZE = 9
        const val SQRT_SIZE = 3
    }
}