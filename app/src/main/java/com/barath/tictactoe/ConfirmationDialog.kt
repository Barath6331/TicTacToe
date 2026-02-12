package com.barath.tictactoe

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.drawable.toDrawable

object ConfirmationDialog {

    private var dialog: Dialog?= null
    
    interface ConfirmationCallback{
        fun onResetBtn()
    }

    fun showDialog(context: Context,alertMsg:String,confirmationCallback: ConfirmationCallback){
        if(dialog != null) return

        dialog =  Dialog(context, R.style.Theme_Dialog)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setGravity(Gravity.CENTER)
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        val window = dialog!!.window
        window!!.setGravity(Gravity.CENTER)
        dialog!!.window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        dialog?.setContentView(R.layout.layout_confirmation)
        dialog?.show()

        val closeBtn = dialog?.findViewById<View>(R.id.close) as ImageView
        val image = dialog?.findViewById<View>(R.id.image) as ImageView
        val textImage = dialog?.findViewById<View>(R.id.textImage) as ImageView
        val playAgainBtn = dialog?.findViewById<View>(R.id.playAgainBtn) as AppCompatButton
        val text = dialog?.findViewById<View>(R.id.text) as TextView

        if(alertMsg == "O"){
            image.setImageResource(R.drawable.o_image)
            textImage.setImageResource(R.drawable.o_wins_text)
            text.text = "Congratulations!"
        }else if(alertMsg == "X"){
            image.setImageResource(R.drawable.x_wins_image)
            textImage.setImageResource(R.drawable.x_wins_text)
            text.text = "Congratulations!"
        }else if(alertMsg == "Computer"){
            image.setImageResource(R.drawable.computer_image)
            textImage.setImageResource(R.drawable.computer_wins_text)
            text.text = "You Lose!"
        }else{
            image.setImageResource(R.drawable.tie_image)
            textImage.setImageResource(R.drawable.tie_text)
            text.text = "It's a Draw!"
        }

        closeBtn.setOnClickListener{
            dialog!!.dismiss()
            dialog = null
        }

        playAgainBtn.setOnClickListener{
            confirmationCallback.onResetBtn()
            dialog!!.dismiss()
            dialog = null
        }
    }
}