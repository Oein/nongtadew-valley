package kr.oein.nongJang.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandExecutor
import kr.oein.nongJang.NongJang
import kr.oein.nongJang.farm.namedkeys
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.block.Block
import org.bukkit.entity.Player
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Comparator

object NongJangCommands {
    var nongjangWorld: World? = null
    // Plugin reference for logging and future plugin-scoped operations
    var plugin: NongJang? = null
    fun ensureNongJangWorld(): World? {
        if (nongjangWorld != null) {
            plugin?.logger?.info("Nongjang world already ensured") ?: java.util.logging.Logger.getLogger("NongJang").info("Nongjang world already ensured")
            return nongjangWorld
        }
        val worldName = "nong-jang"

        var path = Paths.get(Bukkit.getServer().worldContainer.path, worldName)
        var fileExsists = Files.exists(path)

        var world = Bukkit.getWorld(worldName)
        if (world == null) {
            // Attempt to create/load the world from the existing folder
            // use NongjangChunkGenerator
            val wc = WorldCreator.name(worldName)
            wc.generator(NongjangChunkGenerator())
            world = Bukkit.createWorld(wc)

            if (world == null) {
                plugin?.logger?.info("Failed to load Nongjang world") ?: java.util.logging.Logger.getLogger("NongJang").info("Failed to load Nongjang world")
                return null
            }
            plugin?.logger?.info("Nongjang world loaded on demand") ?: java.util.logging.Logger.getLogger("NongJang").info("Nongjang world loaded on demand")

            // Ensure important game rules are set when we load the world
            try {
                world.setGameRule(GameRule.DO_MOB_LOOT, false)
                world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
                world.setGameRule(GameRule.FALL_DAMAGE, false)
                world.setGameRule(GameRule.DO_TRADER_SPAWNING, false)
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
                world.setGameRule(GameRule.PLAYERS_NETHER_PORTAL_DEFAULT_DELAY, Int.MAX_VALUE)
                world.setGameRule(GameRule.PLAYERS_NETHER_PORTAL_CREATIVE_DELAY, Int.MAX_VALUE)
                world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)

                if(plugin != null && !fileExsists) {
                    // Preload chunks in the T3 radius
                    plugin!!.logger.info { "Preloading chunks in Nongjang world..." }
                    var t3r = plugin!!.chunkManager.T3_radius
                    for (x in -t3r..t3r) {
                        for (z in -t3r..t3r) {
                            plugin!!.logger.info { "Preloading chunks (${(x + t3r) * (2 * t3r + 1) + z + t3r} / ${(t3r * 2 + 1) * (t3r * 2 + 1)})" }
                            var chunk = world.getChunkAt(x, z)
                            if (!chunk.isLoaded) {
                                chunk.load()
                            }
                        }
                        world.save(true)
                        // unload all chunks after preloading
                        plugin!!.logger.info { "Unloading preloaded chunks... (${world.loadedChunks.size})" }
                        for (chunk in world.loadedChunks) {
                            chunk.unload(false)
                        }
                    }
                }
            } catch (e: Exception) {
                plugin?.logger?.warning("Failed to apply game rules to Nongjang world: ${e.message}") ?: java.util.logging.Logger.getLogger("NongJang").warning("Failed to apply game rules to Nongjang world: ${e.message}")
            }
        } else {
            plugin?.logger?.info("Nongjang world already loaded") ?: java.util.logging.Logger.getLogger("NongJang").info("Nongjang world already loaded")
        }

