package pl.wsei.pam.lab03

import android.os.Bundle
import android.view.Gravity
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pl.wsei.pam.lab01.R
import java.util.Timer
import kotlin.concurrent.schedule

class Lab03Activity : AppCompatActivity() {

    lateinit var mBoard: GridLayout
    private lateinit var mBoardModel: MemoryBoardView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab03)

        mBoard = findViewById(R.id.gridLayout)
        val rows = intent.getIntExtra("rows", 3)
        val columns = intent.getIntExtra("columns", 3)
        mBoard.columnCount = columns
        mBoard.rowCount = rows

        if (savedInstanceState != null) {
            val flatState = savedInstanceState.getIntegerArrayList("boardStateFull") ?: arrayListOf()
            val state = flatState.chunked(2).map { Pair(it[0], it[1] == 1) }
            mBoardModel = MemoryBoardView(mBoard, columns, rows)
            mBoardModel.setStateFull(state)
        } else {
            mBoardModel = MemoryBoardView(mBoard, columns, rows)
        }

        // ustaw listener zdarzeń gry
        mBoardModel.setOnGameChangeListener { e ->
            when (e.state) {
                GameStates.Matching -> e.tiles.forEach { it.revealed = true }
                GameStates.Match -> e.tiles.forEach { it.revealed = true }
                GameStates.NoMatch -> {
                    e.tiles.forEach { it.revealed = true }
                    Timer().schedule(1000) {
                        runOnUiThread {
                            e.tiles.forEach { it.revealed = false }
                        }
                    }
                }
                GameStates.Finished -> {
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

