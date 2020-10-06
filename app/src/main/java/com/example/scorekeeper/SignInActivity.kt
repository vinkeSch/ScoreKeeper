package com.example.scorekeeper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.scorekeeper.databinding.ActivitySignInBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class SignInActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var binding: ActivitySignInBinding

    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var callbackManager: CallbackManager // For Facebook login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Buttons
        binding.btnSignIn.setOnClickListener(this)
        binding.tvNotHaveAccount.setOnClickListener(this)
        binding.btnGuest.setOnClickListener(this)
        binding.btnGoogle.setOnClickListener(this)
        binding.btnFacebook.setOnClickListener(this)
        binding.tvForgotPassword.setOnClickListener(this)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        database = Firebase.database.reference
        // Initialize Firebase Auth
        auth = Firebase.auth

        // Initialize Facebook Login button
        callbackManager = CallbackManager.Factory.create()

        binding.btnFacebook2.setPermissions("email", "public_profile")
        binding.btnFacebook2.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
                updateUI(null)
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
                println(error.message)
                updateUI(null)
            }
        })
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun createAccount(email: String, password: String) {
        Log.d(TAG, "createAccount:$email")
        if (!validateForm()) {
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)

                    // Main activity launch after login
                    Toast.makeText(
                        baseContext, "Login success",
                        Toast.LENGTH_SHORT
                    ).show()
                    startMain(user!!.uid)
                } else {
                    if(task.exception is FirebaseAuthUserCollisionException) // User with this email already exists
                        Toast.makeText(
                            this,
                            "User with this email already exists.", Toast.LENGTH_SHORT
                        ).show()
                    else Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)

                    updateUI(null)
                }
            }
    }

    private fun signIn(email: String, password: String) {
        Log.d(TAG, "signIn:$email")
        if (!validateForm()) {
            return
        }
        // trim email & password from blank spaces

        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)

                    // Main activity launch after login
                    Toast.makeText(
                        baseContext, "Login success",
                        Toast.LENGTH_SHORT
                    ).show()
                    startMain(user!!.uid)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun startMain(userUID: String) {
        val intent = Intent(this@SignInActivity, MainActivity::class.java)
        val b = Bundle()
        b.putString("userUID", userUID) //Your id

        intent.putExtras(b) //Put your id to your next Intent

        startActivity(intent)
        finish()
    }

    private fun signOut() {
        auth.signOut()
        LoginManager.getInstance().logOut()

        updateUI(null)
    }

    private fun updateUI(user: FirebaseUser?) {
        user?.let {
            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            val uid = user.uid
            println("USER ID: $uid")

            database.child("users").child(uid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) { // the user exists

                    } else { // create new user
                        database.child("users").child(uid)
                            .child("email").setValue(user.email)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }


            })
        }
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = binding.etEmailField.text.toString()
        if (TextUtils.isEmpty(email)) {
            binding.etEmailField.error = "Required."
            valid = false
        } else {
            binding.etEmailField.error = null
        }

        val password = binding.etPasswordField.text.toString()
        if (TextUtils.isEmpty(password)) {
            binding.etPasswordField.error = "Required."
            valid = false
        } else {
            binding.etPasswordField.error = null
        }

        return valid
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        } else {
            // Pass the activity result back to the Facebook SDK
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)

                    // Main activity launch after login
                    Toast.makeText(
                        baseContext, "Login success. Hello ${auth.currentUser?.displayName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    startMain(user!!.uid)
                    signOut()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    val view = binding.root
                    Snackbar.make(view, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_not_have_account -> {
                createAccount(
                    binding.etEmailField.text.toString().trim(),
                    binding.etPasswordField.text.toString()
                )
            }
            R.id.btn_sign_in ->
                signIn(
                    binding.etEmailField.text.toString(),
                    binding.etPasswordField.text.toString()
                )
            R.id.btn_guest -> signInAnonymously()
            R.id.btn_google -> signInGoogle()
            R.id.btn_facebook -> binding.btnFacebook2.performClick()
            R.id.tv_forgot_password -> sendPasswordReset()
        }
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInAnonymously:success")
                    val user = auth.currentUser
                    updateUI(user)

                    // Main activity launch after login
                    Toast.makeText(
                        baseContext, "Login success",
                        Toast.LENGTH_SHORT
                    ).show()
                    signOut()
                    startMain(user!!.uid)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInAnonymously:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)

                    // Main activity launch after login
                    Toast.makeText(
                        baseContext, "Login success. Hello ${auth.currentUser?.displayName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    startMain(user!!.uid)
                    signOut()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun sendPasswordReset() {
        // [START send_password_reset]
        val auth = FirebaseAuth.getInstance()

        val emailAddress = binding.etEmailField.text.toString()
        if (TextUtils.isEmpty(emailAddress)) {
            binding.etEmailField.error = "Required."
            Snackbar.make(
                binding.root,
                "Please indicate your email",
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            binding.etEmailField.error = null
            auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Email sent.")
                        hideKeyboard()
                        Snackbar.make(
                            binding.root,
                            "Email sent! Check your inbox to reset your password",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    companion object {
        private const val TAG = "SignIn"
        private const val RC_SIGN_IN = 9001
    }
}