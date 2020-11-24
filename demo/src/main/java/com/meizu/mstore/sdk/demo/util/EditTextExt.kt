package com.meizu.mstore.sdk.demo.util

import android.widget.EditText

fun EditText.setEditable(editable : Boolean) {
    isEnabled = editable
    isFocusable = editable
    isFocusableInTouchMode = editable
    isClickable = editable
}