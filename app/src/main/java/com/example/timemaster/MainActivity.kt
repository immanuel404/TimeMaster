package com.example.timemaster

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    // fb->login/register auth
    private lateinit var auth: FirebaseAuth
    // fb-google sign-in
    lateinit var mGoogleSignInClient: GoogleSignInClient
    val Req_Code: Int = 123
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize->Firebase Auth
        auth = Firebase.auth
        // Initialize->Firebase Google Sign-in
        FirebaseApp.initializeApp(this)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("")
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()


        // UI STYLING
        val clLayout = findViewById<ConstraintLayout>(R.id.layout)
        val animeDrawable = clLayout.background as AnimationDrawable
        animeDrawable.setEnterFadeDuration(2000)
        animeDrawable.setExitFadeDuration(3000)
        animeDrawable.start()


        // DECLARATIONS
        var txtEmail = findViewById(R.id.txtEmail) as EditText
        var txtPassword = findViewById(R.id.txtPassword) as EditText
        var btnRegister = findViewById(R.id.btnRegister) as Button
        var btnLogin = findViewById(R.id.btnLogin) as Button
        // progress bar
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility= View.GONE
        // int->auth object
        val obj = uAuth()


        // GOOGLE SIGN-IN
        val btnGoogleSignin = findViewById<Button>(R.id.btnGoogleSignin)
        btnGoogleSignin.setOnClickListener { view: View? ->
            //Toast.makeText(this, "Logging In", Toast.LENGTH_SHORT).show()
            signInGoogle()
        }


        // ON-REGISTER
        btnRegister.setOnClickListener()
        {
            val email = txtEmail.text.toString().trim()
            val password = txtPassword.text.toString().trim()

            // check user input
            if(email=="" || password=="") {
                Toast.makeText(this, "Enter Details", Toast.LENGTH_SHORT).show()
            }
            else {
                auth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {

                            // create userid
                            val emailSplit = email.split("@")
                            val user_id = emailSplit[0]

                            // store in realtime-db
                            val db = FirebaseDatabase.getInstance()
                            val dbRef = db.getReference("Users/$user_id")
                            val data = mapOf("Email" to email, "UserId" to user_id)
                            dbRef.setValue(data)

                            // store locally
                            obj.StoreUser(email, user_id)

                            // clear input fields
                            txtEmail.setText("")
                            txtPassword.setText("")

                            // redirect
                            val i = Intent(this, HomeActivity::class.java)
                            startActivity(i); finish();
                            Toast.makeText(this, "Registration Success", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d("Output", (task.exception?.message ?: "null"))
                            Toast.makeText(this, "Error: "+ (task.exception?.message ?: "null"), Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }


        // ON-LOGIN
        btnLogin.setOnClickListener()
        {
            val email = txtEmail.text.toString()
            val password = txtPassword.text.toString()

            // quick sign-in
//            if(email=="test") {
//                // store locally
//                obj.StoreUser(email,email)
//                // clear input fields
//                txtEmail.setText("")
//                txtPassword.setText("")
//                // redirect
//                val i = Intent(this, HomeActivity::class.java);
//                startActivity(i); finish();
//                Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()
//            }

            // check user data
            if(email=="test" || email=="" || password=="") {
                Toast.makeText(this, "Enter Details", Toast.LENGTH_SHORT).show()
            }
            else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {

                            // create userid
                            val emailSplit = email.split("@")
                            val user_id = emailSplit[0]
                            obj.StoreUser(email, user_id)

                            // clear input fields
                            txtEmail.setText("")
                            txtPassword.setText("")

                            // redirect
                            val i = Intent(this, HomeActivity::class.java);
                            startActivity(i); finish();
                            Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Log.d("Output", (task.exception?.message ?: "null"))
                            Toast.makeText(this, "Error: "+ (task.exception?.message ?: "null"), Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }


    // GOOGLE SIGN-IN METHODS
    private fun signInGoogle() {
        progressBar.visibility = View.VISIBLE

        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, Req_Code)
    }
    // we provide the task and data for the Google Account
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Req_Code) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }
    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                UpdateUI(account)
            }
        } catch (e: ApiException) {
            progressBar.visibility= View.GONE
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }
    // update UI after Google sign-in
    private fun UpdateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //SavedPreference.setEmail(this, account.email.toString())
                //SavedPreference.setUsername(this, account.displayName.toString())

                // create userid
                val obj = uAuth()
                val emailSplit = account.email.toString().split("@")
                val user_id = emailSplit[0]
                obj.StoreUser(account.email.toString(), user_id)

                progressBar.visibility= View.GONE
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

// RE-ENTER APP WITHOUT LOGIN AGAIN
//    override fun onStart() {
//        super.onStart()
//        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
//            startActivity(
//                Intent(
//                    this, HomeActivity::class.java
//                )
//            )
//            finish()
//        }
//    }
}