package com.meizu.mstore.sdk.demo

import android.annotation.SuppressLint
import android.support.v4.app.Fragment
import android.view.MenuItem
import android.widget.TextView
import com.meizu.mstore.sdk.MzAppCenterPlatform
import com.meizu.mstore.sdk.account.ILoginResultListener

open class BaseFragment : Fragment() {

    public val UNITS = arrayOf("台", "个", "只", "头", "部", "根", "本", "架", "辆", "条")


    companion object {
        const val MAX_AMOUNT = 3

        const val MAX_PER_PRICE = 0.05
        const val MIN_PER_PRICE = 0.01
    }

    fun invokeSdkToLogin(requestCode: Int, listener: ILoginResultListener) {
        activity?.let {
            MzAppCenterPlatform.getInstance()?.login(requestCode, it,
                    listener)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            activity?.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        MzAppCenterPlatform.getInstance()?.onDestroy()
    }

    @SuppressLint("SetTextI18n")
    fun logMessage(text: String, textView: TextView) {
        textView.text = text + "\n" + textView.text.toString()
    }

}