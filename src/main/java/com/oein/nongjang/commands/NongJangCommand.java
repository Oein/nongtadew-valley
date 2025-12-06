package com.oein.nongjang.commands;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.oein.nongjang.NongJangPlugin;

public class NongJangCommand implements CommandExecutor {
    private final NongJangPlugin plugin;

    public NongJangCommand(NongJangPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }
        Player player = (Player) sender;
        World world = plugin.getServer().getWorld("nong-jang");
        if (world == null) {
            player.sendMessage("월드 'nong-jang'이 로드되지 않았습니다.");
            return true;
        }
        Location spawn = world.getSpawnLocation();
        // 안전을 위해 높이 보정 (스폰 높이가 비정상적이면 64로 강제)
        if (spawn.getY() <= 0) spawn.setY(64);
        player.teleport(spawn);
        player.sendMessage("nong-jang 세계로 이동했습니다.");
        return true;
    }
}

