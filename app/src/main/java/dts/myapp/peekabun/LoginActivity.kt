package dts.myapp.peekabun

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize UI components
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)

        // Handle login
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val userId = authResult.user?.uid
                    if (userId != null) {
                        Log.d("Login", "Authenticated user UID: $userId")
                        // Check user role
                        FirebaseDatabase.getInstance("https://peek-a-bun-default-rtdb.asia-southeast1.firebasedatabase.app")
                            .getReference("users/$userId/role")
                            .get()
                            .addOnSuccessListener { snapshot ->
                                val role = snapshot.getValue(String::class.java)
                                Log.d("Login", "User role: $role")
                                if (role == "admin") {
                                    startActivity(Intent(this, AdminActivity::class.java))
                                } else {
                                    // Store customer role if not already set
                                    if (role == null) {
                                        FirebaseDatabase.getInstance("https://peek-a-bun-default-rtdb.asia-southeast1.firebasedatabase.app")
                                            .getReference("users/$userId")
                                            .setValue(mapOf("email" to email, "role" to "customer"))
                                            .addOnSuccessListener {
                                                Log.d("Login", "Set customer role for $userId")
                                            }
                                    }
                                    startActivity(Intent(this, MainActivity::class.java))
                                }
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e("Login", "Failed to fetch role: ${e.message}")
                                Toast.makeText(this, "Failed to fetch role: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Login", "Login failed: ${e.message}")
                    Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    override fun onStart() {
        super.onStart()
        // Redirect if already logged in
        auth.currentUser?.let { user ->
            FirebaseDatabase.getInstance("https://peek-a-bun-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users/${user.uid}/role")
                .get()
                .addOnSuccessListener { snapshot ->
                    val role = snapshot.getValue(String::class.java)
                    if (role == "admin") {
                        startActivity(Intent(this, AdminActivity::class.java))
                    } else {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    finish()
                }
        }
    }
}