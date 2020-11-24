package com.meizu.mstore.sdk.demo.util

import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity

fun Fragment.getSupportActionBar(): ActionBar? {
    if (activity is AppCompatActivity) {
        return (activity as AppCompatActivity).supportActionBar
    }
    return null
}