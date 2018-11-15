package com.meizu.mstore.sdk.demo

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog

class InstructionDialog : DialogFragment() {

    companion object {
        fun show(fragmentManager: FragmentManager) = InstructionDialog().show(fragmentManager,"dialog")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
                .setMessage("Demo 仅演示发起支付请求的过程，但因为签名与 appKey 系伪造，" +
                "结果肯定是 onFailed()，请在实际接入过程中替换成您真实的即可。")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    //do nothing
                }.create()
    }

}