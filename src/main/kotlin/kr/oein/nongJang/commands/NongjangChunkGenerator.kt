package kr.oein.nongJang.commands

import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import java.util.Random

class NongjangChunkGenerator: ChunkGenerator() {
    override fun shouldGenerateNoise(): Boolean {
        return false
    }

    override fun shouldGenerateSurface(): Boolean {
        return true
    }

    override fun shouldGenerateCaves(): Boolean {
        return false
    }

    override fun shouldGenerateDecorations(): Boolean {
        return false
    }

    override fun shouldGenerateMobs(): Boolean {
        return false
    }

    override fun shouldGenerateStructures(): Boolean {
        return false
    }

    override fun getDefaultBiomeProvider(worldInfo: WorldInfo): BiomeProvider {
        return object : BiomeProvider() {
            override fun getBiome(worldInfo: WorldInfo, x: Int, y: Int, z: Int): org.bukkit.block.Biome {
                return org.bukkit.block.Biome.PLAINS
            }

            override fun getBiomes(worldInfo: WorldInfo): MutableList<org.bukkit.block.Biome> {
                return mutableListOf(org.bukkit.block.Biome.PLAINS)
            }
        }
    }

    override fun generateBedrock(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
        // generate bedrock layer at y=0
        for (x in 0 .. 15) {
            for (z in 0 .. 15) {
                chunkData.setBlock(x, 0, z, org.bukkit.Material.BEDROCK)
            }
        }
    }

    override fun generateSurface(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
        // generate dirt from y=1 to y=5
        for (x in 0 .. 14) {
            for (z in 0 .. 14) {
                for (y in 1..5) {
                    if(y == 5) {
                        chunkData.setBlock(x, y, z, org.bukkit.Material.GRASS_BLOCK)
                    } else {
                        chunkData.setBlock(x, y, z, org.bukkit.Material.DIRT)
                    }
                }
            }
        }

        // generate barriers from y=6 to y=255, x=16 or z=16
        for (y in 1..255) {
            for (i in 0 .. 15) {
                chunkData.setBlock(i, y, 16 - 1, org.bukkit.Material.BARRIER)
                chunkData.setBlock(16 - 1, y, i, org.bukkit.Material.BARRIER)
            }
        }

        // generate barriers at top layer y=256
        for (x in 0 until 16) {
            for (z in 0 until 16) {
                chunkData.setBlock(x, 256 - 1, z, org.bukkit.Material.BARRIER)
            }
        }
    }
}