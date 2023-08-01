package com.example.timemaster

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.lang.Integer.parseInt
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class AddEntryAActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_entry_a)

        // UI STYLING
        val clLayout = findViewById<ConstraintLayout>(R.id.layout)
        val animeDrawable = clLayout.background as AnimationDrawable
        animeDrawable.setEnterFadeDuration(2000)
        animeDrawable.setExitFadeDuration(3000)
        animeDrawable.start()


        // DECLARATIONS
        var txtStart = ""
        var txtEnd = ""
        val currentDate = LocalDate.now()
        val txtDate = currentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val txtMin = findViewById(R.id.txtmin) as EditText
        val txtMax = findViewById(R.id.txtmax) as EditText
        val txtPay = findViewById(R.id.txtpayrate) as EditText
        val txtTax = findViewById(R.id.txttaxrate) as EditText
        val txtDescription = findViewById(R.id.txtdescription) as EditText
        val btnStart = findViewById(R.id.btnstart) as Button
        val btnSubmit = findViewById(R.id.btnsubmit) as Button
        // timer variables
        var isTimerStarted = false
        var onStopTimer = false
        val viewTimer = findViewById(R.id.lbltimer) as TextView


        // CATEGORY->DROPDOWN OPTIONS
        val spinner = findViewById<Spinner>(R.id.dropdown2)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrCategories)
        spinner.adapter = adapter


        // ONCLICK->START TIMER
        btnStart.setOnClickListener()
        {
            var hours = 0;
            var minutes = 0;
            var seconds = 0

            // prepare to stop timer
            if (isTimerStarted == true) {
                onStopTimer = true
            }

            // start timer
            if (isTimerStarted == false) {
                isTimerStarted = true
                btnStart.setText("Stop") // change btn-title
                btnSubmit.isEnabled = false // disable submit

                // get start-time
                var currentTime = LocalTime.now()
                var formatter = DateTimeFormatter.ofPattern("HH:mm")
                txtStart = currentTime.format(formatter).toString()

                // start the timer
                val timer = Timer()
                val task = object : TimerTask() {
                    override fun run() {
                        seconds++
                        if (onStopTimer == false && seconds == 60) {
                            seconds = 0
                            minutes++
                            if (minutes == 60) {
                                minutes = 0
                                hours++
                            }
                        }
                        // stop timer
                        if (isTimerStarted && onStopTimer) {
                            isTimerStarted = false; onStopTimer = false
                            cancel()
                        }
                        viewTimer.setText(hours.toString() + " : " + minutes.toString() + " : " + seconds.toString())
                    }
                }
                timer.scheduleAtFixedRate(task, 0, 1000)
            } else {
                // get end-time
                var currentTime = LocalTime.now()
                var formatter = DateTimeFormatter.ofPattern("HH:mm")
                txtEnd = currentTime.format(formatter).toString()
                // reset
                btnStart.setText("Start") // change btn-title
                btnSubmit.isEnabled = true // enable submit
            }
        }


        // INPUT VALIDATION
        fun isInputCorrect(): Boolean {
            var isInputCorrect = false
            if (txtStart != "" && txtEnd != "" && txtDescription.text.toString() != "" && txtMin.text.toString() != "" && txtMax.text.toString() != "")
            {
                // format time data
                val _startHr = txtStart.substring(0, 2); val startHr = parseInt(_startHr)
                val _startMin = txtStart.takeLast(2); val startMin = parseInt(_startMin)
                val _endHr = txtStart.substring(0, 2); val endHr = parseInt(_endHr)
                val _endMin = txtStart.takeLast(2); val endMin = parseInt(_endMin)

                // validate start-time & end-time
                if(txtStart.length!=5 || txtEnd.length!=5 || startHr>endHr || startHr>23
                    || startHr<0 || startMin>60 || startMin<0 || endHr>23 || endHr<0 || endMin>60|| endMin<0){
                    Toast.makeText(this, "Incorrect time!", Toast.LENGTH_SHORT).show()
                }
                // validate min & max goal
                else if (parseInt(txtMax.text.toString()) < parseInt(txtMin.text.toString())) {
                    Toast.makeText(this, "Incorrect goal!", Toast.LENGTH_SHORT).show()
                }
                // validate tax-rate
                else if (txtTax.text.toString() != "" && parseInt(txtTax.text.toString()) > 100) {
                    Toast.makeText(this, "Incorrect tax-rate!", Toast.LENGTH_SHORT).show()
                } else {
                    isInputCorrect = true
                }
            } else {
                Toast.makeText(this, "Enter required fields!", Toast.LENGTH_SHORT).show()
            }
            return isInputCorrect
        }


        // ONCLICK->ENTRY SUBMISSION
        btnSubmit.setOnClickListener()
        {
            try {
                if (isInputCorrect()) {
                    // if tax & pay-rate not entered
                    if (txtPay.text.toString() == "") {
                        txtPay.setText("0")
                    }
                    if (txtTax.text.toString() == "") {
                        txtTax.setText("0")
                    }
                    var imgData = ""
                    if(imageUri==null) {imgData="null"} else {
                        imgData=imageUri.toString()
                    }
                    // SUBMIT ENTRY
                    val db = FirebaseDatabase.getInstance()
                    var randomId = getRandomID(5)
                    val dbRef = db.getReference("uEntry/" + arrUserId[0] + "/" + randomId)
                    val data = mapOf(
                        "Id" to randomId,
                        "UserId" to arrUserId[0],
                        "Date" to txtDate.toString(),
                        "Start" to txtStart,
                        "End" to txtEnd,
                        "Min" to txtMin.text.toString(),
                        "Max" to txtMax.text.toString(),
                        "Pay" to txtPay.text.toString(),
                        "Tax" to txtTax.text.toString(),
                        "Description" to txtDescription.text.toString(),
                        "Category" to spinner.getSelectedItem().toString(),
                        "ImageUrl" to imgData
                    )
                    dbRef.setValue(data)

//                    Log.d("OUTPUT",
//                        "Id " +randomId+" "+"UserId "+arrUserId[0]+" "+
//                        "Date "+txtDate.text.toString()+" "+"Start "+txtStart+" "+
//                        "End "+txtEnd+" "+"Min "+txtMin.text.toString()+" "+
//                        "Max "+txtMax.text.toString()+" "+"Pay "+txtPay.text.toString()+" "+
//                        "Tax "+txtTax.text.toString()+" "+"Description "+txtDescription.text.toString()+" "+
//                        "Category "+spinner.getSelectedItem().toString()+" "+"ImageUrl "+imgData
//                    )

                    // clear input fields
                    txtMin.setText("")
                    txtMax.setText("")
                    txtPay.setText("")
                    txtTax.setText("")
                    txtDescription.setText("")

                    // redirect to homepage
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent); finish();
                    Toast.makeText(this, "Entry added!", Toast.LENGTH_SHORT).show()
                }
            }
            catch(ex: Exception) {
                println(ex.message)
            }
        }


        // IMAGE UPLOAD
        imageView = findViewById(R.id.imgupload)
        val lblPickImg = findViewById<TextView>(R.id.lblPickImg)
        lblPickImg.setOnClickListener()
        {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)
        }
    }

    // RENDER IMAGE
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 100) {
            val Uri = data?.data
            val bit: Bitmap= MediaStore.Images.Media.getBitmap(contentResolver, Uri)
            imageUri = getImageUriFromBitmap(applicationContext, bit!!)
            imageView.setImageURI(imageUri)
            Log.d("OUTPUT", imageUri.toString())
        }
    }
    private fun getImageUriFromBitmap(context: Context?, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes)
        val path = MediaStore.Images.Media.insertImage(context!!.contentResolver,bitmap,"File",null)
        return Uri.parse(path.toString())
    }


    // GENERATE RANDOM ID
    private fun getRandomID(length: Int) : String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZ0123456789"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }
}