package kr.oein.nongJang.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandExecutor
import kr.oein.nongJang.NongJang
import kr.oein.nongJang.farm.FarmConfig
import kr.oein.nongJang.kvdb.KVDBScope
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import org.bukkit.block.Block
import org.bukkit.entity.Player
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Comparator

object NongJangCommands {
    var nongjangWorld: World? = null
    var lobbyWorld: World? = null

    // Plugin reference for logging and future plugin-scoped operations
    var plugin: NongJang? = null
    fun ensureNongJangWorld(): World? {
        if (nongjangWorld != null) {
            return nongjangWorld
        }
        val worldName = "nong-jang"

        val path = Paths.get(Bukkit.getServer().worldContainer.path, worldName)
        val fileExists = Files.exists(path)

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

                world.save(true)
            } catch (e: Exception) {
                plugin?.logger?.warning("Failed to apply game rules to Nongjang world: ${e.message}") ?: java.util.logging.Logger.getLogger("NongJang").warning("Failed to apply game rules to Nongjang world: ${e.message}")
            }
        } else {
            plugin?.logger?.info("Nongjang world already loaded") ?: java.util.logging.Logger.getLogger("NongJang").info("Nongjang world already loaded")
        }

        nongjangWorld = world
        return nongjangWorld
    }

    fun ensureLobbyWorld(): World? {
        if (lobbyWorld != null) {
            return lobbyWorld
        }
        val worldName = "lobby"

        // create flat world
        var world = Bukkit.getWorld(worldName)
        if (world == null) {
            val wc = WorldCreator.name(worldName)
            wc.environment(World.Environment.NORMAL)
            wc.type(WorldType.FLAT)
            world = Bukkit.createWorld(wc)

            if (world == null) {
                plugin?.logger?.info("Failed to load Lobby world") ?: java.util.logging.Logger.getLogger("NongJang").info("Failed to load Lobby world")
                return null
            } else {
                world.setGameRule(GameRule.DO_MOB_LOOT, false)
                world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
                world.setGameRule(GameRule.FALL_DAMAGE, false)
                world.setGameRule(GameRule.DO_TRADER_SPAWNING, false)
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
                world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
            }
            plugin?.logger?.info("Lobby world loaded on demand") ?: java.util.logging.Logger.getLogger("NongJang").info("Lobby world loaded on demand")
        } else {
            plugin?.logger?.info("Lobby world already loaded") ?: java.util.logging.Logger.getLogger("NongJang").info("Lobby world already loaded")
        }
        lobbyWorld = world
        return lobbyWorld
    }

    var shiftf_cooldown: KVDBScope? = null

    fun register(nj: NongJang) {
        // store plugin reference for logging and later use
        this.plugin = nj
        this.shiftf_cooldown = nj.kvdb.loadScope("shiftf_cooldown")
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
                                val targetPlayer = arguments[0] as Player
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
                                val targetPlayer = arguments[0] as Player
                                val amount = arguments[1] as Long
                                nj.moneyManager.removeMoney(targetPlayer, amount)
                                sender.sendMessage(Component.text("Added ₩$amount to ${targetPlayer.name}."))
                            })
                    )
                    .withSubcommand(
                        CommandAPICommand("get")
                            .withArguments(dev.jorel.commandapi.arguments.PlayerArgument("player"))
                            .executes(CommandExecutor { sender, arguments ->
                                val targetPlayer = arguments[0] as Player
                                val balance = nj.moneyManager.getMoney(targetPlayer)
                                sender.sendMessage(Component.text("${targetPlayer.name} has ₩$balance."))
                            })
                    )
                    .withSubcommand(
                        CommandAPICommand("set")
                            .withArguments(dev.jorel.commandapi.arguments.PlayerArgument("player"))
                            .withArguments(dev.jorel.commandapi.arguments.LongArgument("amount"))
                            .executes(CommandExecutor { sender, arguments ->
                                val targetPlayer = arguments[0] as Player
                                val amount = arguments[1] as Long
                                nj.moneyManager.setMoney(targetPlayer, amount)
                                sender.sendMessage(Component.text("Set ${targetPlayer.name}'s balance to ₩$amount."))
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

                        val playerPos = sender.location
                        var hblock: Block? = null

                        for (y in 254 downTo 1) {
                            val checkBlock = sender.world.getBlockAt(playerPos.blockX, y, playerPos.blockZ)
                            // if it is not air
                            if (checkBlock.type != org.bukkit.Material.AIR) {
                                // set hblock to this block
                                hblock = checkBlock
                                break
                            }
                        }

                        if (hblock == null) {
                            sender.sendMessage(Component.text("No HBlock found at your location."))
                            return@CommandExecutor
                        }

                        sender.sendMessage {
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
                            .replaceSuggestions(ArgumentSuggestions.strings { FarmConfig.products.map { it.id }.toTypedArray() })
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
            .withSubcommand(
                CommandAPICommand("reloadhttp")
                    .withPermission(CommandPermission.OP)
                    .executes(CommandExecutor { sender, _ ->
                        nj.httpServer.fm.calculateGrownStateForAllPlayers()

                        sender.sendMessage(Component.text("Recalculated farmland states for all players."))
                    })
            )
            .withSubcommand(
                CommandAPICommand("lobby")
                    .executes(CommandExecutor { sender, _ ->
                        if(sender !is Player) {
                            sender.sendMessage("This command can only be used by players.")
                            return@CommandExecutor
                        }
                        val lobbyWorld = ensureLobbyWorld()
                        if(lobbyWorld == null) {
                            sender.sendMessage(Component.text("Lobby world is not available."))
                            return@CommandExecutor
                        }
                        sender.teleport(lobbyWorld.spawnLocation)
                        sender.sendMessage(Component.text("Teleported to the lobby world."))
                    })
                    .withSubcommand(
                        CommandAPICommand("ensure")
                            .withPermission(CommandPermission.OP)
                            .executes(CommandExecutor { sender, _ ->
                                val lobbyWorld = ensureLobbyWorld()
                                if(lobbyWorld == null) {
                                    sender.sendMessage(Component.text("Failed to create or load the lobby world."))
                                    return@CommandExecutor
                                }
                                sender.sendMessage(Component.text("Lobby world is ensured to exist."))
                            })
                    )
            )
            .withSubcommand(
                CommandAPICommand("shopprice")
                    .withPermission(CommandPermission.OP)
                    .executes(CommandExecutor { sender, _ ->
                        nj.productPrice.refreshAllPrices()
                        sender.sendMessage(Component.text("Shop prices reloaded."))
                    })
            )
            .withSubcommand(
                CommandAPICommand("resetcoolddown")
                    .withPermission(CommandPermission.OP)
                    .withArguments(
                        dev.jorel.commandapi.arguments.PlayerArgument("player")
                    )
                    .executes(CommandExecutor { sender, arguments ->
                        val targetPlayer = arguments[0] as Player
                        sender.sendMessage(Component.text("Reset Shift+F teleport cooldown for ${targetPlayer.name}."))

                        // iron_hoe_harvest, diamond_hoe_harvest, netherite_hoe_harvest
                        // in scope shiftf_cooldown
                        //
                        val keys = listOf(
                            "iron_hoe_harvest",
                            "diamond_hoe_harvest_replant",
                            "netherite_hoe_harvest_replant"
                        )
                        for(key in keys)
                        {
                            val cooldownKey = "${targetPlayer.uniqueId}__$key"
                            shiftf_cooldown?.set(cooldownKey, 0L)
                        }
                    })
            )
            .register()
    }
}
