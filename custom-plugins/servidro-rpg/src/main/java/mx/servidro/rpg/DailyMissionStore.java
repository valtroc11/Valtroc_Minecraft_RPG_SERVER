package mx.servidro.rpg;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

final class DailyMissionStore {
    enum Activity {
        MINE,
        SMITH,
        HARVEST
    }

    record Completion(String description, int reward) {
    }

    private enum Template {
        MINE_STONE(Activity.MINE, "Cantero aprendiz", "Extrae %d bloques de piedra natural.", 1, 120, 75),
        MINE_COAL(Activity.MINE, "Combustible para la forja", "Extrae %d minerales de carbon.", 1, 16, 90),
        MINE_IRON(Activity.MINE, "Reservas de hierro", "Extrae %d minerales de hierro.", 5, 20, 110),
        MINE_GOLD(Activity.MINE, "Oro del reino", "Extrae %d minerales de oro.", 10, 10, 145),
        MINE_GEMS(Activity.MINE, "Gemas del reino", "Extrae %d diamantes o esmeraldas.", 15, 4, 200),
        SMITH_TRAINING(Activity.SMITH, "Primer encargo", "Fabrica %d herramientas o piezas de entrenamiento.", 1, 6, 90),
        SMITH_IRON(Activity.SMITH, "Equipo para la guardia", "Fabrica %d piezas de hierro.", 5, 4, 150),
        SMITH_GOLD(Activity.SMITH, "Pedido ceremonial", "Fabrica %d piezas de oro.", 10, 4, 175),
        SMITH_DIAMOND(Activity.SMITH, "Arsenal de elite", "Fabrica %d piezas de diamante.", 15, 3, 220),
        FARM_COMMON(Activity.HARVEST, "Despensa local", "Cosecha %d cultivos comunes maduros.", 1, 64, 80),
        FARM_ADVANCED(Activity.HARVEST, "Cultivos especiales", "Cosecha %d cultivos avanzados maduros.", 8, 32, 140);

        private final Activity activity;
        private final String title;
        private final String description;
        private final int minLevel;
        private final int target;
        private final int reward;

        Template(Activity activity, String title, String description, int minLevel, int target, int reward) {
            this.activity = activity;
            this.title = title;
            this.description = description;
            this.minLevel = minLevel;
            this.target = target;
            this.reward = reward;
        }

        String description() {
            return title + ": " + description.formatted(target);
        }

        boolean matches(Material material) {
            String name = material.name();
            return switch (this) {
                case MINE_STONE -> material == Material.STONE || material == Material.DEEPSLATE
                        || material == Material.TUFF || material == Material.GRANITE
                        || material == Material.DIORITE || material == Material.ANDESITE
                        || material == Material.CALCITE || material == Material.DRIPSTONE_BLOCK
                        || material == Material.BLACKSTONE || material == Material.BASALT
                        || material == Material.SMOOTH_BASALT || material == Material.NETHERRACK
                        || material == Material.END_STONE;
                case MINE_COAL -> material == Material.COAL_ORE || material == Material.DEEPSLATE_COAL_ORE;
                case MINE_IRON -> material == Material.IRON_ORE || material == Material.DEEPSLATE_IRON_ORE;
                case MINE_GOLD -> material == Material.GOLD_ORE || material == Material.DEEPSLATE_GOLD_ORE
                        || material == Material.NETHER_GOLD_ORE;
                case MINE_GEMS -> material == Material.DIAMOND_ORE || material == Material.DEEPSLATE_DIAMOND_ORE
                        || material == Material.EMERALD_ORE || material == Material.DEEPSLATE_EMERALD_ORE;
                case SMITH_TRAINING -> name.startsWith("WOODEN_") || name.startsWith("STONE_");
                case SMITH_IRON -> name.startsWith("IRON_") || name.startsWith("CHAINMAIL_")
                        || material == Material.SHIELD;
                case SMITH_GOLD -> name.startsWith("GOLDEN_");
                case SMITH_DIAMOND -> name.startsWith("DIAMOND_");
                case FARM_COMMON -> material == Material.WHEAT || material == Material.CARROTS
                        || material == Material.POTATOES || material == Material.BEETROOTS;
                case FARM_ADVANCED -> material == Material.COCOA || material == Material.NETHER_WART
                        || material == Material.MELON || material == Material.PUMPKIN;
            };
        }
    }

