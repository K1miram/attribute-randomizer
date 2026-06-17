package com.kimiram.attributerandomizer;

import com.kimiram.attributerandomizer.task.BossBarTimerTick;
import com.kimiram.attributerandomizer.task.RandomizeAttributes;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class AttributeRandomizer {
    public static final List<Attribute> DEFAULT_ATTRIBUTES = List.of(
            new Attribute("armor", 0, 30, 0),
            new Attribute("attack_damage", 0, 64, 1),
            new Attribute("attack_knockback", 0, 5, 0),
            new Attribute("attack_speed", 0, 24, 4),
            new Attribute("block_break_speed", 0, 64, 1),
            new Attribute("block_interaction_range", 0, 64, 4.5),
            new Attribute("burning_time", 0, 16, 1),
            new Attribute("entity_interaction_range", 0, 64, 3),
            new Attribute("fall_damage_multiplier", 0, 20, 1),
            new Attribute("gravity", -0.025, 0.5, 0.08),
            new Attribute("jump_strength", 0, 8, 0.42),
            new Attribute("knockback_resistance", 0, 1, 0),
            new Attribute("max_health", 1, 128, 20),
            new Attribute("movement_speed", 0, 8, 0.1),
            new Attribute("safe_fall_distance", -16, 64, 3),
            new Attribute("scale", 0.0625, 4, 1),
            new Attribute("sneaking_speed", 0, 1, 0.3),
            new Attribute("step_height", 0, 10, 0.6),
            new Attribute("submerged_mining_speed", 0, 20, 0.2),
            new Attribute("water_movement_efficiency", 0, 1, 0)
    );
    public static Random rand = new Random();
    public static List<Boolean> doChange;
    public static int randomizerTaskId = -1;
    public static int timerTaskId = -1;

    public static boolean isEnabled = false;
    public static Mode mode = Mode.ON_TIMER;
    public static List<Attribute> attributes = List.copyOf(DEFAULT_ATTRIBUTES);
    public static long period = 20 * 60 * 5;
    public static int amount = 5;
    public static boolean sendChangesToPlayers = true;

    public static void startRandomizer() {
        updateDoChange();
        if (mode == Mode.ON_TIMER) {
            RandomizeAttributes ra = new RandomizeAttributes();
            ra.runTaskTimer(Main.INSTANCE, 0, period);
            randomizerTaskId = ra.getTaskId();
            BossBarTimerTick bb = new BossBarTimerTick();
            bb.runTaskTimer(Main.INSTANCE, 0, 1);
            timerTaskId = bb.getTaskId();
        }
    }

    public static void stopRandomizer() {
        if (mode == Mode.ON_TIMER) {
            Bukkit.getScheduler().cancelTask(randomizerTaskId);
            randomizerTaskId = -1;
            Bukkit.getScheduler().cancelTask(timerTaskId);
            timerTaskId = -1;
            BossBarTimerTick.removeBossBar();
            BossBarTimerTick.cnt = 0;
        }
    }

    public static void updateDoChange() {
        doChange = new ArrayList<>();
        for (int i = 0; i < attributes.size(); i++) {
            doChange.add(i < amount);
        }
    }

    public static List<ChangedAttribute> getChanges() {
        List<ChangedAttribute> changes = new ArrayList<>();
        Collections.shuffle(doChange);
        for (int i = 0; i < doChange.size(); i++) {
            if (doChange.get(i)) {
                changes.add(attributes.get(i).changeAttribute());
            }
        }
        return changes;
    }

    public static void setAttributes(Player player, List<ChangedAttribute> changes) {
        for (ChangedAttribute attribute: changes) {
            player.getAttribute(Registry.ATTRIBUTE.get(NamespacedKey.fromString(attribute.id)))
                    .setBaseValue(attribute.value);
        }
    }

    public static void sendChangesToPlayer(Player player, List<ChangedAttribute> changes) {
        player.sendMessage("Changed attributes:");
        for (AttributeRandomizer.ChangedAttribute attribute: changes) {
            player.sendMessage(" " + getNameById(attribute.id()) + ": " +
                    new BigDecimal(attribute.value()).setScale(3, RoundingMode.HALF_UP).doubleValue());
        }
    }

    public static String getNameById(String id) {
        StringBuilder name = new StringBuilder();
        boolean isUpper = true;
        for (char c: id.toCharArray()) {
            if (Character.isLetter(c)) {
                if (isUpper) {
                    name.append(Character.toUpperCase(c));
                    isUpper = false;
                } else {
                    name.append(c);
                }
            } else {
                if (c == '_') {
                    name.append(' ');
                } else {
                    name.append(c);
                }
                isUpper = true;
            }
        }
        return name.toString();
    }

    public static void resetAttributes(Player player) {
        for (Attribute attribute : attributes) {
            player.getAttribute(Registry.ATTRIBUTE.get(NamespacedKey.fromString(attribute.id)))
                    .setBaseValue(attribute.defaultValue);
        }
    }

    public enum Mode {
        ON_TIMER,
        ON_DEATH
    }

    public record Attribute(String id, double minValue, double maxValue, double defaultValue) {
        public ChangedAttribute changeAttribute() {
            ChangedAttribute attribute;
            if (minValue < defaultValue && defaultValue < maxValue) {
                boolean increase = rand.nextBoolean();
                if (increase) {
                    attribute = new ChangedAttribute(id, defaultValue + rand.nextDouble() * (maxValue - defaultValue));
                } else {
                    attribute = new ChangedAttribute(id, minValue + rand.nextDouble() * (defaultValue - minValue));
                }
            } else {
                attribute = new ChangedAttribute(id, minValue + rand.nextDouble() * (maxValue - minValue));
            }
            return attribute;
        }
    }

    public record ChangedAttribute(String id, double value) {
    }
}
