package com.technofreak.projetcv15.utils

import android.content.Context
import android.content.Intent
import com.technofreak.projetcv15.ui.MainActivity

fun backPress(context: Context){
    val  intent = Intent(context, MainActivity::class.java)
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    context.startActivity(intent)
}