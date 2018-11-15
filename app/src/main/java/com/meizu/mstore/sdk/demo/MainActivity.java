package com.meizu.mstore.sdk.demo;

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
import com.meizu.mstore.sdk.pay.IPayResultListener;
import com.meizu.mstore.sdk.pay.PayInfo;
import com.meizu.mstore.sdk.pay.PayResult;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;

    private void invokeSdkToPay() {
        PayInfo payInfo = new PayInfo(System.currentTimeMillis(), "tradeNo" + System.currentTimeMillis(), "productId",
                "productName", "productBody", "个", 1, 1.00, 1.00 ,"attach");
        MzAppCenterPlatform.Companion.getInstance().pay(this, payInfo, new IPayResultListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "onSuccess()", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int i, String s) {
                Toast.makeText(MainActivity.this, "onFailed(), code = [" + i
                        + "], message = [" + s + "]", Toast.LENGTH_SHORT).show();
                if (i == PayResult.CODE_ERROR_USER_CANCEL) {
                    //用户取消
                }/* else if (...) {
                    //见文档
                }*/
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
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        android.Manifest.permission.READ_PHONE_STATE)
                        == PackageManager.PERMISSION_GRANTED) {
                    invokeSdkToPay();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{android.Manifest.permission.READ_PHONE_STATE},
                            MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //太好了，用户终于授权了，调用 SDK 接口发起支付请求
                    invokeSdkToPay();
                } else {
                    //用户不授予“android.Manifest.permission.READ_PHONE_STATE”权限，无法完成支付
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
