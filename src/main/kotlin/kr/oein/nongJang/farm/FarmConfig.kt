package kr.oein.nongJang.farm

import org.bukkit.NamespacedKey

object FarmConfig {
    val products = listOf(
        Product(
            id = "carrot",
            name = "당근",

            seedCbd = "carrot_seed",
            growingCbd = "carrot_growing",
            grownCbd = "carrot_grown",
            shitCbd = "carrot_shit"
        )
    )
    val productType = NamespacedKey("nongjang", "product_type")
    val grownLevel = NamespacedKey("nongjang", "grown_level")
    val shitLevel = NamespacedKey("nongjang", "shit_level")
}