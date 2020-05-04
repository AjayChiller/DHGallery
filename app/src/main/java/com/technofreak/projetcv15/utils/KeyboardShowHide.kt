package com.technofreak.projetcv15.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText


fun showKeyboard(activity: Activity?) {
    if (activity == null) return
    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)

}


fun focusAndShowKeyboard(activity: Activity?, view: View) {
    if (activity == null) return
    if (view.isFocusable) {
        view.requestFocus()
    }
    if (view is EditText) {
        showKeyboard(activity)
    }
}

fun hideKeyboard(activity: Activity?) {
    if (activity == null) return
    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = activity.currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)

}
