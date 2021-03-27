package com.frankenstein.screenx.ui

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.frankenstein.screenx.R

class ScreenXToast(private val context: Context) {
    private var toast: Toast? = null
    private var rootView: View = View.inflate(context, R.layout.custom_toast, null)
    private val textView: TextView by lazy {
        rootView.findViewById<TextView>(R.id.text)
    }

    fun show(msg: String, toastDuration: Int, yOffset: Int = 0) {
        textView.text = msg
        toast?.cancel()


          // This check is unnecessary , as the target SDK is downgraded from 30 to 29 now
          // TODO Enable the check once the target SDK is upgraded back to 30
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            toast = Toast(context).apply {
                setGravity(Gravity.FILL_HORIZONTAL or Gravity.BOTTOM, 0, yOffset)
                view = rootView
                duration = toastDuration
                show()
            }
//        } else {
//            toast = Toast.makeText(context,msg, toastDuration).apply { show() }
//        }
    }
}
