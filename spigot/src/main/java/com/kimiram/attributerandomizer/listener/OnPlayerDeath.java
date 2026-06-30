package com.kimiram.attributerandomizer.listener;

import com.kimiram.attributerandomizer.AttributeRandomizer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;

public class OnPlayerDeath implements Listener {
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (AttributeRandomizer.isEnabled && AttributeRandomizer.mode == AttributeRandomizer.Mode.ON_DEATH) {
                AttributeRandomizer.resetAttributes(player);
                List<AttributeRandomizer.ChangedAttribute> changes = AttributeRandomizer.getChanges();
                AttributeRandomizer.setAttributes(player, changes);
                if (AttributeRandomizer.sendChangesToPlayers) {
                    AttributeRandomizer.sendChangesToPlayer(player, changes);
                }
            }
        }
    }
}
