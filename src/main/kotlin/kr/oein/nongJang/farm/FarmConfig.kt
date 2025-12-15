package kr.oein.nongJang.farm

import org.bukkit.NamespacedKey
import kotlin.math.abs
import kotlin.math.max

fun growFunctionMaker(needTemp: Double, needWet: Double, needMin: Double = 24.0): (Double, Double, Double) -> Double {
    return { temperature: Double, soil: Double, wet: Double ->
        val needTime = needMin + (abs(needTemp - temperature) * abs(needWet - wet)) / 10.0 // 분
        // FarmConfig.fullGrowTicks 틱 마다 실행
        // FarmConfig.fullGrowTicks / 20 초 마다 실행
        // needTime 분 걸리려면 몇 번 실행해야 하는가?
        max(((FarmConfig.fullGrowTicks / (20.0 * 60.0 * needTime)) * (soil / 100.0) * 100.0) + (Math.random() - 0.5), 0.0)
    }
}

fun shitFunctionMaker(needTemp: Double, needWet: Double): (Double, Double, Double) -> Double {
    return { temperature: Double, soil: Double, wet: Double ->
        max((max(abs(needWet - wet) - 3.0, 0.000001) * max(abs(needTemp - temperature) - 3.0, 0.00001) / 100.0)  + (Math.random() - 0.5), 0.0)
    }
}

object FarmConfig {
    val products = listOf(
        Product(
            id = "carrot",
            name = "당근",

            seedCbd = "carrot_seed",
            growingCbd = "carrot_growing",
            grownCbd = "carrot_grown",
            shitCbd = "carrot_shit",
            seedGuiCbd = "carrot_seed_gui",

            calculateGrow = growFunctionMaker(17.5, 75.0),
            calculateShit = shitFunctionMaker(17.5, 75.0)
        ),
        Product(
            id = "sugarcane",
            name = "사탕수수",

            seedCbd = "sugarcane_seed",
            growingCbd = "sugarcane_growing",
            grownCbd = "sugarcane_grown",
            shitCbd = "sugarcane_shit",
            seedGuiCbd = "sugarcane_seed_gui",

            calculateGrow = growFunctionMaker(26.0, 77.5),
            calculateShit = shitFunctionMaker(26.0, 77.5)
        )
    )
    val productType = NamespacedKey("nongjang", "product_type")
    val grownLevel = NamespacedKey("nongjang", "grown_level")
    val shitLevel = NamespacedKey("nongjang", "shit_level")

    const val fullGrowTicks = 20 * 60 // 1 min to full grow
}