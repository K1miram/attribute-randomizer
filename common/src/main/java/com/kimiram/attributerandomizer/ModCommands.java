package com.kimiram.attributerandomizer;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.function.Predicate;

public class ModCommands {
    public static String modVersion;

    public static Command<CommandSourceStack> arInfo = context -> {
        CommandSourceStack source = context.getSource();
        source.sendSystemMessage(Component.literal("AttributeRandomizer Info:"));
        source.sendSystemMessage(Component.literal(" Mod version: " + modVersion));
        source.sendSystemMessage(Component.literal(
                " Status: " + (AttributeRandomizer.isEnabled ?
                        ChatFormatting.GREEN + "On" :
                        ChatFormatting.RED + "Off"
                )
        ));
        source.sendSystemMessage(Component.literal(
                " Mode: " + AttributeRandomizer.mode.toString().toLowerCase().replace('_', '-')
        ));
        if (AttributeRandomizer.mode == AttributeRandomizer.Mode.ON_TIMER) {
            source.sendSystemMessage(Component.literal(" Period: " +
                    AttributeRandomizer.period / 20 / 60 + " minutes " +
                    AttributeRandomizer.period / 20 % 60 + " seconds " +
                    AttributeRandomizer.period % 20 + " ticks "
            ));
        }
        source.sendSystemMessage(Component.literal(" Amount: " + AttributeRandomizer.amount));
        return 1;
    };


    public static Command<CommandSourceStack> arStart = context -> {
        if (!AttributeRandomizer.isEnabled) {
            AttributeRandomizer.cnt = AttributeRandomizer.period;
            AttributeRandomizer.isEnabled = true;

            AttributeRandomizer.updateDoChange();

            context.getSource().sendSystemMessage(Component.literal(
                    ChatFormatting.GREEN + "AttributeRandomizer has been started.")
            );
        } else {
            context.getSource().sendSystemMessage(Component.literal(
                    ChatFormatting.RED + "AttributeRandomizer is already enabled.")
            );
        }

        return 1;
    };


    public static Command<CommandSourceStack> arStop = context -> {
        if (AttributeRandomizer.isEnabled) {
            AttributeRandomizer.isEnabled = false;

            AttributeRandomizer.bossBar.removeAllPlayers();

            context.getSource().sendSystemMessage(Component.literal(
                    ChatFormatting.GREEN + "AttributeRandomizer has been stopped.")
            );
        } else {
            context.getSource().sendSystemMessage(Component.literal(
                    ChatFormatting.RED + "AttributeRandomizer is not enabled."
            ));
        }

        return 1;
    };


    public static Command<CommandSourceStack> arAmountInfo = context -> {
        context.getSource().sendSystemMessage(Component.literal(
                "Modifies the amount of attributes changing each time."
        ));

        return 1;
    };

    public static Command<CommandSourceStack> arSetAmount = context -> {
        AttributeRandomizer.amount = IntegerArgumentType.getInteger(context, "amount");

        AttributeRandomizer.updateDoChange();

        context.getSource().sendSystemMessage(Component.literal(
                ChatFormatting.GREEN + "Amount has been changed."
        ));
        return 1;
    };


    public static Command<CommandSourceStack> arPeriodInfo = context -> {
        context.getSource().sendSystemMessage(Component.literal(
                "Modifies the time between each randomization when mode set to on-timer. " +
                "Usage: /attribute-randomizer period <number>. " +
                "Number can have suffixes 't', 's' and 'm' for ticks, seconds and minutes respectively."
        ));

        return 1;
    };

    public static SuggestionProvider<CommandSourceStack> periodSuggestion = (context, builder) -> {
        builder.suggest("20t");
        builder.suggest("5s");
        builder.suggest("5m");
        return builder.buildFuture();
    };

    public static Command<CommandSourceStack> arSetPeriod = context -> {
        String arg = StringArgumentType.getString(context, "period").toLowerCase();
        long period = parseTicks(arg);
        if (period < 0) {
            context.getSource().sendSystemMessage(Component.literal(
                    ChatFormatting.RED + "Invalid number format."
            ));
            return 0;
        }
        AttributeRandomizer.period = period;
        context.getSource().sendSystemMessage(Component.literal(
                ChatFormatting.GREEN + "Period has been set."
        ));
        return 1;
    };

