package com.example.timemaster

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddCategoryActivity : AppCompatActivity() {

    private lateinit var newRecyclerView: RecyclerView
    private lateinit var newArrayList: ArrayList<uCategory>
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category)

        // UI STYLING
        val clLayout = findViewById<ConstraintLayout>(R.id.layout)
        val animeDrawable = clLayout.background as AnimationDrawable
        animeDrawable.setEnterFadeDuration(2000)
        animeDrawable.setExitFadeDuration(3000)
        animeDrawable.start()


        // DECLARATIONS
        var txtCategory = findViewById(R.id.txtAddCategory) as EditText
        var btnAddCategory = findViewById(R.id.btnAddCategory) as Button
        newArrayList = arrayListOf<uCategory>()
        // progress bar->loader
        progressBar = findViewById<ProgressBar>(R.id.progressBar1)
        progressBar.visibility = View.VISIBLE


        // RECYCLERVIEW SETUP
        newRecyclerView = findViewById(R.id.recyclerCategories)
        newRecyclerView.layoutManager = LinearLayoutManager(this)
        newRecyclerView.setHasFixedSize(true)
        getCategories()


        // GENERATE RANDOM ID
        fun getRandomID(length: Int) : String {
            val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZ0123456789"
            return (1..length)
                .map { charset.random() }
                .joinToString("")
        }


        // ONCLICK->ADD-CATEGORY
        btnAddCategory.setOnClickListener()
        {
            if(txtCategory.text.toString() != "")
            {
                // store in realtime-db
                val randomId = getRandomID(5)
                val db = FirebaseDatabase.getInstance()
                val dbRef = db.getReference("uCategory/"+arrUserId[0]+"/"+randomId)
                val data = mapOf(
                    "Id" to randomId,
                    "Name" to txtCategory.text.toString()
                )
                dbRef.setValue(data)

                // clear input field
                txtCategory.setText("")

                // re-fetch data
                getCategories()
                Toast.makeText(this, "Category added!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // GET CATEGORIES
    private fun getCategories() {
        newArrayList.clear()
        arrCategoryId.clear()
        arrCategories.clear()

        // get data from fb-realtime-db
        val rootRef = FirebaseDatabase.getInstance().reference
        val categoryRef = rootRef.child("uCategory").child(arrUserId[0])
        val eventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children) {
                    val categoryObj: uCategory? = data.getValue(uCategory::class.java)
                    if (categoryObj != null) {
                        // add category into array
                        arrCategoryId.add(categoryObj.Id.toString())
                        arrCategories.add(categoryObj.Name.toString())
                        println("ID + Name: " + categoryObj.Name + " " + categoryObj.Name)

                        // store data in array
                        val objCategory = uCategory(categoryObj.Id,categoryObj.Name);
                        newArrayList.add(objCategory)
                        progressBar.visibility=View.GONE

                        // send data to adapter
                        var adapter = uCategoryAdapter(newArrayList)
                        newRecyclerView.adapter = adapter
                        adapter.setOnClickListener(object : uCategoryAdapter.onItemClickListener{
                            override fun onItemClick(position: Int) {
                                deleteCategory(position)
                            }
                        })
                    }
                }
                progressBar.visibility=View.GONE
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("OUTPUT", databaseError.message)
            }
        }
        categoryRef.addListenerForSingleValueEvent(eventListener)
    }


    // DELETE CATEGORY
    private fun deleteCategory(itmPosition:Int) {
        val categoryId = arrCategoryId[itmPosition]
        //Log.d("OUTPUT", categoryId)
        val db = FirebaseDatabase.getInstance()
        val dbRef = db.getReference("uCategory/"+arrUserId[0]+"/"+categoryId)
        // Remove data
        dbRef.removeValue().addOnSuccessListener {
            Toast.makeText(this, "Category deleted!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { error -> Log.d("OUTPUT",error.toString()) }

        // re-fetch data
        getCategories()
    }
}