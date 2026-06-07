package mx.servidro.rpg;

import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

final class ProgressionGate {
    enum Action {
        EQUIP_OR_USE,
        CRAFT,
        PLACE
    }

    record Denial(String requirement, int requiredLevel, int currentLevel) {
    }

    private static final Set<Material> EXPEDITION_UTILITIES = EnumSet.of(
            Material.COMPASS, Material.RECOVERY_COMPASS, Material.SPYGLASS);
    private static final Set<Material> ADVANCED_CONSUMABLES = EnumSet.of(
            Material.BREWING_STAND, Material.GOLDEN_APPLE, Material.GOLDEN_CARROT);
    private static final Set<Material> ADVANCED_CROPS = EnumSet.of(
            Material.NETHER_WART, Material.COCOA_BEANS, Material.MELON_SEEDS, Material.PUMPKIN_SEEDS);

    private final FileConfiguration config;

    ProgressionGate(FileConfiguration config) {
        this.config = config;
    }

    Denial check(PlayerProfile profile, Material material, Action action) {
        if (action == Action.EQUIP_OR_USE && isCombatEquipment(material)) {
            return compare("nivel de clase", profile.level(),
                    levelForTier("progression-gates.class-equipment", tier(material)));
        }
        if (action == Action.CRAFT) {
            String tier = tier(material);
            if (isCombatEquipment(material) && tier != null) {
                return compare("herrero", profile.professionLevel("herrero"),
                        levelForTier("progression-gates.profession-crafting.herrero", tier));
            }
            if (ADVANCED_CONSUMABLES.contains(material)) {
                return compare("alquimista", profile.professionLevel("alquimista"),
                        config.getInt("progression-gates.profession-crafting.alquimista.advanced-consumables", 8));
            }
            if (ADVANCED_CROPS.contains(material)) {
                return compare("agricultor", profile.professionLevel("agricultor"),
                        config.getInt("progression-gates.profession-crafting.agricultor.advanced-crops", 8));
            }
        }
        if (action == Action.PLACE) {
            String mineralTier = blockTier(material);
            if (mineralTier != null) {
                return compare("minero", profile.professionLevel("minero"),
                        levelForTier("progression-gates.profession-use.minero", mineralTier + "-blocks"));
            }
            if (EXPEDITION_UTILITIES.contains(material)) {
                return compare("explorador", profile.professionLevel("explorador"),
                        config.getInt("progression-gates.profession-use.explorador.expedition-utilities", 8));
            }
        }
        return null;
    }

    private Denial compare(String requirement, int currentLevel, int requiredLevel) {
        return requiredLevel > currentLevel ? new Denial(requirement, requiredLevel, currentLevel) : null;
    }

    private int levelForTier(String path, String tier) {
        return tier == null ? 0 : config.getInt(path + "." + tier, 0);
    }

    private boolean isCombatEquipment(Material material) {
        String name = material.name();
        return name.endsWith("_SWORD")
                || name.endsWith("_AXE")
                || name.endsWith("_PICKAXE")
                || name.endsWith("_SHOVEL")
                || name.endsWith("_HOE")
                || name.endsWith("_HELMET")
                || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS")
                || name.endsWith("_BOOTS")
                || material == Material.SHIELD;
    }

    private String tier(Material material) {
        String name = material.name();
        if (name.startsWith("IRON_") || material == Material.SHIELD) {
            return "iron";
        }
        if (name.startsWith("GOLDEN_")) {
            return "gold";
        }
        if (name.startsWith("DIAMOND_")) {
            return "diamond";
        }
        return null;
    }

    private String blockTier(Material material) {
        return switch (material) {
            case IRON_BLOCK, RAW_IRON_BLOCK -> "iron";
            case GOLD_BLOCK, RAW_GOLD_BLOCK -> "gold";
            case DIAMOND_BLOCK -> "diamond";
            default -> null;
        };
    }
}
