package org.wordpress.android.fluxc.example

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import org.wordpress.android.fluxc.example.contract.CustomRangeContract
import org.wordpress.android.fluxc.model.StatsCustomRange
import org.wordpress.android.fluxc.network.rest.wpcom.wc.orderstats.OrderStatsRestClient
import org.wordpress.android.fluxc.network.rest.wpcom.wc.orderstats.OrderStatsRestClient.OrderStatsApiUnit
import org.wordpress.android.fluxc.store.WCStatsStore
import org.wordpress.android.fluxc.store.WCStatsStore.StatsGranularity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardCustomDialog : DialogFragment() {
    private var simpleDateFormat : SimpleDateFormat? = null
    private var calendar: Calendar? = null

    private var startDate: EditText? = null
    private var endDate: EditText? = null
    private var granularityList: Spinner? = null

    private var customRangeContract: CustomRangeContract? = null

    private val formatter = SimpleDateFormat(WCStatsStore.DATE_FORMAT_DAY, Locale.ROOT)

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        customRangeContract = targetFragment as CustomRangeContract
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        simpleDateFormat = SimpleDateFormat(WCStatsStore.DATE_FORMAT_DAY, Locale.ROOT)
        calendar = Calendar.getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.custom_range_dialog, container, false)

        startDate = view.findViewById(R.id.startDate)
        endDate = view.findViewById(R.id.endDate)
        granularityList = view.findViewById(R.id.granularitySpinner)
        val okButton = view.findViewById<Button>(R.id.confirmCustomStatsButton)

        granularityList!!.adapter = ArrayAdapter<StatsGranularity>(
                activity,
                android.R.layout.simple_spinner_dropdown_item,
                granularityList()
        )

        dateListeners(calendar!!, startDate!!, endDate!!)
        buttonListener(okButton)

        return view
    }

    fun setContract(customRangeContract: CustomRangeContract) {
        this.customRangeContract = customRangeContract
    }

    private fun buttonListener(okButton: Button) {
        okButton.setOnClickListener {
            if (!startDate!!.text.isEmpty() && !endDate!!.text.isEmpty()) {
                val statsCustomRange = StatsCustomRange(
                        formatter.parse(startDate!!.text.toString()),
                        formatter.parse(endDate!!.text.toString()),
                        OrderStatsRestClient.OrderStatsApiUnit.fromStatsGranularity(
                                (granularityList!!.selectedItem as StatsGranularity),
                                OrderStatsApiUnit.CUSTOM
                        )

                )
                customRangeContract!!.userDefinedCustomRange(statsCustomRange)
                dismissDialog()
            }else {
                Toast.makeText(
                        activity,
                        "Please Fill all required fields",
                        Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun granularityList() : MutableList<StatsGranularity> {
        val values: MutableList<StatsGranularity> = ArrayList()

        StatsGranularity.values().forEach { granularity ->
            if(granularity != StatsGranularity.CUSTOM){
                values.add(granularity)
            }
        }

        return values
    }

    private fun dateListeners(
        calendar: Calendar,
        startDate: EditText,
        endDate: EditText
    ) {
        val startDatePicker = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            startDate.setText(simpleDateFormat!!.format(calendar.time))
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
            endDate.setText(simpleDateFormat!!.format(calendar.time))
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
