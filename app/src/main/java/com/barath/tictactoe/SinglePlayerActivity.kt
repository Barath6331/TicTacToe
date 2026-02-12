package com.barath.tictactoe

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.barath.tictactoe.databinding.ActivitySinglePlayerBinding

class SinglePlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySinglePlayerBinding
    private val board = MutableList(9) { "" }

    private var chance = "X"
    private var gameOver = false

    private val playerSymbol = "X"
    private val computerSymbol = "O"

    private lateinit var buttons: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySinglePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        buttons = listOf(
            binding.button0, binding.button1, binding.button2,
            binding.button3, binding.button4, binding.button5,
            binding.button6, binding.button7, binding.button8
        )

        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (!gameOver && board[index].isEmpty() && chance == playerSymbol) {
                    playMove(index)
                }
            }
        }

        binding.buttonReset.setOnClickListener { recreate() }

        binding.imageViewBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    // ==========================
    // MAIN MOVE
    // ==========================

    private fun playMove(index: Int) {

        board[index] = chance

        val lottieViews = listOf(
            binding.lottie0, binding.lottie1, binding.lottie2,
            binding.lottie3, binding.lottie4, binding.lottie5,
            binding.lottie6, binding.lottie7, binding.lottie8
        )

        val animationView = lottieViews[index]
        animationView.setAnimation(
            if (chance == "X") "x_anim.json" else "o_anim.json"
        )
        animationView.visibility = View.VISIBLE
        animationView.playAnimation()

        buttons[index].isEnabled = false

        val winPattern = getWinPattern(chance)
        if (winPattern != null) {
            gameOver = true
            winEffect(winPattern)
            return
        }

        if (board.all { it.isNotEmpty() }) {
            gameOver = true
            showTieDialog()
            return
        }

        chance = if (chance == "X") "O" else "X"

        if (!gameOver && chance == computerSymbol) {
            Handler(Looper.getMainLooper()).postDelayed({
                computerMove()
            }, 1000)
        }
    }

    // ==========================
    // COMPUTER AI
    // ==========================

    private fun computerMove() {
        val position = getComputerMove()
        if (position != -1) {
            playMove(position)
        }
    }

    private fun getComputerMove(): Int {

        // Win
        for (i in board.indices) {
            if (board[i].isEmpty()) {
                val copy = board.toMutableList()
                copy[i] = computerSymbol
                if (checkResult(copy, computerSymbol)) return i
            }
        }

        // Block
        for (i in board.indices) {
            if (board[i].isEmpty()) {
                val copy = board.toMutableList()
                copy[i] = playerSymbol
                if (checkResult(copy, playerSymbol)) return i
            }
        }

        // Corners
        val corners = listOf(0, 2, 6, 8).filter { board[it].isEmpty() }
        if (corners.isNotEmpty()) return corners.random()

        // Center
        if (board[4].isEmpty()) return 4

        // Sides
        val sides = listOf(1, 3, 5, 7).filter { board[it].isEmpty() }
        if (sides.isNotEmpty()) return sides.random()

        return -1
    }

    // ==========================
    // WIN LOGIC
    // ==========================

    private fun getWinPattern(p: String): IntArray? {
        val wins = arrayOf(
            intArrayOf(0, 1, 2),
            intArrayOf(3, 4, 5),
            intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6),
            intArrayOf(1, 4, 7),
            intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8),
            intArrayOf(2, 4, 6)
        )
        return wins.firstOrNull { it.all { i -> board[i] == p } }
    }

    private fun checkResult(bd: List<String>, s: String): Boolean {
        val wins = listOf(
            listOf(0,1,2), listOf(3,4,5), listOf(6,7,8),
            listOf(0,3,6), listOf(1,4,7), listOf(2,5,8),
            listOf(0,4,8), listOf(2,4,6)
        )
        return wins.any { pattern -> pattern.all { bd[it] == s } }
    }

    // ==========================
    // WIN EFFECT + POPUP
    // ==========================

    private fun winEffect(pattern: IntArray) {

        // VIBRATION (SAFE)
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        200,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(200)
            }
        }

        // BOARD SHAKE
        binding.boardLayout.animate()
            .rotation(3f)
            .setDuration(100)
            .withEndAction {
                binding.boardLayout.animate()
                    .rotation(0f)
                    .setDuration(100)
                    .start()
            }.start()

        val text = if (chance == "X") "X" else "Computer"

        Handler(Looper.getMainLooper()).postDelayed({
            ConfirmationDialog.showDialog(
                this,
                text,
                object : ConfirmationDialog.ConfirmationCallback {
                    override fun onResetBtn() {
                        recreate()
                    }
                })
        }, 1000)

        // WIN LINE
        val line = binding.winLine
        line.visibility = View.VISIBLE
        binding.winLine.setBackgroundResource(R.drawable.white_line)

        val cell = binding.button0.width.toFloat()

        when (pattern.joinToString()) {

            // ROWS
            "0, 1, 2" -> animateHorizontal(line, -cell)
            "3, 4, 5" -> animateHorizontal(line, 0f)
            "6, 7, 8" -> animateHorizontal(line, cell)

            // COLUMNS
            "0, 3, 6" -> animateVertical(line, -cell)
            "1, 4, 7" -> animateVertical(line, 0f)
            "2, 5, 8" -> animateVertical(line, cell)

            // DIAGONALS
            "0, 4, 8" -> animateDiagonal(line, 45f)
            "2, 4, 6" -> animateDiagonal(line, -45f)
        }
    }

    private fun showTieDialog() {
        Handler(Looper.getMainLooper()).postDelayed({
            ConfirmationDialog.showDialog(
                this,
                "TIE",
                object : ConfirmationDialog.ConfirmationCallback {
                    override fun onResetBtn() {
                        recreate()
                    }
                })
        }, 800)
    }

    private fun animateHorizontal(line: View, offsetY: Float) {
        line.rotation = 0f
        line.translationY = offsetY
        line.layoutParams.width = binding.boardLayout.width
        line.requestLayout()
        line.scaleX = 0f
        line.animate().scaleX(1f).setDuration(300).start()
    }

    private fun animateVertical(line: View, offsetX: Float) {
        line.rotation = 90f
        line.translationX = offsetX
        line.layoutParams.width = binding.boardLayout.height
        line.requestLayout()
        line.scaleX = 0f
        line.animate().scaleX(1f).setDuration(300).start()
    }

    private fun animateDiagonal(line: View, angle: Float) {
        line.rotation = angle
        line.layoutParams.width = binding.boardLayout.width
        line.requestLayout()
        line.scaleX = 0f
        line.animate().scaleX(1f).setDuration(350).start()
    }
}
