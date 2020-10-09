package com.fantasmaplasma.sudokusolver

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.fantasmaplasma.sudokusolver.Cell.Companion.GRID_SIZE
import com.fantasmaplasma.sudokusolver.Cell.Companion.SQRT_SIZE
import java.util.ArrayList
import kotlin.math.min

class SudokuBoardView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    // these are set in onDraw
    private var cellSizePixels = 0F
    private var noteSizePixels = 0F
    private var width = 0F
    private var height = 0F
    private var startX = 0F
    private var startY = 0F
    private lateinit var boardRectangle: List<Float>

    private var selectedRow = -1
    private var selectedCol = -1

    private var listener: OnTouchListener? = null

    private var cells: Array<Cell>? = null

    private val thickLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(getContext(), R.color.text)
        strokeWidth = 4f
    }

    private val thinLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(getContext(), R.color.text)
        strokeWidth = 1f
    }

    private val conflictingCellPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(getContext(), R.color.conflictingCellBackground)
    }

    private val greenCellPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(getContext(), R.color.conflictingValidCellBackground)
    }

    private val redCellPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(getContext(), R.color.red)
    }

    private val textPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(getContext(), R.color.text)
    }

    private val startingCellUnderlinePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(6f, 5f), 0f)
        color = ContextCompat.getColor(getContext(), R.color.text)
    }

    private val startingCellTextPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(getContext(), R.color.text)
        typeface = Typeface.DEFAULT_BOLD
    }

    private val invalidCellTextPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(getContext(), R.color.invalidCellText)
    }

    private val noteTextPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(getContext(), R.color.text)
    }

    private val startingCellPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(getContext(), R.color.selectedCellBackground)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        width = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        height = MeasureSpec.getSize(heightMeasureSpec).toFloat()

        updateMeasurements(width, height)

        val sizePixels = min(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(sizePixels, sizePixels)
    }

    override fun onDraw(canvas: Canvas) {
        fillCells(canvas)
        drawLines(canvas)
        drawText(canvas)
    }

    private fun updateMeasurements(width: Float, height: Float) {
        cellSizePixels = min(width, height) / GRID_SIZE.toFloat()

        val boardSize: Float = cellSizePixels*GRID_SIZE
        when {
            width < height -> {
                startY = (height - boardSize) / 2
                startX = 0f
            }
            height < width -> {
                startX = (width - boardSize) / 2
                startY = 0f
            }
            else -> {
                startX = 0f
                startY = 0f
            }
        }

        noteSizePixels = cellSizePixels / SQRT_SIZE.toFloat()
        noteTextPaint.textSize = (cellSizePixels / SQRT_SIZE.toFloat())
        textPaint.textSize = cellSizePixels / 1.5F
        invalidCellTextPaint.textSize = textPaint.textSize
        startingCellTextPaint.textSize = cellSizePixels / 1.5F

        boardRectangle = listOf(
            startX+thickLinePaint.strokeWidth/2,
            startY+thickLinePaint.strokeWidth/2,
            startX+boardSize-thickLinePaint.strokeWidth/2,
            startY+boardSize-thickLinePaint.strokeWidth/2
        )
    }

    private fun fillCells(canvas: Canvas) {
        cells?.forEach { cell ->
            val r = cell.row
            val c = cell.col
            if(cell.isGreen != null) {
                val paintToUse =
                    when (cell.isGreen) {
                        true -> greenCellPaint
                        else -> redCellPaint
                    }
                fillCell(canvas, r, c, paintToUse)
            } else if(selectedRow != -1 && selectedCol != -1) {
                if (r == selectedRow && c == selectedCol) {
                    fillCell(canvas, r, c, startingCellPaint)
                    drawSelectedSquare(canvas, r, c)
                } else if (r == selectedRow || c == selectedCol ||
                    (r / SQRT_SIZE == selectedRow / SQRT_SIZE && c / SQRT_SIZE == selectedCol / SQRT_SIZE)) {
                    fillCell(canvas, r, c, conflictingCellPaint)
                }
            }
        }
    }

    private fun fillCell(canvas: Canvas, r: Int, c: Int, paint: Paint) {
        canvas.drawRect(
            startX + c * cellSizePixels,
            startY + r * cellSizePixels,
            startX + (c + 1) * cellSizePixels,
            startY + (r + 1) * cellSizePixels,
            paint
        )
    }

    private fun drawSelectedSquare(canvas: Canvas, r: Int, c: Int) {
        val path = Path()
        path.moveTo(
            startX + c * cellSizePixels + cellSizePixels*.1f,
            startY + (r + 1) * cellSizePixels - cellSizePixels*.1f
        )
        path.quadTo(
            startX + c * cellSizePixels + cellSizePixels*.1f,
            startY + (r + 1) * cellSizePixels - cellSizePixels*.1f,
            startX + (c + 1) * cellSizePixels - cellSizePixels*.1f,
            startY + (r + 1) * cellSizePixels - cellSizePixels*.1f
        )
        canvas.drawPath(path, startingCellUnderlinePaint)
    }

    private fun drawLines(canvas: Canvas) {
        canvas.drawRect(boardRectangle[0], boardRectangle[1], boardRectangle[2], boardRectangle[3], thickLinePaint)

        for (i in 1 until GRID_SIZE) {
            val paintToUse = when (i % SQRT_SIZE) {
                0 -> thickLinePaint
                else -> thinLinePaint
            }

            val columnX = startX + i * cellSizePixels
            canvas.drawLine(
                columnX,
                startY,
                columnX,
                height-startY,
                paintToUse
            )

            val rowY = startY + i * cellSizePixels
            canvas.drawLine(
                startX,
                rowY,
                width-startX,
                rowY,
                paintToUse
            )
        }
    }

    private fun drawText(canvas: Canvas) {
        cells?.forEach { cell ->
            val value = cell.value
            if (value != 0) {
                val row = cell.row
                val col = cell.col
                val valueString = cell.value.toString()

                val paintToUse = if (cell.isBold) startingCellTextPaint else if(cell.conflictingCells == 0) textPaint else invalidCellTextPaint
                val textBounds = Rect()
                paintToUse.getTextBounds(valueString, 0, valueString.length, textBounds)
                val textWidth = paintToUse.measureText(valueString)
                val textHeight = textBounds.height()
                canvas.drawText(valueString, startX+(col * cellSizePixels) + cellSizePixels / 2 - textWidth / 2,
                    startY+(row * cellSizePixels) + cellSizePixels / 2 + textHeight / 2, paintToUse)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return handleTouchEvent(event.x, event.y)
            }
            else -> false
        }
    }

    private fun handleTouchEvent(x: Float, y: Float) : Boolean {
        if (inGameBounds(x,y)) {
            val selectedRow = ((y - startY) / cellSizePixels).toInt()
            val selectedCol = ((x - startX) / cellSizePixels).toInt()
            if(this.selectedCol == selectedCol && this.selectedRow == selectedRow) {
                nothingSelected()
                contentDescription = "Cell unselected."
                invalidate()
                return true
            }
            this.selectedRow = selectedRow
            this.selectedCol = selectedCol
            listener?.onCellTouched(selectedRow, selectedCol)
            contentDescription = "Selected value ${getCell(selectedRow, selectedCol)?.value} from row $selectedRow, column $selectedCol."
            invalidate()
        } else {
            nothingSelected()
            contentDescription = "Nothing selected."
            invalidate()
            return false
        }
        return true
    }

    private fun nothingSelected() {
        selectedRow = -1
        selectedCol = -1
        listener?.onCellTouched(
            selectedRow,
            selectedCol
        )
    }

    private fun getCell(selectedRow: Int, selectedCol: Int): Cell?
            = cells?.get(selectedRow * GRID_SIZE + selectedCol)

    private fun inGameBounds(x: Float, y: Float) : Boolean
            = startX < x && width-startX > x
            && startY < y && height-startY > y

    fun updateCells(cells: Array<Cell>) {
        this.cells = cells
        invalidate()
    }

    fun registerListener(listener: OnTouchListener) {
        this.listener = listener
    }

    interface OnTouchListener {
        fun onCellTouched(row: Int, col: Int)
    }
}