    public static long parseTicks(String s) {
        try {
            if (Character.isDigit(s.charAt(s.length() - 1))) {
                return Long.parseLong(s);
            }
            long n = Long.parseLong(s.substring(0, s.length() - 1));
            if (n < 1) return -1;
            return switch (s.charAt(s.length() - 1)) {
                case 't' -> n;
                case 's' -> n * 20;
                case 'm' -> n * 20 * 60;
                default -> -1;
            };
        } catch (NumberFormatException e) {
            return -1;
        }
    }


    public static Command<CommandSourceStack> arModeInfo = context -> {
        context.getSource().sendSystemMessage(Component.literal(
                "Modifies the randomization mode. " +
                "On-timer - changes attributes at specified time intervals. " +
                "On-death - changes attributes after player dies."
        ));

        return 1;
    };

    public static SuggestionProvider<CommandSourceStack> modeSuggestion = (context, builder) -> {
        builder.suggest("on-timer");
        builder.suggest("on-death");
        return builder.buildFuture();
    };

    public static Command<CommandSourceStack> arSetMode = context -> {
        String mode = StringArgumentType.getString(context, "mode").toLowerCase();
        if (mode.equals("on-death")) {
            AttributeRandomizer.mode = AttributeRandomizer.Mode.ON_DEATH;
        } else if (mode.equals("on-timer")) {
            AttributeRandomizer.mode = AttributeRandomizer.Mode.ON_TIMER;
        } else {
            context.getSource().sendSystemMessage(Component.literal(
                    ChatFormatting.RED + "Unknown mode!"
            ));
            return 0;
        }

        AttributeRandomizer.bossBar.removeAllPlayers();

        context.getSource().sendSystemMessage(Component.literal(
                ChatFormatting.GREEN + "Mode has been changed."
        ));
        return 1;
    };


    public static Command<CommandSourceStack> arReset = context -> {
        if (context.getSource().getPlayer() != null) {
            AttributeRandomizer.resetAttributes(context.getSource().getPlayer());
            context.getSource().sendSystemMessage(Component.literal(
                    ChatFormatting.GREEN + "Attributes have been reset."
            ));
            return 1;
        }
        return 0;
    };

    public static SuggestionProvider<CommandSourceStack> resetTargetSuggestion = (context, builder) -> {
        builder.suggest("@s");
        builder.suggest("@a");
        builder.suggest("@p");
        builder.suggest("@r");
        for (String player: context.getSource().getOnlinePlayerNames()) {
            builder.suggest(player);
        }
        return builder.buildFuture();
    };

    public static Command<CommandSourceStack> arResetTarget = context -> {
        for (ServerPlayer player: EntityArgument.getPlayers(context, "target")) {
            AttributeRandomizer.resetAttributes(player);
            if (player != context.getSource().getPlayer()) {
                player.sendSystemMessage(Component.literal(
                        ChatFormatting.GREEN + "Attributes have been reset."
                ));
            }
        }
        context.getSource().sendSystemMessage(Component.literal(
                ChatFormatting.GREEN + "Attributes have been reset."
        ));
        return 1;
    };

    public static Predicate<CommandSourceStack> hasOp = source ->
            source.permissions().hasPermission(Permissions.COMMANDS_ADMIN);


    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("attribute-randomizer").executes(arInfo).requires(hasOp)
                        .then(Commands.literal("start").executes(arStart))
                        .then(Commands.literal("stop").executes(arStop))
                        .then(Commands.literal("amount").executes(arAmountInfo)
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(arSetAmount)))
                        .then(Commands.literal("period").executes(arPeriodInfo)
                                .then(Commands.argument("period", StringArgumentType.word())
                                        .suggests(periodSuggestion)
                                        .executes(arSetPeriod)))
                        .then(Commands.literal("mode").executes(arModeInfo)
                                .then(Commands.argument("mode", StringArgumentType.string())
                                        .suggests(modeSuggestion)
                                        .executes(arSetMode)))
                        .then(Commands.literal("reset").executes(arReset)
                                .then(Commands.argument("target", EntityArgument.players())
                                        .suggests(resetTargetSuggestion)
                                        .executes(arResetTarget)))
        );
    }
}
