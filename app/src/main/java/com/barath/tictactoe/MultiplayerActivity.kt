package com.barath.tictactoe

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.airbnb.lottie.LottieAnimationView
import com.barath.tictactoe.databinding.ActivityMultiplayerBinding
import kotlin.math.sqrt

class MultiplayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMultiplayerBinding
    private var gridSize = 3 // Default 3x3
    private lateinit var board: MutableList<String>
    private var chance = "X"
    private var gameOver = false
    private val buttons = mutableListOf<AppCompatButton>()
    private val lottieViews = mutableListOf<LottieAnimationView>()
    private var mediaPlayer: MediaPlayer? = null
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

        binding.buttonReset.setOnClickListener { resetGame() }

        binding.imageViewBack.setOnClickListener {
            finish() // Since MainActivity might be the caller
        }

        // Grid Size Selection
        binding.btn3x3.setOnClickListener { switchGridSize(3) }
        binding.btn6x6.setOnClickListener { switchGridSize(6) }
        binding.btn9x9.setOnClickListener { switchGridSize(9) }

        // Initial setup
        setupBoard(gridSize)
    }

    private fun switchGridSize(size: Int) {
        if (gridSize == size) return
        gridSize = size
        resetGame()
    }

    private fun resetGame() {
        gameOver = false
        chance = "X"
        binding.winLine.visibility = View.INVISIBLE
        binding.linesContainer.removeAllViews() // Clear old grid lines
        setupBoard(gridSize)
    }

    private fun setupBoard(size: Int) {
        gridSize = size
        board = MutableList(size * size) { "" }
        buttons.clear()
        lottieViews.clear()
        binding.gridLayout.removeAllViews()
        binding.gridLayout.columnCount = size
        binding.gridLayout.rowCount = size

        // Dynamic Cell Size Calculation
        // Assuming the board container is roughly 300dp or match_parent width with margins.
        // We'll let GridLayout handle it but for better square look, we might need fixed sizes or weights.
        // Simplified approach: using fixed dp sizes logic or layout params.

        // Calculate cell size based on screen width or fixed container size (300dp from XML)
        val density = resources.displayMetrics.density
        val totalWidthDp = 300 // The width of FrameLayout in XML
        val cellSizeDp = totalWidthDp / size
        val cellSizePx = (cellSizeDp * density).toInt()


        for (i in 0 until size * size) {
            val cellFrame = FrameLayout(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = cellSizePx
                    height = cellSizePx
                }
            }

            val button = AppCompatButton(this).apply {
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                setBackgroundResource(R.drawable.cell_bg) // Ensure this drawable exists
                setOnClickListener {
                    if (!gameOver) playMove(i, this)
                }
            }
            buttons.add(button)
            cellFrame.addView(button)

            val lottieView = LottieAnimationView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    (cellSizePx * 0.6).toInt(), // 60% of cell size
                    (cellSizePx * 0.6).toInt()
                ).apply {
                    gravity = Gravity.CENTER
                }
                visibility = View.GONE
            }
            lottieViews.add(lottieView)
            cellFrame.addView(lottieView)

            binding.gridLayout.addView(cellFrame)
        }

        // Add Grid Lines
        setupGridLines(size, cellSizePx)
    }

    private fun setupGridLines(size: Int, cellSizePx: Int) {
        binding.linesContainer.removeAllViews()

        // Vertical Lines
        for (i in 1 until size) {
            val line = View(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    (4 * resources.displayMetrics.density).toInt(), // 4dp width
                    FrameLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    leftMargin = i * cellSizePx
                    gravity = Gravity.START
                }
                setBackgroundResource(R.drawable.line1) // Ensure drawable exists
                scaleY = 0f
            }
            binding.linesContainer.addView(line)
            line.animate().scaleY(1f).setDuration(400).setStartDelay((i * 100).toLong()).start()
        }

        // Horizontal Lines
        for (i in 1 until size) {
            val line = View(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    (4 * resources.displayMetrics.density).toInt() // 4dp height
                ).apply {
                    topMargin = i * cellSizePx
                    gravity = Gravity.TOP
                }
                setBackgroundResource(R.drawable.line1)
                scaleX = 0f
            }
            binding.linesContainer.addView(line)
            line.animate().scaleX(1f).setDuration(400).setStartDelay((i * 100 + 200).toLong()).start()
        }
    }

    private fun playMove(index: Int, button: AppCompatButton) {
        if (board[index].isNotEmpty()) return

        board[index] = chance

        val animationView = lottieViews[index]
        animationView.setAnimation(if (chance == "X") "x_anim.json" else "o_anim.json")
        animationView.visibility = View.VISIBLE
        animationView.playAnimation()

        button.isEnabled = false

        if (checkWin()) {
            gameOver = true
            return
        }

        if (board.all { it.isNotEmpty() }) {
            gameOver = true
            showTieDialog()
            return
        }

        chance = if (chance == "X") "O" else "X"
    }

    private fun getWinLength(): Int {
        return when (gridSize) {
            3 -> 3
            6 -> 4
            9 -> 5
            else -> 3
        }
    }

    private fun checkWin(): Boolean {
        val winLen = getWinLength()

        // Iterate over all cells to find a winning sequence starting from that cell
        for (i in 0 until gridSize * gridSize) {
            val row = i / gridSize
            val col = i % gridSize
            val p = board[i]

            if (p.isEmpty()) continue

            // Check Horizontal (Right)
            if (col + winLen <= gridSize) {
                if (checkSequence(i, 1, winLen)) return true
            }

            // Check Vertical (Down)
            if (row + winLen <= gridSize) {
                if (checkSequence(i, gridSize, winLen)) return true
            }

            // Check Diagonal (Down-Right)
            if (col + winLen <= gridSize && row + winLen <= gridSize) {
                if (checkSequence(i, gridSize + 1, winLen)) return true
            }

            // Check Diagonal (Down-Left)
            if (col - winLen + 1 >= 0 && row + winLen <= gridSize) {
                if (checkSequence(i, gridSize - 1, winLen)) return true
            }
        }
        return false
    }

    private fun checkSequence(start: Int, step: Int, length: Int): Boolean {
        val p = board[start]
        for (k in 1 until length) {
            if (board[start + k * step] != p) return false
        }
        winEffect(start, step, length)
        return true
    }

    private fun winEffect(start: Int, step: Int, length: Int) {
        // Vibration
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(200)
            }
        }
        //mediaPlayer = MediaPlayer.create(this, R.raw.dboss_fun)
        //mediaPlayer?.start()

        // Board Shake
        binding.boardContainer.animate()
            .rotation(3f)
            .setDuration(100)
            .withEndAction {
                binding.boardContainer.animate()
                    .rotation(0f)
                    .setDuration(100)
                    .start()
            }.start()

        // Win Line Animation
        val line = binding.winLine
        line.visibility = View.VISIBLE
        line.setBackgroundResource(R.drawable.white_line)

        val density = resources.displayMetrics.density
        val totalWidthDp = 300
        val cellSizeDp = totalWidthDp / gridSize
        val cellSizePx = (cellSizeDp * density).toInt()

        // Calculate visual start and end coordinates relative to the board
        // Start center
        val startRow = start / gridSize
        val startCol = start % gridSize

        // End center (based on length-1 steps)
        val endIndex = start + (length - 1) * step
        val endRow = endIndex / gridSize
        val endCol = endIndex % gridSize

        val startX = startCol * cellSizePx + cellSizePx / 2f
        val startY = startRow * cellSizePx + cellSizePx / 2f
        val endX = endCol * cellSizePx + cellSizePx / 2f
        val endY = endRow * cellSizePx + cellSizePx / 2f

        val deltaX = endX - startX
        val deltaY = endY - startY
        val lineLen = sqrt((deltaX * deltaX + deltaY * deltaY).toDouble()).toFloat()
        val angle = Math.toDegrees(Math.atan2(deltaY.toDouble(), deltaX.toDouble())).toFloat()

        line.layoutParams.width = lineLen.toInt()
        line.requestLayout()

        line.pivotX = 0f
        line.pivotY = line.height / 2f
        line.translationX = startX
        line.translationY = startY - line.height / 2f
        line.rotation = angle

        line.scaleX = 0f
        line.animate().scaleX(1f).setDuration(400).start()

        Handler(Looper.getMainLooper()).postDelayed({
            showConfirmationDialog(if (chance == "X") "X" else "O")
        }, 1000)
    }

    private fun showTieDialog() {
        Handler(Looper.getMainLooper()).postDelayed({
            showConfirmationDialog("TIE")
        }, 800)
    }

    // Simple inline version of ConfirmationDialog if not found
    private fun showConfirmationDialog(winner: String) {
        /*
           Assuming 'ConfirmationDialog.showDialog' exists from user context.
           If not, we'd use AlertDialog.
        */
        try {
            // Reflection or direct call if we had the class.
            // Since I am writing the file, I can't see the other file.
            // I will use a simple AlertDialog fallback to be safe, or just assume the user has it.
            // Given the user prompt asked to "change this", I should probably keep using it.
            // But I don't have the import. I will try to use the class name directly if it's in the same package.
            // Ideally, I should have asked for it. I'll implement a local fallback.
            ConfirmationDialog.showDialog(this, winner, object : ConfirmationDialog.ConfirmationCallback {
                override fun onResetBtn() {
                    resetGame()
                }
            })

        } catch (e: Exception) {
            // Fallback
            AlertDialog.Builder(this)
                .setTitle(if (winner == "TIE") "It's a Tie!" else "$winner Wins!")
                .setPositiveButton("Play Again") { _, _ -> resetGame() }
                .setCancelable(false)
                .show()
        }
    }
}
