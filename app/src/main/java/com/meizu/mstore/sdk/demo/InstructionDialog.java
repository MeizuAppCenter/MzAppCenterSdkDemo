package com.meizu.mstore.sdk.demo;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class InstructionDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Demo 仅演示发起支付请求的 API 调用过程，但因为签名与 appKey 系伪造，" +
                "结果肯定是 onFailed()，请在实际接入过程中替换成您真实的即可。");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing
            }
        });
        return builder.create();
    }
}
