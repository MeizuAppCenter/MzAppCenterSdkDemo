package com.meizu.mstore.sdk.demo.util

import android.text.InputFilter
import android.text.Spanned


const val DECIMAL_DIGITS = 2

class PointLengthFilter : InputFilter {

    override fun filter(source: CharSequence, start: Int, end: Int,
                        dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        // 删除等特殊字符，直接返回
        if ("" == source.toString()) {
            return null
        }
        val dValue = dest.toString()
        val splitArray = dValue.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (splitArray.size > 1) {
            val dotValue = splitArray[1]
            val diff = dotValue.length + 1 - DECIMAL_DIGITS
            if (diff > 0) {
                return source.subSequence(start, end - diff)
            }
        }
        return null
    }
}