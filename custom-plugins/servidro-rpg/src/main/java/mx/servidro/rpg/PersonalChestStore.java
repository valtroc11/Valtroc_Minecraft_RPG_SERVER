package mx.servidro.rpg;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

final class PersonalChestStore {
    enum Rarity {
        COMMON("common"),
        RARE("rare"),
        CORRUPTED("corrupted");

        private final String id;

        Rarity(String id) {
            this.id = id;
        }

        String id() {
            return id;
        }

        static Rarity find(String value) {
            for (Rarity rarity : values()) {
                if (rarity.id.equalsIgnoreCase(value)
                        || (rarity == COMMON && value.equalsIgnoreCase("comun"))
                        || (rarity == RARE && value.equalsIgnoreCase("raro"))
                        || (rarity == CORRUPTED && value.equalsIgnoreCase("corrompido"))) {
                    return rarity;
                }
            }
            return null;
        }
    }

    record ChestDefinition(Rarity rarity, Map<UUID, Long> lootedAt) {
    }

    private final File file;
    private final Map<String, ChestDefinition> chests = new HashMap<>();

    PersonalChestStore(File dataFolder) {
        file = new File(dataFolder, "personal-chests.yml");
    }

    void load() {
        chests.clear();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = yaml.getConfigurationSection("chests");
        if (root == null) {
            return;
        }
        for (String key : root.getKeys(false)) {
            Rarity rarity = Rarity.find(yaml.getString("chests." + key + ".rarity", ""));
            if (rarity == null) {
                continue;
            }
            Map<UUID, Long> lootedAt = new HashMap<>();
            ConfigurationSection cooldowns = yaml.getConfigurationSection("chests." + key + ".looted-at");
            if (cooldowns != null) {
                for (String uuidValue : cooldowns.getKeys(false)) {
                    try {
                        lootedAt.put(UUID.fromString(uuidValue), cooldowns.getLong(uuidValue));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
            chests.put(key, new ChestDefinition(rarity, lootedAt));
        }
    }

    ChestDefinition get(Location location) {
        return chests.get(key(location));
    }

    void register(Location location, Rarity rarity) {
        chests.put(key(location), new ChestDefinition(rarity, new HashMap<>()));
    }

    boolean remove(Location location) {
        return chests.remove(key(location)) != null;
    }

    void recordLoot(Location location, UUID player, long timestamp) {
        ChestDefinition chest = get(location);
        if (chest != null) {
            chest.lootedAt().put(player, timestamp);
        }
    }

    void save() throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<String, ChestDefinition> entry : chests.entrySet()) {
            String path = "chests." + entry.getKey();
            yaml.set(path + ".rarity", entry.getValue().rarity().id());
            for (Map.Entry<UUID, Long> cooldown : entry.getValue().lootedAt().entrySet()) {
                yaml.set(path + ".looted-at." + cooldown.getKey(), cooldown.getValue());
            }
        }
        yaml.save(file);
    }

    private String key(Location location) {
        return location.getWorld().getName() + ";" + location.getBlockX() + ";"
                + location.getBlockY() + ";" + location.getBlockZ();
    }
}

