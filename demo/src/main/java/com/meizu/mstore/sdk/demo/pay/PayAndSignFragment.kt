package com.meizu.mstore.sdk.demo.pay

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import com.meizu.mstore.sdk.MzAppCenterPlatform
import com.meizu.mstore.sdk.account.ILoginResultListener
import com.meizu.mstore.sdk.demo.BaseFragment
import com.meizu.mstore.sdk.demo.R
import com.meizu.mstore.sdk.demo.util.PointLengthFilter
import com.meizu.mstore.sdk.demo.util.setEditable
import com.meizu.mstore.sdk.demo.widget.DatePickerFragment
import com.meizu.mstore.sdk.payandsign.*
import kotlinx.android.synthetic.main.fragment_sign_pay.*
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * 周期签约型支付
 */
class PayAndSignFragment : BaseFragment() {

    private var datePickerFragment: DatePickerFragment? = null

    private var mLastRequestCpTradeNo : String = ""

    private val loginListener = object : ILoginResultListener {

        override fun onError(code: Int, message: String?) {
            logMessage("× 登录失败，code = [$code], message = [$message]", tv_message_board)
        }

        override fun onLoginSuccess() {
            logMessage("√ 登录成功，已经获取到 token，开始下预付单", tv_message_board)
            invokeSdkToPayAndSign()
        }

    }

