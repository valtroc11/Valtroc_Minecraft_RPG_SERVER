package mx.servidro.rpg;

import java.util.List;

enum MobSkill {
    CARGA,
    ONDA_HELADA,
    LLAMADO_CORRUPTO,
    RETROCESO_TACTICO,
    GOLPE_TERRITORIAL;

    String id() {
        return name().toLowerCase();
    }

    static MobSkill find(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (MobSkill skill : values()) {
            if (skill.name().equalsIgnoreCase(value) || skill.id().equalsIgnoreCase(value)) {
                return skill;
            }
        }
        return null;
    }

    static List<MobSkill> forType(String entityTypeName) {
        return switch (entityTypeName) {
            case "ZOMBIE", "HUSK", "DROWNED", "SPIDER", "CAVE_SPIDER" ->
                    List.of(CARGA, LLAMADO_CORRUPTO);
            case "CREEPER" ->
                    List.of(CARGA, ONDA_HELADA, LLAMADO_CORRUPTO);
            case "SKELETON", "STRAY" ->
                    List.of(RETROCESO_TACTICO, ONDA_HELADA);
            case "POLAR_BEAR", "WOLF", "IRON_GOLEM", "LLAMA", "TRADER_LLAMA" ->
                    List.of(GOLPE_TERRITORIAL, CARGA);
            case "PIGLIN", "ZOMBIFIED_PIGLIN" ->
                    List.of(LLAMADO_CORRUPTO, CARGA);
            case "ENDERMAN", "BEE" ->
                    List.of(ONDA_HELADA, RETROCESO_TACTICO);
            default ->
                    List.of(ONDA_HELADA, LLAMADO_CORRUPTO);
        };
    }
}
