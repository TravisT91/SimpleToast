package com.example.simpletoast

import android.content.Context
import android.widget.Toast

object SimpleToast{
    fun show(context: Context, message: String){
        Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
    }
}