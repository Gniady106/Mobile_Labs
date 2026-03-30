package pl.wsei.pam.lab03

import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pl.wsei.pam.lab01.R
import java.util.Timer
import kotlin.concurrent.schedule

class Lab03Activity : AppCompatActivity() {

    lateinit var mBoard: GridLayout
    private lateinit var mBoardModel: MemoryBoardView
    lateinit var completionPlayer: MediaPlayer
    lateinit var negativePLayer: MediaPlayer

    private var isSound: Boolean = true

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.board_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.board_activity_sound -> {
                if (isSound) {
                    Toast.makeText(this, "Sound turn off", Toast.LENGTH_SHORT).show()
                    item.setIcon(R.drawable.baseline_volume_off_24)
                    isSound = false
                } else {
                    Toast.makeText(this, "Sound turn on", Toast.LENGTH_SHORT).show()
                    item.setIcon(R.drawable.baseline_volume_up_24)
                    isSound = true
                }
            }
        }
        return true
    }


    protected override fun onResume() {
        super.onResume()
        completionPlayer = MediaPlayer.create(applicationContext, R.raw.completion)
        negativePLayer = MediaPlayer.create(applicationContext, R.raw.negative_guitar)
    }

    protected override fun onPause() {
        super.onPause();
        completionPlayer.release()
        negativePLayer.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab03)

        mBoard = findViewById(R.id.gridLayout)
        val rows = intent.getIntExtra("rows", 3)
        val columns = intent.getIntExtra("columns", 3)
        mBoard.columnCount = columns
        mBoard.rowCount = rows
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        if (savedInstanceState != null) {
            val flatState = savedInstanceState.getIntegerArrayList("boardStateFull") ?: arrayListOf()
            val state = flatState.chunked(2).map { Pair(it[0], it[1] == 1) }
            mBoardModel = MemoryBoardView(mBoard, columns, rows)
            mBoardModel.setStateFull(state)
        } else {
            mBoardModel = MemoryBoardView(mBoard, columns, rows)
        }

        mBoardModel.setOnGameChangeListener { e ->
            when (e.state) {
                GameStates.Matching -> {
                    e.tiles.forEach { it.revealed = true }
                }
                GameStates.Match -> {
                    e.tiles.forEach { it.revealed = true }
                    mBoard.isEnabled = false
                    e.tiles.forEach { tile ->
                        if (isSound) completionPlayer.start()
                        mBoardModel.animatePairedButton(tile.button) {
                            runOnUiThread {
                                mBoard.isEnabled = true
                            }
                        }
                    }
                }
                GameStates.NoMatch -> {
                    e.tiles.forEach { it.revealed = true }
                    e.tiles.forEach { tile ->
                        if (isSound) negativePLayer.start()
                        mBoardModel.animateNoMatchButton(tile.button)
                    }
                    Timer().schedule(1000) {
                        runOnUiThread {
                            e.tiles.forEach { it.revealed = false }
                        }
                    }
                }
                GameStates.Finished -> {
                    e.tiles.forEach { it.revealed = true }
                    e.tiles.forEach { tile ->
                        mBoardModel.animatePairedButton(tile.button) {}
                    }
                    Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val state = mBoardModel.getStateFull()
        val flatState = state.flatMap { listOf(it.first, if (it.second) 1 else 0) }
        outState.putIntegerArrayList("boardStateFull", ArrayList(flatState))
    }




}

