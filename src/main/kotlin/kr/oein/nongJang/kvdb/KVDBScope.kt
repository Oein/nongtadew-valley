package kr.oein.nongJang.kvdb

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class KVDBScope(val plugin: JavaPlugin, val scope: String) {
    var yamlcfg: YamlConfiguration = YamlConfiguration()

    fun getFile(): File {
        return plugin.dataFolder.resolve("$scope.yml")
    }

    fun load() {
        // load from db.yml
        val file = getFile()
        if (file.exists()) {
            // Load the data from the file
            yamlcfg = YamlConfiguration.loadConfiguration(file)
        }
    }

    fun save() {
        // Save the data to db.yml
        val file = getFile()
        yamlcfg.save(file)
    }

    fun get(key: String): Any? {
        return yamlcfg.get(key)
    }

    fun set(key: String, value: Any) {
        yamlcfg.set(key, value)
        save() // Save after setting a new value
    }

    fun setSilent(key: String, value: Any) {
        yamlcfg.set(key, value)
        // No save here for silent set
    }

    fun remove(key: String) {
        yamlcfg.set(key, null)
        save() // Save after removing a value
    }

    fun removeSilent(key: String) {
        yamlcfg.set(key, null)
        // No save here for silent remove
    }

    fun has(key: String): Boolean {
        return yamlcfg.contains(key)
    }

    fun keys(): Set<String> {
        return yamlcfg.getKeys(false)
    }

    fun clear() {
        yamlcfg = YamlConfiguration()
        save() // Save after clearing the configuration
    }
}