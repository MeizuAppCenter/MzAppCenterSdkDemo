package com.meizu.mstore.sdk.demo.widget

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.DatePicker
import java.util.*

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private var mYear = -1
    private var mMonth = -1
    private var mDayOfMonth = -1

    private var listener: OnDateSetListener? = null

    companion object {

        private const val ARG_YEAR = "arg_year"
        private const val ARG_MONTH = "arg_month"
        private const val ARG_DAY_OF_MONTH = "arg_day_of_month"

        @JvmStatic
        fun newInstance() = newInstance(-1, -1,-1)

        @JvmStatic
        fun newInstance(year: Int, month: Int, dayOfMonth: Int) = DatePickerFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_YEAR, year)
                putInt(ARG_MONTH, month)
                putInt(ARG_DAY_OF_MONTH, dayOfMonth)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mYear = arguments?.getInt(ARG_YEAR, -1)?:-1
        mMonth = arguments?.getInt(ARG_MONTH, -1)?:-1
        mDayOfMonth = arguments?.getInt(ARG_DAY_OF_MONTH, -1)?:-1
        activity?.let {
            // Use the current date as the default date in the picker
            val c = Calendar.getInstance()
            if (mYear == -1) {
                mYear = c.get(Calendar.YEAR)
            }
            if (mMonth == -1) {
                mMonth = c.get(Calendar.MONTH)
            }
            if (mDayOfMonth == -1) {
                mDayOfMonth = c.get(Calendar.DAY_OF_MONTH)
            }

            // Create a new instance of DatePickerDialog and return it
            return DatePickerDialog(it, this, mYear, mMonth, mDayOfMonth)
        }
        return super.onCreateDialog(savedInstanceState)
    }

    public fun setOnDateSetListener(listener: OnDateSetListener) {
        this.listener = listener
    }

    interface OnDateSetListener {
        fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        // Do something with the date chosen by the user
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE) + 2
        val second = 0
        listener?.onDateSet(view, year, month, day, hour, minute, second)
    }
}