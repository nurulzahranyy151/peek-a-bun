package dts.myapp.peekabun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BakeryItemAdapter(
    private val items: List<BakeryItem>,
    private val onItemClick: (BakeryItem) -> Unit
) : RecyclerView.Adapter<BakeryItemAdapter.ViewHolder>() {

    val selectedItems = mutableListOf<BakeryItem>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.itemName)
        val descriptionTextView: TextView = itemView.findViewById(R.id.itemDescription)
        val priceTextView: TextView = itemView.findViewById(R.id.itemPrice)
        val checkBox: CheckBox = itemView.findViewById(R.id.itemCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bakery_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.nameTextView.text = item.name
        holder.descriptionTextView.text = item.description
        holder.priceTextView.text = "$${item.price}"
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedItems.add(item)
            } else {
                selectedItems.remove(item)
            }
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }
}