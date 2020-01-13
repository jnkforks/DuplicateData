package com.sudoajay.duplication_data.customDialog

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.dpro.widgets.WeekdaysPicker
import com.sudoajay.duplication_data.R
import com.sudoajay.duplication_data.databaseClasses.BackgroundTimerDataBase
import com.sudoajay.duplication_data.foregroundService.ForegroundDialog
import com.sudoajay.duplication_data.helperClass.CustomToast
import com.sudoajay.duplication_data.sharedPreferences.BackgroundProcess
import com.sudoajay.duplication_data.sharedPreferences.TraceBackgroundService
import com.sudoajay.duplication_data.sharedPreferences.TraceBackgroundService.Companion.nextDate
import org.angmarch.views.NiceSpinner
import java.text.SimpleDateFormat
import java.util.*

class CustomDialogForBackgroundTimer : DialogFragment(), OnItemSelectedListener, View.OnClickListener {
    private var repeatedlySpinner: NiceSpinner? = null
    private var weekdaysPicker: WeekdaysPicker? = null
    private var endlesslyEditText: EditText? = null
    private var getSelectedEndlesslyDate: String? = null
    private var backgroundTimerDataBase: BackgroundTimerDataBase? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.activity_background_timer, container, false)
        reference(rootView)
        // configure and setup custom spinner
        setCustomRepeatSpinner()
        if (!backgroundTimerDataBase!!.checkForEmpty()) {
            val cursor = backgroundTimerDataBase!!.getTheValueFromId()
            cursor.moveToFirst()
            //                chooseSpinner.setSelectedIndex(cursor.getInt(1));
            repeatedlySpinner!!.selectedIndex = cursor.getInt(1)
            if (cursor.getInt(2) == 3) {
                fillTheSelectedWeekdays(cursor.getString(2))
                weekdaysPicker!!.visibility = View.VISIBLE
            }
            try {
                val calendar = Calendar.getInstance()
                 val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                simpleDateFormat.calendar = calendar
                if (cursor.getString(3).equals(simpleDateFormat.format(calendar.time), ignoreCase = true)) {
                    endlesslyEditText!!.setText(resources.getText(R.string.today_Date))
                } else {
                    endlesslyEditText!!.setText(cursor.getString(3))
                }
            } catch (e: Exception) {
                CustomToast.toastIt(context, "Something Wrong")
            }
        }
        // setup choose_ImageView
//        setupChooseImageView(chooseSpinner.getSelectedIndex());
        this.isCancelable = true
        return rootView
    }

    // reference the object
    private fun reference(view: View) { // global variable
        val cancelButton = view.findViewById<Button>(R.id.cancelButton)
        val okButton = view.findViewById<Button>(R.id.okButton)
        repeatedlySpinner = view.findViewById(R.id.repeatedlySpinner)
        weekdaysPicker = view.findViewById(R.id.weekdaysPicker)
        endlesslyEditText = view.findViewById(R.id.endlesslyEditText)
        // local variable
        val backImageViewChange = view.findViewById<ImageView>(R.id.back_Image_View_Change)
        val repeatOffImageView = view.findViewById<ImageView>(R.id.repeat_Off_Image_View)
        backgroundTimerDataBase = BackgroundTimerDataBase(context)
        // default weekdays
        val c = Calendar.getInstance()
        weekdaysPicker!!.selectDay(c[Calendar.DAY_OF_WEEK])
        // setup for listener
        okButton.setOnClickListener(this)
        cancelButton.setOnClickListener(this)
        backImageViewChange.setOnClickListener(this)
        repeatOffImageView.setOnClickListener(this)
        endlesslyEditText!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.okButton -> {
                // check if no process selected
                if (repeatedlySpinner!!.selectedIndex != 0) { // checking for foreground Service
                    checkingAndSetting()
                    //Save To Database
                    saveToDatabase()
                    // save into Trace
                    traceTheData()
                } else { // clear all The Data realated Background Process
                    clearAll()
                }
                dismiss()
            }
            R.id.cancelButton, R.id.back_Image_View_Change -> dismiss()
            R.id.repeat_Off_Image_View, R.id.endlesslyEditText ->  // Get Current Date
                getEndlesslyDate()
        }
    }

    // spinner for choose
