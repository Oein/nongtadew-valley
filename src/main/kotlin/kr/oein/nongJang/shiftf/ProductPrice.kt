package kr.oein.nongJang.shiftf

import kr.oein.nongJang.NongJang
import kr.oein.nongJang.farm.FarmConfig
import kr.oein.nongJang.farm.Product

class ProductPrice(val nj: NongJang) {
    val scope = nj.kvdb.loadScope("productPrice")

    fun getProductById(productId: String): Product? {
        return FarmConfig.products.find { it.id == productId }
    }

    fun getRandPrice(productId: String): Long {
        val product = getProductById(productId)
        val minPrice = product?.priceMin ?: 0L
        val maxPrice = product?.priceMax ?: 0L
        return (minPrice..maxPrice).random()
    }

    fun getPrice(productId: String): Long {
        scope.get(productId)?.let {
            return it.toString().toLong()
        }
        val price = getRandPrice(productId)
        scope.set(productId, price.toString())
        return price
    }

    fun refreshAllPrices() {
        for (product in FarmConfig.products) {
            val price = getRandPrice(product.id)
            scope.set(product.id, price.toString())
        }
    }

    fun schedulePriceRefresh() {
        nj.server.scheduler.scheduleSyncRepeatingTask(nj, {
            val now = System.currentTimeMillis()
            val lastRefresh = scope.get("last_refresh")?.toString()?.toLongOrNull() ?: 0L
            if(lastRefresh == 0L || now - lastRefresh >= 20L * 60L * 60L * 4L) { // 4 hours
                refreshAllPrices()
                scope.set("last_refresh", now.toString())
            }
        }, 0L, 20L * 60L * 30L) // 30 min
    }
}