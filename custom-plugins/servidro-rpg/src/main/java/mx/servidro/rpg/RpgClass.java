package mx.servidro.rpg;

import java.util.Arrays;
import java.util.List;

enum RpgClass {
    GUERRERO("guerrero", List.of("guardian", "berserker")),
    EXPLORADOR("explorador", List.of("cazador", "picaro")),
    MAGO("mago", List.of("piromante", "arcanista")),
    CLERIGO("clerigo", List.of("paladin", "druida"));

    private final String id;
    private final List<String> specializations;

    RpgClass(String id, List<String> specializations) {
        this.id = id;
        this.specializations = specializations;
    }

    String id() {
        return id;
    }

    List<String> specializations() {
        return specializations;
    }

    static RpgClass find(String id) {
        return Arrays.stream(values())
                .filter(value -> value.id.equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    static RpgClass owningSpecialization(String specialization) {
        return Arrays.stream(values())
                .filter(value -> value.specializations.contains(specialization.toLowerCase()))
                .findFirst()
                .orElse(null);
    }
}

