package com.kimiram.attributerandomizer.command;

import com.kimiram.attributerandomizer.AttributeRandomizer;
import com.kimiram.attributerandomizer.Main;
import com.kimiram.attributerandomizer.task.BossBarTimerTick;
import com.kimiram.attributerandomizer.task.RandomizeAttributes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class AttributeRandomizerCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].isEmpty()) {
            sender.sendMessage("AttributeRandomizer Info:");
            sender.sendMessage(" Plugin version: " + Bukkit.getPluginManager().getPlugin("AttributeRandomizer").getDescription().getVersion());
            sender.sendMessage(" Status: " + (AttributeRandomizer.isEnabled ? ChatColor.GREEN + "On" : ChatColor.RED + "Off"));
            sender.sendMessage(" Mode: " + AttributeRandomizer.mode.toString().toLowerCase().replace('_', '-'));
            if (AttributeRandomizer.mode == AttributeRandomizer.Mode.ON_TIMER) {
                sender.sendMessage(" Period: " +
                        AttributeRandomizer.period / 20 / 60 + " minutes " +
                        AttributeRandomizer.period / 20 % 60 + " seconds " +
                        AttributeRandomizer.period % 20      + " ticks ");
            }
            sender.sendMessage(" Amount: " + AttributeRandomizer.amount);
            return true;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("start")) {
                if (!AttributeRandomizer.isEnabled) {
                    AttributeRandomizer.isEnabled = true;
                    AttributeRandomizer.startRandomizer();
                    sender.sendMessage(ChatColor.GREEN + "AttributeRandomizer has been started.");
                } else {
                    sender.sendMessage(ChatColor.RED + "AttributeRandomizer is already enabled.");
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("stop")) {
                if (AttributeRandomizer.isEnabled) {
                    AttributeRandomizer.isEnabled = false;
                    AttributeRandomizer.stopRandomizer();
                    sender.sendMessage(ChatColor.GREEN + "AttributeRandomizer has been stopped.");
                } else {
                    sender.sendMessage(ChatColor.RED + "AttributeRandomizer is not enabled.");
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("reset")) {
                if (sender instanceof Player player) {
                    AttributeRandomizer.resetAttributes(player);
                    sender.sendMessage(ChatColor.GREEN + "Attributes have been reset.");
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("period")) {
                sender.sendMessage(
                        "Modifies the time between each randomization when mode set to on-timer. " +
                        "Usage: /attribute-randomizer period <number>. " +
                        "Number can have suffixes 't', 's' and 'm' for ticks, seconds and minutes respectively."
                );
                return true;
            }
            if (args[0].equalsIgnoreCase("mode")) {
                sender.sendMessage(
                        "Modifies the randomization mode. " +
                        "On-timer - changes attributes at specified time intervals. " +
                        "On-death - changes attributes after player dies."
                );
                return true;
            }
            if (args[0].equalsIgnoreCase("amount")) {
                sender.sendMessage(
                        "Modifies the amount of attributes changing each time."
                );
                return true;
            }
        }
        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("period")) {
                String arg = args[1].toLowerCase();
                long period = parseTicks(arg);
                if (period < 0) {
                    sender.sendMessage(ChatColor.RED + "Invalid number format.");
                    return true;
                }
                AttributeRandomizer.period = period;

                if (AttributeRandomizer.isEnabled) {
                    AttributeRandomizer.stopRandomizer();
                    AttributeRandomizer.startRandomizer();
                }

                sender.sendMessage(ChatColor.GREEN + "Period has been set.");
                return true;
            }
            if (args[0].equalsIgnoreCase("mode")) {
                String arg = args[1].toLowerCase();
                if (arg.equalsIgnoreCase("on-death")) {
                    AttributeRandomizer.mode = AttributeRandomizer.Mode.ON_DEATH;
                } else if (arg.equalsIgnoreCase("on-timer")) {
                    AttributeRandomizer.mode = AttributeRandomizer.Mode.ON_TIMER;
                } else {
                    sender.sendMessage(ChatColor.RED + "Unknown mode!");
                    return true;
                }

                if (AttributeRandomizer.isEnabled) {
                    AttributeRandomizer.stopRandomizer();
                    AttributeRandomizer.startRandomizer();

                    BossBarTimerTick.removeBossBar();
                }

                sender.sendMessage(ChatColor.GREEN + "Mode has been changed.");
                return true;
            }
            if (args[0].equalsIgnoreCase("amount")) {
                String arg = args[1].toLowerCase();
                try {
                    AttributeRandomizer.amount = Integer.parseInt(arg);
                    AttributeRandomizer.updateDoChange();
                    sender.sendMessage(ChatColor.GREEN + "Amount has been changed.");
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Amount must be an integer.");
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("reset")) {
                String target = args[1];
                for (Entity entity: Bukkit.selectEntities(sender, target)) {
                    if (entity instanceof Player player) {
                        AttributeRandomizer.resetAttributes(player);
                        if (player != sender) {
                            player.sendMessage(ChatColor.GREEN + "Attributes have been reset.");
                        }
                    }
                }
                sender.sendMessage(ChatColor.GREEN + "Attributes have been reset.");
                return true;
            }
        }
        return false;
    }

    public static long parseTicks(String s) {
        try {
            if (Character.isDigit(s.charAt(s.length() - 1))) {
                return Long.parseLong(s);
            }
            long n = Long.parseLong(s.substring(0, s.length() - 1));
            if (n < 1) return -1;
            switch (s.charAt(s.length() - 1)) {
                case 't':
                    return n;
                case 's':
                    return n * 20;
                case 'm':
                    return n * 20 * 60;
                default:
                    return -1;
            }
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
