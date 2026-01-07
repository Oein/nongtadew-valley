package kr.oein.nongJang.utils

import kotlin.math.pow

object GetPricing {
    fun getRandomizingPricing(count: Int, landPrice: Long): Long {
        // 가격 = landPrice * 0.3 * (1.2 ^ (count - 1))
        return (landPrice.toDouble() * 0.25 * 1.125.pow((count - 1).toDouble())).toLong()
    }

    fun getPurchasingPricing(count: Int, landPrice: Long): Long {
        // 가격 = landPrice * (1.2 ^ (count - 1))
        return (landPrice.toDouble() * 1.12.pow((count - 1).toDouble())).toLong()
    }
}