package com.kimiram.attributerandomizer.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ARTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        switch (args.length) {
            case 1:
                return List.of("start", "stop", "mode", "period", "amount", "reset");
            case 2:
                if (args[0].equalsIgnoreCase("mode")) {
                    return List.of("on-death", "on-timer");
                } else if (args[0].equalsIgnoreCase("period")) {
                    return List.of("20t", "5s", "5m");
                } else if (args[0].equalsIgnoreCase("reset")) {
                    List<String> list = new ArrayList<>(List.of("@s", "@a", "@p", "@r"));
                    for (Player player: Bukkit.getOnlinePlayers()) {
                        list.add(player.getName());
                    }
                    return list;
                }
            default:
                return Collections.emptyList();
        }
    }
}
