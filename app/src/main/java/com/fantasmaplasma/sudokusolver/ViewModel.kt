package com.fantasmaplasma.sudokusolver

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*

class ViewModel : ViewModel() {
    private val repository = Repository()
    private var solveJob: Job? = null
    var delay = 100L
        set(value) {
            field = value
            activeDelay = value
        }
    private var activeDelay = 0L

    val cellsLiveData = MutableLiveData<Array<Cell>>()
    val undoBtnEnabledLiveData = MutableLiveData<Boolean>()
    val redoBtnEnabledLiveData = MutableLiveData<Boolean>()
    val deleteBtnEnabledLiveData = MutableLiveData<Boolean>()

    fun rowColumnTouched(row: Int, col: Int) {
        if(isSolving()) return
        repository.updateSelectedCell(row, col)
        deleteBtnEnabledLiveData.postValue(
            repository.deleteBtnEnabled
        )
    }

    fun handleInput(number: Int) {
        if(isSolving()) return
        repository.handleInput(number)
        updateLiveData()
    }

    fun undo() {
        if(isSolving()) return
        repository.undo()
        updateLiveData()
    }

    fun redo() {
        if(isSolving()) return
        repository.redo()
        updateLiveData()
    }

    fun delete() {
        if(isSolving()) return
        repository.delete()
        updateLiveData()
    }

    fun solveBoard() {
        if(isSolving()) return
        activeDelay = delay
        solveJob = GlobalScope.launch(Dispatchers.Default) {
            val ensureActive = {ensureActive()}
            repository.solveBoard(ensureActive) {
                cellsLiveData.postValue (
                    repository.board.cells
                )
                Thread.sleep(activeDelay)
            }
        }
        repository.resetActions()
        updateLiveData()
    }

    fun cancelSolve() : Boolean {
        if(isSolving()) {
            solveJob?.cancel()
            repository.setBlankBoard()
            updateLiveData()
            return true
        }
        return false
    }

    private fun isSolving() : Boolean {
        if(solveJob?.isActive == true)
            return true
        if(solveJob != null) {
            repository.resetColors()
            updateLiveData()
            solveJob = null
        }
        return false
    }

    fun updateLiveData() {
        undoBtnEnabledLiveData.postValue(
            repository.undoBtnEnabled
        )
        redoBtnEnabledLiveData.postValue(
            repository.redoBtnEnabled
        )
        deleteBtnEnabledLiveData.postValue(
            repository.deleteBtnEnabled
        )
        cellsLiveData.postValue(
            repository.board.cells
        )
    }

    fun restartBoard() {
        if(!cancelSolve()) {
            repository.setBlankBoard()
            updateLiveData()
        }
    }

    fun solveInstantly() {
        activeDelay =
            if (isSolving())
                0
            else {
                repository.clearBoardIfFullWithNoBoldCells()
                solveBoard()
                0
            }
    }

}