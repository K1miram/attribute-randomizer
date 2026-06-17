package com.kimiram.attributerandomizer.task;

import com.kimiram.attributerandomizer.AttributeRandomizer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BossBarTimerTick extends BukkitRunnable {
    public static BossBar bossBar = Bukkit.createBossBar("Timer", BarColor.WHITE, BarStyle.SOLID);
    public static long cnt = 0;

    @Override
    public void run() {
        cnt = (cnt + 1) % AttributeRandomizer.period;
        bossBar.setProgress(cnt * 1d / AttributeRandomizer.period);
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }
    }

    public static void removeBossBar() {
        bossBar.removeAll();
        cnt = 0;
    }
}
