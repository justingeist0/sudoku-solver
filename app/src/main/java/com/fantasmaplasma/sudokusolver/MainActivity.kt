package com.fantasmaplasma.sudokusolver

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fantasmaplasma.sudokusolver.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import kotlinx.android.synthetic.main.dialog_speed_settings.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ViewModel::class.java)
        viewModel.delay = getPreferredSpeedPref()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        binding.gameLayout.registerListener(
                object: SudokuBoardView.OnTouchListener {
                override fun onCellTouched(row: Int, col: Int) {
                    viewModel.rowColumnTouched(row, col)
                }
            }
        )
        binding.viewModel = viewModel
        with(viewModel) {
            cellsLiveData.observe(this@MainActivity, Observer {
                binding.gameLayout.updateCells(it)
            })
            deleteBtnEnabledLiveData.observe(this@MainActivity, Observer {
                binding.deleteBtn.isEnabled = it
            })
            redoBtnEnabledLiveData.observe(this@MainActivity, Observer {
                binding.redoBtn.isEnabled = it
            })
            undoBtnEnabledLiveData.observe(this@MainActivity, Observer {
                binding.undoBtn.isEnabled = it
            })
        }
        binding.adView.loadAd(
            AdRequest.Builder().build()
        )
        viewModel.updateLiveData()
        setContentView(binding.root)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.reset -> {
                viewModel.restartBoard()
                true
            }
            R.id.forward -> {
                viewModel.solveInstantly()
                true
            }
            R.id.settings -> {
                openSettingsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openSettingsMenu() {
        val speedSettings = Dialog(this)
        speedSettings.setContentView(R.layout.dialog_speed_settings)
        val slider = speedSettings.slider
        val setText: (String) -> Unit = { delay ->
            speedSettings.speed_settings_tv.text =
                getString(R.string.default_solver_speed_in_milliseconds, delay)
        }
        setText(viewModel.delay.toString())
        slider.value = viewModel.delay.toFloat()
        slider.addOnChangeListener { _, value, _ ->
            val delay = value.toLong()
            viewModel.delay = delay
            setText(delay.toString())
        }
        speedSettings.setOnCancelListener {
            updatePreferredSpeedPref(viewModel.delay)
        }
        speedSettings.window?.attributes?.windowAnimations =
            R.style.dialogAnimation
        speedSettings.show()
    }

    private fun updatePreferredSpeedPref(delay: Long) {
        val prefs = getSharedPreferences(KEY_PREFERENECES, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putLong(KEY_SPEED, delay)
        editor.apply()
    }

    private fun getPreferredSpeedPref(): Long {
        val prefs = getSharedPreferences(KEY_PREFERENECES, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_SPEED, 10L)
    }

    override fun onBackPressed() {
        if(!viewModel.cancelSolve())
            super.onBackPressed()
    }

    companion object {
        const val KEY_PREFERENECES =
            "${BuildConfig.APPLICATION_ID}_KEY_PREFERENCES"
        const val KEY_SPEED =
            "${BuildConfig.APPLICATION_ID}_KEY_PREFERENCES"
    }
}