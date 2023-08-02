package com.example.timemaster

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class HomeActivity : AppCompatActivity() {
    // google sign-in->sign_out
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onRestart() {
        super.onRestart()
        if(db_Count>0){
            checkUserID()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        // UI STYLING
        val clLayout = findViewById<ConstraintLayout>(R.id.layout)
        val animeDrawable = clLayout.background as AnimationDrawable
        animeDrawable.setEnterFadeDuration(2000)
        animeDrawable.setExitFadeDuration(3000)
        animeDrawable.start()


        // GOOGLE SIGN-IN->SIGN-OUT
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("")
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


        // DECLARATIONS
        val btnManual = findViewById(R.id.btnManual) as Button
        val btnAutomatic = findViewById(R.id.btnAutomatic) as Button
        val btnViewEntry = findViewById(R.id.btnViewEntry) as Button
        val btnCategory = findViewById(R.id.btnCategory) as Button
        val btnLogout = findViewById(R.id.txtLogout) as TextView
        getCategories()
        getEntryCount()


        // PAGE->REDIRECTS
        btnLogout.setOnClickListener(){
            mGoogleSignInClient.signOut().addOnCompleteListener {
                arrUserId.clear() //remove userId
                val intent = Intent(this, MainActivity::class.java)
                Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
                startActivity(intent); finish();
            }
        }
        btnCategory.setOnClickListener(){
            val intent = Intent(this, AddCategoryActivity::class.java)
            startActivity(intent)
        }
        btnAutomatic.setOnClickListener(){
            val intent = Intent(this, AddEntryAActivity::class.java)
            startActivity(intent)
        }
        btnManual.setOnClickListener(){
            val intent = Intent(this, AddEntryMActivity::class.java)
            startActivity(intent)
        }
        btnViewEntry.setOnClickListener()
        {
            val intent = Intent(this, EntryListActivity::class.java)
            startActivity(intent)
        }
    }


    // GET CATEGORIES->DROPDOWN
    private fun getCategories() {
        arrCategories.clear()
        val rootRef = FirebaseDatabase.getInstance().reference
        val categoryRef = rootRef.child("uCategory").child(arrUserId[0])
        val eventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children) {
                    val categoryObj: uCategory? = data.getValue(uCategory::class.java)
                    if (categoryObj != null) {
                        // add category into array
                        arrCategories.add(categoryObj.Name.toString())
                        //Log.d("OUTPUT", categoryObj.Name)
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("OUTPUT", databaseError.message)
            }
        }
        categoryRef.addListenerForSingleValueEvent(eventListener)
    }


    // GET ENTRY COUNT
    private fun getEntryCount() {
        db_Count = 0
        val rootRef = FirebaseDatabase.getInstance().reference
        val entryRef = rootRef.child("uEntry").child(arrUserId[0])
        val eventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children) {
                    val entryObj: uEntry? = data.getValue(uEntry::class.java)
                    if (entryObj != null) {
                        db_Count++
                        // Log.d("OUTPUT ",db_Count.toString())
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("OUTPUT", databaseError.message)
            }
        }
        entryRef.addListenerForSingleValueEvent(eventListener)
    }


    // CHECK USER-ID
    private fun checkUserID() {
        // if unauthorized keep out
        if (arrUserId.size < 1) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent);finish();
        }
    }
}