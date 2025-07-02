package dts.myapp.peekabun

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BakeryItemAdapter
    private lateinit var customerName: EditText
    private lateinit var customerAddress: EditText
    private lateinit var customerPhone: EditText
    private lateinit var sensorStatus: TextView
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = FirebaseDatabase.getInstance("https://peek-a-bun-default-rtdb.asia-southeast1.firebasedatabase.app")
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView)
        customerName = findViewById(R.id.customerName)
        customerAddress = findViewById(R.id.customerAddress)
        customerPhone = findViewById(R.id.customerPhone)
        sensorStatus = findViewById(R.id.sensorStatus)
        val orderButton: Button = findViewById(R.id.orderButton)
        logoutButton = findViewById(R.id.logoutButton)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            sensorStatus.text = "Light sensor not available"
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        val bakeryItems = listOf(
            BakeryItem("Croissant", "Buttery and flaky croissant", 2.5),
            BakeryItem("Chocolate Cake", "Rich chocolate layered cake", 15.0),
            BakeryItem("Sourdough Bread", "Artisanal sourdough loaf", 5.0),
            BakeryItem("Choco Lava Cake", "Sweet and moist lava cake with chocolate filling", 12.0)
        )
        adapter = BakeryItemAdapter(bakeryItems) { item ->
            Toast.makeText(this, "Selected: ${item.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

        // Handle order submission
        orderButton.setOnClickListener {
            val name = customerName.text.toString()
            val address = customerAddress.text.toString()
            val phone = customerPhone.text.toString()

            if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (adapter.selectedItems.isEmpty()) {
                Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check location permission for GPS
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
                Toast.makeText(this, "Requesting location permission", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // GPS: Retrieve location coordinates
            Toast.makeText(this, "Fetching location...", Toast.LENGTH_SHORT).show()
            Log.d("GPS", "Attempting to fetch location")
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        Log.d("GPS", "Location obtained: ${location.latitude}, ${location.longitude}")
                        val order = Order(
                            name,
                            address,
                            phone,
                            adapter.selectedItems.joinToString(", ") { it.name },
                            location.latitude,
                            location.longitude
                        )
                        saveOrderToFirebase(order)
                    } else {
                        Log.d("GPS", "Location is null")
                        Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("GPS", "Location error: ${e.message}")
                    Toast.makeText(this, "Location error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // Handle logout
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun saveOrderToFirebase(order: Order) {
        Log.d("Firebase", "Saving order: $order")
        val databaseReference = database.reference.child("orders")
        val orderId = databaseReference.push().key
        if (orderId != null) {
            databaseReference.child(orderId).setValue(order)
                .addOnSuccessListener {
                    Log.d("Firebase", "Order saved successfully: $orderId")
                    Toast.makeText(this, "Order placed successfully", Toast.LENGTH_SHORT).show()
                    customerName.text.clear()
                    customerAddress.text.clear()
                    customerPhone.text.clear()
                    adapter.clearSelection()
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to save order: ${e.message}")
                    Toast.makeText(this, "Failed to place order: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Log.e("Firebase", "Failed to generate order ID")
            Toast.makeText(this, "Failed to generate order ID", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lightLevel = event.values[0]
            sensorStatus.text = "Light Level: $lightLevel lux"
            if (lightLevel < 10) {
                Toast.makeText(this, "Low light detected, consider brighter environment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permissions", "Location permission granted")
            Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("Permissions", "Location permission denied")
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}