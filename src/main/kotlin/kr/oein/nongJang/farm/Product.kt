package kr.oein.nongJang.farm

data class Product(
    val id: String,

    val seed_cbd: String,
    val growing_cbd: String,
    val grown_cbd: String,
    val shit_cbd: String,

    val calculateGrow: (temperature: Double, soil: Double, wet: Double) -> Double = { t, s, w ->
        1.0
    },
    val calculateShit: (temperature: Double, soil: Double, wet: Double) -> Double = { t, s, w ->
        0.5
    }
)
