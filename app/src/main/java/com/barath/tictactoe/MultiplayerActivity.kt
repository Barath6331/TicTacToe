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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.barath.tictactoe.databinding.ActivityMultiplayerBinding

class MultiplayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMultiplayerBinding
    private val board = MutableList(9) { "" }
    private var chance = "X"
    private var gameOver = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMultiplayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false

        val buttons = listOf(
            binding.button0, binding.button1, binding.button2,
            binding.button3, binding.button4, binding.button5,
            binding.button6, binding.button7, binding.button8
        )

        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (!gameOver) playMove(index, button)
            }
        }

        binding.buttonReset.setOnClickListener { recreate() }

        binding.imageViewBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }


        binding.verticalLine1.scaleY = 0f
        binding.verticalLine2.scaleY = 0f
        binding.horizontalLine1.scaleX = 0f
        binding.horizontalLine2.scaleX = 0f

        binding.verticalLine1.animate().scaleY(1f).setDuration(400).start()
        binding.verticalLine2.animate().scaleY(1f).setDuration(400).setStartDelay(100).start()
        binding.horizontalLine1.animate().scaleX(1f).setDuration(400).setStartDelay(200).start()
        binding.horizontalLine2.animate().scaleX(1f).setDuration(400).setStartDelay(300).start()
    }

    private fun playMove(index: Int, button: Button) {
        if (board[index].isNotEmpty()) return

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

        button.isEnabled = false

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
    }

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

        val text = if (chance == "X") "X" else "O"

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
}
