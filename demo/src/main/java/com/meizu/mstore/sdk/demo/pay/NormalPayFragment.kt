package com.meizu.mstore.sdk.demo.pay

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.meizu.mstore.sdk.MzAppCenterPlatform
import com.meizu.mstore.sdk.account.ILoginResultListener
import com.meizu.mstore.sdk.demo.BaseFragment
import com.meizu.mstore.sdk.demo.R
import com.meizu.mstore.sdk.demo.util.PointLengthFilter
import com.meizu.mstore.sdk.demo.util.getSupportActionBar
import com.meizu.mstore.sdk.demo.util.setEditable
import com.meizu.mstore.sdk.pay.IPayResultListener
import com.meizu.mstore.sdk.pay.PayInfo
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * 常规一次性支付
 */
class NormalPayFragment : BaseFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = NormalPayFragment()

        public const val TAG = "NormalPayFragment"

        private const val ACTIVITY_REQUEST_CODE_AUTH = 1216  /*别猜了，就是今天的日期而已*/

    }

    private lateinit var mProductIdView: EditText
    private lateinit var mProductNameView: EditText
    private lateinit var mProductDetailView: EditText
    private lateinit var mProductAmount: EditText
    private lateinit var mProductPrice: EditText
    private lateinit var mProductPriceTotal: EditText
    private lateinit var mProductUnit: EditText
    private lateinit var mProductCpAttach: EditText
    private lateinit var mNotifyUrl: EditText

    private lateinit var mPayButton: Button
    private lateinit var mMagicButton: Button

    private lateinit var mMessageBoard: TextView

    private val loginListener = object : ILoginResultListener {

        override fun onError(code: Int, message: String?) {
            logMessage("× 登录失败，code = [$code], message = [$message]", mMessageBoard)
            setAllBlanksEditable(true)
        }

        override fun onLoginSuccess() {
            logMessage("√ 登录成功，已经获取到 token，开始下预付单", mMessageBoard)
            invokeSdkToPay()
        }

    }

    private fun assertNotBlanks() = !TextUtils.isEmpty(mProductIdView.text)
            && !TextUtils.isEmpty(mProductNameView.text)
            && !TextUtils.isEmpty(mProductDetailView.text)
            && !TextUtils.isEmpty(mProductAmount.text)
            && !TextUtils.isEmpty(mProductPrice.text)
            && !TextUtils.isEmpty(mProductPriceTotal.text)
            && !TextUtils.isEmpty(mProductUnit.text)
            && !TextUtils.isEmpty(mNotifyUrl.text)

    private fun setAllBlanksEditable(editable: Boolean) {
        mProductIdView.setEditable(editable)
        mProductNameView.setEditable(editable)
        mProductDetailView.setEditable(editable)
        mProductAmount.setEditable(editable)
        mProductPrice.setEditable(editable)
        mProductPriceTotal.setEditable(editable)
        mProductUnit.setEditable(editable)
        mProductCpAttach.setEditable(editable)
        mNotifyUrl.setEditable(editable)
    }

    private fun sumUp() {
        try {
            mProductPriceTotal.setText(DecimalFormat("0.00")
                    .format(mProductPrice.text.toString().toDouble()
                            * mProductAmount.text.toString().toInt()))
        } catch (e: Exception) {
        }
        //改变了价格之后，订单号也要变
        mProductIdView.setText(System.currentTimeMillis().toString())
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_normal_pay, container,false)
    }

    @SuppressLint("SetTextI18n", "NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getSupportActionBar()?.title = "魅族联运SDK(Ver.${com.meizu.mstore.sdk.BuildConfig.VERSION_NAME}) Demo"

        mProductIdView = view.findViewById<EditText>(R.id.et_product_id)
        mProductNameView = view.findViewById<EditText>(R.id.et_product_name)
        mProductDetailView = view.findViewById<EditText>(R.id.et_product_detail)
        mProductAmount = view.findViewById<EditText>(R.id.et_product_amount)

        mProductAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (TextUtils.isEmpty(s)) return
                sumUp()
            }

        })

        mProductPriceTotal = view.findViewById<EditText>(R.id.et_product_total)
        mProductPriceTotal.filters = arrayOf(PointLengthFilter())

        mProductPrice = view.findViewById<EditText>(R.id.et_product_price)
        mProductPrice.filters = arrayOf(PointLengthFilter())
        mProductPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (TextUtils.isEmpty(s)) return
                sumUp()
            }

        })
        mProductUnit = view.findViewById(R.id.et_product_unit)
        mProductCpAttach = view.findViewById(R.id.et_cp_attach)
        mMessageBoard = view.findViewById(R.id.tv_message_board)
        mNotifyUrl = view.findViewById(R.id.et_notify_url)

        mMagicButton = view.findViewById<Button>(R.id.btn_god_mode)
        mMagicButton.setOnClickListener {
            val random = Random()
            val price = DecimalFormat("0.01").format(MIN_PER_PRICE + (MAX_PER_PRICE - MIN_PER_PRICE)
                    * random.nextDouble())
            val amount = ThreadLocalRandom.current().nextInt(1, MAX_AMOUNT + 1)
            mProductIdView.setText(System.currentTimeMillis().toString())

            val d = Date()
            mProductNameView.setText("购买于${
                SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒",
                    Locale.CHINA).format(d)}的商品")
            mProductDetailView.setText("购买于${SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒",
                    Locale.CHINA).format(d)}的商品详情")

            mProductAmount.setText(amount.toString())
            mNotifyUrl.setText("http://developer.manage.meizu.com/console/UNION/bill/company/notify?id=5")
            mProductPrice.setText(price.toString())
            mProductPriceTotal.setText(DecimalFormat("0.00").format(price.toDouble() * amount))
            mProductUnit.setText(UNITS[ThreadLocalRandom.current().nextInt(0, UNITS.size - 1)])
        }

        mPayButton = view.findViewById<Button>(R.id.btn_pay)
        mPayButton.setOnClickListener {
            if (assertNotBlanks()) {
                invokeSdkToLogin(ACTIVITY_REQUEST_CODE_AUTH, loginListener)
            } else {
                Toast.makeText(activity, "请完整填写上述信息", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACTIVITY_REQUEST_CODE_AUTH) {
            if (MzAppCenterPlatform.getInstance()?.onActivityResult(requestCode, resultCode, data) == true) {
                //用户成功授权，再次尝试获取 token
                invokeSdkToLogin(ACTIVITY_REQUEST_CODE_AUTH, loginListener)
            } else {
                logMessage("× Flyme 账户授权失败，用户不同意，你说气不气？", mMessageBoard)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    private fun invokeSdkToPay() {
        mPayButton.isEnabled = false
        mMagicButton.isEnabled = false
        setAllBlanksEditable(false)
        val payInfoBuilder = PayInfo.Builder(System.currentTimeMillis(), mProductIdView.text.toString(),
                mProductIdView.text.toString(), mProductNameView.text.toString(),
                mProductDetailView.text.toString(), mProductUnit.text.toString(),
                mProductAmount.text.toString().toInt(), mProductPrice.text.toString().toDouble(),
                mProductPriceTotal.text.toString().toDouble(), mNotifyUrl.text.toString()).apply {
            if (!TextUtils.isEmpty(mProductCpAttach.text.toString())) {
                setAttach(mProductCpAttach.text.toString())
            }
        }
        activity?.let { act ->
            MzAppCenterPlatform.getInstance()?.payV2(act, payInfoBuilder.build(),
                    object : IPayResultListener {
                        override fun onSuccess() {
                            mPayButton.isEnabled = true
                            mMagicButton.isEnabled = true
                            setAllBlanksEditable(true)

                            logMessage("√ IPayResultListener onSuccess, 你为什么这么叼？", mMessageBoard)
                        }

                        override fun onFailed(code: Int, message: String) {
                            mPayButton.isEnabled = true
                            mMagicButton.isEnabled = true
                            setAllBlanksEditable(true)

                            logMessage("× IPayResultListener onFailed, code = [$code], message = [$message]", mMessageBoard)
                        }
                    })

        }
    }

}