package kr.oein.nongJang.farm

data class Product(
    val id: String,
    val name: String? = null,

    val seedCbd: String,
    val growingCbd: String,
    val grownCbd: String,
    val shitCbd: String,

    val seedGuiCbd: String,

    val calculateGrow: (temperature: Double, soil: Double, wet: Double) -> Double = { _, _, _ ->
        Math.random() * 20.0
    },
    val calculateShit: (temperature: Double, soil: Double, wet: Double) -> Double = { _, _, _ ->
        Math.random() * 10.0
    },
    val waterReq: Int = 0,
    val seedPrice: Long = 999999999L,
    val priceMin: Long = 0L,
    val priceMax: Long = 0L
)
