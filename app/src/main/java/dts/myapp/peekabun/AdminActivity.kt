package dts.myapp.peekabun

import android.os.Bundle
import android.util.Log
import android.content.Intent
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderAdapter
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        recyclerView = findViewById(R.id.orderRecyclerView)
        logoutButton = findViewById(R.id.logoutButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = OrderAdapter(emptyList())
        recyclerView.adapter = adapter
        val database = FirebaseDatabase.getInstance("https://peek-a-bun-default-rtdb.asia-southeast1.firebasedatabase.app")

        database.getReference("orders").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = mutableListOf<Order>()
                for (orderSnapshot in snapshot.children) {
                    val order = orderSnapshot.getValue(Order::class.java)
                    order?.let { orders.add(it) }
                }
                adapter.updateOrders(orders)
                Log.d("Admin", "Loaded ${orders.size} orders")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Admin", "Failed to load orders: ${error.message}")
                Toast.makeText(this@AdminActivity, "Failed to load orders: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}