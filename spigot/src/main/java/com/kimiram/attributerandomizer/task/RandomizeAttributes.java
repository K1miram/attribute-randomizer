package com.kimiram.attributerandomizer.task;

import com.kimiram.attributerandomizer.AttributeRandomizer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class RandomizeAttributes extends BukkitRunnable {
    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            AttributeRandomizer.resetAttributes(player);
            List<AttributeRandomizer.ChangedAttribute> changes = AttributeRandomizer.getChanges();
            AttributeRandomizer.setAttributes(player, changes);
            if (AttributeRandomizer.sendChangesToPlayers) {
                AttributeRandomizer.sendChangesToPlayer(player, changes);
            }
        }
    }
}