// set spinner list for repeat
    private fun setCustomRepeatSpinner() {
        val repeatArray: List<String> = LinkedList(listOf(*resources.getStringArray(R.array.customWeekdaysSetup)))
        repeatedlySpinner!!.attachDataSource(repeatArray)
        // custom listener with
        repeatedlySpinner!!.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                if (position == 4) {
                    weekdaysPicker!!.visibility = View.VISIBLE
                } else {
                    weekdaysPicker!!.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }

    @SuppressLint("SetTextI18n")
    private fun getEndlesslyDate() {
        val c = Calendar.getInstance()
        val cYear = c[Calendar.YEAR]
        val cMonth = c[Calendar.MONTH]
        val cDay = c[Calendar.DAY_OF_MONTH]
        val datePickerDialog: DatePickerDialog
        // Theme
        val theme1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.R.style.Theme_Material_Light_Dialog
        } else {
            R.style.AppTheme
        }
        datePickerDialog = DatePickerDialog(this.context!!, theme1,
                OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    endlesslyEditText!!.setText(dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
                    val calendar = Calendar.getInstance()
                    calendar[year, monthOfYear] = dayOfMonth
                    val getEndlesslyDate = SimpleDateFormat("dd-MM-yyyy",Locale.getDefault())
                    getSelectedEndlesslyDate = getEndlesslyDate.format(calendar.time)
                    // check for today date
                    try {
                        val calendars = Calendar.getInstance()
                         val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        simpleDateFormat.calendar = calendars
                        if (getSelectedEndlesslyDate.equals(simpleDateFormat.format(calendars.time), ignoreCase = true)) {
                            endlesslyEditText!!.setText(resources.getText(R.string.today_Date))
                        } else {
                            endlesslyEditText!!.setText(getSelectedEndlesslyDate)
                        }
                    } catch (e: Exception) {
                        CustomToast.toastIt(context, "Something Wrong")
                    }
                    // if date already done then show the user
                    val splitDate = getSelectedEndlesslyDate!!.split("-").toTypedArray()
                    if (splitDate[2].toInt() < c[Calendar.YEAR] ||
                            splitDate[2].toInt() <= c[Calendar.YEAR] && splitDate[1].toInt() < c[Calendar.MONTH]
                            || splitDate[2].toInt() <= c[Calendar.YEAR] && splitDate[1].toInt() <= c[Calendar.MONTH] && splitDate[0].toInt() < c[Calendar.DAY_OF_MONTH])
                        CustomToast.toastIt(context, "Oops... The Date You selected is Already gone")
                    else { // print the endlessly_Edit_Text text
                        CustomToast.toastIt(context, endlesslyEditText!!.text.toString())
                    }
                }, cYear, cMonth, cDay)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            datePickerDialog.setIcon(R.drawable.check_icon)
            datePickerDialog.setTitle("Please select Date.")
        }
        datePickerDialog.show()
    }

    private fun traceTheData() {
        var hour = 12
        when (repeatedlySpinner!!.selectedIndex) {
            0 -> hour = 12
            1 -> hour = 24
            2 ->  // At Every 2 Day
                hour = 24 * 2
            3 -> {
                val calendar = Calendar.getInstance()
                val currentDay = calendar[Calendar.DAY_OF_WEEK]
                val weekdays = getRepeat()
                val listWeekdays: MutableList<Int> = ArrayList()
                var i = 0
                while (i < weekdays.length) {
                    listWeekdays.add(Character.getNumericValue(weekdays[i]))
                    i++
                }
                hour = 24 * countDay(currentDay, listWeekdays)
            }
            4 -> hour = 24 * 30
        }
        // send the data to Trace Background Service
        val traceBackgroundService = TraceBackgroundService(this.context!!)
        traceBackgroundService.taskB = nextDate(hour)
    }

    private fun saveToDatabase() { // save to Database
        if (getSelectedEndlesslyDate == null) getSelectedEndlesslyDate = "No Date Fixed"
        if (backgroundTimerDataBase!!.checkForEmpty()) {
            backgroundTimerDataBase!!.fillIt(repeatedlySpinner!!.selectedIndex, getRepeat(), getSelectedEndlesslyDate)
        } else {
            backgroundTimerDataBase!!.updateTheTable("1", repeatedlySpinner!!.selectedIndex, getRepeat(), getSelectedEndlesslyDate)
        }
        CustomToast.toastIt(context, resources.getText(R.string.setting_Updated).toString())
    }

    // week days selected
    private fun fillTheSelectedWeekdays(week: String) {
        val split = week.split("").toTypedArray()
        val list: MutableList<Int> = ArrayList()
        for (weeks_Days in split) {
            try {
                list.add(weeks_Days.toInt())
            } catch (ignored: Exception) {
            }
        }
        weekdaysPicker!!.selectedDays = list
    }

    private fun getRepeat(): String {
        if (repeatedlySpinner!!.selectedIndex == 3) {
            val weekday = weekdaysPicker!!.selectedDays
            val join = StringBuilder()
            for (week in weekday) {
                join.append(week)
            }
            return join.toString()
        }
        return "No Weekday"
    }

    override fun onStart() { // This MUST be called first! Otherwise the view tweaking will not be present in the displayed Dialog (most likely overriden)
        super.onStart()
        forceWrapContent(this.view)
    }

    private fun forceWrapContent(v: View?) { // Start with the provided view
        var current = v
        val dm = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(dm)
        val width = dm.widthPixels
        // Travel up the tree until fail, modifying the LayoutParams
        do { // Get the parent
            val parent = current!!.parent
            // Check if the parent exists
            if (parent != null) { // Get the view
                current = try {
                    parent as View
                } catch (e: ClassCastException) { // This will happen when at the top view, it cannot be cast to a View
                    break
                }
                // Modify the layout
                current.layoutParams.width = width - 10 * width / 100
            }
        } while (current!!.parent != null)
        // Request a layout to be re-done
        current!!.requestLayout()
    }



    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {}
    override fun onNothingSelected(parent: AdapterView<*>?) {}
    // only for foreground service
    private fun checkingAndSetting() {
        val traceBackgroundService = TraceBackgroundService(this.context!!)
        if (!traceBackgroundService.isBackgroundServiceWorking
                && traceBackgroundService.isForegroundServiceWorking) { // call thread and dialog to run foreground service
            val foregroundDialog = ForegroundDialog(context!!, activity!!)
            foregroundDialog.callThread()
        }
    }

    private fun clearAll() { // delete from DataBase
        val backgroundTimerDataBase = BackgroundTimerDataBase(context)
        if (!backgroundTimerDataBase.checkForEmpty()) {
            backgroundTimerDataBase.deleteData()
        }
        // Clear from shared preference
        val traceBackgroundService = TraceBackgroundService(this.context!!)
        traceBackgroundService.taskB = ""
        val backgroundProcess = BackgroundProcess(context!!)
        backgroundProcess.setTaskCDone(true)
    }

    companion object {
        private fun countDay(day: Int, week_Days: List<Int>): Int {
            var temp = day
            var count = 0
            do {
                count++
                temp++
                if (temp == 8) temp = 1
                for (week in week_Days) {
                    if (temp == week) return count
                }
            } while (temp != day)
            return 0
        }
    }
}