package kr.oein.nongJang.utils

import kotlin.math.pow

object GetRandomizingPricing {
    fun getRandomizingPricing(count: Int, landPrice: Long): Long {
        // 가격 = landPrice * 0.3 * (1.2 ^ (count - 1))
        return (landPrice.toDouble() * 0.25 * 1.125.pow((count - 1).toDouble())).toLong()
    }
}