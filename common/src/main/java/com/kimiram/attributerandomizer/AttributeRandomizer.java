package com.kimiram.attributerandomizer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
    public static long cnt;
    public static ServerBossEvent bossBar = new ServerBossEvent(
            Component.literal("Timer"),
            BossEvent.BossBarColor.WHITE,
            BossEvent.BossBarOverlay.PROGRESS
    );

    public static boolean isEnabled = false;
    public static Mode mode = Mode.ON_TIMER;
    public static List<Attribute> attributes = List.copyOf(DEFAULT_ATTRIBUTES);
    public static long period = 20 * 60 * 5;
    public static int amount = 5;
    public static boolean sendChangesToPlayers = true;

    public static void onTick(MinecraftServer server) {
        if (isEnabled && mode == Mode.ON_TIMER) {
            if (cnt >= period) {
                cnt = 0;
                server.getPlayerList().getPlayers().forEach(player -> {
                    resetAttributes(player);
                    List<ChangedAttribute> changes = getChanges();
                    setAttributes(player, changes);
                    if (sendChangesToPlayers) {
                        sendChangesToPlayer(player, changes);
                    }
                });
            }
            cnt++;
            for (ServerPlayer player: server.getPlayerList().getPlayers()) {
                bossBar.addPlayer(player);
            }
            bossBar.setProgress(cnt * 1f / AttributeRandomizer.period);
        }
    }

    public static void onDeath(LivingEntity entity, DamageSource damageSource) {
        if (entity instanceof ServerPlayer player) {
            if (isEnabled && mode == Mode.ON_DEATH) {
                resetAttributes(player);
                List<ChangedAttribute> attributes = getChanges();
                setAttributes(player, attributes);
                if (sendChangesToPlayers) {
                    sendChangesToPlayer(player, attributes);
                }
            }
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
            player.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(attribute.id)).get())
                    .setBaseValue(attribute.value);
        }
    }

    public static void sendChangesToPlayer(ServerPlayer player, List<ChangedAttribute> changes) {
        player.sendSystemMessage(Component.literal("Changed attributes:"));
        for (AttributeRandomizer.ChangedAttribute attribute: changes) {
            player.sendSystemMessage(Component.literal(" " + getNameById(attribute.id().replace("minecraft:", "")) + ": " +
                    new BigDecimal(attribute.value()).setScale(3, RoundingMode.HALF_UP).doubleValue()));
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
            player.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(attribute.id)).get())
                    .setBaseValue(attribute.defaultValue);
        }
    }

    public enum Mode {
        ON_TIMER,
        ON_DEATH;

        private static String modeToString(Mode mode) {
            if (mode == Mode.ON_DEATH) {
                return "on-death";
            } else {
                return "on-timer";
            }
        }

        private static Mode modeFromString(String string) {
            if (string.equals("on-death")) {
                return Mode.ON_DEATH;
            } else {
                return Mode.ON_TIMER;
            }
        }

        public static final Codec<Mode> CODEC = Codec.stringResolver(
                Mode::modeToString,
                Mode::modeFromString
        );
    }

    public record Attribute(String id, double minValue, double maxValue, double defaultValue) {
        public static final Codec<Attribute> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(Attribute::id),
                Codec.DOUBLE.fieldOf("minValue").forGetter(Attribute::minValue),
                Codec.DOUBLE.fieldOf("maxValue").forGetter(Attribute::maxValue),
                Codec.DOUBLE.fieldOf("defaultValue").forGetter(Attribute::defaultValue)
        ).apply(instance, Attribute::new));

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
