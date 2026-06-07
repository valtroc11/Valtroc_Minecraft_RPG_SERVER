package mx.servidro.rpg;

import java.util.List;
import net.kyori.adventure.text.format.NamedTextColor;

enum MobAffix {
    BRUTAL("Brutal", NamedTextColor.RED),
    ROBUSTO("Robusto", NamedTextColor.DARK_GREEN),
    VELOZ("Veloz", NamedTextColor.AQUA),
    CONGELANTE("Congelante", NamedTextColor.BLUE),
    VAMPIRICO("Vampirico", NamedTextColor.DARK_RED);

    private final String displayName;
    private final NamedTextColor color;

    MobAffix(String displayName, NamedTextColor color) {
        this.displayName = displayName;
        this.color = color;
    }

    String id() {
        return name().toLowerCase();
    }

    String displayName() {
        return displayName;
    }

    NamedTextColor color() {
        return color;
    }

    static MobAffix find(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (MobAffix affix : values()) {
            if (affix.name().equalsIgnoreCase(value) || affix.id().equalsIgnoreCase(value)) {
                return affix;
            }
        }
        return null;
    }

    static List<MobAffix> forType(String entityTypeName) {
        return switch (entityTypeName) {
            case "ZOMBIE", "HUSK", "DROWNED", "SKELETON", "STRAY", "SPIDER", "CAVE_SPIDER" ->
                    List.of(BRUTAL, ROBUSTO, VELOZ, CONGELANTE, VAMPIRICO);
            case "POLAR_BEAR", "WOLF", "BEE", "ENDERMAN", "PIGLIN", "ZOMBIFIED_PIGLIN", "IRON_GOLEM",
                    "LLAMA", "TRADER_LLAMA" ->
                    List.of(BRUTAL, ROBUSTO, VELOZ, CONGELANTE);
            default -> List.of(BRUTAL, ROBUSTO, VELOZ);
        };
    }
}
