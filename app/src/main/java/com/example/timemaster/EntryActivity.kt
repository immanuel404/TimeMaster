package com.example.timemaster

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import java.lang.Double.parseDouble
import java.lang.Integer.parseInt

class EntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        // UI STYLING
        val clLayout = findViewById<ConstraintLayout>(R.id.layout)
        val animeDrawable = clLayout.background as AnimationDrawable
        animeDrawable.setEnterFadeDuration(2000)
        animeDrawable.setExitFadeDuration(3000)
        animeDrawable.start()


        // GET DATA BY INDEX
        var id = intent.getStringExtra("_id")
        var index = 0
        var i = 0
        while(i < arrId.size){
            if(arrId[i]==id){
                index = i
            }
            ++i
        }


        // TIME-SHEET ENTRY DATA
        val date = findViewById<TextView>(R.id.lblDate)
        val start = findViewById<TextView>(R.id.lblStart)
        val end = findViewById<TextView>(R.id.lblEnd)
        val minNMax = findViewById<TextView>(R.id.lblMinMax)
        val pay = findViewById<TextView>(R.id.lblPay)
        val tax = findViewById<TextView>(R.id.lblTax)
        val description = findViewById<TextView>(R.id.lblDescription)
        val category = findViewById<TextView>(R.id.lblCategory)
        val entryImg = findViewById<ImageView>(R.id.imgEntry)
        val earnings = findViewById<TextView>(R.id.lblEarning)
        val hrsWorked = findViewById<TextView>(R.id.lblHrsWorked)


        // RENDER ENTRY DATA
        date.setText("Date: "+arrDate[index].toString())
        start.setText("Start Time: "+arrStart[index].toString())
        end.setText("End Time: "+arrEnd[index].toString())
        minNMax.setText("Min goal: "+arrMin[index].toString()+"\tMax goal: "+arrMax[index].toString())
        pay.setText("Pay Rate:  R"+arrPay[index].toString() +"/hr")
        tax.setText("Tax Rate:  "+arrTax[index].toString() +"%")
        description.setText("Description: "+arrDescription[index].toString())
        category.setText("Category: "+arrCategory[index].toString())
        // RENDER IMAGE
        if(arrImage[index]!="null") {
            Glide.with(this).load(arrImage[index]).into(entryImg)
        }


        // CALCULATE EARNINGS
        val taxation = parseInt(arrTax[index])
        val payrate = parseInt(arrPay[index])
        // format time data
        val startTime = arrStart[index]
        val _startHr = startTime.substring(0, 2); val hr = parseDouble(_startHr)
        val _startMin = startTime.takeLast(2); val min = parseDouble(_startMin)
        val endTime = arrEnd[index]
        val _endHr = endTime.substring(0, 2); val hr2 = parseDouble(_endHr)
        val _endMin = endTime.takeLast(2); val min2 = parseDouble(_endMin)
        // calculation
        val totalTime = (hr2-hr)+((min2-min)/60)
        val gross = totalTime*payrate
        val net = gross-((gross/100)*taxation)
        // hours worked & amount earned
        hrsWorked.setText("Hrs Worked: "+Math.round(totalTime * 10.0) / 10.0)
        earnings.setText("Earnings: R"+Math.round(net * 10.0) / 10.0)
        if(payrate<1){
            pay.visibility=View.GONE
            tax.visibility=View.GONE
        }


        // DELETE ENTRY
        var btnDelete= findViewById(R.id.btnDelete) as Button
        btnDelete.setOnClickListener {
            val entryId = arrId[index]
            val db = FirebaseDatabase.getInstance()
            val dbRef = db.getReference("uEntry/"+arrUserId[0]+"/"+entryId)
            // Remove data
            dbRef.removeValue().addOnSuccessListener {
                db_Count -= 1
                Toast.makeText(this, "Entry deleted!", Toast.LENGTH_SHORT).show()
                val i = Intent(this, EntryListActivity::class.java)
                startActivity(i); finish();
            }
            .addOnFailureListener { error -> Log.d("OUTPUT",error.toString()) }
        }


        // RETURN TO PREVIOUS ACTIVITY
        var btnBack = findViewById(R.id.btnBack) as Button
        btnBack.setOnClickListener {
            finish();
        }
    }
}