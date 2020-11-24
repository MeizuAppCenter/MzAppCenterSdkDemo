package com.meizu.mstore.sdk.demo.pay

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import com.meizu.mstore.sdk.MzAppCenterPlatform
import com.meizu.mstore.sdk.account.ILoginResultListener
import com.meizu.mstore.sdk.demo.BaseFragment
import com.meizu.mstore.sdk.demo.R
import com.meizu.mstore.sdk.payandsign.ICancelSignResultListener
import kotlinx.android.synthetic.main.fragment_cancel_sign.*
import kotlinx.android.synthetic.main.fragment_cancel_sign.tv_message_board
import kotlinx.android.synthetic.main.fragment_sign_pay.et_cp_sign_no

/**
 * 解除签约
 */
class CancelSignFragment : BaseFragment() {

    companion object {
        fun newInstance() = CancelSignFragment()

        private const val REQUEST_CODE_CANCEL_SIGN = 1022
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cancel_sign, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        et_cp_sign_no.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                btn_unpay?.isEnabled = !TextUtils.isEmpty(s)
            }

        })
        btn_unpay?.setOnClickListener {
            btn_unpay?.isEnabled = false
            invokeSdkToLogin(REQUEST_CODE_CANCEL_SIGN, object : ILoginResultListener {
                override fun onError(code: Int, message: String?) {
                    logMessage("× 登录失败，code = [$code], message = [$message]", tv_message_board)
                }

                override fun onLoginSuccess() {
                    invokeSdkToCancelSign()
                }

            })
        }
    }

    private fun invokeSdkToCancelSign() {
        activity?.let{
            MzAppCenterPlatform.getInstance()?.unSign(it, et_cp_sign_no.text.toString(), object : ICancelSignResultListener {
                override fun onCancelSignSuccess() {
                    btn_unpay?.isEnabled = true
                    logMessage("√ ICancelSignResultListener onSuccess, 请以服务端回调为准",
                            tv_message_board)
                }

                override fun onFailed(code: Int, message: String) {
                    btn_unpay?.isEnabled = true
                    logMessage("× ICancelSignResultListener onFailed, code = [$code], message = [$message]",
                            tv_message_board)
                }
            })
        }
    }

}