        nongjangWorld = world
        return nongjangWorld
    }

    fun register(nj: NongJang) {
        // store plugin reference for logging and later use
        this.plugin = nj
        nongjangWorld = ensureNongJangWorld()
        CommandAPICommand("admin_nj")
            .withSubcommand(
                CommandAPICommand("world")
                    .withSubcommand(
                        CommandAPICommand("createworld")
                            .withPermission(CommandPermission.OP)
                            .executes(CommandExecutor { sender, _ ->
                                if(nongjangWorld != null) {
                                    sender.sendMessage(Component.text("Nong-jang world already exists."))
                                    return@CommandExecutor
                                }
                                sender.sendMessage("Creating nong-jang world...")
                                ensureNongJangWorld()

                                sender.sendMessage("Generating chunk data...")
                                nj.chunkManager.genAllChunkData()
                            })
                    )
                    .withSubcommand(
                        CommandAPICommand("deleteworld")
                            .withPermission(CommandPermission.OP)
                            .executes(CommandExecutor { sender, _ ->
                                if(nongjangWorld == null) {
                                    sender.sendMessage(Component.text("Nong-jang world does not exist."))
                                    return@CommandExecutor
                                }

                                // move all players in the world to main world
                                val world = nongjangWorld!!
                                val mainWorld = Bukkit.getWorld("world")
                                if (mainWorld == null) {
                                    sender.sendMessage(Component.text("Main world does not exist. Cannot move players out of nong-jang world."))
                                    return@CommandExecutor
                                }
                                for (player in world.players) {
                                    player.teleport(mainWorld.spawnLocation)
                                }

                                // unload and delete the world named "nong-jang"
                                val worldName = "nong-jang"
                                Bukkit.getServer().unloadWorld(world, false)
                                val worldPath: Path = Paths.get(Bukkit.getServer().worldContainer.path, worldName)
                                Files.walk(worldPath)
                                    .sorted(Comparator.reverseOrder())
                                    .forEach { path -> Files.delete(path) }
                                sender.sendMessage(Component.text("World '$worldName' has been deleted."))
                                nongjangWorld = null
                            })
                    )
                    .withSubcommand(
                        CommandAPICommand("regen")
                            .withPermission(CommandPermission.OP)
                            .executes(CommandExecutor { sender, _ ->
                                nj.chunkManager.genAllChunkData()
                                sender.sendMessage(Component.text("All chunk data regenerated."))
                            })
                    )
                    .withSubcommand(
                        CommandAPICommand("ownerclear")
                            .withPermission(CommandPermission.OP)
                            .executes(CommandExecutor { sender, _ ->
                                nj.chunkManager.clearPurchases()
                                sender.sendMessage(Component.text("All chunk owners cleared."))
                            })
                    )
            )
            .withSubcommand(
                CommandAPICommand("money")
                    .withPermission(CommandPermission.OP)
                    .withSubcommand(
                        CommandAPICommand("add")
                            .withArguments(dev.jorel.commandapi.arguments.PlayerArgument("player"))
                            .withArguments(dev.jorel.commandapi.arguments.LongArgument("amount"))
                            .executes(CommandExecutor { sender, arguments ->
                                val targetPlayer = arguments[0] as org.bukkit.entity.Player
                                val amount = arguments[1] as Long
                                nj.moneyManager.addMoney(targetPlayer, amount)
                                sender.sendMessage(Component.text("Added ₩$amount to ${targetPlayer.name}."))
                            })
                    )
                    .withSubcommand(
                        CommandAPICommand("remove")
                            .withArguments(dev.jorel.commandapi.arguments.PlayerArgument("player"))
                            .withArguments(dev.jorel.commandapi.arguments.LongArgument("amount"))
                            .executes(CommandExecutor { sender, arguments ->
                                val targetPlayer = arguments[0] as org.bukkit.entity.Player
                                val amount = arguments[1] as Long
                                nj.moneyManager.removeMoney(targetPlayer, amount)
                                sender.sendMessage(Component.text("Added ₩$amount to ${targetPlayer.name}."))
                            })
                    )
            )
            .withSubcommand(
                CommandAPICommand("debit")
                    .withPermission(CommandPermission.OP)
                    .withSubcommand(
                        CommandAPICommand("add")
                            .withArguments(dev.jorel.commandapi.arguments.PlayerArgument("player"))
                            .withArguments(dev.jorel.commandapi.arguments.LongArgument("amount"))
                            .executes(CommandExecutor { sender, arguments ->
                                val targetPlayer = arguments[0] as org.bukkit.entity.Player
                                val amount = arguments[1] as Long
                                nj.moneyManager.addDebit(targetPlayer, amount)
                                sender.sendMessage(Component.text("Added debit ₩$amount to ${targetPlayer.name}."))
                            })
                    )
                    .withSubcommand(
                        CommandAPICommand("remove")
                            .withArguments(dev.jorel.commandapi.arguments.PlayerArgument("player"))
                            .withArguments(dev.jorel.commandapi.arguments.LongArgument("amount"))
                            .executes(CommandExecutor { sender, arguments ->
                                val targetPlayer = arguments[0] as org.bukkit.entity.Player
                                val amount = arguments[1] as Long
                                nj.moneyManager.removeDebit(targetPlayer, amount)
                                sender.sendMessage(Component.text("Removed debit ₩$amount from ${targetPlayer.name}."))
                            })
                    )
            )
            .withSubcommand(
                CommandAPICommand("grow")
                    .withPermission(CommandPermission.OP)
                    .executes(CommandExecutor { sender, _ ->
                        nj.grow.handleGrowth()
                        sender.sendMessage { Component.text("Grown handled") }
                    })
            )
            .withSubcommand(
                CommandAPICommand("hblock")
                    .withPermission(CommandPermission.OP)
                    .executes(CommandExecutor { sender, _ ->
                        if(sender !is Player) {
                            sender.sendMessage("This command can only be used by players.")
                            return@CommandExecutor
                        }

                        val player = sender as Player
                        val playerPos = player.location
                        var hblock: Block? = null

                        for (y in 254 downTo 1) {
                            val checkBlock = player.world.getBlockAt(playerPos.blockX, y, playerPos.blockZ)
                            // if it is not air
                            if (checkBlock.type != org.bukkit.Material.AIR) {
                                // set hblock to this block
                                hblock = checkBlock
                                break
                            }
                        }

                        if (hblock == null) {
                            player.sendMessage(Component.text("No HBlock found at your location."))
                            return@CommandExecutor
                        }

                        player.sendMessage {
                            Component.text("HBlock info =====\n")
                                .append {
                                    Component.text("Location: (${hblock.location.x.toInt()}, ${hblock.location.y.toInt()}, ${hblock.location.z.toInt()})\n")
                                }
                                .append {
                                    Component.text("Block Type: ${hblock.type}\n")
                                }
                        }
                    })
            )
            .withSubcommand(
                CommandAPICommand("seed")
                    .withPermission(CommandPermission.OP)
                    .withArguments(
                        StringArgument("product")
                            .replaceSuggestions(ArgumentSuggestions.strings { namedkeys.products.map { it.id }.toTypedArray() })
                    )
                    .executes(CommandExecutor { sender, arguments ->
                        val productName = arguments[0] as String
                        val seedItem = nj.grow.createSeedItem(productName)
                        if(sender is Player) {
                            sender.inventory.addItem(seedItem)
                            sender.sendMessage(Component.text("Added seed item for product '$productName' to your inventory."))
                        } else {
                            sender.sendMessage("This command can only be used by players.")
                        }
                    })
            )
            .register()
    }
}
