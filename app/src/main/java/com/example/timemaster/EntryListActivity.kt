package com.example.timemaster

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Double
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class EntryListActivity : AppCompatActivity() {
    private lateinit var barchart: BarChart
    private lateinit var minEntry: ArrayList<BarEntry>
    private lateinit var maxEntry: ArrayList<BarEntry>
    private lateinit var spentEntry: ArrayList<BarEntry>

    private lateinit var newRecyclerView: RecyclerView
    private lateinit var newArrayList: ArrayList<uEntry>
    private lateinit var progressBar: ProgressBar

    private lateinit var totalHrSpent: TextView

    override fun onRestart() {
        super.onRestart()
        displayEntries()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_list)

        // UI STYLING
        val clLayout = findViewById<ConstraintLayout>(R.id.layout)
        val animeDrawable = clLayout.background as AnimationDrawable
        animeDrawable.setEnterFadeDuration(2000)
        animeDrawable.setExitFadeDuration(3000)
        animeDrawable.start()


        // DECLARATIONS
        val startDate = findViewById(R.id.datePick1) as TextView
        val endDate = findViewById(R.id.datePick2) as TextView
        totalHrSpent = findViewById(R.id.lblHourSpent) as TextView
        // progress bar
        progressBar = findViewById<ProgressBar>(R.id.progressBar2)
        progressBar.visibility = View.VISIBLE
        // entry & barchart -> data
        newArrayList = arrayListOf<uEntry>()
        minEntry = arrayListOf<BarEntry>()
        maxEntry = arrayListOf<BarEntry>()
        spentEntry = arrayListOf<BarEntry>()
        // fetch data
        displayEntries()


        // CATEGORY->DROPDOWN OPTIONS
        val spinner = findViewById<Spinner>(R.id.dropdown1)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrCategories)
        spinner.adapter = adapter


        // DATE PICKERS
        val dateTextView1 = findViewById<TextView>(R.id.datePick1)
        val dateTextView2 = findViewById<TextView>(R.id.datePick2)
        dateTextView1.setOnClickListener{
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
                    dateTextView1.setText(selectedDate.toString())
                }, year, month, day)
            datePickerDialog.show()
        }
        dateTextView2.setOnClickListener{
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
                    dateTextView2.setText(selectedDate.toString())
                }, year, month, day)
            datePickerDialog.show()
        }


        // RECYCLERVIEW SETUP
        newRecyclerView = findViewById(R.id.recyclerEntries)
        newRecyclerView.layoutManager = LinearLayoutManager(this)
        newRecyclerView.setHasFixedSize(true)


        // ONCLICK->REFRESH DATA
        val btnRefresh = findViewById(R.id.iconRefresh) as ImageView
        btnRefresh.setOnClickListener()
        {
            totalHrSpent.setText("")
            displayEntries();
        }


        // ONCLICK->FILTER DATA
        val btnFilter = findViewById(R.id.btnViewEntry) as Button
        btnFilter.setOnClickListener()
        {
            val date1 = startDate.text.toString()
            val date2 = endDate.text.toString()
            val _category = spinner.getSelectedItem().toString()
            totalHrSpent.setText("")

            // required input entered?
            if(_category!="" && date1!="Date 1" && date2!="Date 2") {
                arrFilterStart.clear();arrFilterEnd.clear();arrFilterMin.clear();arrFilterMax.clear()
                newArrayList.clear()

                // format dates
                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val _startDate = format.parse(date1)
                val _endDate = format.parse(date2)

                // filter data
                var i = 0
                while(i < arrStart.size) {
                    var i_date = format.parse(arrDate[i])
                    // by: category & date
                    if (_category == arrCategory[i] && i_date.compareTo(_startDate)>=0 && i_date.compareTo(_endDate)<=0) {
                        // filtered data->array
                        arrFilterStart.add(arrStart[i]);arrFilterEnd.add(arrEnd[i]);arrFilterMin.add(arrMin[i]);arrFilterMax.add(arrMax[i])
                        // filtered data->obj array
                        val entryItm = uEntry(arrId[i],arrDate[i],arrStart[i],arrEnd[i],arrMin[i],arrMax[i],arrPay[i],arrTax[i],arrDescription[i],arrCategory[i],arrImage[i])
                        newArrayList.add(entryItm)
                    }
                    ++i
                }

                // render recyclerview layout
                var adapter1 = uEntryAdapter(newArrayList)
                newRecyclerView.adapter = adapter1
                adapter1.setOnClickListener(object : uEntryAdapter.onItemClickListener{
                    override fun onItemClick(position: Int) {
                        val i = Intent(this@EntryListActivity, EntryActivity::class.java)
                        i.putExtra("_id", newArrayList[position].Id.toString())
                        startActivity(i)
                    }
                })
                getFilteredBarChart() // display chart
                calculateHourSpent() // display hours spent
            }
        }
    }


    // SHOW ALL ENTRIES
    private fun displayEntries() {
        // empty arrays
        arrId.clear();arrDate.clear();arrStart.clear();arrEnd.clear();arrMin.clear();arrMax.clear()
        arrPay.clear();arrTax.clear();arrDescription.clear();arrCategory.clear();arrImage.clear()
        newArrayList.clear()

        // get data from fb-realtime-db
        val rootRef = FirebaseDatabase.getInstance().reference
        val entryRef = rootRef.child("uEntry").child(arrUserId[0])
        val eventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children) {
                    val entryObj: uEntry? = data.getValue(uEntry::class.java)
                    if (entryObj != null) {

                        // add category into data array
                        arrId.add(entryObj.Id.toString())
                        arrDate.add(entryObj.Date.toString())
                        arrStart.add(entryObj.Start.toString())
                        arrEnd.add(entryObj.End.toString())
                        arrMin.add(entryObj.Min.toString())
                        arrMax.add(entryObj.Max.toString())
                        arrPay.add(entryObj.Pay.toString())
                        arrTax.add(entryObj.Tax.toString())
                        arrDescription.add(entryObj.Description.toString())
                        arrCategory.add(entryObj.Category.toString())
                        arrImage.add(entryObj.ImageUrl.toString())
                        // Log.d("OUTPUT", entryObj.ImageUrl+" "+entryObj.Id)

                        // store data in array
                        val objEntry = uEntry(entryObj.Id,entryObj.Date,entryObj.Start,entryObj.End,entryObj.Min,entryObj.Max,entryObj.Pay,entryObj.Tax,entryObj.Description,entryObj.Category,entryObj.ImageUrl);
                        newArrayList.add(objEntry)
                        progressBar.visibility= View.GONE // remove loader

                        // send data to adapter
                        var adapter = uEntryAdapter(newArrayList)
                        newRecyclerView.adapter = adapter
                        adapter.setOnClickListener(object : uEntryAdapter.onItemClickListener{
                            override fun onItemClick(position: Int) {
                                // navigate to timesheet-entry-page
                                val i = Intent(this@EntryListActivity, EntryActivity::class.java)
                                i.putExtra("_id", newArrayList[position].Id)
                                startActivity(i)
                            }
                        })
                        getBarChart() // display chart
                        calculateHourSpent() // display hours spent
                    }
                }
                progressBar.visibility= View.GONE
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("OUTPUT", databaseError.message)
            }
        }
        entryRef.addListenerForSingleValueEvent(eventListener)
    }


    // SHOW BAR_CHART
    private fun getBarChart() {
        if (arrStart.size>0 && arrStart.size==db_Count) {
            // clear array
            minEntry.clear();maxEntry.clear();spentEntry.clear();
            // ui specifications
            barchart = findViewById(R.id.chart)
            barchart.animateY(1000)
            barchart.setPinchZoom(true)
            barchart.setDrawValueAboveBar(true)
            barchart.setMaxVisibleValueCount(31)
            barchart.setDrawGridBackground(false)
            barchart!!.xAxis.axisMaximum = arrStart.size.toFloat()
            barchart.description.isEnabled = false

            var count = 0
            while (count < arrStart.size) {
                arrMin[count]?.let { BarEntry(count.toFloat(), it.toFloat()) }?.let { minEntry.add(it) }
                arrMax[count]?.let { BarEntry(count.toFloat(), it.toFloat()) }?.let { maxEntry.add(it) }
                // calc hour worked
                val startTime = arrStart[count]
                val _startHr = startTime.substring(0, 2);val hr = Double.parseDouble(_startHr)
                val _startMin = startTime.takeLast(2);val min = Double.parseDouble(_startMin)
                val endTime = arrEnd[count]
                val _endHr = endTime.substring(0, 2);val hr2 = Double.parseDouble(_endHr)
                val _endMin = endTime.takeLast(2);val min2 = Double.parseDouble(_endMin)
                // get hours worked
                val totalTime = (hr2 - hr) + ((min2 - min) / 60)
                val hrsWorked = (totalTime * 10.0).roundToInt() / 10.0
                hrsWorked?.let { BarEntry(count.toFloat(), it.toFloat()) }?.let { spentEntry.add(it) }
                count++
            }
            // set data
            val dataSet1 = BarDataSet(minEntry, "Min");dataSet1.color = Color.WHITE
            val dataSet2 = BarDataSet(maxEntry, "Max");dataSet2.color = Color.DKGRAY
            val dataSet3 = BarDataSet(spentEntry, "Hrs Spent");dataSet3.color = Color.BLUE
            val data = BarData(dataSet1, dataSet2, dataSet3)
            barchart.data = data
            // bar grouping
            val groupSpace = 0.4f;val barSpace = 0.0f;val barWidth = 0.2f;data.barWidth = barWidth
            barchart.groupBars(0.0f, groupSpace, barSpace)
            barchart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        }
    }


    // BARCHART->UPON FILTER
    private fun getFilteredBarChart() {
        if (arrFilterStart.size>0) {
            // clear array
            minEntry.clear();maxEntry.clear();spentEntry.clear();
            // ui specifications
            barchart = findViewById(R.id.chart)
            barchart.animateY(1000)
            barchart.setPinchZoom(true)
            barchart.setDrawValueAboveBar(true)
            barchart.setMaxVisibleValueCount(31)
            barchart.setDrawGridBackground(false)
            barchart!!.xAxis.axisMaximum = arrFilterStart.size.toFloat()
            barchart.description.isEnabled = false

            var count = 0
            while (count < arrFilterStart.size) {
                arrFilterMin[count]?.let { BarEntry(count.toFloat(), it.toFloat()) }?.let { minEntry.add(it) }
                arrFilterMax[count]?.let { BarEntry(count.toFloat(), it.toFloat()) }?.let { maxEntry.add(it) }
                // calc hour worked
                val startTime = arrFilterStart[count]
                val _startHr = startTime.substring(0, 2);val hr = Double.parseDouble(_startHr)
                val _startMin = startTime.takeLast(2);val min = Double.parseDouble(_startMin)
                val endTime = arrFilterEnd[count]
                val _endHr = endTime.substring(0, 2);val hr2 = Double.parseDouble(_endHr)
                val _endMin = endTime.takeLast(2);val min2 = Double.parseDouble(_endMin)
                // get hours worked
                val totalTime = (hr2 - hr) + ((min2 - min) / 60)
                val hrsWorked = (totalTime * 10.0).roundToInt() / 10.0
                hrsWorked?.let { BarEntry(count.toFloat(), it.toFloat()) }?.let { spentEntry.add(it) }
                // Log.d("OUTPUT", hrsWorked.toString()+" "+spentEntry.size+" "+ spentEntry.toString())
                count++
            }
            // set data
            val dataSet1 = BarDataSet(minEntry, "Min");dataSet1.color = Color.WHITE
            val dataSet2 = BarDataSet(maxEntry, "Max");dataSet2.color = Color.DKGRAY
            val dataSet3 = BarDataSet(spentEntry, "Hrs Spent");dataSet3.color = Color.BLUE
            val data = BarData(dataSet1, dataSet2, dataSet3)
            barchart.data = data
            // bar grouping
            val groupSpace = 0.4f;val barSpace = 0.0f;val barWidth = 0.2f;data.barWidth = barWidth
            barchart.groupBars(0.0f, groupSpace, barSpace)
            barchart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        }
    }


    // CALCULATE TOTAL HOURS-SPENT
    private fun calculateHourSpent() {
        var totalHrs = 0.00;
        var i = 0

        while(i<newArrayList.size) {
            // get data
            val startTime = newArrayList[i].Start
            val endTime = newArrayList[i].End
            // format data
            val _startHr = startTime.toString().substring(0, 2); val startHr = Double.parseDouble(_startHr)
            val _startMin = startTime.toString().takeLast(2); val startMin = Double.parseDouble(_startMin)
            val _endHr = endTime.toString().substring(0, 2); val endHr = Double.parseDouble(_endHr)
            val _endMin = endTime.toString().takeLast(2); val endMin = Double.parseDouble(_endMin)
            // calculate
            totalHrs += (endHr-startHr)+((endMin-startMin)/60)
            ++i
        }
        val totalSpentHrs = Math.round(totalHrs * 10.0) / 10.0
        totalHrSpent.text = "Total Hours Spent: "+totalSpentHrs.toString()
    }
}