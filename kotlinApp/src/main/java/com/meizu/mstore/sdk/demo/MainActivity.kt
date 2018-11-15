package com.meizu.mstore.sdk.demo

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.meizu.mstore.sdk.MzAppCenterPlatform
import com.meizu.mstore.sdk.pay.IPayResultListener
import com.meizu.mstore.sdk.pay.PayInfo
import com.meizu.mstore.sdk.pay.PayResult

private const val MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1

class MainActivity : AppCompatActivity() {


    private fun invokeSdkToPay() {
        val payInfo = PayInfo(System.currentTimeMillis(), "tradeNo${System.currentTimeMillis()}",
                "productId", "productName", "productBody", "个",
                1, 1.00, 1.00, "attach")
        MzAppCenterPlatform.getInstance()?.pay(this, payInfo, object : IPayResultListener {
            override fun onFailed(code: Int, message: String) {
                showToast("onFailed(), code = [$code], message = [$message]")
                when (code) {
                    PayResult.CODE_ERROR_USER_CANCEL -> {
                        //用户取消
                    }
                    else -> {
                        //见文档
                    }
                }
            }

            override fun onSuccess() {
                showToast("onSuccess()")
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        InstructionDialog.show(supportFragmentManager)

        findViewById<Button>(R.id.btn_pay).setOnClickListener { _ ->
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                //权限正常，调用 SDK 接口发起支付请求
                invokeSdkToPay()
            } else {
                //如果没有权限，调用系统框架去请求用户授权
                ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.READ_PHONE_STATE), MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_PHONE_STATE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //太好了，用户终于授权了，调用 SDK 接口发起支付请求
                    invokeSdkToPay()
                } else {
                    //用户不授予“android.Manifest.permission.READ_PHONE_STATE”权限，无法完成支付
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

}