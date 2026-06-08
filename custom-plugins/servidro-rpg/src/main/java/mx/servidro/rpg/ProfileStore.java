package mx.servidro.rpg;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;

final class ProfileStore {
    private final File file;
    private final int maxClassLevel;
    private final int maxSpecializationLevel;
    private final int maxProfessionLevel;
    private final Map<UUID, PlayerProfile> profiles = new HashMap<>();

    ProfileStore(File dataFolder, int maxClassLevel, int maxSpecializationLevel, int maxProfessionLevel) {
        this.file = new File(dataFolder, "players.yml");
        this.maxClassLevel = maxClassLevel;
        this.maxSpecializationLevel = maxSpecializationLevel;
        this.maxProfessionLevel = maxProfessionLevel;
    }

    void load() {
        profiles.clear();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String key : yaml.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            PlayerProfile profile = new PlayerProfile();
            profile.setLoadedValues(
                    yaml.getString(key + ".class"),
                    yaml.getString(key + ".specialization"),
                    yaml.getInt(key + ".level", 1),
                    yaml.getInt(key + ".class-experience", 0),
                    yaml.getInt(key + ".specialization-level", 0),
                    yaml.getBoolean(key + ".starter-kit-claimed", false),
                    maxClassLevel,
                    maxSpecializationLevel);
            profile.setLoadedClaimedGuides(new LinkedHashSet<>(yaml.getStringList(key + ".claimed-guides")));
            profile.setLoadedClassXpFlask(
                    yaml.getString(key + ".active-class-xp-flask.tier"),
                    yaml.getLong(key + ".active-class-xp-flask.expires-at", 0L));
            for (Profession profession : Profession.values()) {
                profile.setLoadedProfessionLevel(
                        profession.id(),
                        yaml.getInt(key + ".profession-levels." + profession.id(), 1),
                        maxProfessionLevel);
                profile.setProfessionProgress(
                        profession.id(),
                        yaml.getInt(key + ".profession-levels." + profession.id(), 1),
                        yaml.getInt(key + ".profession-experience." + profession.id(), 0),
                        maxProfessionLevel);
            }
            String legacyProfession = yaml.getString(key + ".profession");
            if (legacyProfession != null) {
                profile.setLoadedProfessionLevel(
                        legacyProfession,
                        yaml.getInt(key + ".profession-level", 1),
                        maxProfessionLevel);
            }
            profiles.put(uuid, profile);
        }
    }

    PlayerProfile get(UUID uuid) {
        return profiles.computeIfAbsent(uuid, ignored -> new PlayerProfile());
    }

    void save() throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, PlayerProfile> entry : profiles.entrySet()) {
            String path = entry.getKey().toString();
            PlayerProfile profile = entry.getValue();
            yaml.set(path + ".class", profile.baseClass());
            yaml.set(path + ".specialization", profile.specialization());
            yaml.set(path + ".level", profile.level());
            yaml.set(path + ".class-experience", profile.classExperience());
            yaml.set(path + ".specialization-level", profile.specializationLevel());
            yaml.set(path + ".starter-kit-claimed", profile.starterKitClaimed());
            yaml.set(path + ".claimed-guides", List.copyOf(profile.claimedGuides()));
            yaml.set(path + ".active-class-xp-flask.tier", profile.activeClassXpFlaskTier());
            yaml.set(path + ".active-class-xp-flask.expires-at", profile.activeClassXpFlaskExpiresAt());
            for (Map.Entry<String, Integer> profession : profile.professionLevels().entrySet()) {
                yaml.set(path + ".profession-levels." + profession.getKey(), profession.getValue());
                yaml.set(path + ".profession-experience." + profession.getKey(),
                        profile.professionExperience(profession.getKey()));
            }
        }
        yaml.save(file);
    }
}
