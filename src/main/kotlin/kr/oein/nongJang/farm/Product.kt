package kr.oein.nongJang.farm

data class Product(
    val id: String,
    val name: String? = null,

    val seedCbd: String,
    val growingCbd: String,
    val grownCbd: String,
    val shitCbd: String,

    val calculateGrow: (temperature: Double, soil: Double, wet: Double) -> Double = { _, _, _ ->
        1.0
    },
    val calculateShit: (temperature: Double, soil: Double, wet: Double) -> Double = { _, _, _ ->
        0.5
    }
)