    private fun invokeSdkToPayAndSign() {
        activity?.let {
            btn_pay.isEnabled = false
            btn_god_mode.isEnabled = false
            setAllBlanksEditable(false)
            val orderInfo = OrderInfo(et_cp_order.text.toString().also { cpTradeNo ->
                mLastRequestCpTradeNo = cpTradeNo
            }, System.currentTimeMillis())
            val productInfo = ProductInfo(et_product_id.text.toString(), et_product_name.text.toString(),
                    et_product_detail.text.toString(), et_product_unit.text.toString(),
                    et_product_price.text.toString().toBigDecimal(), et_product_amount.text.toString().toInt(),
                    et_product_total.text.toString().toBigDecimal())
            val notifyUrlInfo = NotifyUrlInfo(et_notify_url.text.toString(), et_sign_notify_url.text.toString())
            val cyclePayInfo = CyclePayInfo.Builder(et_store_name.text.toString(),
                    et_service_name.text.toString(), et_service_detail.text.toString(),
                    sceneSpinner.selectedItem as CyclePayInfo.SignScene, et_cp_sign_no.text.toString(),
                    periodSpinner.selectedItem as CyclePayInfo.PeriodType,
                    et_period.text.toString().toInt(), et_single_pay_price.text.toString().toBigDecimal(),
                    et_first_pay_time.text.toString())
            if (!TextUtils.isEmpty(et_total_pay.text.toString())) {
                cyclePayInfo.setTotalAmount(BigDecimal(et_total_pay.text.toString()))
            }
            if (!TextUtils.isEmpty(et_total_count.text.toString())) {
                cyclePayInfo.setTotalPayments(et_total_count.text.toString().toInt())
            }
            val payAndSignInfo = PayAndSignInfo(orderInfo, productInfo, notifyUrlInfo, cyclePayInfo.build())
            MzAppCenterPlatform.getInstance()?.payAndSign(it, payAndSignInfo, object : IPayAndSignResultListener {
                override fun onPaySuccess() {
                    btn_pay.isEnabled = true
                    btn_god_mode.isEnabled = true
                    setAllBlanksEditable(true)
                    logMessage("√ IPayAndSignResultListener onPaySuccess, 请以服务端回调为准",
                            tv_message_board)
                }

                override fun onFailed(code: Int, message: String) {
                    btn_pay.isEnabled = true
                    btn_god_mode.isEnabled = true
                    setAllBlanksEditable(true)

                    logMessage("× IPayAndSignResultListener onFailed, code = [$code], message = [$message]",
                            tv_message_board)
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.add(Menu.NONE, MENU_ID_CANCEL_SIGN, Menu.NONE, "解除签约")
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == MENU_ID_CANCEL_SIGN) {
            activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.container, CancelSignFragment.newInstance())
                    ?.addToBackStack("")
                    ?.commit()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_pay, container, false)
    }

    private fun sumUp() {
        try {
            val sum = DecimalFormat("0.00")
                    .format(et_product_price.text.toString().toDouble()
                            * et_product_amount.text.toString().toInt())
            et_product_total.setText(sum)
        } catch (e: Exception) {
        }
    }

    @SuppressLint("SetTextI18n", "NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let {

            val inputFilter = arrayOf(PointLengthFilter())
            et_product_price.filters = inputFilter
            et_single_pay_price.filters = inputFilter
            et_product_total.filters = inputFilter
            et_total_pay.filters = inputFilter

            val periodAdapter = ArrayAdapter<CyclePayInfo.PeriodType>(it, android.R.layout.simple_spinner_item,
                    CyclePayInfo.PeriodType.values()).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            val sceneAdapter = ArrayAdapter<CyclePayInfo.SignScene>(it, android.R.layout.simple_spinner_item,
                    CyclePayInfo.SignScene.values()).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            periodSpinner.adapter = periodAdapter
            sceneSpinner.adapter = sceneAdapter
            et_product_price.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (TextUtils.isEmpty(s)) return
                    sumUp()
                }

            })
            et_product_amount.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (TextUtils.isEmpty(s)) return
                    sumUp()
                }

            })
            btn_god_mode.setOnClickListener {
                val spf = SimpleDateFormat("yyyyMMddHHmmss")
                val date = Date()
                et_cp_sign_no.setText(spf.format(Date()))
                et_cp_order.setText(System.currentTimeMillis().toString())
                et_product_id.setText(System.currentTimeMillis().toString())
                et_product_name.setText("商店会员服务")
                et_product_detail.setText("购买于${
                    SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒",
                            Locale.CHINA).format(date)
                }的商品")
                et_product_unit.setText(UNITS[ThreadLocalRandom.current().nextInt(0, UNITS.size - 1)])
                et_product_amount.setText("1.00")
                et_period.setText("1")
                et_store_name.setText("深圳市魅族应用商店有限公司")
                et_service_name.setText("周期扣款服务标题")
                et_service_detail.setText("周期扣款服务详情描述")
                et_notify_url.setText("http://developer.manage.meizu.com/console/UNION/bill/company/notify?id=5")
                et_sign_notify_url.setText("http://developer.manage.meizu.com/console/UNION/bill/company/notify?id=5")

                val dotOnePrice = BigDecimal(0.01)
                val amount = 1
                et_product_price.setText(DecimalFormat("0.00").format(dotOnePrice))
                et_product_amount.setText(amount.toString())
                et_single_pay_price.setText("1.00")
                val sceneArray = CyclePayInfo.SignScene.values()
                sceneSpinner.setSelection(Random().nextInt(sceneArray.size))

            }
            invalidateDatePickerDialog()
            et_first_pay_time.apply {
//                inputType = InputType.TYPE_NULL
                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        invalidateDatePickerDialog()
                        datePickerFragment?.show(fragmentManager, "datePicker")
                    } else {
                        datePickerFragment?.dismiss()
                    }
                }
            }
            btn_pay.setOnClickListener {
                if (assertNotBlanks()) {
                    if (!TextUtils.isEmpty(mLastRequestCpTradeNo)
                            && TextUtils.equals(et_cp_order.text.toString(), mLastRequestCpTradeNo)) {
                        Toast.makeText(activity, "前后两次请求的 CP 订单号不允许一致！", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    invokeSdkToLogin(ACTIVITY_REQUEST_CODE_AUTH, loginListener)
                } else {
                    Toast.makeText(activity, "请完整填写上述信息", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun invalidateDatePickerDialog() {
        datePickerFragment = if (TextUtils.isEmpty(et_first_pay_time.text.toString())) {
            DatePickerFragment.newInstance()
        } else {
            val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(et_first_pay_time.text.toString())
            DatePickerFragment.newInstance(parsedDate.year + 1900, parsedDate.month, parsedDate.day)
        }
        datePickerFragment?.setOnDateSetListener(object : DatePickerFragment.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int,
                                   hour: Int, minute: Int, second: Int) {
                val s = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
                et_first_pay_time.setText(s.format(Date(year - 1900, month, day, hour, minute, second)))
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACTIVITY_REQUEST_CODE_AUTH) {
            if (MzAppCenterPlatform.getInstance()?.onActivityResult(requestCode, resultCode, data) == true) {
                //用户成功授权，再次尝试获取 token
                invokeSdkToLogin(ACTIVITY_REQUEST_CODE_AUTH, loginListener)
            } else {
                logMessage("× Flyme 账户授权失败，用户不同意，你说气不气？", tv_message_board)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun assertNotBlanks() = !TextUtils.isEmpty(et_cp_order.text)
            && !TextUtils.isEmpty(et_product_id.text)
            && !TextUtils.isEmpty(et_product_name.text)
            && !TextUtils.isEmpty(et_product_detail.text)
            && !TextUtils.isEmpty(et_product_price.text)
            && !TextUtils.isEmpty(et_product_unit.text)
            && !TextUtils.isEmpty(et_product_id.text)
            && !TextUtils.isEmpty(et_period.text)
            && !TextUtils.isEmpty(et_product_amount.text)
            && !TextUtils.isEmpty(et_store_name.text)
            && !TextUtils.isEmpty(et_service_name.text)
            && !TextUtils.isEmpty(et_service_detail.text)
            && !TextUtils.isEmpty(et_first_pay_time.text)
            && !TextUtils.isEmpty(et_product_total.text)
            && !TextUtils.isEmpty(et_notify_url.text)
            && !TextUtils.isEmpty(et_single_pay_price.text)
            && !TextUtils.isEmpty(et_sign_notify_url.text)

    private fun setAllBlanksEditable(editable: Boolean) {
        et_product_id.setEditable(editable)
        et_product_name.setEditable(editable)
        et_product_detail.setEditable(editable)
        et_product_price.setEditable(editable)
        et_product_unit.setEditable(editable)
        et_product_id.setEditable(editable)
        et_period.setEditable(editable)
        et_product_amount.setEditable(editable)
        et_service_name.setEditable(editable)
        et_service_detail.setEditable(editable)
        et_first_pay_time.setEditable(editable)
        et_product_total.setEditable(editable)
        et_notify_url.setEditable(editable)
        et_sign_notify_url.setEditable(editable)
        et_single_pay_price.setEditable(editable)
        et_total_pay.setEditable(editable)
        et_total_count.setEditable(editable)
    }

    companion object {

        public const val TAG = "SignPayFragment"

        private const val ACTIVITY_REQUEST_CODE_AUTH = 929  /*别猜了，就是今天的日期而已*/
        private const val MENU_ID_CANCEL_SIGN = 0X10086  /*别猜了，就是今天的日期而已*/

        @JvmStatic
        fun newInstance() =
                PayAndSignFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }
}