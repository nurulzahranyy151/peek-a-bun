package dts.myapp.peekabun

data class Order(
    val customerName: String = "",
    val address: String = "",
    val phone: String = "",
    val items: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
