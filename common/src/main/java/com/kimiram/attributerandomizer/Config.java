package com.kimiram.attributerandomizer;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.kimiram.attributerandomizer.Constants.LOGGER;

// this code is shit sorry
public record Config(
        boolean isEnabled,
        AttributeRandomizer.Mode mode,
        long period,
        int amount,
        boolean sendChangesToPlayers,
        List<AttributeRandomizer.Attribute> attributes
) {
    public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("is_enabled").orElse(AttributeRandomizer.isEnabled).forGetter(Config::isEnabled),
            AttributeRandomizer.Mode.CODEC.fieldOf("mode").orElse(AttributeRandomizer.mode).forGetter(Config::mode),
            Codec.LONG.fieldOf("period").orElse(AttributeRandomizer.period).forGetter(Config::period),
            Codec.INT.fieldOf("amount").orElse(AttributeRandomizer.amount).forGetter(Config::amount),
            Codec.BOOL.fieldOf("send_changes_to_players").orElse(AttributeRandomizer.sendChangesToPlayers).forGetter(Config::sendChangesToPlayers),
            AttributeRandomizer.Attribute.CODEC.listOf().fieldOf("attributes").orElse(AttributeRandomizer.DEFAULT_ATTRIBUTES).forGetter(Config::attributes)
    ).apply(instance, Config::new));

    public static Path file;
    public static boolean failedToRead = false;
    public static List<AttributeRandomizer.Attribute> loadedAttributes = AttributeRandomizer.DEFAULT_ATTRIBUTES;

    public static void createDirectory() {
        try {
            Files.createDirectories(file.getParent());
        } catch (Exception e) {
            LOGGER.error("Could not create config directory because: {}", e.toString());
        }
    }

    public static void createFile() {
        try {
            Files.createFile(file);
        } catch (Exception e) {
            LOGGER.error("Could not create config file because: {}", e.toString());
        }
    }

    public static List<AttributeRandomizer.Attribute> verifyAttributes(List<AttributeRandomizer.Attribute> attrs) {
        List<AttributeRandomizer.Attribute> attributes = new ArrayList<>();
        for (AttributeRandomizer.Attribute attribute: attrs) {
            if (BuiltInRegistries.ATTRIBUTE.containsKey(Identifier.parse(attribute.id()))) {
                attributes.add(attribute);
            } else {
                LOGGER.warn("Attribute with id {} does not exist", attribute.id());
            }
        }
        return attributes;
    }

    public static void readConfig() {
        if (!file.getParent().toFile().exists()) {
            createDirectory();
            createFile();
            return;
        }
        if (!file.toFile().exists()) {
            createFile();
            return;
        }

        try {
            String string = Files.readString(file).replace(" ", "").replace("\n", "");
            DataResult<Pair<Config, JsonElement>> res = CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(string));

            Config config = res.getOrThrow().getFirst();

            AttributeRandomizer.isEnabled = config.isEnabled();
            AttributeRandomizer.mode = config.mode();
            AttributeRandomizer.period = config.period();
            AttributeRandomizer.amount = config.amount();
            AttributeRandomizer.sendChangesToPlayers = config.sendChangesToPlayers();
            loadedAttributes = config.attributes();
            AttributeRandomizer.attributes = verifyAttributes(config.attributes());
        } catch (Exception e) {
            failedToRead = true;
            LOGGER.error("Could not read config file because: {}", e.toString());
        }
    }

    public static String getString(JsonElement jsonElement) {
        String string = jsonElement.toString();
        String res = "";
        int cnt = 0;
        boolean in = false;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '\"') {
                in = !in;
            }
            if (string.charAt(i) == '}' || string.charAt(i) == ']') {
                cnt--;
                res += "\n" + " ".repeat(2 * cnt);
            }
            res += string.charAt(i);
            if (string.charAt(i) == ':' && !in) {
                res += " ";
            }
            if (string.charAt(i) == '{' || string.charAt(i) == '[') {
                cnt++;
                res += "\n" + " ".repeat(2 * cnt);
            }
            if (string.charAt(i) == ',') {
                res += "\n" + " ".repeat(2 * cnt);
            }
        }
        return res;
    }

    public static void writeConfig() {
        if (!file.getParent().toFile().exists()) {
            createDirectory();
            createFile();
        }
        if (!file.toFile().exists()) {
            createFile();
        }

        if (!failedToRead) {
            DataResult<JsonElement> res = CODEC.encodeStart(JsonOps.INSTANCE, new Config(
                    AttributeRandomizer.isEnabled,
                    AttributeRandomizer.mode,
                    AttributeRandomizer.period,
                    AttributeRandomizer.amount,
                    AttributeRandomizer.sendChangesToPlayers,
                    loadedAttributes
            ));

            try {
                Files.writeString(file, getString(res.getOrThrow()));
            } catch (Exception e) {
                LOGGER.error("Could not write config file because: {}", e.toString());
            }
        }
    }
}
