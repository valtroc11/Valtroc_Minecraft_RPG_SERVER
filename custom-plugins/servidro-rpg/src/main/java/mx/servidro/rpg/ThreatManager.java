package mx.servidro.rpg;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

final class ThreatManager {
    private final NamespacedKey managedMobKey;
    private final Map<UUID, Map<UUID, Double>> tables = new HashMap<>();

    ThreatManager(JavaPlugin plugin) {
        managedMobKey = new NamespacedKey(plugin, "managed_mob");
    }

    void markManaged(LivingEntity mob) {
        mob.getPersistentDataContainer().set(managedMobKey, PersistentDataType.BYTE, (byte) 1);
    }

    boolean isManaged(LivingEntity mob) {
        return mob.getPersistentDataContainer().has(managedMobKey, PersistentDataType.BYTE);
    }

    void addThreat(LivingEntity mob, Player player, double amount) {
        if (!isManaged(mob) || amount <= 0) {
            return;
        }
        tables.computeIfAbsent(mob.getUniqueId(), ignored -> new HashMap<>())
                .merge(player.getUniqueId(), amount, Double::sum);
        retarget(mob);
    }

    void addHealingThreat(Player healer, double amount) {
        if (amount <= 0) {
            return;
        }
        for (UUID mobId : tables.keySet()) {
            LivingEntity mob = findLivingEntity(mobId);
            if (mob != null && mob.getWorld() == healer.getWorld()
                    && mob.getLocation().distanceSquared(healer.getLocation()) <= 24 * 24) {
                addThreat(mob, healer, amount);
            }
        }
    }

    List<LivingEntity> tauntNearby(Player player, double radius, double bonusThreat) {
        List<LivingEntity> affected = new ArrayList<>();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity mob && !(mob instanceof Player)) {
                markManaged(mob);
                taunt(mob, player, bonusThreat);
                affected.add(mob);
            }
        }
        return affected;
    }

    LivingEntity tauntTarget(Player player, int range, double bonusThreat) {
        LivingEntity mob = player.getTargetEntity(range) instanceof LivingEntity living ? living : null;
        if (mob == null || mob instanceof Player) {
            return null;
        }
        markManaged(mob);
        taunt(mob, player, bonusThreat);
        return mob;
    }

    private void taunt(LivingEntity mob, Player player, double bonusThreat) {
        double highest = table(mob).values().stream().max(Double::compareTo).orElse(0.0);
        addThreat(mob, player, highest + bonusThreat);
    }

    String describe(LivingEntity mob) {
        if (!isManaged(mob)) {
            return "La criatura seleccionada no usa la tabla de amenaza de Servidro.";
        }
        Player target = currentTarget(mob);
        double value = target == null ? 0.0 : table(mob).getOrDefault(target.getUniqueId(), 0.0);
        return "Objetivo: " + (target == null ? "ninguno" : target.getName())
                + " | Amenaza: " + Math.round(value)
                + " | Participantes: " + table(mob).size();
    }

    void remove(LivingEntity mob) {
        tables.remove(mob.getUniqueId());
    }

    void reduceThreat(Player player, double ratio) {
        double boundedRatio = Math.max(0.0, Math.min(1.0, ratio));
        for (Map.Entry<UUID, Map<UUID, Double>> entry : tables.entrySet()) {
            entry.getValue().computeIfPresent(player.getUniqueId(), (ignored, value) -> value * (1.0 - boundedRatio));
            LivingEntity mob = findLivingEntity(entry.getKey());
            if (mob != null) {
                retarget(mob);
            }
        }
    }

    void removeThreat(Player player) {
        UUID playerId = player.getUniqueId();
        tables.entrySet().removeIf(entry -> {
            entry.getValue().remove(playerId);
            LivingEntity mob = findLivingEntity(entry.getKey());
            if (mob instanceof Mob pathfindingMob) {
                if (player.equals(pathfindingMob.getTarget())) {
                    pathfindingMob.setTarget(null);
                }
                retarget(mob);
            }
            return entry.getValue().isEmpty();
        });

        for (LivingEntity entity : player.getWorld().getLivingEntities()) {
            if (entity instanceof Mob mob && player.equals(mob.getTarget())) {
                mob.setTarget(null);
            }
        }
    }

    void transferThreat(Player from, Player to, double ratio) {
        double boundedRatio = Math.max(0.0, Math.min(1.0, ratio));
        for (Map.Entry<UUID, Map<UUID, Double>> entry : tables.entrySet()) {
            Map<UUID, Double> table = entry.getValue();
            double sourceThreat = table.getOrDefault(from.getUniqueId(), 0.0);
            double transferred = sourceThreat * boundedRatio;
            if (transferred <= 0) {
                continue;
            }
            table.put(from.getUniqueId(), sourceThreat - transferred);
            table.merge(to.getUniqueId(), transferred, Double::sum);
            LivingEntity mob = findLivingEntity(entry.getKey());
            if (mob != null) {
                retarget(mob);
            }
        }
    }

    private Map<UUID, Double> table(LivingEntity mob) {
        return tables.computeIfAbsent(mob.getUniqueId(), ignored -> new HashMap<>());
    }

    private Player currentTarget(LivingEntity mob) {
        return table(mob).entrySet().stream()
                .map(entry -> Map.entry(Bukkit.getPlayer(entry.getKey()), entry.getValue()))
                .filter(entry -> entry.getKey() != null && entry.getKey().isOnline() && !entry.getKey().isDead())
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private void retarget(LivingEntity mob) {
        Player target = currentTarget(mob);
        if (mob instanceof Mob pathfindingMob) {
            pathfindingMob.setTarget(target);
        }
    }

    private LivingEntity findLivingEntity(UUID uuid) {
        return Bukkit.getWorlds().stream()
                .map(world -> world.getEntity(uuid))
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .findFirst()
                .orElse(null);
    }
}
