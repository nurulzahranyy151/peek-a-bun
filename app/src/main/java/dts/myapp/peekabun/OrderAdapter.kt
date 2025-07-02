package dts.myapp.peekabun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrderAdapter(private var orders: List<Order>) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.orderName)
        val addressTextView: TextView = itemView.findViewById(R.id.orderAddress)
        val phoneTextView: TextView = itemView.findViewById(R.id.orderPhone)
        val itemsTextView: TextView = itemView.findViewById(R.id.orderItems)
        val locationTextView: TextView = itemView.findViewById(R.id.orderLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        holder.nameTextView.text = "Name: ${order.customerName}"
        holder.addressTextView.text = "Address: ${order.address}"
        holder.phoneTextView.text = "Phone: ${order.phone}"
        holder.itemsTextView.text = "Items: ${order.items}"
        holder.locationTextView.text = "Location: (${order.latitude}, ${order.longitude})"
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}