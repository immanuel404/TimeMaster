package com.example.timemaster

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.*

class AddEntryMActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_entry_m)

        // UI STYLING
        val clLayout = findViewById<ConstraintLayout>(R.id.layout)
        val animeDrawable = clLayout.background as AnimationDrawable
        animeDrawable.setEnterFadeDuration(2000)
        animeDrawable.setExitFadeDuration(3000)
        animeDrawable.start()


        // DECLARATIONS
        val txtDate = findViewById(R.id.lbldate2) as TextView
        val txtStart = findViewById(R.id.txtstart2) as EditText
        val txtEnd = findViewById(R.id.txtend2) as EditText
        val txtMin = findViewById(R.id.txtmin2) as EditText
        val txtMax = findViewById(R.id.txtmax2) as EditText
        val txtPay = findViewById(R.id.txtpayrate2) as EditText
        val txtTax = findViewById(R.id.txttaxrate2) as EditText
        val txtDescription = findViewById(R.id.txtdescription2) as EditText
        val btnSubmit = findViewById(R.id.btnsubmit2) as Button


        // CATEGORY->DROPDOWN OPTIONS
        val spinner = findViewById<Spinner>(R.id.dropdown3)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrCategories)
        spinner.adapter = adapter


        // DATE PICKER
        val dateTextView = findViewById<TextView>(R.id.lbldate2)
        dateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
                    dateTextView.setText(selectedDate.toString())
                }, year, month, day)
            datePickerDialog.show()
        }


        // INPUT VALIDATION
        fun isInputCorrect() : Boolean {
            var isInputCorrect = false
            if(txtStart.text.toString()!="" && txtEnd.text.toString()!="" && txtDescription.text.toString()!="" && txtMin.text.toString()!=""&& txtMax.text.toString()!="" && txtDate.text.toString()!="Click here to pick date")
            {
                // format time data
                val _startHr = txtStart.text.toString().substring(0, 2); val startHr = Integer.parseInt(_startHr)
                val _startMin = txtStart.text.toString().takeLast(2); val startMin = Integer.parseInt(_startMin)
                val _endHr = txtEnd.text.toString().substring(0, 2); val endHr = Integer.parseInt(_endHr)
                val _endMin = txtEnd.text.toString().takeLast(2); val endMin = Integer.parseInt(_endMin)

                // validate start-time & end-time
                if(txtStart.text.toString().length!=5 || txtEnd.text.toString().length!=5 || startHr>endHr || startHr>23
                    || startHr<0 || startMin>60 || startMin<0 || endHr>23 || endHr<0 || endMin>60|| endMin<0){
                    Toast.makeText(this, "Incorrect time!", Toast.LENGTH_SHORT).show()
                }
                // validate min & max goal
                else if(Integer.parseInt(txtMax.text.toString()) < Integer.parseInt(txtMin.text.toString())){
                    Toast.makeText(this, "Incorrect goal!", Toast.LENGTH_SHORT).show()
                }
                // validate tax-rate
                else if(txtTax.text.toString() != "" && Integer.parseInt(txtTax.text.toString()) >100){
                    Toast.makeText(this, "Incorrect tax-rate!", Toast.LENGTH_SHORT).show()
                }
                else {
                    isInputCorrect = true
                }
            }
            else {
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
                        "Date" to txtDate.text.toString(),
                        "Start" to txtStart.text.toString(),
                        "End" to txtEnd.text.toString(),
                        "Min" to txtMin.text.toString(),
                        "Max" to txtMax.text.toString(),
                        "Pay" to txtPay.text.toString(),
                        "Tax" to txtTax.text.toString(),
                        "Description" to txtDescription.text.toString(),
                        "Category" to spinner.getSelectedItem().toString(),
                        "ImageUrl" to imgData
                    )
                    dbRef.setValue(data)

//                        Log.d("OUTPUT",
//                            "Id " +randomId+" "+"UserId "+arrUserId[0]+" "+
//                            "Date "+txtDate.text.toString()+" "+"Start "+txtStart.text.toString()+" "+
//                            "End "+txtEnd.text.toString()+" "+"Min "+txtMin.text.toString()+" "+
//                            "Max "+txtMax.text.toString()+" "+"Pay "+txtPay.text.toString()+" "+
//                            "Tax "+txtTax.text.toString()+" "+"Description "+txtDescription.text.toString()+" "+
//                            "Category "+spinner.getSelectedItem().toString()+" "+"ImageUrl "+imgData
//                        )

                    // clear input fields
                    txtDate.setText("Click here to pick date")
                    txtStart.setText("")
                    txtEnd.setText("")
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
        imageView = findViewById(R.id.imgupload2)
        val lblPickImg = findViewById<TextView>(R.id.lblPickImg2)
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