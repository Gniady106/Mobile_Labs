package pl.wsei.pam.lab03

import Tile
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.Toast
import pl.wsei.pam.lab01.R
import java.util.Stack
import java.util.Random

class MemoryBoardView(
    private val gridLayout: GridLayout,
    private val cols: Int,
    private val rows: Int

) {
    private val tiles: MutableMap<String, Tile> = mutableMapOf()
    private val icons: List<Int> = listOf(
        R.drawable.outline_favorite_24,
        R.drawable.outline_candle_24,
        R.drawable.outline_downhill_skiing_24,
        R.drawable.outline_cyclone_24,
        R.drawable.outline_currency_bitcoin_24,
        R.drawable.outline_android_24,
        R.drawable.outline_exercise_24,
        R.drawable.outline_planet_24,
        R.drawable.outline_helicopter_24,
        R.drawable.outline_lips_24,
        R.drawable.outline_nature_24,
        R.drawable.outline_mountain_flag_24,
        R.drawable.outline_pets_24,
        R.drawable.outline_social_leaderboard_24,
        R.drawable.outline_mode_cool_24,
        R.drawable.outline_pet_supplies_24,
        R.drawable.outline_rocket_launch_24,
        R.drawable.outline_savings_24













    )

    private val deckResource: Int = R.drawable.outline_award_star_24
    private var onGameChangeStateListener: (MemoryGameEvent) -> Unit = {}
    private val matchedPair: Stack<Tile> = Stack()
    private val logic: MemoryGameLogic = MemoryGameLogic(cols * rows / 2)
    init {
        val shuffledIcons: MutableList<Int> = mutableListOf<Int>().also {
            it.addAll(icons.subList(0, cols * rows / 2))
            it.addAll(icons.subList(0, cols * rows / 2))
            it.shuffle()
        }

        for (row in 0 until rows) {
            for (col in 0 until cols) {

                val button = ImageButton(gridLayout.context).also {

                    it.tag = "${row}x${col}"

                    val layoutParams = GridLayout.LayoutParams()

                    layoutParams.width = 0
                    layoutParams.height = 0

                    layoutParams.setGravity(Gravity.CENTER)

                    layoutParams.columnSpec = GridLayout.spec(col, 1, 1f)
                    layoutParams.rowSpec = GridLayout.spec(row, 1, 1f)

                    it.layoutParams = layoutParams

                    gridLayout.addView(it)
                }

                val icon = shuffledIcons.removeAt(0)


                addTile(button, icon)
            }
        }
    }






    private fun onClickTile(v: View) {
        val tile = tiles[v.tag] ?: return


        matchedPair.push(tile)

        val matchResult = logic.process {
            tile.tileResource
        }

        onGameChangeStateListener(MemoryGameEvent(matchedPair.toList(), matchResult))

        if (matchResult != GameStates.Matching) {
            if (matchResult == GameStates.NoMatch) {
                v.postDelayed({
                    matchedPair.forEach { it.revealed = false }
                    matchedPair.clear()
                }, 500)
            } else {
                matchedPair.clear()
            }
        }
    }
    fun getStateFull(): List<Pair<Int, Boolean>> {
        return tiles.values.map { tile -> Pair(tile.tileResource, tile.revealed) }
    }

    fun setStateFull(state: List<Pair<Int, Boolean>>) {
        val tileList = tiles.values.toList()
        for (i in state.indices) {
            val (resource, revealed) = state[i]
            val tile = tileList[i]
            tile.revealed = revealed
            tile.button.setImageResource(if (revealed) resource else deckResource)
        }
    }

    fun setOnGameChangeListener(listener: (event: MemoryGameEvent) -> Unit) {
        onGameChangeStateListener = listener
    }

    fun animatePairedButton(button: ImageButton, action: Runnable) {
        val set = AnimatorSet()
        val random = Random()
        button.pivotX = random.nextFloat() * 200f
        button.pivotY = random.nextFloat() * 200f

        val rotation = ObjectAnimator.ofFloat(button, "rotation", 1080f)
        val scallingX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 4f)
        val scallingY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 4f)
        val fade = ObjectAnimator.ofFloat(button, "alpha", 1f, 0f)

        set.startDelay = 500
        set.duration = 2000
        set.interpolator = DecelerateInterpolator()
        set.playTogether(rotation, scallingX, scallingY, fade)

        set.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                button.scaleX = 1f
                button.scaleY = 1f
                button.alpha = 0.0f
                action.run()
            }
            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
        set.start()
    }


    fun animateNoMatchButton(button: ImageButton) {
        button.pivotX = button.width / 2f
        button.pivotY = button.height / 2f

        val rotation = ObjectAnimator.ofFloat(button, "rotation", 0f, -20f, 20f, -20f, 20f, 0f)
        rotation.duration = 600
        rotation.interpolator = DecelerateInterpolator()
        rotation.start()
    }

    private fun addTile(button: ImageButton, resourceImage: Int) {
        button.setOnClickListener(::onClickTile)
        val tile = Tile(button, resourceImage, deckResource)
        tiles[button.tag.toString()] = tile
    }
}