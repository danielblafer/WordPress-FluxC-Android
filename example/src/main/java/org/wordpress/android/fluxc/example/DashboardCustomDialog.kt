package org.wordpress.android.fluxc.example

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.custom_range_dialog.*
import org.wordpress.android.fluxc.example.contract.CustomRangeContract
import org.wordpress.android.fluxc.model.StatsCustomRange
import org.wordpress.android.fluxc.store.WCStatsStore
import org.wordpress.android.fluxc.store.WCStatsStore.StatsGranularity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardCustomDialog : DialogFragment() {
    private val calendar: Calendar by lazy {
        Calendar.getInstance()
    }

    private var customRangeContract: CustomRangeContract? = null
    private val formatter = SimpleDateFormat(WCStatsStore.DATE_FORMAT_DAY, Locale.ROOT)

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        customRangeContract = targetFragment as CustomRangeContract
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.custom_range_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog_spinner.adapter = ArrayAdapter<StatsGranularity>(
                activity,
                android.R.layout.simple_spinner_dropdown_item,
                granularityList()
        )

        dateListeners(calendar, dialog_start_date, dialog_end_date)
        buttonListener()
    }

    private fun buttonListener() {
        dialog_confirm_stats.setOnClickListener {
            if (!dialog_start_date.text.isEmpty() && !dialog_end_date.text.isEmpty()) {
                val statsCustomRange = StatsCustomRange(
                        formatter.parse(dialog_start_date.text.toString()),
                        formatter.parse(dialog_end_date.text.toString()),
                        true
                )

                customRangeContract!!.userDefinedCustomRange(
                        statsCustomRange,
                        dialog_spinner.selectedItem as StatsGranularity
                )
                dismissDialog()
            } else {
                Toast.makeText(
                        activity,
                        "Please Fill all required fields",
                        Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun granularityList() = StatsGranularity.values().toMutableList()

    private fun dateListeners(
        calendar: Calendar,
        startDate: EditText,
        endDate: EditText
    ) {
        val startDatePicker = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            startDate.setText(formatter.format(calendar.time))
        }

        startDate.setOnClickListener {
            DatePickerDialog(
                    activity,
                    startDatePicker,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val endDatePicker = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            endDate.setText(formatter.format(calendar.time))
        }

        endDate.setOnClickListener {
            DatePickerDialog(
                    activity,
                    endDatePicker,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun dismissDialog() {
        this.dismiss()
    }
}
