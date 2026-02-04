package kr.oein.nongJang.farm

import org.bukkit.Material
import org.bukkit.NamespacedKey
import kotlin.math.abs
import kotlin.math.max

fun growFunctionMaker(needTemp: Double, needWet: Double, needMin: Double = 24.0): (Double, Double, Double) -> Double {
    return { temperature: Double, soil: Double, wet: Double ->
        val needTime = needMin + (abs(needTemp - temperature) * abs(needWet - wet)) / 10.0 // 분
        // FarmConfig.fullGrowTicks 틱 마다 실행
        // FarmConfig.fullGrowTicks / 20 초 마다 실행
        // needTime 분 걸리려면 몇 번 실행해야 하는가?
        max(((FarmConfig.FULL_GROW_TICKS / (20.0 * 60.0 * needTime)) * (soil / 100.0) * 100.0) + (Math.random() - 0.5), 0.0)
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
            calculateShit = shitFunctionMaker(17.5, 75.0),
            waterReq = 25,

            seedPrice = 500L,
            priceMin = 1000L,
            priceMax = 1400L
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
            calculateShit = shitFunctionMaker(26.0, 77.5),
            waterReq = 125,

            seedPrice = 1650L,
            priceMin = 3300L,
            priceMax = 4000L
        ),
        Product(
            id = "corn",
            name = "옥수수",

            seedCbd = "corn_seed",
            growingCbd = "corn_growing",
            grownCbd = "corn_grown",
            shitCbd = "corn_shit",
            seedGuiCbd = "corn_seed_gui",

            calculateGrow = growFunctionMaker(25.0, 70.0),
            calculateShit = shitFunctionMaker(25.0, 70.0),
            waterReq = 60,

            seedPrice = 800L,
            priceMin = 1600L,
            priceMax = 2000L
        ),
        Product(
            id = "star",
            name = "스타푸르츠",

            seedCbd = "star_seed",
            growingCbd = "star_growing",
            grownCbd = "star_grown",
            shitCbd = "star_shit",
            seedGuiCbd = "star_seed_gui",

            calculateGrow = growFunctionMaker(27.5, 67.5),
            calculateShit = shitFunctionMaker(27.5, 67.5),
            waterReq = 50,

            seedPrice = 3500L,
            priceMin = 7000L,
            priceMax = 7777L
        ),
        Product(
            id = "potato",
            name = "감자",

            seedCbd = "potato_seed",
            growingCbd = "potato_growing",
            grownCbd = "potato_grown",
            shitCbd = "potato_shit",
            seedGuiCbd = "potato_seed_gui",

            calculateGrow = growFunctionMaker(17.5, 85.0),
            calculateShit = shitFunctionMaker(17.5, 85.0),
            waterReq = 40,

            seedPrice = 250L,
            priceMin = 450L,
            priceMax = 650L
        )
    )
    val productType = NamespacedKey("nongjang", "product_type")
    val grownLevel = NamespacedKey("nongjang", "grown_level")
    val shitLevel = NamespacedKey("nongjang", "shit_level")

    const val FULL_GROW_TICKS = 20 * 60 // 1 min to full grow

    val farmlandBlocks = listOf(
        Material.RED_WOOL,
        Material.ORANGE_WOOL,
        Material.YELLOW_WOOL,
        Material.LIME_WOOL,
        Material.LIGHT_BLUE_WOOL,
        Material.CYAN_WOOL,
        Material.BLUE_WOOL,
        Material.PURPLE_WOOL,
        Material.MAGENTA_WOOL,
        Material.PINK_WOOL,
        Material.WHITE_WOOL,
        Material.GREEN_WOOL,
        Material.BROWN_WOOL,
        Material.GRAY_WOOL,
        Material.LIGHT_GRAY_WOOL,
        Material.BLACK_WOOL,

        Material.RED_CONCRETE,
        Material.ORANGE_CONCRETE,
        Material.YELLOW_CONCRETE,
        Material.LIME_CONCRETE,
        Material.LIGHT_BLUE_CONCRETE,
        Material.CYAN_CONCRETE,
        Material.BLUE_CONCRETE,
        Material.PURPLE_CONCRETE,
        Material.MAGENTA_CONCRETE,
        Material.PINK_CONCRETE,
        Material.WHITE_CONCRETE,
        Material.GREEN_CONCRETE,
        Material.BROWN_CONCRETE,
        Material.GRAY_CONCRETE,
        Material.LIGHT_GRAY_CONCRETE,
        Material.BLACK_CONCRETE,

        Material.RED_TERRACOTTA,
        Material.ORANGE_TERRACOTTA,
        Material.YELLOW_TERRACOTTA,
        Material.LIME_TERRACOTTA,
        Material.LIGHT_BLUE_TERRACOTTA,
        Material.CYAN_TERRACOTTA,
        Material.BLUE_TERRACOTTA,
        Material.PURPLE_TERRACOTTA,
        Material.MAGENTA_TERRACOTTA,
        Material.PINK_TERRACOTTA,
        Material.WHITE_TERRACOTTA,
        Material.GREEN_TERRACOTTA,
        Material.BROWN_TERRACOTTA,
        Material.GRAY_TERRACOTTA,
        Material.LIGHT_GRAY_TERRACOTTA,
        Material.BLACK_TERRACOTTA,

        Material.RED_STAINED_GLASS,
        Material.ORANGE_STAINED_GLASS,
        Material.YELLOW_STAINED_GLASS,
        Material.LIME_STAINED_GLASS,
        Material.LIGHT_BLUE_STAINED_GLASS,
        Material.CYAN_STAINED_GLASS,
        Material.BLUE_STAINED_GLASS,
        Material.PURPLE_STAINED_GLASS,
        Material.MAGENTA_STAINED_GLASS,
        Material.PINK_STAINED_GLASS,
        Material.WHITE_STAINED_GLASS,
        Material.GREEN_STAINED_GLASS,
        Material.BROWN_STAINED_GLASS,
        Material.GRAY_STAINED_GLASS,
        Material.LIGHT_GRAY_STAINED_GLASS,
        Material.BLACK_STAINED_GLASS,
    )
}