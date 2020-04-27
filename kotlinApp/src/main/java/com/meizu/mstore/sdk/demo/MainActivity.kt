package com.meizu.mstore.sdk.demo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.meizu.mstore.sdk.MzAppCenterPlatform
import com.meizu.mstore.sdk.account.ILoginResultListener
import com.meizu.mstore.sdk.pay.IPayResultListener
import com.meizu.mstore.sdk.pay.PayInfo
import com.meizu.mstore.sdk.pay.PayResult

class MainActivity : AppCompatActivity() {

    companion object {
        private const val ACTIVITY_REQUEST_CODE_AUTH = 2020
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        InstructionDialog.show(supportFragmentManager)

        findViewById<Button>(R.id.btn_pay).setOnClickListener { _ ->
            invokeSdkToLogin()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (MzAppCenterPlatform.getInstance()?.onActivityResult(requestCode, resultCode, data) == true) {
            //用户成功授权，再次尝试获取 token
            invokeSdkToLogin()
        } else {
            if (requestCode == ACTIVITY_REQUEST_CODE_AUTH) {
                showToast("OAuth 授权失败，无法继续支付")
            } else {
                //应用自己其它的处理逻辑
            }
        }
    }

    private fun invokeSdkToPay() {
        val payInfo = PayInfo(System.currentTimeMillis(), "tradeNo${System.currentTimeMillis()}",
                "201903311234", "MacPro顶配", "此乃码农的信仰", "台",
                1, 1.00, 1.00,
                "https://api.xx.com/receive_notify", "attach字段")
        MzAppCenterPlatform.getInstance()?.payV2(this, payInfo, object : IPayResultListener {
            override fun onFailed(code: Int, message: String) {
                when (code) {
                    PayResult.CODE_ERROR_USER_CANCEL -> {
                        //用户取消，引导用户重新支付
                    }
                    else -> {
                        //见文档
                        showToast("onFailed(), code = [$code], message = [$message]")
                    }
                }
            }

            override fun onSuccess() {
                showToast("支付成功，最终请以服务端回调为准！")
            }
        })
    }

    private fun invokeSdkToLogin() {
        MzAppCenterPlatform.getInstance()?.login(ACTIVITY_REQUEST_CODE_AUTH, this,
                object : ILoginResultListener {
                    override fun onError(code: Int, message: String?) {
                        showToast("登录失败，code = [$code], message = [$message]")
                    }

                    override fun onLoginSuccess() {
                        invokeSdkToPay()
                    }
                })
    }

}