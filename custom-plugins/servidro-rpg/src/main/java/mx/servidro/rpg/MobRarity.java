package mx.servidro.rpg;

import net.kyori.adventure.text.format.NamedTextColor;

enum MobRarity {
    NORMAL("Normal", NamedTextColor.GRAY),
    RARE("Raro", NamedTextColor.BLUE),
    ELITE("Elite", NamedTextColor.DARK_PURPLE),
    MINIBOSS("MiniBoss", NamedTextColor.GOLD);

    private final String displayName;
    private final NamedTextColor color;

    MobRarity(String displayName, NamedTextColor color) {
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

    static MobRarity find(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (MobRarity rarity : values()) {
            if (rarity.name().equalsIgnoreCase(value) || rarity.id().equalsIgnoreCase(value)) {
                return rarity;
            }
        }
        return null;
    }
}