    private static final class ActiveMission {
        private final Template template;
        private int progress;
        private boolean rewarded;

        private ActiveMission(Template template) {
            this.template = template;
        }
    }

    private final File file;
    private final Map<UUID, Map<Activity, ActiveMission>> missions = new java.util.HashMap<>();
    private LocalDate date = LocalDate.now();

    DailyMissionStore(File dataFolder) {
        file = new File(dataFolder, "daily-missions.yml");
    }

    void load() {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        date = LocalDate.parse(yaml.getString("date", LocalDate.now().toString()));
        for (String playerKey : yaml.getConfigurationSection("players") == null
                ? List.<String>of() : yaml.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(playerKey);
                Map<Activity, ActiveMission> playerMissions = new EnumMap<>(Activity.class);
                for (Activity activity : Activity.values()) {
                    String path = "players." + playerKey + "." + activity.name();
                    String templateName = yaml.getString(path + ".template");
                    if (templateName == null) {
                        continue;
                    }
                    ActiveMission mission = new ActiveMission(Template.valueOf(templateName));
                    mission.progress = yaml.getInt(path + ".progress", 0);
                    mission.rewarded = yaml.getBoolean(path + ".rewarded", false);
                    playerMissions.put(activity, mission);
                }
                missions.put(uuid, playerMissions);
            } catch (IllegalArgumentException ignored) {
                // Ignore obsolete or malformed mission records.
            }
        }
    }

    void save() throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("date", date.toString());
        for (Map.Entry<UUID, Map<Activity, ActiveMission>> player : missions.entrySet()) {
            for (Map.Entry<Activity, ActiveMission> entry : player.getValue().entrySet()) {
                String path = "players." + player.getKey() + "." + entry.getKey().name();
                yaml.set(path + ".template", entry.getValue().template.name());
                yaml.set(path + ".progress", entry.getValue().progress);
                yaml.set(path + ".rewarded", entry.getValue().rewarded);
            }
        }
        yaml.save(file);
    }

    List<String> describe(Player player, PlayerProfile profile) {
        Map<Activity, ActiveMission> active = assignments(player.getUniqueId(), profile);
        List<String> lines = new ArrayList<>();
        for (Activity activity : Activity.values()) {
            ActiveMission mission = active.get(activity);
            lines.add(mission.template.description() + " [" + Math.min(mission.progress, mission.template.target)
                    + "/" + mission.template.target + "] | " + mission.template.reward + " coronas"
                    + (mission.rewarded ? " | completada" : ""));
        }
        return lines;
    }

    Completion progress(Player player, PlayerProfile profile, Activity activity, Material material, int amount) {
        ActiveMission mission = assignments(player.getUniqueId(), profile).get(activity);
        if (mission.rewarded || !mission.template.matches(material)) {
            return null;
        }
        mission.progress = Math.min(mission.template.target, mission.progress + Math.max(1, amount));
        if (mission.progress < mission.template.target) {
            return null;
        }
        mission.rewarded = true;
        return new Completion(mission.template.description(), mission.template.reward);
    }

    private Map<Activity, ActiveMission> assignments(UUID uuid, PlayerProfile profile) {
        rotateIfNeeded();
        return missions.computeIfAbsent(uuid, ignored -> {
            Map<Activity, ActiveMission> selected = new EnumMap<>(Activity.class);
            selected.put(Activity.MINE, select(Activity.MINE, profile.professionLevel("minero")));
            selected.put(Activity.SMITH, select(Activity.SMITH, profile.professionLevel("herrero")));
            selected.put(Activity.HARVEST, select(Activity.HARVEST, profile.professionLevel("agricultor")));
            return selected;
        });
    }

    private ActiveMission select(Activity activity, int level) {
        List<Template> options = java.util.Arrays.stream(Template.values())
                .filter(template -> template.activity == activity && template.minLevel <= level)
                .toList();
        return new ActiveMission(options.get(ThreadLocalRandom.current().nextInt(options.size())));
    }

    private void rotateIfNeeded() {
        LocalDate today = LocalDate.now();
        if (!today.equals(date)) {
            date = today;
            missions.clear();
        }
    }
}
