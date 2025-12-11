package kr.oein.nongJang.farm

import org.bukkit.NamespacedKey

object namedkeys {
    val products = listOf<Product>(
        Product(
            "carrot",
            "carrot_seed",
            "carrot_growing",
            "carrot_grown",
            "carrot_shit"
        )
    )
    val productType = NamespacedKey("nongjang", "product_type")
    val grownLevel = NamespacedKey("nongjang", "grown_level")
    val shitLevel = NamespacedKey("nongjang", "shit_level")
}