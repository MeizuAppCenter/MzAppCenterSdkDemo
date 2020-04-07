package com.meizu.mstore.sdk.demo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.meizu.mstore.sdk.MzAppCenterPlatform;
import com.meizu.mstore.sdk.account.ILoginResultListener;
import com.meizu.mstore.sdk.pay.IPayResultListener;
import com.meizu.mstore.sdk.pay.PayInfo;
import com.meizu.mstore.sdk.pay.PayResult;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;

    private void invokeSdkToPay() {
        PayInfo payInfo = new PayInfo(System.currentTimeMillis(), "tradeNo" + System.currentTimeMillis(),
                "201903311234", "MacPro顶配", "此乃码农的信仰", "台",
                1, 1.00, 1.00,
                "https://api.xx.com/receive_notify", "attach字段");
        MzAppCenterPlatform.getInstance().payV2(this, payInfo, new IPayResultListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "支付成功，最终请以服务端回调为准！",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int i, String s) {
                if (i == PayResult.CODE_ERROR_USER_CANCEL) {
                    //用户取消，引导用户重新支付
                } else {
                    //见文档
                    Toast.makeText(MainActivity.this, "支付失败, code = [" + i
                            + "], message = [" + s + "]", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void invokeSdkToLogin() {
        MzAppCenterPlatform.getInstance().login(this, new ILoginResultListener() {
            @Override
            public void onError(int i, String s) {
                Toast.makeText(MainActivity.this, "登录失败, code = [" + i
                        + "], message = [" + s + "]", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoginSuccess() {
                invokeSdkToPay();
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new InstructionDialog().show(getSupportFragmentManager(), "dialog");

        Button button = findViewById(R.id.btn_pay);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invokeSdkToLogin();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (MzAppCenterPlatform.getInstance().onActivityResult(requestCode, resultCode, data)) {
            //用户成功授权，再次尝试获取 token
            invokeSdkToLogin();
        } else {
            Toast.makeText(MainActivity.this, "OAuth 授权失败，无法继续支付", Toast.LENGTH_SHORT).show();
        }
    }
}
