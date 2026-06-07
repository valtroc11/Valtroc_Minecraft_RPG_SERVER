package mx.servidro.rpg;

import java.util.Arrays;

enum Profession {
    MINERO("minero"),
    HERRERO("herrero"),
    ALQUIMISTA("alquimista"),
    AGRICULTOR("agricultor"),
    EXPLORADOR("explorador");

    private final String id;

    Profession(String id) {
        this.id = id;
    }

    String id() {
        return id;
    }

    static Profession find(String id) {
        return Arrays.stream(values())
                .filter(value -> value.id.equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }
}

