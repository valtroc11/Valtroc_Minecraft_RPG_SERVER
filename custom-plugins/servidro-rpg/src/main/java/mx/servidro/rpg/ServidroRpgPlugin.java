package mx.servidro.rpg;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Pose;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Ageable;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class ServidroRpgPlugin extends JavaPlugin implements Listener {
    private final Map<UUID, DownedState> downed = new HashMap<>();
    private final Map<UUID, Location> lastSafeLocations = new HashMap<>();
    private final Map<String, Long> abilityCooldowns = new HashMap<>();
    private final Map<UUID, Long> hunterMarks = new HashMap<>();
    private final Map<UUID, Long> tauntHighlights = new HashMap<>();
    private final Map<UUID, Long> slowHighlights = new HashMap<>();
    private final Map<UUID, Long> combatHealthBarUntil = new HashMap<>();
    private final Map<UUID, Long> surrenderCooldowns = new HashMap<>();
    private final Map<MobRarity, Long> rareSpawnCooldowns = new HashMap<>();
    private final Map<UUID, Component> trackedNameplates = new HashMap<>();
    private final Map<UUID, Boolean> trackedNameplateVisibility = new HashMap<>();
    private final Set<String> playerPlacedMiningBlocks = new HashSet<>();
    private ProfileStore profiles;
    private Economy economy;
    private ThreatManager threatManager;
    private ProgressionGate progressionGate;
    private PersonalChestStore personalChests;
    private DailyMissionStore dailyMissions;
    private BukkitTask healthBarTask;
    private BukkitTask scaledMobSkillTask;
    private NamespacedKey mobLevelKey;
    private NamespacedKey mobRarityKey;
    private NamespacedKey mobAffixKey;
    private NamespacedKey mobSkillKey;
    private NamespacedKey mobSkillCooldownKey;
    private NamespacedKey mobSecondarySkillKey;
    private NamespacedKey mobEnragedKey;
    private NamespacedKey mobLastStandKey;
    private NamespacedKey mobSkillPendingKey;
    private NamespacedKey mobMinionOwnerKey;
    private NamespacedKey mobHuntActiveKey;
    private NamespacedKey mobVillageCorruptedKey;
    private NamespacedKey mobWaveAnnouncedKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        mobLevelKey = new NamespacedKey(this, "mob-level");
        mobRarityKey = new NamespacedKey(this, "mob-rarity");
        mobAffixKey = new NamespacedKey(this, "mob-affix");
        mobSkillKey = new NamespacedKey(this, "mob-skill");
        mobSkillCooldownKey = new NamespacedKey(this, "mob-skill-cooldown");
        mobSecondarySkillKey = new NamespacedKey(this, "mob-secondary-skill");
        mobEnragedKey = new NamespacedKey(this, "mob-enraged");
        mobLastStandKey = new NamespacedKey(this, "mob-last-stand");
        mobSkillPendingKey = new NamespacedKey(this, "mob-skill-pending");
        mobMinionOwnerKey = new NamespacedKey(this, "mob-minion-owner");
        mobHuntActiveKey = new NamespacedKey(this, "mob-hunt-active");
        mobVillageCorruptedKey = new NamespacedKey(this, "mob-village-corrupted");
        mobWaveAnnouncedKey = new NamespacedKey(this, "mob-wave-announced");
        profiles = new ProfileStore(
                getDataFolder(),
                classMaxLevel(),
                getConfig().getInt("progression-caps.specialization-level", 20),
                professionMaxLevel());
        profiles.load();
        threatManager = new ThreatManager(this);
        progressionGate = new ProgressionGate(getConfig());
        personalChests = new PersonalChestStore(getDataFolder());
        personalChests.load();
        dailyMissions = new DailyMissionStore(getDataFolder());
        dailyMissions.load();
        setupEconomy();
        getServer().getPluginManager().registerEvents(this, this);
        healthBarTask = Bukkit.getScheduler().runTaskTimer(this, this::updateNearbyHealthBars, 10L, 10L);
        scaledMobSkillTask = Bukkit.getScheduler().runTaskTimer(this, this::runScaledMobSkills, 40L, 20L);
        getLogger().info("ServidroRpg habilitado. Economia Vault: " + (economy != null ? "activa" : "no disponible"));
    }

    @Override
    public void onDisable() {
        downed.values().forEach(state -> {
            state.cancel();
            state.player.setSwimming(false);
            state.player.removePotionEffect(PotionEffectType.DARKNESS);
            state.player.removePotionEffect(PotionEffectType.BLINDNESS);
            unhighlightDownedPlayer(state.player);
        });
        downed.clear();
        if (healthBarTask != null) {
            healthBarTask.cancel();
        }
        if (scaledMobSkillTask != null) {
            scaledMobSkillTask.cancel();
        }
        restoreTrackedNameplates();
        saveProfiles();
        savePersonalChests();
        saveDailyMissions();
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> provider =
                getServer().getServicesManager().getRegistration(Economy.class);
        economy = provider == null ? null : provider.getProvider();
    }

    private void depositCrowns(Player player, double amount) {
        if (economy != null && amount > 0) {
            economy.depositPlayer(player, amount);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return switch (command.getName().toLowerCase()) {
            case "clase" -> handleClass(sender, args);
            case "especializacion" -> handleSpecialization(sender, args);
            case "profesion" -> handleProfession(sender, args);
            case "desbloqueos" -> handleUnlocks(sender);
            case "guia" -> handleGuide(sender);
            case "misiones" -> handleMissions(sender);
            case "servidro" -> handleAdmin(sender, args);
            case "provocar" -> handleTaunt(sender);
            case "rendirse" -> handleSurrender(sender);
            case "habilidades" -> handleAbilities(sender, args);
            case "estadisticas" -> handleStats(sender);
            default -> false;
        };
    }

    private boolean handleSurrender(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo esta disponible dentro del juego.");
            return true;
        }
        if (!downed.containsKey(player.getUniqueId())) {
            player.sendMessage(Component.text("Solo puedes rendirte mientras estas derribado.", NamedTextColor.RED));
            return true;
        }
        long now = System.currentTimeMillis();
        long readyAt = surrenderCooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (readyAt > now) {
            long remaining = Math.max(1L, (readyAt - now + 999L) / 1000L);
            player.sendMessage(Component.text("Debes esperar " + remaining + " s para volver a rendirte.", NamedTextColor.RED));
            return true;
        }
        surrender(player);
        return true;
    }

    private boolean handleAbilities(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo esta disponible dentro del juego.");
            return true;
        }
        if (args.length == 0) {
            PlayerProfile profile = profiles.get(player.getUniqueId());
            if (profile.baseClass() == null) {
                player.sendMessage(Component.text("Clases disponibles: guerrero, explorador, mago, clerigo.", NamedTextColor.YELLOW));
                player.sendMessage(Component.text("Usa /habilidades <clase> para ver un resumen.", NamedTextColor.GRAY));
                return true;
            }
            sendBaseClassInfo(player, profile.baseClass());
            if (profile.specialization() != null) {
                sendSpecializationInfo(player, profile.specialization());
            }
            return true;
        }
        String query = args[0].toLowerCase(Locale.ROOT);
        RpgClass baseClass = RpgClass.find(query);
        if (baseClass != null) {
            sendBaseClassInfo(player, baseClass.id());
            return true;
        }
        if (RpgClass.owningSpecialization(query) != null) {
            sendSpecializationInfo(player, query);
            return true;
        }
        player.sendMessage(Component.text("No reconozco esa clase o especializacion.", NamedTextColor.RED));
        return true;
    }

    private boolean handleStats(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo esta disponible dentro del juego.");
            return true;
        }
        PlayerProfile profile = profiles.get(player.getUniqueId());
        player.sendMessage(Component.text("=== Estadisticas Totales ===", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Clase: " + display(profile.baseClass())
                + " | Especializacion: " + display(profile.specialization())
                + " | Nivel: " + profile.level(), NamedTextColor.YELLOW));
        player.sendMessage(Component.text(String.format(Locale.US,
                "Vida max: %.1f | Armadura: %.1f | Tenacidad: %.1f",
                attributeValue(player, Attribute.MAX_HEALTH),
                attributeValue(player, Attribute.ARMOR),
                attributeValue(player, Attribute.ARMOR_TOUGHNESS)), NamedTextColor.GREEN));
        player.sendMessage(Component.text(String.format(Locale.US,
                "Dano base: %.1f | Velocidad: %.3f | Vel. ataque: %.1f",
                attributeValue(player, Attribute.ATTACK_DAMAGE),
                attributeValue(player, Attribute.MOVEMENT_SPEED),
                attributeValue(player, Attribute.ATTACK_SPEED)), NamedTextColor.AQUA));
        player.sendMessage(Component.text("Comida: " + player.getFoodLevel()
                + " | Saturacion: " + String.format(Locale.US, "%.1f", player.getSaturation()), NamedTextColor.GRAY));
        return true;
    }

    private boolean handleClass(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo esta disponible dentro del juego.");
            return true;
        }
        PlayerProfile profile = profiles.get(player.getUniqueId());
        if (args.length == 0 || args[0].equalsIgnoreCase("estado")) {
            player.sendMessage(Component.text("Clase: " + display(profile.baseClass())
                    + " | Nivel: " + profile.level()
                    + " | XP: " + profile.classExperience() + "/" + profile.requiredClassExperience(classXpBase())
                    + " | Especializacion: " + display(profile.specialization())
                    + " (" + profile.specializationLevel() + ")"
                    + " | Profesiones: " + professionSummary(profile), NamedTextColor.GOLD));
            return true;
        }
        if (args[0].equalsIgnoreCase("lista")) {
            player.sendMessage(Component.text("Clases: guerrero, explorador, mago, clerigo.", NamedTextColor.YELLOW));
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("elegir")) {
            if (profile.baseClass() != null) {
                player.sendMessage(Component.text("Tu clase base queda fijada durante la temporada.", NamedTextColor.RED));
                return true;
            }
            RpgClass selected = RpgClass.find(args[1]);
            if (selected == null) {
                player.sendMessage(Component.text("Clase desconocida. Usa /clase lista.", NamedTextColor.RED));
                return true;
            }
            profile.setBaseClass(selected.id());
            saveProfiles();
            refreshPlayerBaseStats(player);
            updateTab(player);
            player.sendMessage(Component.text("Has elegido la clase " + selected.id() + ".", NamedTextColor.GREEN));
            return true;
        }
        return false;
    }

    private boolean handleSpecialization(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo esta disponible dentro del juego.");
            return true;
        }
        PlayerProfile profile = profiles.get(player.getUniqueId());
        if (profile.baseClass() == null) {
            player.sendMessage(Component.text("Elige primero una clase con /clase elegir <clase>.", NamedTextColor.RED));
            return true;
        }
        RpgClass baseClass = RpgClass.find(profile.baseClass());
        if (args.length == 0 || args[0].equalsIgnoreCase("lista")) {
            player.sendMessage(Component.text("Especializaciones disponibles: "
                    + String.join(", ", baseClass.specializations()) + ".", NamedTextColor.YELLOW));
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("elegir")) {
            String selected = args[1].toLowerCase();
            if (!baseClass.specializations().contains(selected)) {
                player.sendMessage(Component.text("Esa especializacion no pertenece a tu clase.", NamedTextColor.RED));
                return true;
            }
            int unlockLevel = getConfig().getInt("specialization.unlock-level", 10);
            if (profile.level() < unlockLevel) {
                player.sendMessage(Component.text("Necesitas nivel " + unlockLevel + " para especializarte.", NamedTextColor.RED));
                return true;
            }
            if (selected.equals(profile.specialization())) {
                player.sendMessage(Component.text("Ya tienes esa especializacion.", NamedTextColor.YELLOW));
                return true;
            }
            if (profile.specialization() != null && !chargeSpecializationChange(player)) {
                return true;
            }
            profile.setSpecialization(selected);
            profile.resetSpecializationLevel();
            saveProfiles();
            player.sendMessage(Component.text("Especializacion activa: " + selected
                    + ". Su progreso comienza desde nivel 0.", NamedTextColor.GREEN));
            return true;
        }
        return false;
    }

    private boolean chargeSpecializationChange(Player player) {
        double cost = getConfig().getDouble("specialization.change-cost", 250.0);
        if (economy == null) {
            player.sendMessage(Component.text("La economia no esta disponible. Avisa a un administrador.", NamedTextColor.RED));
            return false;
        }
        if (!economy.has(player, cost)) {
            player.sendMessage(Component.text("Cambiar especializacion cuesta " + economy.format(cost) + ".", NamedTextColor.RED));
            return false;
        }
        economy.withdrawPlayer(player, cost);
        return true;
    }

    private boolean handleProfession(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo esta disponible dentro del juego.");
            return true;
        }
        PlayerProfile profile = profiles.get(player.getUniqueId());
        if (args.length == 0 || args[0].equalsIgnoreCase("estado")) {
            player.sendMessage(Component.text("Profesiones: " + professionSummary(profile), NamedTextColor.GOLD));
            return true;
        }
        if (args[0].equalsIgnoreCase("lista")) {
            player.sendMessage(Component.text("Profesiones: minero, herrero, alquimista, agricultor, explorador.", NamedTextColor.YELLOW));
            return true;
        }
        if (args.length == 1) {
            Profession selected = Profession.find(args[0]);
            if (selected != null) {
                player.sendMessage(Component.text("Profesion " + selected.id()
                        + " | Nivel: " + profile.professionLevel(selected.id())
                        + " | XP: " + profile.professionExperience(selected.id())
                        + "/" + profile.requiredExperience(selected.id(), professionXpBase()), NamedTextColor.GOLD));
                return true;
            }
        }
        player.sendMessage(Component.text("Usa /profesion estado o /profesion <profesion>.", NamedTextColor.RED));
        return true;
    }

    private boolean handleUnlocks(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo esta disponible dentro del juego.");
            return true;
        }
        PlayerProfile profile = profiles.get(player.getUniqueId());
        player.sendMessage(Component.text("Nivel de clase: " + profile.level()
                + " | Equipo: hierro 5, oro 8, diamante 15.", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Herrero " + profile.professionLevel("herrero")
                + " | Crafteo: hierro 5, oro 10, diamante 15.", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Minero " + profile.professionLevel("minero")
                + " | Bloques minerales: hierro 5, oro 10, diamante 15.", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Alquimista " + profile.professionLevel("alquimista")
                + " | Consumibles avanzados: 8.", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Agricultor " + profile.professionLevel("agricultor")
                + " | Cultivos avanzados: 8.", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Explorador " + profile.professionLevel("explorador")
                + " | Utilidades de expedicion: 8.", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("La piedra natural y los minerales otorgan XP de Minero. "
                + "Las herramientas, armas, escudos y armaduras otorgan XP de Herrero.", NamedTextColor.GRAY));
        return true;
    }

    private boolean handleGuide(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo esta disponible dentro del juego.");
            return true;
        }
        sendGuide(player);
        return true;
    }

    private void sendGuide(Player player) {
        player.sendMessage(Component.text("===== Servidro MX | Reino Corrompido =====", NamedTextColor.GOLD));
        player.sendMessage(Component.text("/clase lista", NamedTextColor.YELLOW)
                .append(Component.text(" | Elige una clase rigida para la temporada.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/spec lista", NamedTextColor.YELLOW)
                .append(Component.text(" | Consulta especializaciones al alcanzar nivel 10.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/profesion estado", NamedTextColor.YELLOW)
                .append(Component.text(" | Revisa tus cinco profesiones.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/desbloqueos", NamedTextColor.YELLOW)
                .append(Component.text(" | Consulta que puedes fabricar, colocar y usar.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/auction", NamedTextColor.YELLOW)
                .append(Component.text(" | Compra y vende con otros jugadores.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/misiones", NamedTextColor.YELLOW)
                .append(Component.text(" | Consulta tus encargos diarios rotativos.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("Los aldeanos no comercian. La economia depende de los jugadores.", NamedTextColor.AQUA));
    }

    private boolean handleMissions(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo esta disponible dentro del juego.");
            return true;
        }
        player.sendMessage(Component.text("===== Encargos diarios =====", NamedTextColor.GOLD));
        for (String line : dailyMissions.describe(player, profiles.get(player.getUniqueId()))) {
            player.sendMessage(Component.text(line, NamedTextColor.YELLOW));
        }
        player.sendMessage(Component.text("Las misiones rotan cada dia y se adaptan a tus niveles de profesion.",
                NamedTextColor.GRAY));
        saveDailyMissions();
        return true;
    }

    private String professionSummary(PlayerProfile profile) {
        StringBuilder summary = new StringBuilder();
        for (Profession profession : Profession.values()) {
            if (!summary.isEmpty()) {
                summary.append(", ");
            }
            summary.append(profession.id()).append(" ").append(profile.professionLevel(profession.id()));
        }
        return summary.toString();
    }

    private boolean handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("servidro.admin")) {
            sender.sendMessage(Component.text("No tienes permiso.", NamedTextColor.RED));
            return true;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("xp")) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage("Jugador no encontrado.");
                return true;
            }
            try {
                int amount = Integer.parseInt(args[2]);
                PlayerProfile profile = profiles.get(target.getUniqueId());
                profile.addLevels(amount, classMaxLevel());
                saveProfiles();
                refreshPlayerBaseStats(target);
                updateTab(target);
                sender.sendMessage("Nivel actualizado: " + target.getName() + " ahora es nivel " + profile.level() + ".");
                target.sendMessage(Component.text("Tu nivel de clase ahora es " + profile.level() + ".", NamedTextColor.GREEN));
            } catch (NumberFormatException ignored) {
                sender.sendMessage("La cantidad debe ser un numero entero.");
            }
            return true;
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("profesionxp")) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage("Jugador no encontrado.");
                return true;
            }
            Profession profession = Profession.find(args[2]);
            if (profession == null) {
                sender.sendMessage("Profesion desconocida.");
                return true;
            }
            try {
                int amount = Integer.parseInt(args[3]);
                grantProfessionExperience(target, profession.id(), amount);
                sender.sendMessage("XP entregada: " + target.getName() + " recibio "
                        + amount + " XP de " + profession.id() + ".");
            } catch (NumberFormatException ignored) {
                sender.sendMessage("La cantidad debe ser un numero entero.");
            }
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("resetclass")) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage("Jugador no encontrado.");
                return true;
            }
            profiles.get(target.getUniqueId()).resetForTesting();
            saveProfiles();
            refreshPlayerBaseStats(target);
            updateTab(target);
            sender.sendMessage("Clase reiniciada para " + target.getName() + ".");
            target.sendMessage(Component.text("Tu clase fue reiniciada para pruebas.", NamedTextColor.YELLOW));
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("levantar")) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage("Jugador no encontrado.");
                return true;
            }
            if (!downed.containsKey(target.getUniqueId())) {
                sender.sendMessage(target.getName() + " no esta derribado.");
                return true;
            }
            revive(target, "Un administrador te ha levantado.");
            sender.sendMessage("Has levantado a " + target.getName() + ".");
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("tumbar")) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage("Jugador no encontrado.");
                return true;
            }
            if (downed.containsKey(target.getUniqueId())) {
                sender.sendMessage(target.getName() + " ya esta derribado.");
                return true;
            }
            knockDown(target);
            sender.sendMessage("Has derribado a " + target.getName() + ".");
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("invocar")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Ejecuta este comando dentro del juego.");
                return true;
            }
            Zombie mob = player.getWorld().spawn(player.getLocation().add(player.getLocation().getDirection().multiply(4)), Zombie.class);
            mob.customName(Component.text("Bandido Corrompido", NamedTextColor.DARK_RED));
            mob.setCustomNameVisible(true);
            threatManager.markManaged(mob);
            sender.sendMessage("Bandido Corrompido invocado.");
            return true;
        }
        if (args.length >= 4 && args[0].equalsIgnoreCase("spawnmob")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Ejecuta este comando dentro del juego.");
                return true;
            }
            EntityType type;
            MobRarity rarity;
            int level;
            MobAffix forcedAffix = null;
            MobSkill forcedSkill = null;
            try {
                type = EntityType.valueOf(args[1].toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                sender.sendMessage("Tipo de entidad invalido. Usa nombres como ZOMBIE, SKELETON o SPIDER.");
                return true;
            }
            rarity = MobRarity.find(args[2]);
            if (rarity == null) {
                sender.sendMessage("Rareza invalida. Usa normal, rare, elite o miniboss.");
                return true;
            }
            try {
                level = Math.max(1, Integer.parseInt(args[3]));
            } catch (NumberFormatException ignored) {
                sender.sendMessage("El nivel debe ser un numero entero.");
                return true;
            }
            if (!type.isAlive() || !LivingEntity.class.isAssignableFrom(type.getEntityClass())) {
                sender.sendMessage("Ese tipo no es una entidad viva escalable.");
                return true;
            }
            if (args.length >= 5 && !args[4].equalsIgnoreCase("none")) {
                forcedAffix = MobAffix.find(args[4]);
                if (forcedAffix == null) {
                    sender.sendMessage("Afinidad invalida. Usa brutal, robusto, veloz, congelante, vampirico o none.");
                    return true;
                }
            }
            if (args.length >= 6 && !args[5].equalsIgnoreCase("none")) {
                forcedSkill = MobSkill.find(args[5]);
                if (forcedSkill == null) {
                    sender.sendMessage("Habilidad invalida. Usa carga, onda_helada, llamado_corrupto, retroceso_tactico, golpe_territorial o none.");
                    return true;
                }
            }
            spawnForcedScaledMob(player, type, rarity, level, forcedAffix, forcedSkill);
            sender.sendMessage("Mob invocado: " + type.name() + " " + rarity.displayName() + " LVL " + level
                    + (forcedAffix == null ? "" : " | afinidad " + forcedAffix.id())
                    + (forcedSkill == null ? "" : " | habilidad " + forcedSkill.id()) + ".");
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("afinidades")) {
            EntityType type;
            try {
                type = EntityType.valueOf(args[1].toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                sender.sendMessage("Tipo de entidad invalido. Usa nombres como ZOMBIE, SKELETON o SPIDER.");
                return true;
            }
            sender.sendMessage("Afinidades de " + type.name() + ": " + MobAffix.forType(type.name()).stream()
                    .map(MobAffix::id).toList());
            sender.sendMessage("Habilidades de " + type.name() + ": " + MobSkill.forType(type.name()).stream()
                    .map(MobSkill::id).toList());
            return true;
        }
        if (args.length >= 4 && args[0].equalsIgnoreCase("spawnloot")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Ejecuta este comando dentro del juego.");
                return true;
            }
            Material material = Material.matchMaterial(args[1]);
            if (material == null || material == Material.AIR) {
                sender.sendMessage("Material invalido.");
                return true;
            }
            MobRarity rarity = MobRarity.find(args[2]);
            if (rarity == null) {
                sender.sendMessage("Rareza invalida. Usa normal, rare, elite o miniboss.");
                return true;
            }
            int mobLevel;
            try {
                mobLevel = Math.max(1, Integer.parseInt(args[3]));
            } catch (NumberFormatException ignored) {
                sender.sendMessage("El nivel del mob debe ser un numero entero.");
                return true;
            }
            MobAffix affix = null;
            if (args.length >= 5 && !args[4].equalsIgnoreCase("none")) {
                affix = MobAffix.find(args[4]);
                if (affix == null) {
                    sender.sendMessage("Afinidad invalida. Usa brutal, robusto, veloz, congelante, vampirico o none.");
                    return true;
                }
            }
            int playerLevel = profiles.get(player.getUniqueId()).level();
            if (args.length >= 6) {
                try {
                    playerLevel = Math.max(1, Integer.parseInt(args[5]));
                } catch (NumberFormatException ignored) {
                    sender.sendMessage("El nivel del jugador debe ser un numero entero.");
                    return true;
                }
            }
            ItemStack reward = createTestLoot(material, rarity, mobLevel, affix, playerLevel);
            player.getInventory().addItem(reward);
            sender.sendMessage("Botin generado: " + material.name() + " | " + rarity.id()
                    + " | mob " + mobLevel + " | jugador " + playerLevel
                    + (affix == null ? "" : " | afinidad " + affix.id()));
            return true;
        }
        if (args.length >= 4 && args[0].equalsIgnoreCase("simcraft")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Ejecuta este comando dentro del juego.");
                return true;
            }
            Material material = Material.matchMaterial(args[1]);
            if (material == null || material == Material.AIR) {
                sender.sendMessage("Material invalido.");
                return true;
            }
            int smithLevel;
            int classLevel;
            try {
                smithLevel = Math.max(1, Integer.parseInt(args[2]));
                classLevel = Math.max(1, Integer.parseInt(args[3]));
            } catch (NumberFormatException ignored) {
                sender.sendMessage("Los niveles deben ser numeros enteros.");
                return true;
            }
            ItemStack crafted = createSimulatedSmithItem(material, smithLevel, classLevel);
            player.getInventory().addItem(crafted);
            sender.sendMessage("Forja simulada: " + material.name() + " | herrero " + smithLevel + " | clase " + classLevel + ".");
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("aggro")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Ejecuta este comando dentro del juego.");
                return true;
            }
            if (!(player.getTargetEntity(12) instanceof org.bukkit.entity.LivingEntity mob)) {
                sender.sendMessage("Mira a una criatura a menos de 12 bloques.");
                return true;
            }
            sender.sendMessage(threatManager.describe(mob));
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("kit")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Ejecuta este comando dentro del juego.");
                return true;
            }
            givePrototypeKit(player);
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("arena")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Ejecuta este comando dentro del juego.");
                return true;
            }
            prepareAlphaArena(player);
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("cofre")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Ejecuta este comando dentro del juego.");
                return true;
            }
            registerPersonalChest(player, args[1]);
            return true;
        }
        return false;
    }

    private void spawnForcedScaledMob(Player player, EntityType type, MobRarity rarity, int level, MobAffix forcedAffix, MobSkill forcedSkill) {
        Location spawn = player.getLocation().add(player.getLocation().getDirection().normalize().multiply(3.0));
        Class<? extends Entity> entityClass = type.getEntityClass();
        if (entityClass == null || !LivingEntity.class.isAssignableFrom(entityClass)) {
            return;
        }
        @SuppressWarnings("unchecked")
        Class<? extends LivingEntity> livingClass = (Class<? extends LivingEntity>) entityClass;
        LivingEntity mob = player.getWorld().spawn(spawn, livingClass);
        MobAffix affix = forcedAffix != null ? forcedAffix : rollMobAffix(type, rarity);
        MobSkill skill = forcedSkill != null ? forcedSkill : rollMobSkill(type, rarity);
        applyScaledMobProfile(mob, level, rarity, affix, skill);
        announceRareMobSpawn(mob, level, rarity);
        threatManager.markManaged(mob);
        if (mob instanceof Monster monster) {
            monster.setTarget(player);
        }
    }

    private void registerPersonalChest(Player player, String rarityValue) {
        Block block = player.getTargetBlockExact(6);
        if (block == null || !(block.getState() instanceof Chest)) {
            player.sendMessage(Component.text("Mira un cofre a menos de 6 bloques.", NamedTextColor.RED));
            return;
        }
        if (rarityValue.equalsIgnoreCase("eliminar")) {
            if (personalChests.remove(block.getLocation())) {
                savePersonalChests();
                player.sendMessage(Component.text("Cofre personal eliminado.", NamedTextColor.YELLOW));
            } else {
                player.sendMessage(Component.text("Ese cofre no estaba registrado.", NamedTextColor.YELLOW));
            }
            return;
        }
        PersonalChestStore.Rarity rarity = PersonalChestStore.Rarity.find(rarityValue);
        if (rarity == null) {
            player.sendMessage(Component.text("Rareza valida: comun, raro, corrompido o eliminar.", NamedTextColor.RED));
            return;
        }
        ((Chest) block.getState()).getInventory().clear();
        personalChests.register(block.getLocation(), rarity);
        savePersonalChests();
        player.sendMessage(Component.text("Cofre personal registrado: " + rarity.id() + ".", NamedTextColor.GREEN));
    }

    private void givePrototypeKit(Player player) {
        Material[] materials = {
            Material.IRON_SWORD, Material.BOW, Material.ARROW, Material.STICK,
            Material.SHIELD, Material.IRON_AXE, Material.COMPASS, Material.FEATHER,
            Material.FIRE_CHARGE, Material.AMETHYST_SHARD, Material.WHEAT_SEEDS, Material.BLAZE_ROD
        };
        for (Material material : materials) {
            player.getInventory().addItem(new ItemStack(material));
        }
        player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 16));
        player.sendMessage(Component.text("Kit temporal entregado. Revisa README.md para ver los controles.", NamedTextColor.GREEN));
    }

    private void prepareAlphaArena(Player player) {
        Location center = player.getLocation().getBlock().getLocation();
        int radius = 7;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block floor = center.clone().add(x, -1, z).getBlock();
                floor.setType(Material.STONE_BRICKS);
                if (Math.abs(x) == radius || Math.abs(z) == radius) {
                    center.clone().add(x, 0, z).getBlock().setType(Material.COBBLESTONE_WALL);
                }
            }
        }
        spawnPrototypeBandit(center.clone().add(4, 0, 0), false);
        spawnPrototypeBandit(center.clone().add(-4, 0, 0), false);
        spawnPrototypeBandit(center.clone().add(0, 0, 4), true);
        givePrototypeKit(player);
        player.sendMessage(Component.text("Arena alfa preparada: dos bandidos y un capitan.", NamedTextColor.GOLD));
    }

    private void spawnPrototypeBandit(Location location, boolean captain) {
        Zombie mob = location.getWorld().spawn(location, Zombie.class);
        mob.customName(Component.text(captain ? "Capitan Corrompido de Prueba" : "Bandido Corrompido de Prueba",
                captain ? NamedTextColor.DARK_RED : NamedTextColor.RED));
        mob.setCustomNameVisible(true);
        if (captain) {
            mob.getAttribute(Attribute.MAX_HEALTH).setBaseValue(60.0);
            mob.setHealth(60.0);
            mob.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
        } else {
            mob.getAttribute(Attribute.MAX_HEALTH).setBaseValue(30.0);
            mob.setHealth(30.0);
        }
        mob.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
        threatManager.markManaged(mob);
    }

    private boolean handleTaunt(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo esta disponible dentro del juego.");
            return true;
        }
        PlayerProfile profile = profiles.get(player.getUniqueId());
        if (!canTaunt(profile)) {
            player.sendMessage(Component.text("Provocar requiere la clase guerrero o la especializacion paladin.", NamedTextColor.RED));
            return true;
        }
        useTauntAbility(player);
        return true;
    }

    private void useTauntAbility(Player player) {
        PlayerProfile profile = profiles.get(player.getUniqueId());
        if ("guerrero".equals(profile.baseClass())) {
            useGuardianTaunt(player);
            return;
        }
        if ("paladin".equals(profile.specialization())) {
            usePaladinChallenge(player);
            return;
        }
        player.sendMessage(Component.text("Tu especializacion no puede provocar enemigos.", NamedTextColor.RED));
    }

    private boolean canTaunt(PlayerProfile profile) {
        return "guerrero".equals(profile.baseClass()) || "paladin".equals(profile.specialization());
    }

    private void useGuardianTaunt(Player player) {
        int cooldown = getConfig().getInt("threat.guardian-taunt.cooldown-seconds", 12);
        if (!startCooldown(player, "guardian-taunt", cooldown)) {
            return;
        }
        showSkillCast(player, Component.text("Provocar", NamedTextColor.GOLD));
        double radius = getConfig().getDouble("threat.guardian-taunt.radius", 8.0);
        double bonus = getConfig().getDouble("threat.guardian-taunt.bonus-threat", 100.0);
        List<LivingEntity> affected = threatManager.tauntNearby(player, radius, bonus);
        if (affected.isEmpty()) {
            clearCooldown(player, "guardian-taunt");
            player.sendMessage(Component.text("No hay enemigos de Servidro cerca.", NamedTextColor.YELLOW));
            return;
        }
        affected.forEach(this::highlightTauntedMob);
        player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, player.getLocation().add(0, 1, 0), 20, radius / 2, 0.8, radius / 2, 0.0);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_0, 1.0f, 0.8f);
        player.sendMessage(Component.text("Provocacion en area: " + affected.size() + " enemigo(s).", NamedTextColor.GREEN));
    }

    private void usePaladinChallenge(Player player) {
        int cooldown = getConfig().getInt("threat.paladin-challenge.cooldown-seconds", 18);
        if (!startCooldown(player, "paladin-challenge", cooldown)) {
            return;
        }
        showSkillCast(player, Component.text("Desafio", NamedTextColor.YELLOW));
        int range = getConfig().getInt("threat.paladin-challenge.range", 12);
        double bonus = getConfig().getDouble("threat.paladin-challenge.bonus-threat", 60.0);
        LivingEntity target = threatManager.tauntTarget(player, range, bonus);
        if (target == null) {
            clearCooldown(player, "paladin-challenge");
            player.sendMessage(Component.text("Mira a un enemigo de Servidro a menos de " + range + " bloques.", NamedTextColor.YELLOW));
            return;
        }
        highlightTauntedMob(target);
        player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1, 0), 18, 0.8, 1.0, 0.8, 0.0);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1.0f, 1.1f);
        player.sendMessage(Component.text("Has desafiado al enemigo.", NamedTextColor.GREEN));
    }

    private boolean startCooldown(Player player, String ability, int seconds) {
        long now = System.currentTimeMillis();
        String key = cooldownKey(player, ability);
        long availableAt = abilityCooldowns.getOrDefault(key, 0L);
        if (availableAt > now) {
            long remaining = Math.max(1, (availableAt - now + 999) / 1000);
            player.sendActionBar(Component.text("Habilidad disponible en " + remaining + " s", NamedTextColor.RED));
            return false;
        }
        abilityCooldowns.put(key, now + seconds * 1000L);
        return true;
    }

    private void clearCooldown(Player player, String ability) {
        abilityCooldowns.remove(cooldownKey(player, ability));
    }

    private void highlightTauntedMob(LivingEntity mob) {
        int duration = getConfig().getInt("threat.taunt-highlight-seconds", 4);
        long highlightedUntil = System.currentTimeMillis() + duration * 1000L;
        tauntHighlights.put(mob.getUniqueId(), highlightedUntil);
        Team team = tauntHighlightTeam();
        team.addEntry(mob.getScoreboardEntryName());
        mob.setGlowing(true);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            Long currentUntil = tauntHighlights.get(mob.getUniqueId());
            if (currentUntil == null || currentUntil > System.currentTimeMillis()) {
                return;
            }
            tauntHighlights.remove(mob.getUniqueId());
            team.removeEntry(mob.getScoreboardEntryName());
            if (mob.isValid()) {
                mob.setGlowing(false);
            }
        }, duration * 20L);
    }

    private Team tauntHighlightTeam() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam("servidro_taunt");
        if (team == null) {
            team = scoreboard.registerNewTeam("servidro_taunt");
        }
        team.color(NamedTextColor.RED);
        return team;
    }

    private String cooldownKey(Player player, String ability) {
        return player.getUniqueId() + ":" + ability;
    }

    private void useMinorHeal(Player player) {
        showSkillCast(player, Component.text("Curacion Menor", NamedTextColor.GREEN));
        PlayerProfile profile = profiles.get(player.getUniqueId());
        if (!"clerigo".equals(profile.baseClass())) {
            player.sendMessage(Component.text("Curacion menor requiere la clase clerigo.", NamedTextColor.RED));
            return;
        }
        int cooldown = getConfig().getInt("cleric.minor-heal.cooldown-seconds", 8);
        if (!startCooldown(player, "minor-heal", cooldown)) {
            return;
        }
        int range = getConfig().getInt("cleric.minor-heal.range", 12);
        Player target = player.getTargetEntity(range) instanceof Player selected ? selected : player;
        if (target.isDead() || downed.containsKey(target.getUniqueId())) {
            clearCooldown(player, "minor-heal");
            player.sendMessage(Component.text("Ese aliado necesita ser reanimado.", NamedTextColor.YELLOW));
            return;
        }
        double amount = getConfig().getDouble("cleric.minor-heal.amount", 6.0);
        double maxHealth = target.getAttribute(Attribute.MAX_HEALTH).getValue();
        double before = target.getHealth();
        target.setHealth(Math.min(maxHealth, before + amount));
        double healed = target.getHealth() - before;
        if (healed <= 0) {
            clearCooldown(player, "minor-heal");
            player.sendMessage(Component.text("El objetivo ya tiene toda su vida.", NamedTextColor.YELLOW));
            return;
        }
        threatManager.addHealingThreat(player, healed * getConfig().getDouble("threat.healing-multiplier", 0.5));
        target.getWorld().spawnParticle(Particle.HEART, target.getLocation().add(0, 1, 0), 8, 0.7, 0.8, 0.7, 0.0);
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.2f);
        player.sendActionBar(Component.text("Curacion menor: +" + Math.round(healed / 2.0) + " corazones", NamedTextColor.GREEN));
        if (target != player) {
            target.sendActionBar(Component.text(player.getName() + " te ha curado.", NamedTextColor.GREEN));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLethalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (downed.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        double finalHealth = player.getHealth() - event.getFinalDamage();
        if (finalHealth > 0) {
            return;
        }
        if (tryUseTotem(player)) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            Location fallback = lastSafeLocations.getOrDefault(player.getUniqueId(), player.getWorld().getSpawnLocation());
            player.teleport(fallback);
        }
        knockDown(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageByDownedPlayer(EntityDamageByEntityEvent event) {
        Player attacker = null;
        if (event.getDamager() instanceof Player player) {
            attacker = player;
        } else if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player player) {
                attacker = player;
            }
        }
        if (attacker != null && downed.containsKey(attacker.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (attacker != null && deny(attacker, attacker.getInventory().getItemInMainHand().getType(),
                ProgressionGate.Action.EQUIP_OR_USE)) {
            event.setCancelled(true);
            return;
        }
        if (attacker != null && event.getEntity() instanceof org.bukkit.entity.LivingEntity mob) {
            applyDamagePassives(event, attacker, mob);
            double multiplier = threatMultiplier(attacker);
            threatManager.addThreat(mob, attacker, event.getFinalDamage() * multiplier);
        }
    }

    private LivingEntity resolveLivingDamager(Entity damager) {
        if (damager instanceof LivingEntity living) {
            return living;
        }
        if (damager instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof LivingEntity living) {
                return living;
            }
        }
        return null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageIndicator(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity target) || target instanceof ArmorStand) {
            return;
        }
        markCombatHealthBar(target);
        if (event instanceof EntityDamageByEntityEvent byEntity
                && resolveLivingDamager(byEntity.getDamager()) instanceof LivingEntity attacker) {
            markCombatHealthBar(attacker);
        }
        double dealt = Math.min(target.getHealth(), event.getFinalDamage());
        if (dealt > 0) {
            showFloatingText(target, Component.text("-" + String.format(Locale.ROOT, "%.1f", dealt), NamedTextColor.RED), 20L);
        }
    }

    private void applyDamagePassives(EntityDamageByEntityEvent event, Player attacker, org.bukkit.entity.LivingEntity target) {
        PlayerProfile profile = profiles.get(attacker.getUniqueId());
        double damage = event.getDamage();
        if ("berserker".equals(profile.specialization())) {
            double missingHealthRatio = 1.0 - (attacker.getHealth()
                    / attacker.getAttribute(Attribute.MAX_HEALTH).getValue());
            damage *= 1.0 + missingHealthRatio * 0.35;
        }
        Long markedUntil = hunterMarks.get(target.getUniqueId());
        if (markedUntil != null) {
            if (markedUntil >= System.currentTimeMillis()) {
                damage *= 1.0 + getConfig().getDouble("specialization-abilities.hunter-mark.damage-bonus", 0.15);
            } else {
                hunterMarks.remove(target.getUniqueId());
            }
        }
        event.setDamage(damage);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWarriorHeavyStrikeMelee(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)
                || !(event.getEntity() instanceof LivingEntity target)
                || downed.containsKey(player.getUniqueId())
                || !player.isSneaking()) {
            return;
        }
        PlayerProfile profile = profiles.get(player.getUniqueId());
        if (!"guerrero".equals(profile.baseClass())
                || !isWarriorWeapon(player.getInventory().getItemInMainHand().getType())
                || !isAbilityTarget(player, target)) {
            return;
        }
        event.setCancelled(true);
        useWarriorOverwhelmingStrike(player, target);
    }

    @EventHandler(ignoreCancelled = true)
    public void onScaledMobAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity mob)) {
            return;
        }
        if (isMythicEnraged(mob) && mob.getAttribute(Attribute.ATTACK_DAMAGE) == null) {
            event.setDamage(event.getDamage() * mythicEnrageSection(mob).getDouble("damage-multiplier", 1.15));
        }
        MobAffix affix = scaledMobAffix(mob);
        if (affix == null) {
            return;
        }
        switch (affix) {
            case BRUTAL -> event.setDamage(event.getDamage() * 1.15);
            case CONGELANTE -> {
                if (event.getEntity() instanceof LivingEntity target) {
                    applySlow(target, getConfig().getInt("mob-scaling.affixes.congelante.slow-ticks", 50));
                    target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 10, 0.5, 0.6, 0.5, 0.02);
                }
            }
            case VAMPIRICO -> {
                double healRatio = getConfig().getDouble("mob-scaling.affixes.vampirico.heal-ratio", 0.25);
                double maxHealth = mob.getAttribute(Attribute.MAX_HEALTH) == null
                        ? mob.getHealth()
                        : mob.getAttribute(Attribute.MAX_HEALTH).getValue();
                mob.setHealth(Math.min(maxHealth, mob.getHealth() + event.getFinalDamage() * healRatio));
                mob.getWorld().spawnParticle(Particle.HEART, mob.getLocation().add(0, 1, 0), 4, 0.4, 0.5, 0.4, 0.01);
            }
            default -> {
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMythicLastStandMitigation(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mob) || !isMythicLastStand(mob)) {
            return;
        }
        ConfigurationSection lastStand = mythicLastStandSection(mob);
        if (lastStand == null) {
            return;
        }
        event.setDamage(event.getDamage() * lastStand.getDouble("damage-taken-multiplier", 0.75));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMythicMobDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mob) || mob.isDead()) {
            return;
        }
        tryActivateMythicEnrage(mob, event.getFinalDamage());
        tryActivateMythicLastStand(mob, event.getFinalDamage());
    }

    private double threatMultiplier(Player attacker) {
        String specialization = profiles.get(attacker.getUniqueId()).specialization();
        if ("guardian".equals(specialization)) {
            return 1.5;
        }
        if ("paladin".equals(specialization)) {
            return 1.25;
        }
        if ("picaro".equals(specialization)) {
            return 0.65;
        }
        if ("piromante".equals(specialization)) {
            return 1.25;
        }
        return getConfig().getDouble("threat.damage-multiplier", 1.0);
    }

    @EventHandler
    public void onManagedMobDeath(EntityDeathEvent event) {
        LivingEntity mob = event.getEntity();
        threatManager.remove(mob);
        Player killer = mob.getKiller();
        if (killer == null) {
            return;
        }
        MobRarity rarity = scaledMobRarity(mob);
        int classXpGained = 0;
        long crownsGained = 0;
        if (rarity != null) {
            classXpGained = grantClassExperience(killer, mob, rarity);
            crownsGained = Math.round(grantMobCrowns(killer, mob, rarity));
            sendKillRewardsActionBar(killer, classXpGained, crownsGained);
        }
        if (mob.hasMetadata("MythicMob")) {
            grantMythicMobReward(killer, mob, event);
            return;
        }
        if (rarity == null) {
            return;
        }
        ItemStack reward = rollScaledMobReward(killer, mob, rarity);
        if (reward != null) {
            event.getDrops().add(reward);
        }
    }

    @EventHandler
    public void onMythicMobSpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.LivingEntity mob)) {
            return;
        }
        Bukkit.getScheduler().runTask(this, () -> {
            if (!mob.isValid()) {
                return;
            }
            if (mob.hasMetadata("MythicMob")) {
                threatManager.markManaged(mob);
                scaleMythicMobIfEligible(mob);
                return;
            }
            scaleMobIfEligible(mob);
        });
    }

    private void knockDown(Player player) {
        player.setHealth(1.0);
        threatManager.removeThreat(player);
        player.sendMessage(Component.text("Has caido. Un aliado puede reanimarte con clic derecho.", NamedTextColor.RED));
        int seconds = getConfig().getInt("downed.auto-revive-seconds", 60);
        int delay = seconds * 20;
        BukkitTask task = Bukkit.getScheduler().runTaskLater(this, () -> revive(player, "Te has levantado con tus ultimas fuerzas."), delay);
        BossBar bossBar = BossBar.bossBar(
                downedBarText(seconds),
                1.0f,
                BossBar.Color.RED,
                BossBar.Overlay.PROGRESS);
        player.showBossBar(bossBar);
        BukkitTask displayTask = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            int ticksRemaining = seconds * 20;

            @Override
            public void run() {
                DownedState state = downed.get(player.getUniqueId());
                if (state == null) {
                    return;
                }
                applyDownedVisuals(state);
                ticksRemaining = Math.max(0, ticksRemaining - 2);
                int remaining = (ticksRemaining + 19) / 20;
                bossBar.name(downedBarText(remaining));
                bossBar.progress(seconds <= 0 ? 0.0f : (float) ticksRemaining / (seconds * 20));
            }
        }, 0L, 2L);
        DownedState state = new DownedState(player, task, displayTask, bossBar);
        downed.put(player.getUniqueId(), state);
        highlightDownedPlayer(player);
        applyDownedVisuals(state);
        state.visualTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            DownedState current = downed.get(player.getUniqueId());
            if (current != null) {
                applyDownedVisuals(current);
            }
        }, 0L, 1L);
    }

    private void applyDownedVisuals(DownedState state) {
        Player player = state.player;
        player.setSprinting(false);
        player.setSneaking(false);
        player.setSwimming(true);
        player.setPose(Pose.SWIMMING, true);
        player.setVelocity(new Vector(0.0, Math.min(0.0, player.getVelocity().getY()), 0.0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false, false));
        if (state.phase == DownedPhase.REVIVING) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 0, false, false, false));
        }
    }

    private void highlightDownedPlayer(Player player) {
        Team team = downedHighlightTeam();
        team.addEntry(player.getName());
        player.setGlowing(true);
    }

    private void unhighlightDownedPlayer(Player player) {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("servidro_downed");
        if (team != null) {
            team.removeEntry(player.getName());
        }
        player.setGlowing(false);
        updateTab(player);
    }

    private Team downedHighlightTeam() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam("servidro_downed");
        if (team == null) {
            team = scoreboard.registerNewTeam("servidro_downed");
        }
        team.color(NamedTextColor.WHITE);
        return team;
    }

    private Component downedBarText(int remaining) {
        return Component.text("Derribado | Te levantaras en " + remaining + " s", NamedTextColor.RED);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobTargetsDownedPlayer(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player player && downed.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAssist(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof AbstractVillager
                && getConfig().getBoolean("economy.disable-villager-trading", true)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text(
                    "El comercio con aldeanos esta deshabilitado. Intercambia con otros jugadores.",
                    NamedTextColor.YELLOW));
            return;
        }
        if (!(event.getRightClicked() instanceof Player target)) {
            return;
        }
        DownedState state = downed.get(target.getUniqueId());
        if (state == null || downed.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        if (state.reviveTask != null) {
            event.getPlayer().sendMessage(Component.text("Otro aliado ya esta ayudando a " + target.getName() + ".", NamedTextColor.YELLOW));
            return;
        }
        Player helper = event.getPlayer();
        int seconds = assistedReviveSeconds(helper);
        state.phase = DownedPhase.REVIVING;
        state.helper = helper.getUniqueId();
        helper.sendMessage(Component.text("Reanimando a " + target.getName() + ". Manten la cercania.", NamedTextColor.GREEN));
        state.reviveTask = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            int remaining = seconds;

            @Override
            public void run() {
                if (!helper.isOnline() || !target.isOnline() || helper.getWorld() != target.getWorld()
                        || helper.getLocation().distanceSquared(target.getLocation()) > 9.0) {
                    helper.sendMessage(Component.text("Reanimacion interrumpida.", NamedTextColor.RED));
                    state.phase = DownedPhase.DOWNED;
                    state.helper = null;
                    state.cancelReviveTask();
                    return;
                }
                if (remaining-- <= 0) {
                    revive(target, helper.getName() + " te ha levantado.");
                    return;
                }
                helper.sendActionBar(Component.text("Reanimando: " + remaining + " s", NamedTextColor.GREEN));
                target.sendActionBar(Component.text(helper.getName() + " te esta reanimando: " + remaining + " s", NamedTextColor.GREEN));
            }
        }, 0L, 20L);
    }

    private int assistedReviveSeconds(Player helper) {
        PlayerProfile profile = profiles.get(helper.getUniqueId());
        String specialization = profile.specialization();
        if ("clerigo".equals(profile.baseClass()) || "paladin".equals(specialization) || "druida".equals(specialization)) {
            return getConfig().getInt("cleric.assisted-revive-seconds", 3);
        }
        return getConfig().getInt("downed.assisted-revive-seconds", 5);
    }

    private void revive(Player player, String message) {
        DownedState state = downed.remove(player.getUniqueId());
        if (state == null) {
            return;
        }
        state.cancel();
        player.setPose(Pose.STANDING, false);
        player.setSwimming(false);
        player.removePotionEffect(PotionEffectType.DARKNESS);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        unhighlightDownedPlayer(player);
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHealth, getConfig().getDouble("downed.revived-health", 1.0)));
        int weakness = getConfig().getInt("downed.weakness-seconds", 45) * 20;
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, weakness, 0, false, true, true));
        player.sendMessage(Component.text(message, NamedTextColor.GREEN));
    }

    private void surrender(Player player) {
        DownedState state = downed.remove(player.getUniqueId());
        if (state == null) {
            return;
        }
        state.cancel();
        surrenderCooldowns.put(player.getUniqueId(),
                System.currentTimeMillis() + getConfig().getLong("downed.surrender-cooldown-seconds", 45L) * 1000L);
        player.setPose(Pose.STANDING, false);
        player.setSwimming(false);
        player.removePotionEffect(PotionEffectType.DARKNESS);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        unhighlightDownedPlayer(player);
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        player.setHealth(maxHealth);
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setExhaustion(0.0f);
        player.setFireTicks(0);
        Location respawn = player.getRespawnLocation();
        if (respawn == null) {
            respawn = player.getWorld().getSpawnLocation();
        }
        player.teleport(respawn);
        player.sendMessage(Component.text("Te has rendido. Has reaparecido como si hubieras muerto.", NamedTextColor.YELLOW));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        DownedState state = downed.get(player.getUniqueId());
        if (state != null && movedPosition(event.getFrom(), event.getTo())) {
            event.setTo(event.getFrom());
            applyDownedVisuals(state);
            return;
        }
        Location location = player.getLocation();
        if (!location.getBlock().getRelative(0, -1, 0).isPassable()) {
            lastSafeLocations.put(player.getUniqueId(), location.clone());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (downed.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (event.getItem() != null && deny(event.getPlayer(), event.getItem().getType(), ProgressionGate.Action.EQUIP_OR_USE)) {
            event.setCancelled(true);
            return;
        }
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                && event.getItem() != null
                && event.getItem().getType() == Material.SHIELD) {
            PlayerProfile profile = profiles.get(event.getPlayer().getUniqueId());
            if (canTaunt(profile)) {
                event.setCancelled(true);
                useTauntAbility(event.getPlayer());
            }
            return;
        }
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                && event.getItem() != null
                && event.getItem().getType() == Material.BLAZE_ROD) {
            PlayerProfile profile = profiles.get(event.getPlayer().getUniqueId());
            if ("clerigo".equals(profile.baseClass())) {
                event.setCancelled(true);
                useMinorHeal(event.getPlayer());
            }
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                useBaseClassAbility(event);
            }
            return;
        }
        if (event.getItem() == null) {
            return;
        }
        Player player = event.getPlayer();
        String specialization = profiles.get(player.getUniqueId()).specialization();
        Material item = event.getItem().getType();
        if ("cazador".equals(specialization) && item == Material.COMPASS) {
            event.setCancelled(true);
            useHunterMark(player);
        } else if ("picaro".equals(specialization) && item == Material.FEATHER) {
            event.setCancelled(true);
            useRogueVanish(player);
        } else if ("piromante".equals(specialization) && item == Material.FIRE_CHARGE) {
            event.setCancelled(true);
            usePyromancerFireball(player);
        } else if ("arcanista".equals(specialization) && item == Material.AMETHYST_SHARD) {
            event.setCancelled(true);
            useArcanistLink(player);
        } else if ("druida".equals(specialization) && item == Material.WHEAT_SEEDS) {
            event.setCancelled(true);
            useDruidBloom(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (downed.containsKey(player.getUniqueId())
                || !"guerrero".equals(profiles.get(player.getUniqueId()).baseClass())) {
            return;
        }
        event.setCancelled(true);
        useWarriorDash(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        DownedState state = downed.get(player.getUniqueId());
        if (state != null) {
            event.setCancelled(true);
            applyDownedVisuals(state);
            return;
        }
        if (!event.isSneaking() || player.isOnGround()
                || !"guerrero".equals(profiles.get(player.getUniqueId()).baseClass())) {
            return;
        }
        useWarriorDesolatingLeap(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onToggleSprint(PlayerToggleSprintEvent event) {
        DownedState state = downed.get(event.getPlayer().getUniqueId());
        if (state != null) {
            event.setCancelled(true);
            applyDownedVisuals(state);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) {
            return;
        }
        if ((isArmorSlot(event.getSlotType()) || event.isShiftClick())
                && deny(player, item.getType(), ProgressionGate.Action.EQUIP_OR_USE)) {
            event.setCancelled(true);
            return;
        }
        ItemStack cursor = event.getCursor();
        if (isArmorSlot(event.getSlotType())
                && cursor != null
                && !cursor.getType().isAir()
                && deny(player, cursor.getType(), ProgressionGate.Action.EQUIP_OR_USE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVillagerTradeOpen(InventoryOpenEvent event) {
        if (!getConfig().getBoolean("economy.disable-villager-trading", true)
                || !(event.getInventory() instanceof MerchantInventory merchantInventory)
                || !(merchantInventory.getMerchant() instanceof AbstractVillager)) {
            return;
        }
        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text(
                "El comercio con aldeanos esta deshabilitado. Intercambia con otros jugadores.",
                NamedTextColor.YELLOW));
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        Material result = event.getRecipe().getResult().getType();
        if (deny(player, result, ProgressionGate.Action.CRAFT)) {
            event.setCancelled(true);
            return;
        }
        if (isSmithEquipment(result)) {
            grantProfessionExperience(player, "herrero", smithingExperience(result));
            enhanceCraftedSmithItem(player, event);
            progressDailyMission(player, DailyMissionStore.Activity.SMITH, result, 1);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (deny(event.getPlayer(), event.getBlockPlaced().getType(), ProgressionGate.Action.PLACE)) {
            event.setCancelled(true);
            return;
        }
        if (miningExperience(event.getBlockPlaced().getType()) > 0) {
            playerPlacedMiningBlocks.add(blockKey(event.getBlockPlaced()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (personalChests.get(event.getBlock().getLocation()) != null) {
            event.setCancelled(true);
            event.getPlayer().sendActionBar(Component.text("Este cofre pertenece al mundo.", NamedTextColor.RED));
            return;
        }
        int miningXp = miningExperience(event.getBlock().getType());
        boolean placedByPlayer = playerPlacedMiningBlocks.remove(blockKey(event.getBlock()));
        if (miningXp > 0 && !placedByPlayer) {
            grantProfessionExperience(event.getPlayer(), "minero", miningXp);
            progressDailyMission(event.getPlayer(), DailyMissionStore.Activity.MINE, event.getBlock().getType(), 1);
        }
        if (event.getBlock().getBlockData() instanceof Ageable ageable
                && ageable.getAge() >= ageable.getMaximumAge()) {
            grantProfessionExperience(event.getPlayer(), "agricultor",
                    getConfig().getInt("profession-xp.agricultor.harvest", 4));
            progressDailyMission(event.getPlayer(), DailyMissionStore.Activity.HARVEST, event.getBlock().getType(), 1);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPersonalChestOpen(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }
        PersonalChestStore.ChestDefinition chest = personalChests.get(event.getClickedBlock().getLocation());
        if (chest == null) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        long now = System.currentTimeMillis();
        long lastLoot = chest.lootedAt().getOrDefault(player.getUniqueId(), 0L);
        long cooldown = getConfig().getLong("personal-chests." + chest.rarity().id() + ".cooldown-hours", 24) * 60 * 60 * 1000L;
        if (lastLoot + cooldown > now) {
            long remainingMinutes = Math.max(1, (lastLoot + cooldown - now + 59999) / 60000);
            player.sendMessage(Component.text("Ya saqueaste este cofre. Disponible en "
                    + remainingMinutes + " min.", NamedTextColor.YELLOW));
            return;
        }
        for (ItemStack item : rollChestLoot(chest.rarity())) {
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(item);
            overflow.values().forEach(extra -> player.getWorld().dropItemNaturally(player.getLocation(), extra));
        }
        personalChests.recordLoot(event.getClickedBlock().getLocation(), player.getUniqueId(), now);
        savePersonalChests();
        int xp = getConfig().getInt("personal-chests." + chest.rarity().id() + ".explorer-xp", 20);
        grantProfessionExperience(player, "explorador", xp);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.2f);
        player.sendMessage(Component.text("Has saqueado un cofre " + chest.rarity().id() + ".", NamedTextColor.GOLD));
    }

    private ItemStack[] rollChestLoot(PersonalChestStore.Rarity rarity) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return switch (rarity) {
            case COMMON -> new ItemStack[] {
                new ItemStack(Material.BREAD, random.nextInt(2, 6)),
                new ItemStack(Material.IRON_NUGGET, random.nextInt(2, 8))
            };
            case RARE -> new ItemStack[] {
                new ItemStack(Material.GOLD_NUGGET, random.nextInt(3, 10)),
                new ItemStack(Material.LAPIS_LAZULI, random.nextInt(2, 7)),
                new ItemStack(Material.EXPERIENCE_BOTTLE, random.nextInt(1, 4))
            };
            case CORRUPTED -> new ItemStack[] {
                new ItemStack(Material.AMETHYST_SHARD, random.nextInt(3, 9)),
                new ItemStack(Material.EMERALD, random.nextInt(1, 4)),
                new ItemStack(Material.GOLDEN_APPLE, 1)
            };
        };
    }

    private void scaleMobIfEligible(LivingEntity mob) {
        if (!getConfig().getBoolean("mob-scaling.enabled", true)
                || !mob.isValid()
                || mob.isDead()
                || !isScalableMob(mob)
                || scaledMobRarity(mob) != null) {
            return;
        }
        if (isPassiveMob(mob)) {
            applyScaledMobProfile(mob, 1, MobRarity.NORMAL, null, null);
            threatManager.markManaged(mob);
            return;
        }
        Player reference = referencePlayerForScaling(mob.getLocation());
        if (reference == null) {
            return;
        }
        PlayerProfile profile = profiles.get(reference.getUniqueId());
        MobRarity rarity = adjustedSpawnRarity(rollMobRarity());
        int level = rollMobLevel(profile.level(), rarity);
        MobAffix affix = rollMobAffix(mob.getType(), rarity);
        MobSkill skill = rollMobSkill(mob.getType(), rarity);
        applyScaledMobProfile(mob, level, rarity, affix, skill);
        markRareSpawn(rarity);
        announceRareMobSpawn(mob, level, rarity);
        threatManager.markManaged(mob);
    }

    private void scaleMythicMobIfEligible(LivingEntity mob) {
        if (!getConfig().getBoolean("mob-scaling.mythic.enabled", true)
                || scaledMobLevel(mob) > 0) {
            return;
        }
        Player reference = referencePlayerForScaling(mob.getLocation());
        if (reference == null) {
            return;
        }
        int level = Math.max(1, profiles.get(reference.getUniqueId()).level()
                + getConfig().getInt("mob-scaling.mythic.level-offset", 2));
        PersistentDataContainer data = mob.getPersistentDataContainer();
        data.set(mobLevelKey, PersistentDataType.INTEGER, level);
        String archetype = mythicArchetype(mob);
        MobRarity rarity = adjustedSpawnRarity(mythicCombatRarity(mob));
        data.set(mobRarityKey, PersistentDataType.STRING, rarity.id());
        MobSkill skill = mythicCombatSkill(archetype);
        if (skill != null) {
            data.set(mobSkillKey, PersistentDataType.STRING, skill.id());
        }
        MobSkill secondarySkill = mythicSecondaryCombatSkill(archetype);
        if (secondarySkill != null) {
            data.set(mobSecondarySkillKey, PersistentDataType.STRING, secondarySkill.id());
        }

        double healthScale = 1.0 + level * getConfig().getDouble("mob-scaling.mythic.health-per-level", 0.06);
        double damageScale = 1.0 + level * getConfig().getDouble("mob-scaling.mythic.damage-per-level", 0.03);
        if (mob.getAttribute(Attribute.MAX_HEALTH) != null) {
            double scaledMaxHealth = Math.max(1.0,
                    mob.getAttribute(Attribute.MAX_HEALTH).getBaseValue() * healthScale);
            mob.getAttribute(Attribute.MAX_HEALTH).setBaseValue(scaledMaxHealth);
            mob.setHealth(scaledMaxHealth);
        }
        if (mob.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            mob.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(Math.max(1.0,
                    mob.getAttribute(Attribute.ATTACK_DAMAGE).getBaseValue() * damageScale));
        }
        Component baseName = mob.customName() != null ? mob.customName() : Component.text(displayEntityType(mob.getType()), NamedTextColor.WHITE);
        mob.customName(Component.text("[LVL " + level + " " + rarity.displayName() + "] ", rarity.color())
                .append(skill == null ? Component.empty() : Component.text(skillLabel(skill) + " ", NamedTextColor.LIGHT_PURPLE))
                .append(baseName));
        mob.setCustomNameVisible(false);
        markRareSpawn(rarity);
        announceRareMobSpawn(mob, level, rarity);
    }

    private void announceRareMobSpawn(LivingEntity mob, int level, MobRarity rarity) {
        if (rarity != MobRarity.ELITE && rarity != MobRarity.MINIBOSS) {
            return;
        }
        Location location = mob.getLocation();
        Component message = Component.text("[Alerta] ", NamedTextColor.GOLD)
                .append(Component.text(displayEntityType(mob.getType()), NamedTextColor.WHITE))
                .append(Component.text(" ha aparecido como ", NamedTextColor.GRAY))
                .append(Component.text(rarity.displayName(), rarity.color()))
                .append(Component.text(" ", NamedTextColor.GRAY))
                .append(Component.text("LVL " + level, NamedTextColor.WHITE))
                .append(Component.text(" en ", NamedTextColor.GRAY))
                .append(Component.text(location.getWorld().getName(), NamedTextColor.AQUA))
                .append(Component.text(" [", NamedTextColor.GRAY))
                .append(Component.text(location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ(),
                        NamedTextColor.YELLOW))
                .append(Component.text("]", NamedTextColor.GRAY));
        Bukkit.broadcast(message);
    }

    private boolean isScalableMob(LivingEntity mob) {
        if (mob instanceof Player || mob instanceof ArmorStand) {
            return false;
        }
        if (mob.customName() != null) {
            return false;
        }
        if (isPassiveMob(mob)) {
            return true;
        }
        if (mob instanceof Monster) {
            return true;
        }
        List<String> neutralTypes = getConfig().getStringList("mob-scaling.eligible-neutrals");
        return neutralTypes.contains(mob.getType().name());
    }

    private boolean isPassiveMob(LivingEntity mob) {
        return (mob instanceof Animals || mob instanceof AbstractVillager)
                && !(mob instanceof Monster)
                && !mob.hasMetadata("MythicMob");
    }

    private Player nearestReferencePlayer(Location location, double range) {
        double bestDistance = range * range;
        Player nearest = null;
        for (Player player : location.getWorld().getPlayers()) {
            if (!player.isOnline()) {
                continue;
            }
            double distance = player.getLocation().distanceSquared(location);
            if (distance <= bestDistance) {
                bestDistance = distance;
                nearest = player;
            }
        }
        return nearest;
    }

    private Player referencePlayerForScaling(Location location) {
        double configuredRange = getConfig().getDouble("mob-scaling.reference-range", 32.0);
        Player withinRange = nearestReferencePlayer(location, configuredRange);
        return withinRange != null ? withinRange : nearestReferencePlayer(location, Double.MAX_VALUE);
    }

    private MobRarity rollMobRarity() {
        double roll = ThreadLocalRandom.current().nextDouble();
        double cumulative = 0.0;
        for (MobRarity rarity : MobRarity.values()) {
            cumulative += getConfig().getDouble("mob-scaling.rarities." + rarity.id() + ".chance",
                    rarity == MobRarity.NORMAL ? 1.0 : 0.0);
            if (roll <= cumulative) {
                return rarity;
            }
        }
        return MobRarity.NORMAL;
    }

    private MobRarity adjustedSpawnRarity(MobRarity rarity) {
        if (rarity == MobRarity.MINIBOSS && isRarityOnCooldown(MobRarity.MINIBOSS)) {
            rarity = isRarityOnCooldown(MobRarity.ELITE) ? MobRarity.RARE : MobRarity.ELITE;
        }
        if (rarity == MobRarity.ELITE && isRarityOnCooldown(MobRarity.ELITE)) {
            rarity = MobRarity.RARE;
        }
        return rarity;
    }

    private boolean isRarityOnCooldown(MobRarity rarity) {
        if (rarity != MobRarity.ELITE && rarity != MobRarity.MINIBOSS) {
            return false;
        }
        return rareSpawnCooldowns.getOrDefault(rarity, 0L) > System.currentTimeMillis();
    }

    private void markRareSpawn(MobRarity rarity) {
        long now = System.currentTimeMillis();
        if (rarity == MobRarity.ELITE) {
            rareSpawnCooldowns.put(MobRarity.ELITE,
                    now + getConfig().getLong("mob-scaling.spawn-cooldowns.elite-seconds", 300L) * 1000L);
        } else if (rarity == MobRarity.MINIBOSS) {
            rareSpawnCooldowns.put(MobRarity.MINIBOSS,
                    now + getConfig().getLong("mob-scaling.spawn-cooldowns.miniboss-seconds", 600L) * 1000L);
        }
    }

    private int rollMobLevel(int playerLevel, MobRarity rarity) {
        String path = "mob-scaling.level-ranges." + rarity.id() + ".";
        int min = Math.max(1, playerLevel + getConfig().getInt(path + "min-offset", 0));
        int max = Math.max(min, playerLevel + getConfig().getInt(path + "max-offset", 0));
        max = Math.min(max, playerLevel + maxBonusLevels(rarity));
        if (rarity == MobRarity.MINIBOSS && playerLevel <= 1) {
            max = Math.min(max, 5);
        }
        min = Math.min(min, max);
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private int maxBonusLevels(MobRarity rarity) {
        return switch (rarity) {
            case NORMAL -> 1;
            case RARE -> 2;
            case ELITE -> 3;
            case MINIBOSS -> 4;
        };
    }

    private MobAffix rollMobAffix(EntityType type, MobRarity rarity) {
        double chance = getConfig().getDouble("mob-scaling.rarities." + rarity.id() + ".affix-chance", 1.0);
        if (ThreadLocalRandom.current().nextDouble() > chance) {
            return null;
        }
        List<MobAffix> pool = MobAffix.forType(type.name());
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    private MobSkill rollMobSkill(EntityType type, MobRarity rarity) {
        if (rarity == MobRarity.NORMAL || rarity == MobRarity.RARE) {
            return null;
        }
        double chance = getConfig().getDouble("mob-scaling.rarities." + rarity.id() + ".skill-chance", 1.0);
        if (ThreadLocalRandom.current().nextDouble() > chance) {
            return null;
        }
        List<MobSkill> pool = new ArrayList<>(MobSkill.forType(type.name()));
        if (type == EntityType.CREEPER && pool.contains(MobSkill.LLAMADO_CORRUPTO)) {
            double corruptChance = getConfig().getDouble("mob-scaling.skills.corrupt-call.type-rolls.creeper", 0.03);
            if (ThreadLocalRandom.current().nextDouble() > corruptChance) {
                pool.remove(MobSkill.LLAMADO_CORRUPTO);
            }
        }
        if (pool.isEmpty()) {
            return null;
        }
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    private void applyScaledMobProfile(LivingEntity mob, int level, MobRarity rarity, MobAffix affix, MobSkill skill) {
        PersistentDataContainer data = mob.getPersistentDataContainer();
        data.set(mobLevelKey, PersistentDataType.INTEGER, level);
        data.set(mobRarityKey, PersistentDataType.STRING, rarity.id());
        if (affix != null) {
            data.set(mobAffixKey, PersistentDataType.STRING, affix.id());
        }
        if (skill != null) {
            data.set(mobSkillKey, PersistentDataType.STRING, skill.id());
        }

        double healthScale = 1.0 + level * getConfig().getDouble("mob-scaling.base-health-per-level", 0.08);
        double damageScale = 1.0 + level * getConfig().getDouble("mob-scaling.base-damage-per-level", 0.04);
        double healthMultiplier = getConfig().getDouble("mob-scaling.rarities." + rarity.id() + ".health-multiplier", 1.0);
        double damageMultiplier = getConfig().getDouble("mob-scaling.rarities." + rarity.id() + ".damage-multiplier", 1.0);
        double speedMultiplier = getConfig().getDouble("mob-scaling.rarities." + rarity.id() + ".speed-multiplier", 1.0);

        if (mob.getAttribute(Attribute.MAX_HEALTH) != null) {
            double baseMaxHealth = mob.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
            double scaledMaxHealth = Math.max(1.0, baseMaxHealth * healthScale * healthMultiplier);
            if (affix == MobAffix.ROBUSTO) {
                scaledMaxHealth *= getConfig().getDouble("mob-scaling.affixes.robusto.health-multiplier", 1.3);
            }
            mob.getAttribute(Attribute.MAX_HEALTH).setBaseValue(scaledMaxHealth);
            mob.setHealth(scaledMaxHealth);
        }
        if (mob.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            double baseDamage = mob.getAttribute(Attribute.ATTACK_DAMAGE).getBaseValue();
            double scaledDamage = Math.max(1.0, baseDamage * damageScale * damageMultiplier);
            if (affix == MobAffix.BRUTAL) {
                scaledDamage *= getConfig().getDouble("mob-scaling.affixes.brutal.damage-multiplier", 1.2);
            }
            mob.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(scaledDamage);
        }
        if (mob.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            double baseSpeed = mob.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue();
            double scaledSpeed = baseSpeed * speedMultiplier;
            if (affix == MobAffix.VELOZ) {
                scaledSpeed *= getConfig().getDouble("mob-scaling.affixes.veloz.speed-multiplier", 1.18);
            }
            mob.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(scaledSpeed);
        }

        applyMobLoadout(mob, rarity);

        mob.customName(Component.text("[LVL " + level + " " + rarity.displayName() + "] ", rarity.color())
                .append(affix == null
                        ? Component.empty()
                        : Component.text(affix.displayName() + " ", affix.color()))
                .append(skill == null
                        ? Component.empty()
                        : Component.text(skillLabel(skill) + " ", NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(displayEntityType(mob.getType()), NamedTextColor.WHITE)));
        mob.setCustomNameVisible(false);
    }

    private MobRarity scaledMobRarity(LivingEntity mob) {
        String stored = mob.getPersistentDataContainer().get(mobRarityKey, PersistentDataType.STRING);
        return MobRarity.find(stored);
    }

    private int scaledMobLevel(LivingEntity mob) {
        Integer stored = mob.getPersistentDataContainer().get(mobLevelKey, PersistentDataType.INTEGER);
        return stored == null ? 0 : stored;
    }

    private MobAffix scaledMobAffix(LivingEntity mob) {
        String stored = mob.getPersistentDataContainer().get(mobAffixKey, PersistentDataType.STRING);
        return MobAffix.find(stored);
    }

    private MobSkill scaledMobSkill(LivingEntity mob) {
        String stored = mob.getPersistentDataContainer().get(mobSkillKey, PersistentDataType.STRING);
        return MobSkill.find(stored);
    }

    private MobSkill scaledMobSecondarySkill(LivingEntity mob) {
        String stored = mob.getPersistentDataContainer().get(mobSecondarySkillKey, PersistentDataType.STRING);
        return MobSkill.find(stored);
    }

    private ItemStack rollScaledMobReward(Player killer, LivingEntity mob, MobRarity rarity) {
        double chance = getConfig().getDouble("mob-scaling.rarities." + rarity.id() + ".equipment-drop-chance", 0.0);
        if (chance <= 0.0 || ThreadLocalRandom.current().nextDouble() > chance) {
            return null;
        }
        Material material = randomTierEquipment(killerLevelForLoot(killer));
        if (material == null) {
            return null;
        }
        ItemStack reward = new ItemStack(material);
        if (rarity == MobRarity.NORMAL) {
            return plainMobDrop(reward, killerLevelForLoot(killer));
        }
        return statMobDrop(reward, rarity, killerLevelForLoot(killer), scaledMobLevel(mob), scaledMobAffix(mob));
    }

    private void grantMythicMobReward(Player killer, LivingEntity mob, EntityDeathEvent event) {
        int mobLevel = Math.max(1, scaledMobLevel(mob));
        int playerLevel = killerLevelForLoot(killer);
        String archetype = mythicArchetype(mob);
        boolean bossTier = mob.getAttribute(Attribute.MAX_HEALTH) != null
                && mob.getAttribute(Attribute.MAX_HEALTH).getValue() >= getConfig().getDouble("mob-scaling.mythic.boss-health-threshold", 70.0);

        ItemStack gearReward = createMythicArchetypeReward(archetype, playerLevel, bossTier, mobLevel);
        if (gearReward != null) {
            event.getDrops().add(gearReward);
        }
        if (isMythicEnraged(mob)) {
            ItemStack enrageReward = createMythicPhaseReward(archetype, "enrage.rewards", playerLevel, bossTier, mobLevel);
            if (enrageReward != null) {
                event.getDrops().add(enrageReward);
            }
        }
        if (isMythicLastStand(mob)) {
            ItemStack lastStandReward = createMythicPhaseReward(archetype, "last-stand.rewards", playerLevel, bossTier, mobLevel);
            if (lastStandReward != null) {
                event.getDrops().add(lastStandReward);
            }
        }

        int crowns = bossTier
                ? getConfig().getInt("mob-scaling.mythic.boss-crowns-base", 40)
                : getConfig().getInt("mob-scaling.mythic.crowns-base", 18);
        crowns += getConfig().getInt("mob-scaling.mythic.archetypes." + archetype + ".crowns-bonus", 0);
        if (isMythicEnraged(mob)) {
            crowns += mythicPhaseCrownsBonus(mob, "enrage.rewards");
        }
        if (isMythicLastStand(mob)) {
            crowns += mythicPhaseCrownsBonus(mob, "last-stand.rewards");
        }
        crowns += Math.max(0, mobLevel / 3);
        depositCrowns(killer, crowns);
        showFloatingText(mob, Component.text("+" + crowns + " coronas", NamedTextColor.GOLD), 28L);
        killer.sendActionBar(Component.text("Botin Mythic: +" + crowns + " coronas", NamedTextColor.GOLD));
    }

    private String mythicArchetype(LivingEntity mob) {
        String plainName = PlainTextComponentSerializer.plainText().serialize(baseNameplate(mob)).toLowerCase(Locale.ROOT);
        ConfigurationSection section = getConfig().getConfigurationSection("mob-scaling.mythic.display-archetypes");
        if (section != null) {
            for (String displayName : section.getKeys(false)) {
                if (plainName.equalsIgnoreCase(displayName)
                        || plainName.contains(displayName.toLowerCase(Locale.ROOT))) {
                    return section.getString(displayName, "default");
                }
            }
        }
        return "default";
    }

    private MobSkill mythicCombatSkill(String archetype) {
        String configured = getConfig().getString("mob-scaling.mythic.archetypes." + archetype + ".combat-skill", "");
        MobSkill skill = MobSkill.find(configured);
        if (skill != null) {
            return skill;
        }
        return MobSkill.find(getConfig().getString("mob-scaling.mythic.archetypes.default.combat-skill", ""));
    }

    private MobSkill mythicSecondaryCombatSkill(String archetype) {
        String configured = getConfig().getString("mob-scaling.mythic.archetypes." + archetype + ".secondary-combat-skill", "");
        return MobSkill.find(configured);
    }

    private MobSkill mythicEnrageCombatSkill(LivingEntity mob) {
        ConfigurationSection enrage = mythicEnrageSection(mob);
        if (enrage == null) {
            return null;
        }
        return MobSkill.find(enrage.getString("combat-skill", ""));
    }

    private MobSkill mythicEnrageEntrySkill(LivingEntity mob) {
        ConfigurationSection enrage = mythicEnrageSection(mob);
        if (enrage == null) {
            return null;
        }
        return MobSkill.find(enrage.getString("entry-skill", ""));
    }

    private ConfigurationSection mythicLastStandSection(LivingEntity mob) {
        ConfigurationSection section = mythicArchetypeSection(mob);
        if (section == null) {
            return null;
        }
        ConfigurationSection lastStand = section.getConfigurationSection("last-stand");
        if (lastStand != null) {
            return lastStand;
        }
        ConfigurationSection defaults = mythicArchetypeSection("default");
        return defaults == null ? null : defaults.getConfigurationSection("last-stand");
    }

    private boolean isMythicLastStand(LivingEntity mob) {
        Byte stored = mob.getPersistentDataContainer().get(mobLastStandKey, PersistentDataType.BYTE);
        return stored != null && stored == (byte) 1;
    }

    private MobSkill mythicLastStandCombatSkill(LivingEntity mob) {
        ConfigurationSection section = mythicLastStandSection(mob);
        return section == null ? null : MobSkill.find(section.getString("combat-skill", ""));
    }

    private MobSkill mythicLastStandEntrySkill(LivingEntity mob) {
        ConfigurationSection section = mythicLastStandSection(mob);
        return section == null ? null : MobSkill.find(section.getString("entry-skill", ""));
    }

    private ConfigurationSection mythicArchetypeSection(String archetype) {
        return getConfig().getConfigurationSection("mob-scaling.mythic.archetypes." + archetype);
    }

    private ConfigurationSection mythicArchetypeSection(LivingEntity mob) {
        return mob.hasMetadata("MythicMob") ? mythicArchetypeSection(mythicArchetype(mob)) : null;
    }

    private ConfigurationSection mythicEnrageSection(LivingEntity mob) {
        ConfigurationSection section = mythicArchetypeSection(mob);
        if (section == null) {
            return null;
        }
        ConfigurationSection enrage = section.getConfigurationSection("enrage");
        if (enrage != null) {
            return enrage;
        }
        ConfigurationSection defaults = mythicArchetypeSection("default");
        return defaults == null ? null : defaults.getConfigurationSection("enrage");
    }

    private boolean isMythicEnraged(LivingEntity mob) {
        Byte stored = mob.getPersistentDataContainer().get(mobEnragedKey, PersistentDataType.BYTE);
        return stored != null && stored == (byte) 1;
    }

    private void tryActivateMythicEnrage(LivingEntity mob, double incomingDamage) {
        if (!mob.hasMetadata("MythicMob") || isMythicEnraged(mob)) {
            return;
        }
        ConfigurationSection enrage = mythicEnrageSection(mob);
        if (enrage == null || !enrage.getBoolean("enabled", false)) {
            return;
        }
        AttributeInstance maxHealthAttribute = mob.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = maxHealthAttribute == null ? mob.getHealth() : maxHealthAttribute.getValue();
        if (maxHealth <= 0.0) {
            return;
        }
        double healthAfterHit = Math.max(0.0, mob.getHealth() - incomingDamage);
        double threshold = enrage.getDouble("health-threshold", 0.35);
        if (healthAfterHit <= 0.0 || (healthAfterHit / maxHealth) > threshold) {
            return;
        }
        mob.getPersistentDataContainer().set(mobEnragedKey, PersistentDataType.BYTE, (byte) 1);
        Color accent = mythicAccentColor(mob);
        if (mob.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            mob.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(
                    mob.getAttribute(Attribute.ATTACK_DAMAGE).getBaseValue()
                            * enrage.getDouble("damage-multiplier", 1.15));
        }
        if (mob.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            mob.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(
                    mob.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue()
                            * enrage.getDouble("speed-multiplier", 1.10));
        }
        mob.getPersistentDataContainer().set(mobSkillCooldownKey, PersistentDataType.LONG,
                System.currentTimeMillis() + enrage.getLong("entry-cooldown-ms", 900L));
        showSkillCast(mob, Component.text(enrage.getString("label", "Furia"), mythicAccentTextColor(mob)));
        mob.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, mob.getLocation().add(0, 1.1, 0), 18, 0.6, 0.6, 0.6, 0.02);
        mob.getWorld().spawnParticle(Particle.DUST, mob.getLocation().add(0, 1.0, 0), 16, 0.45, 0.4, 0.45, 0.0,
                new Particle.DustOptions(accent, 1.15f));
        mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.7f, 1.2f);
        playMythicArchetypeCue(mob, 0.65f, 1.0f);
        triggerMythicEnrageEntrySkill(mob);
    }

    private void triggerMythicEnrageEntrySkill(LivingEntity mob) {
        MobSkill entrySkill = mythicEnrageEntrySkill(mob);
        MobRarity rarity = scaledMobRarity(mob);
        if (entrySkill == null || rarity == null) {
            return;
        }
        Player target = nearestReferencePlayer(mob.getLocation(), mythicAggroRange(mob));
        switch (entrySkill) {
            case CARGA -> {
                if (target != null) {
                    useMobCharge(mob, target, rarity);
                }
            }
            case ONDA_HELADA -> {
                if (target != null) {
                    useMobFrostWave(mob, target, rarity);
                }
            }
            case LLAMADO_CORRUPTO -> useMobCorruptCall(mob, rarity);
            case RETROCESO_TACTICO -> {
                if (target != null) {
                    useMobTacticalRetreat(mob, target, rarity);
                }
            }
            case GOLPE_TERRITORIAL -> {
                if (target != null) {
                    useMobTerritorialSlam(mob, target, rarity);
                }
            }
        }
    }

    private void tryActivateMythicLastStand(LivingEntity mob, double incomingDamage) {
        if (!mob.hasMetadata("MythicMob") || isMythicLastStand(mob)) {
            return;
        }
        ConfigurationSection lastStand = mythicLastStandSection(mob);
        if (lastStand == null || !lastStand.getBoolean("enabled", false)) {
            return;
        }
        AttributeInstance maxHealthAttribute = mob.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = maxHealthAttribute == null ? mob.getHealth() : maxHealthAttribute.getValue();
        if (maxHealth <= 0.0) {
            return;
        }
        double healthAfterHit = Math.max(0.0, mob.getHealth() - incomingDamage);
        double threshold = lastStand.getDouble("health-threshold", 0.18);
        if (healthAfterHit <= 0.0 || (healthAfterHit / maxHealth) > threshold) {
            return;
        }
        mob.getPersistentDataContainer().set(mobLastStandKey, PersistentDataType.BYTE, (byte) 1);
        Color accent = mythicAccentColor(mob);
        mob.getPersistentDataContainer().set(mobSkillCooldownKey, PersistentDataType.LONG,
                System.currentTimeMillis() + lastStand.getLong("entry-cooldown-ms", 700L));
        showSkillCast(mob, Component.text(lastStand.getString("label", "Ultima Resistencia"), mythicAccentTextColor(mob)));
        mob.getWorld().spawnParticle(Particle.DUST, mob.getLocation().add(0, 1.0, 0), 24, 0.55, 0.8, 0.55, 0.0,
                new Particle.DustOptions(accent, 1.2f));
        mob.getWorld().spawnParticle(Particle.CLOUD, mob.getLocation().add(0, 0.8, 0), 18, 0.6, 0.4, 0.6, 0.02);
        mob.getWorld().playSound(mob.getLocation(), Sound.ITEM_TOTEM_USE, 0.7f, 0.8f);
        playMythicArchetypeCue(mob, 0.55f, 0.85f);
        triggerMythicLastStandEntrySkill(mob);
    }

    private void triggerMythicLastStandEntrySkill(LivingEntity mob) {
        MobSkill entrySkill = mythicLastStandEntrySkill(mob);
        MobRarity rarity = scaledMobRarity(mob);
        if (entrySkill == null || rarity == null) {
            return;
        }
        Player target = nearestReferencePlayer(mob.getLocation(), mythicAggroRange(mob));
        switch (entrySkill) {
            case CARGA -> {
                if (target != null) {
                    useMobCharge(mob, target, rarity);
                }
            }
            case ONDA_HELADA -> {
                if (target != null) {
                    useMobFrostWave(mob, target, rarity);
                }
            }
            case LLAMADO_CORRUPTO -> useMobCorruptCall(mob, rarity);
            case RETROCESO_TACTICO -> {
                if (target != null) {
                    useMobTacticalRetreat(mob, target, rarity);
                }
            }
            case GOLPE_TERRITORIAL -> {
                if (target != null) {
                    useMobTerritorialSlam(mob, target, rarity);
                }
            }
        }
    }

    private MobRarity mythicCombatRarity(LivingEntity mob) {
        boolean bossTier = mob.getAttribute(Attribute.MAX_HEALTH) != null
                && mob.getAttribute(Attribute.MAX_HEALTH).getValue() >= getConfig().getDouble("mob-scaling.mythic.boss-health-threshold", 70.0);
        return bossTier ? MobRarity.MINIBOSS : MobRarity.RARE;
    }

    private ItemStack createMythicArchetypeReward(String archetype, int level, boolean bossTier, int mobLevel) {
        String tierKey = mythicRewardTier(level);
        ConfigurationSection section = getConfig().getConfigurationSection("mob-scaling.mythic.archetypes." + archetype);
        Material material = randomMaterialFromNames(section == null ? List.of() : section.getStringList("materials." + tierKey));
        if (material == null) {
            material = randomTierEquipment(level);
        }
        if (material == null) {
            return null;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        MobRarity rarity = bossTier ? MobRarity.MINIBOSS : MobRarity.RARE;
        String label = section == null ? "Botin Mythic" : section.getString("label", "Botin Mythic");
        MythicRewardStat rewardStat = mythicRewardStat(section, bossTier);
        EquipmentSlotGroup slotGroup = itemAttributeSlotGroup(item.getType(), rewardStat.attribute());
        meta.displayName(Component.text(label + " [" + tierLabel(level) + "]", rarity.color()));
        meta.lore(List.of(
                Component.text(rewardStat.label(), NamedTextColor.GREEN),
                Component.text("Trofeo de enemigo mythic nivel " + mobLevel + ".", NamedTextColor.GRAY)));
        applyBaseCombatAttributes(item.getType(), meta);
        upsertAttributeModifier(meta, rewardStat.attribute(), new AttributeModifier(
                NamespacedKey.minecraft("servidro_mythic_" + archetype + "_" + rewardStat.attribute().key().value()),
                rewardStat.amount(),
                AttributeModifier.Operation.ADD_NUMBER,
                slotGroup));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createMythicPhaseReward(String archetype, String phasePath, int level, boolean bossTier, int mobLevel) {
        String tierKey = mythicRewardTier(level);
        ConfigurationSection archetypeSection = getConfig().getConfigurationSection("mob-scaling.mythic.archetypes." + archetype);
        ConfigurationSection phaseSection = archetypeSection == null ? null : archetypeSection.getConfigurationSection(phasePath);
        if (phaseSection == null || !phaseSection.getBoolean("enabled", false)) {
            ConfigurationSection defaultSection = mythicArchetypeSection("default");
            phaseSection = defaultSection == null ? null : defaultSection.getConfigurationSection(phasePath);
        }
        if (phaseSection == null || !phaseSection.getBoolean("enabled", false)) {
            return null;
        }
        Material material = randomMaterialFromNames(phaseSection.getStringList("materials." + tierKey));
        if (material == null && archetypeSection != null) {
            material = randomMaterialFromNames(archetypeSection.getStringList("materials." + tierKey));
        }
        if (material == null) {
            material = randomTierEquipment(level);
        }
        if (material == null) {
            return null;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        MobRarity rarity = bossTier ? MobRarity.MINIBOSS : MobRarity.RARE;
        String label = phaseSection.getString("label", "Trofeo de fase");
        MythicRewardStat rewardStat = mythicPhaseRewardStat(phaseSection.getConfigurationSection("bonus"), bossTier);
        EquipmentSlotGroup slotGroup = itemAttributeSlotGroup(item.getType(), rewardStat.attribute());
        meta.displayName(Component.text(label + " [" + tierLabel(level) + "]", rarity.color()));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(rewardStat.label(), NamedTextColor.GREEN));
        lore.add(Component.text("Recompensa de fase del enemigo mythic.", NamedTextColor.GRAY));
        lore.add(Component.text("Nivel del trofeo: " + mobLevel + ".", NamedTextColor.DARK_GRAY));
        meta.lore(lore);
        applyBaseCombatAttributes(item.getType(), meta);
        upsertAttributeModifier(meta, rewardStat.attribute(), new AttributeModifier(
                NamespacedKey.minecraft("servidro_mythic_phase_" + archetype + "_" + rewardStat.attribute().key().value()),
                rewardStat.amount(),
                AttributeModifier.Operation.ADD_NUMBER,
                slotGroup));
        item.setItemMeta(meta);
        return item;
    }

    private Material randomMaterialFromPool(List<Material> pool) {
        return pool.isEmpty() ? null : pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    private Material randomMaterialFromNames(List<String> names) {
        List<Material> pool = new ArrayList<>();
        for (String name : names) {
            Material material = Material.matchMaterial(name);
            if (material != null) {
                pool.add(material);
            }
        }
        return randomMaterialFromPool(pool);
    }

    private String mythicRewardTier(int level) {
        if (level >= 15) {
            return "diamond";
        }
        if (level >= 10) {
            return "gold";
        }
        if (level >= 5) {
            return "iron";
        }
        return "apprentice";
    }

    private MythicRewardStat mythicRewardStat(ConfigurationSection archetypeSection, boolean bossTier) {
        String path = bossTier ? "bonus.boss." : "bonus.normal.";
        String label = archetypeSection == null ? null : archetypeSection.getString(path + "label");
        String attributeId = archetypeSection == null ? null : archetypeSection.getString(path + "attribute");
        double amount = archetypeSection == null ? 0.0 : archetypeSection.getDouble(path + "amount", 0.0);
        Attribute attribute = parseAttribute(attributeId);
        if (label == null || attribute == null || amount == 0.0) {
            return new MythicRewardStat(
                    bossTier ? "+2 Dano" : "+1 Dano",
                    bossTier ? 2.0 : 1.0,
                    Attribute.ATTACK_DAMAGE);
        }
        return new MythicRewardStat(label, amount, attribute);
    }

    private MythicRewardStat mythicPhaseRewardStat(ConfigurationSection bonusSection, boolean bossTier) {
        String path = bossTier ? "boss." : "normal.";
        String label = bonusSection == null ? null : bonusSection.getString(path + "label");
        String attributeId = bonusSection == null ? null : bonusSection.getString(path + "attribute");
        double amount = bonusSection == null ? 0.0 : bonusSection.getDouble(path + "amount", 0.0);
        Attribute attribute = parseAttribute(attributeId);
        if (label == null || attribute == null || amount == 0.0) {
            return new MythicRewardStat(
                    bossTier ? "+2 Dano" : "+1 Dano",
                    bossTier ? 2.0 : 1.0,
                    Attribute.ATTACK_DAMAGE);
        }
        return new MythicRewardStat(label, amount, attribute);
    }

    private int mythicPhaseCrownsBonus(LivingEntity mob, String phasePath) {
        ConfigurationSection phaseSection = mythicArchetypeSection(mob);
        phaseSection = phaseSection == null ? null : phaseSection.getConfigurationSection(phasePath);
        if (phaseSection == null || !phaseSection.getBoolean("enabled", false)) {
            ConfigurationSection defaults = mythicArchetypeSection("default");
            phaseSection = defaults == null ? null : defaults.getConfigurationSection(phasePath);
        }
        return phaseSection == null ? 0 : phaseSection.getInt("crowns-bonus", 0);
    }

    private Attribute parseAttribute(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return switch (value.toUpperCase(Locale.ROOT)) {
            case "ATTACK_DAMAGE" -> Attribute.ATTACK_DAMAGE;
            case "MAX_HEALTH" -> Attribute.MAX_HEALTH;
            case "MOVEMENT_SPEED" -> Attribute.MOVEMENT_SPEED;
            case "ARMOR" -> Attribute.ARMOR;
            case "ARMOR_TOUGHNESS" -> Attribute.ARMOR_TOUGHNESS;
            case "KNOCKBACK_RESISTANCE" -> Attribute.KNOCKBACK_RESISTANCE;
            default -> null;
        };
    }

    private int killerLevelForLoot(Player killer) {
        PlayerProfile profile = profiles.get(killer.getUniqueId());
        return profile == null ? 1 : profile.level();
    }

    private Material randomTierEquipment(int level) {
        List<Material> pool = new ArrayList<>();
        if (level >= 15) {
            pool.add(Material.DIAMOND_SWORD);
            pool.add(Material.DIAMOND_AXE);
            pool.add(Material.DIAMOND_HELMET);
            pool.add(Material.DIAMOND_CHESTPLATE);
            pool.add(Material.DIAMOND_LEGGINGS);
            pool.add(Material.DIAMOND_BOOTS);
        } else if (level >= 10) {
            pool.add(Material.IRON_SWORD);
            pool.add(Material.IRON_AXE);
            pool.add(Material.GOLDEN_SWORD);
            pool.add(Material.GOLDEN_AXE);
            pool.add(Material.IRON_HELMET);
            pool.add(Material.IRON_CHESTPLATE);
            pool.add(Material.IRON_LEGGINGS);
            pool.add(Material.IRON_BOOTS);
        } else if (level >= 5) {
            pool.add(Material.IRON_SWORD);
            pool.add(Material.IRON_AXE);
            pool.add(Material.IRON_HELMET);
            pool.add(Material.IRON_CHESTPLATE);
            pool.add(Material.IRON_LEGGINGS);
            pool.add(Material.IRON_BOOTS);
            pool.add(Material.SHIELD);
        } else {
            pool.add(Material.STONE_SWORD);
            pool.add(Material.STONE_AXE);
            pool.add(Material.LEATHER_HELMET);
            pool.add(Material.LEATHER_CHESTPLATE);
            pool.add(Material.LEATHER_LEGGINGS);
            pool.add(Material.LEATHER_BOOTS);
        }
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    private ItemStack plainMobDrop(ItemStack item, int level) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Botin de campo [" + tierLabel(level) + "]", NamedTextColor.GRAY));
        meta.lore(List.of(
                Component.text("Equipo sin estadisticas adicionales.", NamedTextColor.DARK_GRAY),
                Component.text("Apto para comercio o progreso base.", NamedTextColor.GRAY)));
        applyBaseCombatAttributes(item.getType(), meta);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack statMobDrop(ItemStack item, MobRarity rarity, int killerLevel, int mobLevel, MobAffix mobAffix) {
        ItemMeta meta = item.getItemMeta();
        String statLabel;
        double amount;
        Attribute attribute;
        EquipmentSlotGroup slotGroup = equipmentSlotGroupForMaterial(item.getType());
        int lootPower = lootPowerTier(rarity, killerLevel, mobLevel);
        if (isArmorPiece(item.getType()) || item.getType() == Material.SHIELD) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                amount = 2.0 + lootPower;
                statLabel = "+" + (int) amount + " Vida";
                attribute = Attribute.MAX_HEALTH;
            } else {
                amount = 1.0 + Math.max(1, (lootPower + 1) / 2);
                statLabel = "+" + (int) amount + " Armadura";
                attribute = Attribute.ARMOR;
            }
        } else {
            statLabel = rarity == MobRarity.MINIBOSS ? "+2 Daño" : "+1 Daño";
            amount = 1.0 + Math.max(1, lootPower / 2);
            attribute = Attribute.ATTACK_DAMAGE;
        }
        meta.displayName(Component.text("Botin " + rarity.displayName() + " [" + tierLabel(killerLevel) + "]", rarity.color()));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(statLabel, NamedTextColor.GREEN));
        if (mobAffix != null) {
            lore.add(Component.text("Afinidad: " + mobAffix.displayName(), mobAffix.color()));
        }
        lore.add(Component.text("Trofeo de enemigo nivel " + mobLevel + ".", NamedTextColor.GRAY));
        meta.lore(lore);
        applyBaseCombatAttributes(item.getType(), meta);
        upsertAttributeModifier(meta, attribute, new AttributeModifier(
                NamespacedKey.minecraft("servidro_" + rarity.id() + "_" + attribute.key().value()),
                amount,
                AttributeModifier.Operation.ADD_NUMBER,
                slotGroup));
        item.setItemMeta(meta);
        return item;
    }

    private boolean isArmorPiece(Material material) {
        String name = material.name();
        return name.endsWith("_HELMET")
                || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS")
                || name.endsWith("_BOOTS");
    }

    private int lootPowerTier(MobRarity rarity, int killerLevel, int mobLevel) {
        int rarityPower = switch (rarity) {
            case NORMAL -> 0;
            case RARE -> 1;
            case ELITE -> 3;
            case MINIBOSS -> 5;
        };
        int killerPower = Math.max(0, (killerLevel - 1) / 5);
        int mobAdvantage = Math.max(0, mobLevel - killerLevel);
        int mobPower = mobAdvantage / 4;
        return rarityPower + killerPower + mobPower;
    }

    private void applyBaseCombatAttributes(Material material, ItemMeta meta) {
        Double damage = baseItemAttackDamage(material);
        Double speed = baseItemAttackSpeed(material);
        Double armor = baseItemArmor(material);
        Double toughness = baseItemArmorToughness(material);
        Double knockbackResistance = baseItemKnockbackResistance(material);
        EquipmentSlotGroup slotGroup = equipmentSlotGroupForMaterial(material);
        if (damage != null) {
            upsertAttributeModifier(meta, Attribute.ATTACK_DAMAGE, new AttributeModifier(
                    NamespacedKey.minecraft("servidro_base_damage_" + material.name().toLowerCase(Locale.ROOT)),
                    damage,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.MAINHAND));
        }
        if (speed != null) {
            upsertAttributeModifier(meta, Attribute.ATTACK_SPEED, new AttributeModifier(
                    NamespacedKey.minecraft("servidro_base_speed_" + material.name().toLowerCase(Locale.ROOT)),
                    speed,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.MAINHAND));
        }
        if (armor != null) {
            upsertAttributeModifier(meta, Attribute.ARMOR, new AttributeModifier(
                    NamespacedKey.minecraft("servidro_base_armor_" + material.name().toLowerCase(Locale.ROOT)),
                    armor,
                    AttributeModifier.Operation.ADD_NUMBER,
                    slotGroup));
        }
        if (toughness != null) {
            upsertAttributeModifier(meta, Attribute.ARMOR_TOUGHNESS, new AttributeModifier(
                    NamespacedKey.minecraft("servidro_base_toughness_" + material.name().toLowerCase(Locale.ROOT)),
                    toughness,
                    AttributeModifier.Operation.ADD_NUMBER,
                    slotGroup));
        }
        if (knockbackResistance != null) {
            upsertAttributeModifier(meta, Attribute.KNOCKBACK_RESISTANCE, new AttributeModifier(
                    NamespacedKey.minecraft("servidro_base_knockback_" + material.name().toLowerCase(Locale.ROOT)),
                    knockbackResistance,
                    AttributeModifier.Operation.ADD_NUMBER,
                    slotGroup));
        }
    }

    private EquipmentSlotGroup equipmentSlotGroupForMaterial(Material material) {
        String name = material.name();
        if (name.endsWith("_HELMET")) {
            return EquipmentSlotGroup.HEAD;
        }
        if (name.endsWith("_CHESTPLATE")) {
            return EquipmentSlotGroup.CHEST;
        }
        if (name.endsWith("_LEGGINGS")) {
            return EquipmentSlotGroup.LEGS;
        }
        if (name.endsWith("_BOOTS")) {
            return EquipmentSlotGroup.FEET;
        }
        if (material == Material.SHIELD) {
            return EquipmentSlotGroup.OFFHAND;
        }
        return EquipmentSlotGroup.MAINHAND;
    }

    private EquipmentSlotGroup itemAttributeSlotGroup(Material material, Attribute attribute) {
        if (attribute == Attribute.ATTACK_DAMAGE || attribute == Attribute.ATTACK_SPEED) {
            return EquipmentSlotGroup.MAINHAND;
        }
        return equipmentSlotGroupForMaterial(material);
    }

    private void upsertAttributeModifier(ItemMeta meta, Attribute attribute, AttributeModifier modifier) {
        Collection<AttributeModifier> existing = meta.getAttributeModifiers(attribute);
        if (existing != null) {
            existing.stream()
                    .filter(current -> modifier.getKey().equals(current.getKey()))
                    .toList()
                    .forEach(current -> meta.removeAttributeModifier(attribute, current));
        }
        meta.addAttributeModifier(attribute, modifier);
    }

    private Double baseItemAttackDamage(Material material) {
        return switch (material) {
            case WOODEN_SWORD, GOLDEN_SWORD -> 4.0;
            case STONE_SWORD -> 5.0;
            case IRON_SWORD -> 6.0;
            case DIAMOND_SWORD -> 7.0;
            case NETHERITE_SWORD -> 8.0;
            case WOODEN_AXE, GOLDEN_AXE -> 7.0;
            case STONE_AXE, IRON_AXE, DIAMOND_AXE -> 9.0;
            case NETHERITE_AXE -> 10.0;
            case WOODEN_PICKAXE, GOLDEN_PICKAXE -> 2.0;
            case STONE_PICKAXE -> 3.0;
            case IRON_PICKAXE -> 4.0;
            case DIAMOND_PICKAXE -> 5.0;
            case NETHERITE_PICKAXE -> 6.0;
            case WOODEN_SHOVEL, GOLDEN_SHOVEL -> 2.5;
            case STONE_SHOVEL -> 3.5;
            case IRON_SHOVEL -> 4.5;
            case DIAMOND_SHOVEL -> 5.5;
            case NETHERITE_SHOVEL -> 6.5;
            case WOODEN_HOE, GOLDEN_HOE -> 1.0;
            case STONE_HOE -> 1.0;
            case IRON_HOE -> 1.0;
            case DIAMOND_HOE -> 1.0;
            case NETHERITE_HOE -> 1.0;
            case TRIDENT -> 9.0;
            default -> null;
        };
    }

    private Double baseItemAttackSpeed(Material material) {
        return switch (material) {
            case WOODEN_SWORD, STONE_SWORD, IRON_SWORD, DIAMOND_SWORD, NETHERITE_SWORD, GOLDEN_SWORD -> 1.6;
            case WOODEN_AXE, STONE_AXE, DIAMOND_AXE, NETHERITE_AXE -> 1.0;
            case IRON_AXE -> 0.9;
            case GOLDEN_AXE -> 1.0;
            case WOODEN_PICKAXE, GOLDEN_PICKAXE -> 1.2;
            case STONE_PICKAXE, IRON_PICKAXE, DIAMOND_PICKAXE, NETHERITE_PICKAXE -> 1.2;
            case WOODEN_SHOVEL, GOLDEN_SHOVEL -> 1.0;
            case STONE_SHOVEL, IRON_SHOVEL, DIAMOND_SHOVEL, NETHERITE_SHOVEL -> 1.0;
            case WOODEN_HOE, GOLDEN_HOE -> 1.0;
            case STONE_HOE -> 2.0;
            case IRON_HOE -> 3.0;
            case DIAMOND_HOE, NETHERITE_HOE -> 4.0;
            case TRIDENT -> 1.1;
            default -> null;
        };
    }

    private Double baseItemArmor(Material material) {
        return switch (material) {
            case LEATHER_HELMET -> 1.0;
            case LEATHER_BOOTS -> 1.0;
            case LEATHER_LEGGINGS -> 2.0;
            case LEATHER_CHESTPLATE -> 3.0;
            case CHAINMAIL_HELMET, IRON_HELMET, GOLDEN_HELMET -> 2.0;
            case CHAINMAIL_BOOTS, IRON_BOOTS, GOLDEN_BOOTS -> 2.0;
            case CHAINMAIL_LEGGINGS -> 4.0;
            case GOLDEN_LEGGINGS -> 3.0;
            case IRON_LEGGINGS -> 5.0;
            case CHAINMAIL_CHESTPLATE, GOLDEN_CHESTPLATE -> 5.0;
            case IRON_CHESTPLATE -> 6.0;
            case DIAMOND_HELMET, NETHERITE_HELMET -> 3.0;
            case DIAMOND_BOOTS, NETHERITE_BOOTS -> 3.0;
            case DIAMOND_LEGGINGS, NETHERITE_LEGGINGS -> 6.0;
            case DIAMOND_CHESTPLATE, NETHERITE_CHESTPLATE -> 8.0;
            case SHIELD -> 0.0;
            default -> null;
        };
    }

    private Double baseItemArmorToughness(Material material) {
        return switch (material) {
            case DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS -> 2.0;
            case NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS -> 3.0;
            default -> null;
        };
    }

    private Double baseItemKnockbackResistance(Material material) {
        return switch (material) {
            case NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS -> 0.1;
            default -> null;
        };
    }

    private String tierLabel(int level) {
        if (level >= 15) {
            return "Diamante";
        }
        if (level >= 10) {
            return "Oro";
        }
        if (level >= 5) {
            return "Hierro";
        }
        return "Aprendiz";
    }

    private String displayEntityType(EntityType type) {
        String[] parts = type.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }

    private String skillLabel(MobSkill skill) {
        return switch (skill) {
            case CARGA -> "Cargador";
            case ONDA_HELADA -> "Helado";
            case LLAMADO_CORRUPTO -> "Invocador";
            case RETROCESO_TACTICO -> "Tactico";
            case GOLPE_TERRITORIAL -> "Territorial";
        };
    }

    private void runScaledMobSkills() {
        Set<UUID> processed = new HashSet<>();
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            for (LivingEntity mob : viewer.getWorld().getLivingEntities()) {
                if (!mob.isValid() || mob.isDead() || !processed.add(mob.getUniqueId())) {
                    continue;
                }
                maintainCorruptCallBehavior(mob);
                MobSkill skill = scaledMobSkill(mob);
                MobRarity rarity = scaledMobRarity(mob);
                if (skill == null || rarity == null) {
                    continue;
                }
                if (viewer.getLocation().distanceSquared(mob.getLocation()) > 20 * 20) {
                    continue;
                }
                tryUseScaledMobSkill(mob, skill, rarity);
            }
        }
    }

    private void maintainCorruptCallBehavior(LivingEntity mob) {
        if (isOwnedMinion(mob)) {
            LivingEntity owner = minionOwner(mob);
            if (owner == null || owner.isDead() || !owner.isValid()) {
                return;
            }
            MobRarity ownerRarity = scaledMobRarity(owner);
            if ((ownerRarity == MobRarity.RARE || ownerRarity == MobRarity.ELITE)
                    && (isHuntActive(owner) || isHuntActive(mob))) {
                retargetHunter(mob, 60.0, false);
            }
            return;
        }
        if (!canUseCorruptCall(mob)) {
            return;
        }
        MobRarity rarity = scaledMobRarity(mob);
        if (rarity == null || rarity == MobRarity.NORMAL) {
            return;
        }
        if (rarity == MobRarity.MINIBOSS) {
            maintainMinibossVillageCorruption(mob);
            attractNearbyHostileFollowers(mob, 15, 18.0, false);
            return;
        }
        int maxMinions = mythicSummonLimit(mob);
        if (ownedMinions(mob).size() < maxMinions) {
            if (rarity == MobRarity.ELITE) {
                Player target = retargetHunter(mob, 60.0, false);
                if (target != null) {
                    attractEliteWave(mob, target);
                }
            }
            return;
        }
        boolean alreadyHunting = isHuntActive(mob);
        Player target = retargetHunter(mob, alreadyHunting ? 60.0 : Double.MAX_VALUE, true);
        if (target == null) {
            return;
        }
        if (!isWaveAnnounced(mob)) {
            announceEliteWave(mob);
            setWaveAnnounced(mob, true);
        }
        setHuntActive(mob, true);
        if (rarity == MobRarity.ELITE) {
            attractEliteWave(mob, target);
        }
        for (LivingEntity minion : ownedMinions(mob)) {
            setHuntActive(minion, true);
            if (minion instanceof Monster monster) {
                monster.setTarget(target);
            }
        }
    }

    private void maintainMinibossVillageCorruption(LivingEntity mob) {
        if (isVillageCorrupted(mob)) {
            return;
        }
        VillageCluster village = nearestVillageCluster(mob.getLocation(), 160.0);
        if (village == null) {
            return;
        }
        moveMobTowardLocation(mob, village.center(), 0.16);
        if (mob.getLocation().distanceSquared(village.center()) <= 8 * 8) {
            corruptVillage(mob, village);
        }
    }

    private void attractEliteWave(LivingEntity elite, Player target) {
        int gathered = 0;
        for (Entity entity : elite.getNearbyEntities(10.0, 6.0, 10.0)) {
            if (!(entity instanceof LivingEntity living) || living == elite || gathered >= 20) {
                continue;
            }
            MobRarity rarity = scaledMobRarity(living);
            if ((rarity != MobRarity.NORMAL && rarity != MobRarity.RARE)
                    || living.getType() != elite.getType()
                    || !(living instanceof Monster monster)
                    || isOwnedMinion(living)) {
                continue;
            }
            monster.setTarget(target);
            gathered++;
        }
    }

    private void attractNearbyHostileFollowers(LivingEntity anchor, int limit, double radius, boolean sameTypeOnly) {
        int gathered = 0;
        Player target = retargetHunter(anchor, Double.MAX_VALUE, false);
        for (Entity entity : anchor.getNearbyEntities(radius, 6.0, radius)) {
            if (!(entity instanceof LivingEntity living) || living == anchor || gathered >= limit) {
                continue;
            }
            MobRarity rarity = scaledMobRarity(living);
            if ((rarity != MobRarity.NORMAL && rarity != MobRarity.RARE)
                    || !(living instanceof Monster monster)
                    || isOwnedMinion(living)
                    || (sameTypeOnly && living.getType() != anchor.getType())) {
                continue;
            }
            if (target != null) {
                monster.setTarget(target);
            } else {
                nudgeTowardAnchor(living, anchor.getLocation(), 0.12);
            }
            gathered++;
        }
    }

    private void nudgeTowardAnchor(LivingEntity mob, Location anchor, double speed) {
        Vector direction = anchor.toVector().subtract(mob.getLocation().toVector());
        if (direction.lengthSquared() < 0.0001) {
            return;
        }
        Vector adjusted = mob.getVelocity().clone().multiply(0.72).add(direction.normalize().setY(0).multiply(speed));
        adjusted.setY(mob.getVelocity().getY());
        mob.setVelocity(adjusted);
    }

    private void moveMobTowardLocation(LivingEntity mob, Location destination, double speed) {
        Vector direction = destination.toVector().subtract(mob.getLocation().toVector());
        if (direction.lengthSquared() < 0.0001) {
            return;
        }
        Vector adjusted = mob.getVelocity().clone().multiply(0.72).add(direction.normalize().setY(0).multiply(speed));
        adjusted.setY(mob.getVelocity().getY());
        mob.setVelocity(adjusted);
    }

    private Player retargetHunter(LivingEntity hunter, double range, boolean assign) {
        if (hunter instanceof Monster monster) {
            Player current = currentValidTarget(monster);
            if (current != null) {
                return current;
            }
            Player target = nearestAlivePlayer(hunter.getLocation(), range);
            if (assign && target != null) {
                monster.setTarget(target);
            }
            return target;
        }
        return nearestAlivePlayer(hunter.getLocation(), range);
    }

    private Player currentValidTarget(Monster monster) {
        if (!(monster.getTarget() instanceof Player player)) {
            return null;
        }
        if (!player.isOnline() || player.isDead() || downed.containsKey(player.getUniqueId())
                || player.getWorld() != monster.getWorld()) {
            return null;
        }
        return player;
    }

    private Player nearestAlivePlayer(Location location, double range) {
        double bestDistance = range == Double.MAX_VALUE ? Double.MAX_VALUE : range * range;
        Player nearest = null;
        for (Player player : location.getWorld().getPlayers()) {
            if (!player.isOnline() || player.isDead() || downed.containsKey(player.getUniqueId())) {
                continue;
            }
            double distance = player.getLocation().distanceSquared(location);
            if (distance <= bestDistance) {
                bestDistance = distance;
                nearest = player;
            }
        }
        return nearest;
    }

    private void tryUseScaledMobSkill(LivingEntity mob, MobSkill skill, MobRarity rarity) {
        tryActivateMythicEnrage(mob, 0.0);
        Player target = nearestReferencePlayer(mob.getLocation(), mythicAggroRange(mob));
        if (target == null || downed.containsKey(target.getUniqueId())) {
            return;
        }
        applyMovementBias(mob, target);
        if (!withinPreferredRange(mob, target)) {
            return;
        }
        long now = System.currentTimeMillis();
        long readyAt = mob.getPersistentDataContainer().getOrDefault(mobSkillCooldownKey, PersistentDataType.LONG, 0L);
        if (readyAt > now || isMobSkillPending(mob)) {
            return;
        }
        MobSkill resolvedSkill = resolveContextualSkill(mob, skill);
        if (maybeTelegraphMythicSkill(mob, target, rarity, resolvedSkill)) {
            return;
        }
        castMobSkill(mob, target, rarity, resolvedSkill);
    }

    private void castMobSkill(LivingEntity mob, Player target, MobRarity rarity, MobSkill resolvedSkill) {
        switch (resolvedSkill) {
            case CARGA -> useMobCharge(mob, target, rarity);
            case ONDA_HELADA -> useMobFrostWave(mob, target, rarity);
            case LLAMADO_CORRUPTO -> useMobCorruptCall(mob, rarity);
            case RETROCESO_TACTICO -> useMobTacticalRetreat(mob, target, rarity);
            case GOLPE_TERRITORIAL -> useMobTerritorialSlam(mob, target, rarity);
        }
    }

    private boolean isMobSkillPending(LivingEntity mob) {
        Byte stored = mob.getPersistentDataContainer().get(mobSkillPendingKey, PersistentDataType.BYTE);
        return stored != null && stored == (byte) 1;
    }

    private boolean maybeTelegraphMythicSkill(LivingEntity mob, Player target, MobRarity rarity, MobSkill skill) {
        if (!mob.hasMetadata("MythicMob")) {
            return false;
        }
        int telegraphTicks = getConfig().getInt("mob-scaling.skills." + mobSkillConfigPath(skill) + ".telegraph-ticks", 0);
        if (telegraphTicks <= 0) {
            return false;
        }
        mob.getPersistentDataContainer().set(mobSkillPendingKey, PersistentDataType.BYTE, (byte) 1);
        long holdUntil = System.currentTimeMillis() + (telegraphTicks * 50L);
        mob.getPersistentDataContainer().set(mobSkillCooldownKey, PersistentDataType.LONG, holdUntil);
        showMobSkillTelegraph(mob, target, skill, telegraphTicks);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!mob.isValid() || mob.isDead()) {
                mob.getPersistentDataContainer().remove(mobSkillPendingKey);
                return;
            }
            mob.getPersistentDataContainer().remove(mobSkillPendingKey);
            Player currentTarget = target != null && target.isOnline() && !downed.containsKey(target.getUniqueId())
                    ? target
                    : nearestReferencePlayer(mob.getLocation(), mythicAggroRange(mob));
            if (skill == MobSkill.LLAMADO_CORRUPTO || currentTarget != null) {
                castMobSkill(mob, currentTarget, rarity, skill);
            }
        }, telegraphTicks);
        return true;
    }

    private String mobSkillConfigPath(MobSkill skill) {
        return switch (skill) {
            case CARGA -> "charge";
            case ONDA_HELADA -> "frost-wave";
            case LLAMADO_CORRUPTO -> "corrupt-call";
            case RETROCESO_TACTICO -> "tactical-retreat";
            case GOLPE_TERRITORIAL -> "territorial-slam";
        };
    }

    private void showMobSkillTelegraph(LivingEntity mob, Player target, MobSkill skill, int telegraphTicks) {
        Color accent = mythicAccentColor(mob);
        NamedTextColor textColor = mythicAccentTextColor(mob);
        switch (skill) {
            case CARGA -> {
                showSkillCast(mob, Component.text("Embiste...", textColor));
                if (target != null) {
                    spawnPersistentChargeTelegraph(mob, target, telegraphTicks, accent);
                }
                mob.getWorld().playSound(mob.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_0, 0.65f, 0.9f);
                playMythicArchetypeCue(mob, 0.45f, 1.1f);
            }
            case ONDA_HELADA -> {
                showSkillCast(mob, Component.text("Canaliza hielo...", textColor));
                mob.getWorld().spawnParticle(Particle.SNOWFLAKE, mob.getLocation().add(0, 1.0, 0), 22, 1.4, 0.5, 1.4, 0.03);
                spawnPersistentTelegraphRing(mob, 4.0, 1.2, accent, 20, 0.12, telegraphTicks);
                mob.getWorld().playSound(mob.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f, 1.7f);
                playMythicArchetypeCue(mob, 0.4f, 1.25f);
            }
            case LLAMADO_CORRUPTO -> {
                showSkillCast(mob, Component.text("Invoca corrupcion...", textColor));
                mob.getWorld().spawnParticle(Particle.SMOKE, mob.getLocation().add(0, 1.0, 0), 20, 0.9, 0.6, 0.9, 0.03);
                mob.getWorld().spawnParticle(Particle.ENCHANT, mob.getLocation().add(0, 1.0, 0), 16, 0.7, 0.7, 0.7, 0.01);
                spawnPersistentTelegraphRing(mob, 1.1, 3.0, accent, 18, 0.08, telegraphTicks);
                mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_EVOKER_PREPARE_SUMMON, 0.7f, 0.8f);
                playMythicArchetypeCue(mob, 0.5f, 0.9f);
            }
            case RETROCESO_TACTICO -> {
                showSkillCast(mob, Component.text("Repliega...", textColor));
                mob.getWorld().spawnParticle(Particle.SMALL_GUST, mob.getLocation().add(0, 0.8, 0), 12, 0.35, 0.35, 0.35, 0.02);
                mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_BREEZE_INHALE, 0.55f, 1.15f);
                playMythicArchetypeCue(mob, 0.35f, 1.2f);
            }
            case GOLPE_TERRITORIAL -> {
                showSkillCast(mob, Component.text("Carga impacto...", textColor));
                mob.getWorld().spawnParticle(Particle.BLOCK, mob.getLocation().add(0, 0.1, 0), 18, 1.2, 0.1, 1.2,
                        Bukkit.createBlockData(Material.COARSE_DIRT));
                double radius = getConfig().getDouble("mob-scaling.skills.territorial-slam.radius", 4.5);
                spawnPersistentTelegraphRing(mob, radius, 1.4, accent, 24, 0.1, telegraphTicks);
                mob.getWorld().playSound(mob.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.55f, 0.7f);
                playMythicArchetypeCue(mob, 0.5f, 0.8f);
            }
        }
    }

    private void spawnTelegraphRing(Location center, double radius, Color color, int points, double heightOffset) {
        Location base = center.clone().add(0, heightOffset, 0);
        Particle.DustOptions dust = new Particle.DustOptions(color, 1.1f);
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2 * i) / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location point = base.clone().add(x, 0, z);
            center.getWorld().spawnParticle(Particle.DUST, point, 1, 0.02, 0.02, 0.02, 0.0, dust);
        }
    }

    private void spawnPersistentTelegraphRing(
            LivingEntity anchor,
            double startRadius,
            double endRadius,
            Color color,
            int points,
            double heightOffset,
            int durationTicks) {
        final int stepTicks = 4;
        new BukkitRunnable() {
            int elapsed = 0;

            @Override
            public void run() {
                if (elapsed > durationTicks || !anchor.isValid() || anchor.isDead()) {
                    cancel();
                    return;
                }
                double progress = Math.min(1.0, (double) elapsed / Math.max(1, durationTicks));
                double radius = startRadius + ((endRadius - startRadius) * progress);
                spawnTelegraphRing(anchor.getLocation(), radius, color, points, heightOffset);
                elapsed += stepTicks;
            }
        }.runTaskTimer(this, 0L, stepTicks);
    }

    private void spawnPersistentChargeTelegraph(LivingEntity anchor, Player target, int durationTicks, Color color) {
        final int stepTicks = 4;
        new BukkitRunnable() {
            int elapsed = 0;

            @Override
            public void run() {
                if (elapsed > durationTicks || !anchor.isValid() || anchor.isDead() || !target.isOnline()) {
                    cancel();
                    return;
                }
                Vector direction = target.getLocation().toVector().subtract(anchor.getLocation().toVector());
                if (direction.lengthSquared() < 0.0001) {
                    direction = anchor.getLocation().getDirection();
                }
                direction = direction.normalize();
                double maxDistance = Math.min(7.0, Math.max(2.5, anchor.getLocation().distance(target.getLocation())));
                for (double step = 0.8; step <= maxDistance; step += 0.55) {
                    Location point = anchor.getLocation().clone().add(direction.clone().multiply(step)).add(0, 0.22, 0);
                    anchor.getWorld().spawnParticle(Particle.DUST, point, 1, 0.02, 0.02, 0.02, 0.0,
                            new Particle.DustOptions(color, 1.0f));
                }
                elapsed += stepTicks;
            }
        }.runTaskTimer(this, 0L, stepTicks);
    }

    private void spawnMobSkillImpactFlash(LivingEntity mob, Color color, double radius) {
        Particle.DustOptions dust = new Particle.DustOptions(color, 1.35f);
        mob.getWorld().spawnParticle(Particle.DUST, mob.getLocation().add(0, 0.2, 0), 28, radius / 3, 0.15, radius / 3, 0.0, dust);
        mob.getWorld().spawnParticle(Particle.FLASH, mob.getLocation().add(0, 1.0, 0), 1, 0, 0, 0, 0);
    }

    private Color mythicAccentColor(LivingEntity mob) {
        if (!mob.hasMetadata("MythicMob")) {
            return Color.ORANGE;
        }
        return switch (mythicArchetype(mob)) {
            case "bandit" -> Color.ORANGE;
            case "captain" -> Color.RED;
            case "knight" -> Color.fromRGB(180, 180, 200);
            case "king" -> Color.PURPLE;
            case "stormbeast" -> Color.AQUA;
            case "sludge" -> Color.fromRGB(70, 120, 70);
            case "minion" -> Color.GRAY;
            default -> Color.ORANGE;
        };
    }

    private NamedTextColor mythicAccentTextColor(LivingEntity mob) {
        if (!mob.hasMetadata("MythicMob")) {
            return NamedTextColor.GOLD;
        }
        return switch (mythicArchetype(mob)) {
            case "bandit" -> NamedTextColor.GOLD;
            case "captain" -> NamedTextColor.RED;
            case "knight" -> NamedTextColor.WHITE;
            case "king" -> NamedTextColor.DARK_PURPLE;
            case "stormbeast" -> NamedTextColor.AQUA;
            case "sludge" -> NamedTextColor.GREEN;
            case "minion" -> NamedTextColor.GRAY;
            default -> NamedTextColor.GOLD;
        };
    }

    private Sound mythicArchetypeSound(LivingEntity mob) {
        if (!mob.hasMetadata("MythicMob")) {
            return Sound.BLOCK_AMETHYST_BLOCK_CHIME;
        }
        return switch (mythicArchetype(mob)) {
            case "bandit" -> Sound.ENTITY_PILLAGER_AMBIENT;
            case "captain" -> Sound.ITEM_SHIELD_BLOCK;
            case "knight" -> Sound.ENTITY_IRON_GOLEM_DAMAGE;
            case "king" -> Sound.ENTITY_WITHER_AMBIENT;
            case "stormbeast" -> Sound.ENTITY_LIGHTNING_BOLT_THUNDER;
            case "sludge" -> Sound.BLOCK_MUD_HIT;
            case "minion" -> Sound.ENTITY_SKELETON_AMBIENT;
            default -> Sound.BLOCK_AMETHYST_BLOCK_CHIME;
        };
    }

    private void playMythicArchetypeCue(LivingEntity mob, float volume, float pitch) {
        if (!mob.hasMetadata("MythicMob")) {
            return;
        }
        mob.getWorld().playSound(mob.getLocation(), mythicArchetypeSound(mob), volume, pitch);
    }

    private MobSkill resolveContextualSkill(LivingEntity mob, MobSkill primarySkill) {
        if (!mob.hasMetadata("MythicMob")) {
            return primarySkill;
        }
        if (isMythicLastStand(mob)) {
            MobSkill lastStandSkill = mythicLastStandCombatSkill(mob);
            if (lastStandSkill != null) {
                return lastStandSkill;
            }
        }
        if (isMythicEnraged(mob)) {
            MobSkill enrageSkill = mythicEnrageCombatSkill(mob);
            if (enrageSkill != null) {
                return enrageSkill;
            }
        }
        String archetype = mythicArchetype(mob);
        MobSkill secondarySkill = scaledMobSecondarySkill(mob);
        double chance = getConfig().getDouble("mob-scaling.mythic.archetypes." + archetype + ".secondary-skill-chance", 0.0);
        if (secondarySkill != null && ThreadLocalRandom.current().nextDouble() < chance) {
            return secondarySkill;
        }
        return primarySkill;
    }

    private double mythicAggroRange(LivingEntity mob) {
        if (!mob.hasMetadata("MythicMob")) {
            return 14.0;
        }
        ConfigurationSection section = mythicArchetypeSection(mob);
        double aggression = section == null ? 1.0 : section.getDouble("aggression", 1.0);
        return Math.max(8.0, 14.0 * Math.max(0.6, aggression));
    }

    private boolean withinPreferredRange(LivingEntity mob, Player target) {
        if (!mob.hasMetadata("MythicMob")) {
            return true;
        }
        ConfigurationSection section = mythicArchetypeSection(mob);
        double minRange = section == null ? 0.0 : section.getDouble("preferred-min-range", 0.0);
        double maxRange = section == null ? 14.0 : section.getDouble("preferred-max-range", 14.0);
        double distanceSquared = mob.getLocation().distanceSquared(target.getLocation());
        return distanceSquared >= minRange * minRange && distanceSquared <= maxRange * maxRange;
    }

    private void applyMovementBias(LivingEntity mob, Player target) {
        if (!mob.hasMetadata("MythicMob")) {
            return;
        }
        ConfigurationSection section = mythicArchetypeSection(mob);
        if (section == null) {
            return;
        }
        String bias = section.getString("movement-bias", "hold").toLowerCase(Locale.ROOT);
        double minRange = section.getDouble("preferred-min-range", 0.0);
        double maxRange = section.getDouble("preferred-max-range", 14.0);
        double distance = mob.getLocation().distance(target.getLocation());
        Vector direction;
        double horizontalBoost;
        switch (bias) {
            case "close" -> {
                if (distance > minRange + 1.0) {
                    direction = target.getLocation().toVector().subtract(mob.getLocation().toVector()).normalize();
                    horizontalBoost = 0.16;
                } else {
                    return;
                }
            }
            case "kite" -> {
                if (distance < maxRange - 1.0) {
                    direction = mob.getLocation().toVector().subtract(target.getLocation().toVector()).normalize();
                    horizontalBoost = 0.14;
                } else {
                    return;
                }
            }
            default -> {
                if (distance < minRange) {
                    direction = mob.getLocation().toVector().subtract(target.getLocation().toVector()).normalize();
                    horizontalBoost = 0.12;
                } else if (distance > maxRange) {
                    direction = target.getLocation().toVector().subtract(mob.getLocation().toVector()).normalize();
                    horizontalBoost = 0.12;
                } else {
                    return;
                }
            }
        }
        if (Double.isFinite(direction.getX()) && Double.isFinite(direction.getZ())) {
            Vector adjusted = mob.getVelocity().clone().multiply(0.72).add(direction.setY(0).multiply(horizontalBoost));
            adjusted.setY(mob.getVelocity().getY());
            mob.setVelocity(adjusted);
        }
        if (mob instanceof Monster monster) {
            monster.setTarget(target);
        }
    }

    private void useMobCharge(LivingEntity mob, Player target, MobRarity rarity) {
        if (mob.getLocation().distanceSquared(target.getLocation()) < 5 * 5
                || mob.getLocation().distanceSquared(target.getLocation()) > 14 * 14) {
            return;
        }
        Vector dash = target.getLocation().toVector().subtract(mob.getLocation().toVector()).normalize()
                .multiply(getConfig().getDouble("mob-scaling.skills.charge.velocity", 1.2))
                .setY(0.32);
        showSkillCast(mob, Component.text("Carga", NamedTextColor.GOLD));
        mob.setVelocity(dash);
        mob.getWorld().spawnParticle(Particle.CLOUD, mob.getLocation().add(0, 0.4, 0), 18, 0.4, 0.25, 0.4, 0.03);
        spawnMobSkillImpactFlash(mob, Color.ORANGE, 2.4);
        mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 0.75f, 1.25f);
        setScaledMobSkillCooldown(mob, rarity, getConfig().getInt("mob-scaling.skills.charge.cooldown-seconds", 10));
    }

    private void useMobFrostWave(LivingEntity mob, Player target, MobRarity rarity) {
        if (mob.getLocation().distanceSquared(target.getLocation()) > 8 * 8) {
            return;
        }
        showSkillCast(mob, Component.text("Onda Helada", NamedTextColor.AQUA));
        int slowTicks = getConfig().getInt("mob-scaling.skills.frost-wave.slow-ticks", 70);
        double damage = getConfig().getDouble("mob-scaling.skills.frost-wave.damage", 3.0);
        for (Entity entity : mob.getNearbyEntities(4.0, 2.0, 4.0)) {
            if (entity instanceof LivingEntity living && isMobSkillTarget(mob, living)) {
                dealMobSkillDamage(mob, living, damage);
                applySlow(living, slowTicks);
            }
        }
        mob.getWorld().spawnParticle(Particle.SNOWFLAKE, mob.getLocation().add(0, 1, 0), 40, 2.0, 0.8, 2.0, 0.04);
        spawnMobSkillImpactFlash(mob, Color.AQUA, 3.6);
        mob.getWorld().playSound(mob.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.8f, 0.8f);
        setScaledMobSkillCooldown(mob, rarity, getConfig().getInt("mob-scaling.skills.frost-wave.cooldown-seconds", 12));
    }

    private void useMobCorruptCall(LivingEntity mob, MobRarity rarity) {
        int maxMinions = mythicSummonLimit(mob);
        List<LivingEntity> ownedMinions = ownedMinions(mob);
        if (ownedMinions.size() >= maxMinions) {
            return;
        }
        int summons = Math.max(1, Math.min(2, maxMinions - ownedMinions.size()));
        showSkillCast(mob, Component.text("Llamado Corrupto", NamedTextColor.DARK_PURPLE));
        for (int i = 0; i < summons; i++) {
            Location spawn = mob.getLocation().clone().add(ThreadLocalRandom.current().nextDouble(-2.5, 2.5), 0, ThreadLocalRandom.current().nextDouble(-2.5, 2.5));
            EntityType minionType = corruptCallMinionType(mob);
            Entity spawned = mob.getWorld().spawnEntity(spawn, minionType);
            if (!(spawned instanceof LivingEntity minion)) {
                spawned.remove();
                continue;
            }
            MobRarity minionRarity = ThreadLocalRandom.current().nextDouble()
                    < getConfig().getDouble("mob-scaling.skills.corrupt-call.rare-minion-chance", 0.25)
                    ? MobRarity.RARE
                    : MobRarity.NORMAL;
            applyScaledMobProfile(minion,
                    Math.max(1, scaledMobLevel(mob) - 2),
                    minionRarity,
                    minionRarity == MobRarity.RARE ? rollMobAffix(minionType, minionRarity) : null,
                    null);
            minion.getPersistentDataContainer().set(mobMinionOwnerKey, PersistentDataType.STRING, mob.getUniqueId().toString());
            applyMinionLoadout(minion, minionRarity);
            minion.customName(Component.text("[MINION] ", NamedTextColor.DARK_PURPLE)
                    .append(Component.text("[LVL " + scaledMobLevel(minion) + " Esbirro] ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(displayEntityType(minion.getType()) + " Corrompido", NamedTextColor.GRAY)));
            threatManager.markManaged(minion);
            Player target = retargetHunter(mob, Double.MAX_VALUE, false);
            if (target != null && minion instanceof Monster monster) {
                monster.setTarget(target);
            }
        }
        mob.getWorld().spawnParticle(Particle.SMOKE, mob.getLocation().add(0, 1, 0), 26, 1.2, 0.8, 1.2, 0.05);
        spawnMobSkillImpactFlash(mob, Color.PURPLE, 2.8);
        mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.45f, 1.5f);
        setScaledMobSkillCooldown(mob, rarity, getConfig().getInt("mob-scaling.skills.corrupt-call.cooldown-seconds", 10));
    }

    private int mythicSummonLimit(LivingEntity mob) {
        int base = getConfig().getInt("mob-scaling.skills.corrupt-call.max-minions", 15);
        if (!mob.hasMetadata("MythicMob")) {
            return base;
        }
        ConfigurationSection section = mythicArchetypeSection(mob);
        return Math.max(1, section == null ? base : Math.max(base, section.getInt("summon-limit", base)));
    }

    private EntityType corruptCallMinionType(LivingEntity mob) {
        if (!getConfig().getBoolean("mob-scaling.skills.corrupt-call.same-type-only", true)) {
            return EntityType.ZOMBIE;
        }
        return switch (mob.getType()) {
            case CREEPER, ZOMBIE, HUSK, DROWNED, SKELETON, STRAY, SPIDER, CAVE_SPIDER,
                    PIGLIN, ZOMBIFIED_PIGLIN, ENDERMAN -> mob.getType();
            default -> EntityType.ZOMBIE;
        };
    }

    private void applyMobLoadout(LivingEntity mob, MobRarity rarity) {
        if (rarity == null || isOwnedMinion(mob) || isPassiveMob(mob)) {
            return;
        }
        EntityEquipment equipment = mob.getEquipment();
        if (equipment == null) {
            return;
        }
        if (canWearArmor(mob.getType())) {
            Material armorTier = switch (rarity) {
                case RARE -> Material.IRON_INGOT;
                case ELITE -> Material.DIAMOND;
                case MINIBOSS -> Material.NETHERITE_INGOT;
                default -> null;
            };
            if (armorTier != null) {
                equipFullArmorSet(equipment, armorTier);
            }
        }
        maybeEquipWeapon(mob, equipment, rarity, false);
        setEquipmentDropChances(equipment, 0.0f);
    }

    private void applyMinionLoadout(LivingEntity minion, MobRarity rarity) {
        EntityEquipment equipment = minion.getEquipment();
        if (equipment == null) {
            return;
        }
        if (canWearArmor(minion.getType())) {
            equipFullArmorSet(equipment, rarity == MobRarity.RARE ? Material.IRON_INGOT : Material.LEATHER);
        }
        maybeEquipWeapon(minion, equipment, rarity, true);
        setEquipmentDropChances(equipment, 0.0f);
    }

    private void maybeEquipWeapon(LivingEntity mob, EntityEquipment equipment, MobRarity rarity, boolean minion) {
        double chance = weaponChanceFor(rarity, minion);
        if (ThreadLocalRandom.current().nextDouble() > chance) {
            return;
        }
        Material weapon = pickWeaponForType(mob.getType(), rarity, minion);
        if (weapon == null) {
            return;
        }
        equipment.setItemInMainHand(new ItemStack(weapon));
        equipment.setItemInMainHandDropChance(0.0f);
    }

    private double weaponChanceFor(MobRarity rarity, boolean minion) {
        if (minion) {
            String key = rarity == MobRarity.RARE ? "minion-rare" : "minion-normal";
            return getConfig().getDouble("mob-scaling.equipment.weapon-chances." + key,
                    rarity == MobRarity.RARE ? 0.65 : 0.35);
        }
        String key = rarity.id();
        double fallback = switch (rarity) {
            case RARE -> 0.45;
            case ELITE -> 0.8;
            case MINIBOSS -> 1.0;
            default -> 0.0;
        };
        return getConfig().getDouble("mob-scaling.equipment.weapon-chances." + key, fallback);
    }

    private Material pickWeaponForType(EntityType type, MobRarity rarity, boolean minion) {
        Material tier = weaponTierMaterial(rarity, minion);
        if (tier == null) {
            return null;
        }
        return switch (type) {
            case SKELETON, STRAY -> ThreadLocalRandom.current().nextDouble()
                    < getConfig().getDouble("mob-scaling.equipment.ranged-bow-chance", 0.85)
                    ? bowForTier(tier)
                    : swordForTier(tier);
            case ZOMBIE, HUSK, DROWNED, ZOMBIFIED_PIGLIN, PIGLIN -> ThreadLocalRandom.current().nextDouble()
                    < getConfig().getDouble("mob-scaling.equipment.axe-chance", 0.3)
                    ? axeForTier(tier)
                    : swordForTier(tier);
            case CREEPER, SPIDER, CAVE_SPIDER, ENDERMAN, BEE, POLAR_BEAR, WOLF, IRON_GOLEM, LLAMA, TRADER_LLAMA -> null;
            default -> swordForTier(tier);
        };
    }

    private Material weaponTierMaterial(MobRarity rarity, boolean minion) {
        if (minion) {
            return rarity == MobRarity.RARE ? Material.IRON_INGOT : Material.LEATHER;
        }
        return switch (rarity) {
            case RARE -> Material.IRON_INGOT;
            case ELITE -> Material.DIAMOND;
            case MINIBOSS -> Material.NETHERITE_INGOT;
            default -> null;
        };
    }

    private boolean canWearArmor(EntityType type) {
        return switch (type) {
            case ZOMBIE, HUSK, DROWNED, SKELETON, STRAY, ZOMBIFIED_PIGLIN, PIGLIN -> true;
            default -> false;
        };
    }

    private void equipFullArmorSet(EntityEquipment equipment, Material tierMaterial) {
        equipment.setHelmet(new ItemStack(helmetForTier(tierMaterial)));
        equipment.setChestplate(new ItemStack(chestplateForTier(tierMaterial)));
        equipment.setLeggings(new ItemStack(leggingsForTier(tierMaterial)));
        equipment.setBoots(new ItemStack(bootsForTier(tierMaterial)));
    }

    private void setEquipmentDropChances(EntityEquipment equipment, float chance) {
        equipment.setHelmetDropChance(chance);
        equipment.setChestplateDropChance(chance);
        equipment.setLeggingsDropChance(chance);
        equipment.setBootsDropChance(chance);
        equipment.setItemInMainHandDropChance(chance);
        equipment.setItemInOffHandDropChance(chance);
    }

    private Material helmetForTier(Material tierMaterial) {
        return switch (tierMaterial) {
            case LEATHER -> Material.LEATHER_HELMET;
            case IRON_INGOT -> Material.IRON_HELMET;
            case DIAMOND -> Material.DIAMOND_HELMET;
            case NETHERITE_INGOT -> Material.NETHERITE_HELMET;
            default -> Material.CHAINMAIL_HELMET;
        };
    }

    private Material chestplateForTier(Material tierMaterial) {
        return switch (tierMaterial) {
            case LEATHER -> Material.LEATHER_CHESTPLATE;
            case IRON_INGOT -> Material.IRON_CHESTPLATE;
            case DIAMOND -> Material.DIAMOND_CHESTPLATE;
            case NETHERITE_INGOT -> Material.NETHERITE_CHESTPLATE;
            default -> Material.CHAINMAIL_CHESTPLATE;
        };
    }

    private Material leggingsForTier(Material tierMaterial) {
        return switch (tierMaterial) {
            case LEATHER -> Material.LEATHER_LEGGINGS;
            case IRON_INGOT -> Material.IRON_LEGGINGS;
            case DIAMOND -> Material.DIAMOND_LEGGINGS;
            case NETHERITE_INGOT -> Material.NETHERITE_LEGGINGS;
            default -> Material.CHAINMAIL_LEGGINGS;
        };
    }

    private Material bootsForTier(Material tierMaterial) {
        return switch (tierMaterial) {
            case LEATHER -> Material.LEATHER_BOOTS;
            case IRON_INGOT -> Material.IRON_BOOTS;
            case DIAMOND -> Material.DIAMOND_BOOTS;
            case NETHERITE_INGOT -> Material.NETHERITE_BOOTS;
            default -> Material.CHAINMAIL_BOOTS;
        };
    }

    private Material swordForTier(Material tierMaterial) {
        return switch (tierMaterial) {
            case LEATHER -> Material.WOODEN_SWORD;
            case IRON_INGOT -> Material.IRON_SWORD;
            case DIAMOND -> Material.DIAMOND_SWORD;
            case NETHERITE_INGOT -> Material.NETHERITE_SWORD;
            default -> Material.STONE_SWORD;
        };
    }

    private Material axeForTier(Material tierMaterial) {
        return switch (tierMaterial) {
            case LEATHER -> Material.WOODEN_AXE;
            case IRON_INGOT -> Material.IRON_AXE;
            case DIAMOND -> Material.DIAMOND_AXE;
            case NETHERITE_INGOT -> Material.NETHERITE_AXE;
            default -> Material.STONE_AXE;
        };
    }

    private Material bowForTier(Material tierMaterial) {
        return Material.BOW;
    }

    private boolean canUseCorruptCall(LivingEntity mob) {
        return scaledMobSkill(mob) == MobSkill.LLAMADO_CORRUPTO
                || scaledMobSecondarySkill(mob) == MobSkill.LLAMADO_CORRUPTO
                || mythicEnrageCombatSkill(mob) == MobSkill.LLAMADO_CORRUPTO
                || mythicLastStandCombatSkill(mob) == MobSkill.LLAMADO_CORRUPTO;
    }

    private boolean isOwnedMinion(LivingEntity mob) {
        return mob.getPersistentDataContainer().has(mobMinionOwnerKey, PersistentDataType.STRING);
    }

    private LivingEntity minionOwner(LivingEntity mob) {
        String ownerId = mob.getPersistentDataContainer().get(mobMinionOwnerKey, PersistentDataType.STRING);
        if (ownerId == null) {
            return null;
        }
        try {
            Entity entity = Bukkit.getEntity(UUID.fromString(ownerId));
            return entity instanceof LivingEntity living ? living : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private List<LivingEntity> ownedMinions(LivingEntity owner) {
        List<LivingEntity> minions = new ArrayList<>();
        String ownerId = owner.getUniqueId().toString();
        for (LivingEntity candidate : owner.getWorld().getLivingEntities()) {
            if (!candidate.isValid() || candidate.isDead()) {
                continue;
            }
            String stored = candidate.getPersistentDataContainer().get(mobMinionOwnerKey, PersistentDataType.STRING);
            if (ownerId.equals(stored)) {
                minions.add(candidate);
            }
        }
        return minions;
    }

    private boolean isHuntActive(LivingEntity mob) {
        Byte stored = mob.getPersistentDataContainer().get(mobHuntActiveKey, PersistentDataType.BYTE);
        return stored != null && stored == (byte) 1;
    }

    private void setHuntActive(LivingEntity mob, boolean active) {
        if (active) {
            mob.getPersistentDataContainer().set(mobHuntActiveKey, PersistentDataType.BYTE, (byte) 1);
        } else {
            mob.getPersistentDataContainer().remove(mobHuntActiveKey);
        }
    }

    private boolean isVillageCorrupted(LivingEntity mob) {
        Byte stored = mob.getPersistentDataContainer().get(mobVillageCorruptedKey, PersistentDataType.BYTE);
        return stored != null && stored == (byte) 1;
    }

    private void setVillageCorrupted(LivingEntity mob, boolean active) {
        if (active) {
            mob.getPersistentDataContainer().set(mobVillageCorruptedKey, PersistentDataType.BYTE, (byte) 1);
        } else {
            mob.getPersistentDataContainer().remove(mobVillageCorruptedKey);
        }
    }

    private boolean isWaveAnnounced(LivingEntity mob) {
        Byte stored = mob.getPersistentDataContainer().get(mobWaveAnnouncedKey, PersistentDataType.BYTE);
        return stored != null && stored == (byte) 1;
    }

    private void setWaveAnnounced(LivingEntity mob, boolean active) {
        if (active) {
            mob.getPersistentDataContainer().set(mobWaveAnnouncedKey, PersistentDataType.BYTE, (byte) 1);
        } else {
            mob.getPersistentDataContainer().remove(mobWaveAnnouncedKey);
        }
    }

    private void announceEliteWave(LivingEntity mob) {
        Component message = Component.text("[Oleada] ", NamedTextColor.DARK_PURPLE)
                .append(Component.text(displayEntityType(mob.getType()), NamedTextColor.WHITE))
                .append(Component.text(" Elite ha creado una Oleada y buscara arrasar con el mundo. ", NamedTextColor.LIGHT_PURPLE))
                .append(Component.text("Preparate!", NamedTextColor.RED));
        Bukkit.broadcast(message);
        broadcastInsaneDifficultyTitle();
    }

    private void announceMinibossAscension(LivingEntity mob) {
        Component message = Component.text("[Ascension] ", NamedTextColor.GOLD)
                .append(Component.text(displayEntityType(mob.getType()), NamedTextColor.WHITE))
                .append(Component.text(" MiniBoss ha ascendido a Lord de su reino, ahora nada lo detendra!", NamedTextColor.RED));
        Bukkit.broadcast(message);
        broadcastInsaneDifficultyTitle();
    }

    private void broadcastInsaneDifficultyTitle() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(net.kyori.adventure.title.Title.title(
                    Component.text("Dificultad aumentada", NamedTextColor.RED),
                    Component.text("INSANO", NamedTextColor.DARK_RED)));
        }
    }

    private VillageCluster nearestVillageCluster(Location from, double maxRange) {
        List<AbstractVillager> villagers = new ArrayList<>();
        for (LivingEntity entity : from.getWorld().getLivingEntities()) {
            if (entity instanceof AbstractVillager villager
                    && villager.isValid()
                    && !villager.isDead()
                    && villager.getLocation().distanceSquared(from) <= maxRange * maxRange) {
                villagers.add(villager);
            }
        }
        if (villagers.isEmpty()) {
            return null;
        }
        AbstractVillager seed = null;
        double best = Double.MAX_VALUE;
        for (AbstractVillager villager : villagers) {
            double distance = villager.getLocation().distanceSquared(from);
            if (distance < best) {
                best = distance;
                seed = villager;
            }
        }
        if (seed == null) {
            return null;
        }
        List<AbstractVillager> cluster = new ArrayList<>();
        Vector sum = new Vector();
        for (AbstractVillager villager : villagers) {
            if (villager.getLocation().distanceSquared(seed.getLocation()) <= 24 * 24) {
                cluster.add(villager);
                sum.add(villager.getLocation().toVector());
            }
        }
        if (cluster.isEmpty()) {
            return null;
        }
        Location center = seed.getLocation().clone();
        center.setX(sum.getX() / cluster.size());
        center.setY(sum.getY() / cluster.size());
        center.setZ(sum.getZ() / cluster.size());
        return new VillageCluster(center, cluster);
    }

    private void corruptVillage(LivingEntity mob, VillageCluster village) {
        setVillageCorrupted(mob, true);
        for (AbstractVillager villager : village.villagers()) {
            if (villager.isValid() && !villager.isDead()) {
                villager.remove();
            }
        }
        mob.teleport(village.center());
        showSkillCast(mob, Component.text("La aldea ha sido corrompida", NamedTextColor.DARK_RED));
        for (int i = 0; i < 20; i++) {
            Location spawn = village.center().clone().add(
                    ThreadLocalRandom.current().nextDouble(-6.0, 6.0),
                    0.0,
                    ThreadLocalRandom.current().nextDouble(-6.0, 6.0));
            Zombie zombie = mob.getWorld().spawn(spawn, Zombie.class);
            MobRarity rarity = ThreadLocalRandom.current().nextDouble() < 0.35 ? MobRarity.RARE : MobRarity.NORMAL;
            applyScaledMobProfile(zombie, Math.max(1, scaledMobLevel(mob) - 2), rarity,
                    rarity == MobRarity.RARE ? rollMobAffix(EntityType.ZOMBIE, rarity) : null,
                    null);
            threatManager.markManaged(zombie);
            Player target = nearestAlivePlayer(mob.getLocation(), 60.0);
            if (target != null) {
                zombie.setTarget(target);
            }
        }
        mob.getWorld().spawnParticle(Particle.SMOKE, village.center().add(0, 1.0, 0), 80, 4.0, 1.0, 4.0, 0.06);
        mob.getWorld().playSound(village.center(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 0.7f);
        announceMinibossAscension(mob);
    }

    private record VillageCluster(Location center, List<AbstractVillager> villagers) {
    }

    private void useMobTacticalRetreat(LivingEntity mob, Player target, MobRarity rarity) {
        double distanceSquared = mob.getLocation().distanceSquared(target.getLocation());
        if (distanceSquared > 8 * 8 || distanceSquared < 2.5 * 2.5) {
            return;
        }
        Vector away = mob.getLocation().toVector().subtract(target.getLocation().toVector()).normalize()
                .multiply(getConfig().getDouble("mob-scaling.skills.tactical-retreat.velocity", 0.9))
                .setY(0.22);
        showSkillCast(mob, Component.text("Retirada Tactica", NamedTextColor.WHITE));
        mob.setVelocity(away);
        if (mob instanceof Monster monster) {
            monster.setTarget(target);
        }
        double damage = getConfig().getDouble("mob-scaling.skills.tactical-retreat.damage", 2.0);
        dealMobSkillDamage(mob, target, damage);
        target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 10, 0.35, 0.45, 0.35, 0.03);
        mob.getWorld().spawnParticle(Particle.SMALL_GUST, mob.getLocation().add(0, 0.8, 0), 12, 0.3, 0.3, 0.3, 0.02);
        spawnMobSkillImpactFlash(mob, Color.WHITE, 1.8);
        mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_SKELETON_SHOOT, 0.9f, 1.1f);
        setScaledMobSkillCooldown(mob, rarity, getConfig().getInt("mob-scaling.skills.tactical-retreat.cooldown-seconds", 9));
    }

    private void useMobTerritorialSlam(LivingEntity mob, Player target, MobRarity rarity) {
        if (mob.getLocation().distanceSquared(target.getLocation()) > 5 * 5) {
            return;
        }
        showSkillCast(mob, Component.text("Golpe Territorial", NamedTextColor.RED));
        double radius = getConfig().getDouble("mob-scaling.skills.territorial-slam.radius", 4.5);
        double damage = getConfig().getDouble("mob-scaling.skills.territorial-slam.damage", 4.0);
        int slowTicks = getConfig().getInt("mob-scaling.skills.territorial-slam.slow-ticks", 50);
        for (Entity entity : mob.getNearbyEntities(radius, 2.5, radius)) {
            if (entity instanceof LivingEntity living && isMobSkillTarget(mob, living)) {
                dealMobSkillDamage(mob, living, damage);
                applySlow(living, slowTicks);
                Vector push = living.getLocation().toVector().subtract(mob.getLocation().toVector()).normalize().multiply(0.6).setY(0.18);
                living.setVelocity(push);
            }
        }
        mob.getWorld().spawnParticle(Particle.BLOCK, mob.getLocation().add(0, 0.1, 0), 24, radius / 2, 0.1, radius / 2,
                Bukkit.createBlockData(Material.COARSE_DIRT));
        mob.getWorld().spawnParticle(Particle.CLOUD, mob.getLocation().add(0, 0.2, 0), 24, radius / 2, 0.15, radius / 2, 0.04);
        spawnMobSkillImpactFlash(mob, Color.RED, radius);
        mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 0.75f);
        setScaledMobSkillCooldown(mob, rarity, getConfig().getInt("mob-scaling.skills.territorial-slam.cooldown-seconds", 13));
    }

    private void setScaledMobSkillCooldown(LivingEntity mob, MobRarity rarity, int baseSeconds) {
        double multiplier = getConfig().getDouble("mob-scaling.rarities." + rarity.id() + ".skill-cooldown-multiplier", 1.0);
        if (mob.hasMetadata("MythicMob")) {
            ConfigurationSection section = mythicArchetypeSection(mob);
            multiplier *= section == null ? 1.0 : section.getDouble("cast-cooldown-multiplier", 1.0);
            if (isMythicEnraged(mob)) {
                ConfigurationSection enrage = mythicEnrageSection(mob);
                multiplier *= enrage == null ? 1.0 : enrage.getDouble("cooldown-multiplier", 0.9);
            }
            if (isMythicLastStand(mob)) {
                ConfigurationSection lastStand = mythicLastStandSection(mob);
                multiplier *= lastStand == null ? 1.0 : lastStand.getDouble("cooldown-multiplier", 0.95);
            }
        }
        long nextReady = System.currentTimeMillis() + Math.max(1000L, Math.round(baseSeconds * multiplier * 1000.0));
        mob.getPersistentDataContainer().set(mobSkillCooldownKey, PersistentDataType.LONG, nextReady);
    }

    private boolean isMobSkillTarget(LivingEntity source, LivingEntity target) {
        return target != source && !(target instanceof ArmorStand) && !target.isDead() && target.isValid();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBrew(BrewEvent event) {
        int xp = getConfig().getInt("profession-xp.alquimista.brew", 18);
        for (Player player : event.getBlock().getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(event.getBlock().getLocation()) <= 8 * 8) {
                grantProfessionExperience(player, "alquimista", xp);
                break;
            }
        }
    }

    private int miningExperience(Material material) {
        String key = switch (material) {
            case STONE, DEEPSLATE, TUFF, GRANITE, DIORITE, ANDESITE, CALCITE,
                    DRIPSTONE_BLOCK, BLACKSTONE, BASALT, SMOOTH_BASALT,
                    NETHERRACK, END_STONE -> "stone";
            case COAL_ORE, DEEPSLATE_COAL_ORE -> "coal";
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> "copper";
            case IRON_ORE, DEEPSLATE_IRON_ORE -> "iron";
            case GOLD_ORE, DEEPSLATE_GOLD_ORE, NETHER_GOLD_ORE -> "gold";
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> "redstone";
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> "lapis";
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> "diamond";
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> "emerald";
            default -> null;
        };
        return key == null ? 0 : getConfig().getInt("profession-xp.minero." + key, 0);
    }

    private boolean isSmithEquipment(Material material) {
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

    private int smithingExperience(Material material) {
        MaterialCost cost = craftingMaterialCost(material);
        if (cost == null) {
            return 0;
        }
        int total = cost.primaryAmount() * getConfig().getInt("profession-xp.herrero." + cost.primaryTier(), 0);
        if (cost.secondaryTier() != null) {
            total += cost.secondaryAmount() * getConfig().getInt("profession-xp.herrero." + cost.secondaryTier(), 0);
        }
        return total;
    }

    private int grantClassExperience(Player player, LivingEntity mob, MobRarity rarity) {
        PlayerProfile profile = profiles.get(player.getUniqueId());
        if (profile.baseClass() == null || profile.level() >= classMaxLevel()) {
            return 0;
        }
        int amount = classExperienceForKill(profile, mob, rarity);
        if (amount <= 0) {
            return 0;
        }
        int previousLevel = profile.level();
        int gained = profile.addClassExperience(amount, classXpBase(), classMaxLevel());
        saveProfiles();
        refreshPlayerBaseStats(player);
        updateTab(player);
        if (gained > 0) {
            player.showTitle(net.kyori.adventure.title.Title.title(
                    Component.text("Nivel de clase " + profile.level(), NamedTextColor.GOLD),
                    Component.text(displayClass(profile.baseClass()), classColor(profile.baseClass()))));
            player.sendMessage(Component.text("Has subido de nivel de clase de " + previousLevel + " a " + profile.level() + ".", NamedTextColor.GREEN));
            if (rarity != MobRarity.NORMAL) {
                Component message = Component.text("[Progreso] ", NamedTextColor.GOLD)
                        .append(Component.text(player.getName(), NamedTextColor.WHITE))
                        .append(Component.text(" ha subido al nivel ", NamedTextColor.GRAY))
                        .append(Component.text(String.valueOf(profile.level()), NamedTextColor.YELLOW))
                        .append(Component.text(" tras derrotar a un ", NamedTextColor.GRAY))
                        .append(Component.text(rarity.displayName() + " ", rarity.color()))
                        .append(Component.text(displayEntityType(mob.getType()), NamedTextColor.WHITE))
                        .append(Component.text(".", NamedTextColor.GRAY));
                Bukkit.broadcast(message);
            }
        }
        return amount;
    }

    private int classExperienceForKill(PlayerProfile profile, LivingEntity mob, MobRarity rarity) {
        int base = getConfig().getInt("class-xp.kill-base", 8);
        int rarityBonus = getConfig().getInt("class-xp.rarity-bonus." + rarity.id(), switch (rarity) {
            case NORMAL -> 0;
            case RARE -> 4;
            case ELITE -> 10;
            case MINIBOSS -> 22;
        });
        int mobLevel = Math.max(1, scaledMobLevel(mob));
        int levelDelta = mobLevel - profile.level();
        double multiplier;
        if (levelDelta >= 0) {
            multiplier = 1.0 + levelDelta * getConfig().getDouble("class-xp.higher-level-multiplier-per-level", 0.12);
        } else {
            multiplier = Math.max(
                    getConfig().getDouble("class-xp.lower-level-min-multiplier", 0.45),
                    1.0 + levelDelta * getConfig().getDouble("class-xp.lower-level-penalty-per-level", 0.06));
        }
        return Math.max(1, (int) Math.round((base + rarityBonus) * multiplier));
    }

    private int classXpBase() {
        return getConfig().getInt("class-xp.base-required-per-level", 120);
    }

    private double grantMobCrowns(Player killer, LivingEntity mob, MobRarity rarity) {
        double base = getConfig().getDouble("mob-scaling.crowns.base", 2.0);
        double multiplier = getConfig().getDouble("mob-scaling.crowns.rarity-multipliers." + rarity.id(), switch (rarity) {
            case NORMAL -> 1.0;
            case RARE -> 1.5;
            case ELITE -> 3.0;
            case MINIBOSS -> 6.0;
        });
        int level = Math.max(1, scaledMobLevel(mob));
        double levelFactor = getConfig().getDouble("mob-scaling.crowns.level-factor", 0.35);
        double amount = Math.max(1.0, base * multiplier + level * levelFactor);
        depositCrowns(killer, amount);
        return amount;
    }

    private void sendKillRewardsActionBar(Player player, int classXp, long crowns) {
        PlayerProfile profile = profiles.get(player.getUniqueId());
        player.sendActionBar(Component.text("+" + classXp + " XP clase", NamedTextColor.AQUA)
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(Component.text(profile.classExperience() + "/" + profile.requiredClassExperience(classXpBase()), NamedTextColor.GRAY))
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(Component.text("+" + crowns + " coronas", NamedTextColor.GOLD)));
    }

    private ItemStack createTestLoot(Material material, MobRarity rarity, int mobLevel, MobAffix affix, int playerLevel) {
        ItemStack item = new ItemStack(material);
        return rarity == MobRarity.NORMAL
                ? plainMobDrop(item, playerLevel)
                : statMobDrop(item, rarity, playerLevel, mobLevel, affix);
    }

    private void enhanceCraftedSmithItem(Player player, CraftItemEvent event) {
        ItemStack crafted = event.getCurrentItem();
        if (crafted == null) {
            crafted = event.getRecipe().getResult().clone();
        } else {
            crafted = crafted.clone();
        }
        ItemMeta meta = crafted.getItemMeta();
        applyBaseCombatAttributes(crafted.getType(), meta);
        PlayerProfile profile = profiles.get(player.getUniqueId());
        int classLevel = profile.level();
        int smithLevel = profile.professionLevel("herrero");
        double chance = Math.min(0.9,
                getConfig().getDouble("profession-xp.herrero.bonus-item-base-chance", 0.12)
                        + classLevel * getConfig().getDouble("profession-xp.herrero.bonus-item-chance-per-class-level", 0.02)
                        + smithLevel * getConfig().getDouble("profession-xp.herrero.bonus-item-chance-per-smith-level", 0.01));
        if (ThreadLocalRandom.current().nextDouble() < chance) {
            applySmithBonus(meta, crafted.getType(), classLevel, smithLevel);
        }
        crafted.setItemMeta(meta);
        event.setCurrentItem(crafted);
    }

    private ItemStack createSimulatedSmithItem(Material material, int smithLevel, int classLevel) {
        ItemStack crafted = new ItemStack(material);
        ItemMeta meta = crafted.getItemMeta();
        applyBaseCombatAttributes(material, meta);
        double chance = Math.min(0.95,
                getConfig().getDouble("profession-xp.herrero.bonus-item-base-chance", 0.12)
                        + classLevel * getConfig().getDouble("profession-xp.herrero.bonus-item-chance-per-class-level", 0.02)
                        + smithLevel * getConfig().getDouble("profession-xp.herrero.bonus-item-chance-per-smith-level", 0.01));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Simulacion de forja", NamedTextColor.GOLD));
        lore.add(Component.text("Herrero " + smithLevel + " | Clase " + classLevel, NamedTextColor.GRAY));
        meta.lore(lore);
        if (ThreadLocalRandom.current().nextDouble() < chance) {
            applySmithBonus(meta, material, classLevel, smithLevel);
        } else {
            lore.add(Component.text("Sin mejora adicional.", NamedTextColor.DARK_GRAY));
            meta.lore(lore);
        }
        crafted.setItemMeta(meta);
        return crafted;
    }

    private void applySmithBonus(ItemMeta meta, Material material, int classLevel) {
        applySmithBonus(meta, material, classLevel, 1);
    }

    private void applySmithBonus(ItemMeta meta, Material material, int classLevel, int smithLevel) {
        int tier = Math.max(1, Math.min(6, 1 + ((classLevel + smithLevel) / 6)));
        List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        EquipmentSlotGroup slotGroup = equipmentSlotGroupForMaterial(material);
        if (isArmorPiece(material) || material == Material.SHIELD) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                int amount = tier + 2;
                lore.add(Component.text("Forja: +" + amount + " Vida", NamedTextColor.GREEN));
                upsertAttributeModifier(meta, Attribute.MAX_HEALTH, new AttributeModifier(
                        NamespacedKey.minecraft("servidro_crafted_health_" + material.name().toLowerCase(Locale.ROOT)),
                        amount,
                        AttributeModifier.Operation.ADD_NUMBER,
                        slotGroup));
            } else {
                int amount = tier + 1;
                lore.add(Component.text("Forja: +" + amount + " Armadura", NamedTextColor.GREEN));
                upsertAttributeModifier(meta, Attribute.ARMOR, new AttributeModifier(
                        NamespacedKey.minecraft("servidro_crafted_armor_" + material.name().toLowerCase(Locale.ROOT)),
                        amount,
                        AttributeModifier.Operation.ADD_NUMBER,
                        slotGroup));
            }
        } else {
            if (ThreadLocalRandom.current().nextBoolean()) {
                int amount = tier;
                lore.add(Component.text("Forja: +" + amount + " Dano", NamedTextColor.GREEN));
                upsertAttributeModifier(meta, Attribute.ATTACK_DAMAGE, new AttributeModifier(
                        NamespacedKey.minecraft("servidro_crafted_damage_" + material.name().toLowerCase(Locale.ROOT)),
                        amount,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.MAINHAND));
            } else {
                double amount = 0.15 * tier;
                lore.add(Component.text("Forja: +" + String.format(Locale.US, "%.1f", amount) + " Vel. ataque", NamedTextColor.GREEN));
                upsertAttributeModifier(meta, Attribute.ATTACK_SPEED, new AttributeModifier(
                        NamespacedKey.minecraft("servidro_crafted_speed_" + material.name().toLowerCase(Locale.ROOT)),
                        amount,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.MAINHAND));
            }
        }
        meta.lore(lore);
    }

    private MaterialCost craftingMaterialCost(Material material) {
        String name = material.name();
        if (name.endsWith("_SWORD")) {
            return materialCostForWeaponTool(name, 2, 1);
        }
        if (name.endsWith("_AXE") || name.endsWith("_PICKAXE")) {
            return materialCostForWeaponTool(name, 3, 2);
        }
        if (name.endsWith("_HOE")) {
            return materialCostForWeaponTool(name, 2, 2);
        }
        if (name.endsWith("_SHOVEL")) {
            return materialCostForWeaponTool(name, 1, 2);
        }
        if (name.endsWith("_HELMET")) {
            return materialCostForArmor(name, 5);
        }
        if (name.endsWith("_CHESTPLATE")) {
            return materialCostForArmor(name, 8);
        }
        if (name.endsWith("_LEGGINGS")) {
            return materialCostForArmor(name, 7);
        }
        if (name.endsWith("_BOOTS")) {
            return materialCostForArmor(name, 4);
        }
        if (material == Material.SHIELD) {
            return new MaterialCost("iron", 1, "wood", 6);
        }
        return null;
    }

    private MaterialCost materialCostForWeaponTool(String name, int headAmount, int handleAmount) {
        String primaryTier = smithPrimaryTier(name);
        return new MaterialCost(primaryTier, headAmount, "wood", handleAmount);
    }

    private MaterialCost materialCostForArmor(String name, int amount) {
        return new MaterialCost(smithPrimaryTier(name), amount, null, 0);
    }

    private String smithPrimaryTier(String name) {
        if (name.startsWith("NETHERITE_")) {
            return "netherite";
        }
        if (name.startsWith("DIAMOND_")) {
            return "diamond";
        }
        if (name.startsWith("GOLDEN_")) {
            return "gold";
        }
        if (name.startsWith("IRON_") || name.startsWith("CHAINMAIL_")) {
            return "iron";
        }
        if (name.startsWith("STONE_")) {
            return "stone";
        }
        return "wood";
    }

    private record MaterialCost(String primaryTier, int primaryAmount, String secondaryTier, int secondaryAmount) {
    }

    private void refreshPlayerBaseStats(Player player) {
        PlayerProfile profile = profiles.get(player.getUniqueId());
        int bonusLevels = Math.max(0, profile.level() - 1);
        applyOrReplaceLevelModifier(player, Attribute.MAX_HEALTH,
                getConfig().getDouble("class-scaling.max-health-per-level", 1.0) * bonusLevels);
        applyOrReplaceLevelModifier(player, Attribute.ATTACK_DAMAGE,
                getConfig().getDouble("class-scaling.attack-damage-per-level", 0.08) * bonusLevels);
        applyOrReplaceLevelModifier(player, Attribute.MOVEMENT_SPEED,
                getConfig().getDouble("class-scaling.movement-speed-per-level", 0.002) * bonusLevels);
        double maxHealth = attributeValue(player, Attribute.MAX_HEALTH);
        if (player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }
    }

    private void applyOrReplaceLevelModifier(Player player, Attribute attribute, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        NamespacedKey key = NamespacedKey.minecraft("servidro_level_" + attribute.key().value());
        instance.getModifiers().stream()
                .filter(modifier -> key.equals(modifier.getKey()))
                .toList()
                .forEach(instance::removeModifier);
        if (amount == 0.0) {
            return;
        }
        instance.addModifier(new AttributeModifier(
                key,
                amount,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.ANY));
    }

    private String blockKey(Block block) {
        return block.getWorld().getUID() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
    }

    private void grantProfessionExperience(Player player, String profession, int amount) {
        if (amount <= 0) {
            return;
        }
        PlayerProfile profile = profiles.get(player.getUniqueId());
        int previousLevel = profile.professionLevel(profession);
        int gained = profile.addProfessionExperience(profession, amount, professionXpBase(), professionMaxLevel());
        saveProfiles();
        int currentLevel = profile.professionLevel(profession);
        player.sendActionBar(Component.text("+" + amount + " XP " + profession
                + " | " + profile.professionExperience(profession)
                + "/" + profile.requiredExperience(profession, professionXpBase()), NamedTextColor.GREEN));
        if (gained > 0) {
            player.showTitle(net.kyori.adventure.title.Title.title(
                    Component.text("Nivel de profesion " + currentLevel, NamedTextColor.GOLD),
                    Component.text(capitalize(profession), NamedTextColor.YELLOW)));
            sendProfessionUnlocks(player, profession, previousLevel, currentLevel);
        }
    }

    private int professionXpBase() {
        return getConfig().getInt("profession-xp.base-required-per-level", 180);
    }

    private int classMaxLevel() {
        return getConfig().getInt("progression-caps.class-level", 30);
    }

    private int professionMaxLevel() {
        return getConfig().getInt("progression-caps.profession-level", 20);
    }

    private void progressDailyMission(Player player, DailyMissionStore.Activity activity, Material material, int amount) {
        DailyMissionStore.Completion completion =
                dailyMissions.progress(player, profiles.get(player.getUniqueId()), activity, material, amount);
        saveDailyMissions();
        if (completion == null) {
            return;
        }
        if (economy != null) {
            economy.depositPlayer(player, completion.reward());
        }
        player.showTitle(net.kyori.adventure.title.Title.title(
                Component.text("Mision diaria completada", NamedTextColor.GOLD),
                Component.text("+" + completion.reward() + " coronas", NamedTextColor.GREEN)));
        player.sendMessage(Component.text(completion.description(), NamedTextColor.YELLOW));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    private void sendProfessionUnlocks(Player player, String profession, int previousLevel, int currentLevel) {
        for (int level = previousLevel + 1; level <= currentLevel; level++) {
            String unlock = professionUnlock(profession, level);
            if (unlock != null) {
                player.sendMessage(Component.text("Desbloqueado: " + unlock, NamedTextColor.AQUA));
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
            }
        }
    }

    private String professionUnlock(String profession, int level) {
        return switch (profession + ":" + level) {
            case "herrero:5" -> "fabricar herramientas, armas, escudos y armaduras de hierro";
            case "herrero:10" -> "fabricar herramientas, armas y armaduras de oro";
            case "herrero:15" -> "fabricar herramientas, armas y armaduras de diamante";
            case "minero:5" -> "colocar bloques minerales de hierro";
            case "minero:10" -> "colocar bloques minerales de oro";
            case "minero:15" -> "colocar bloques minerales de diamante";
            case "alquimista:8" -> "fabricar consumibles avanzados";
            case "agricultor:8" -> "trabajar cultivos avanzados";
            case "explorador:8" -> "usar utilidades de expedicion";
            default -> null;
        };
    }

    private String capitalize(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    private boolean deny(Player player, Material material, ProgressionGate.Action action) {
        ProgressionGate.Denial denial = progressionGate.check(profiles.get(player.getUniqueId()), material, action);
        if (denial == null) {
            return false;
        }
        player.sendActionBar(Component.text("Requiere " + denial.requirement()
                + " nivel " + denial.requiredLevel()
                + " | Tienes " + denial.currentLevel(), NamedTextColor.RED));
        return true;
    }

    private boolean isArmorSlot(InventoryType.SlotType slotType) {
        return slotType == InventoryType.SlotType.ARMOR;
    }

    private void useBaseClassAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking() || event.getItem() == null) {
            return;
        }
        String baseClass = profiles.get(player.getUniqueId()).baseClass();
        if (baseClass == null) {
            return;
        }
        Material item = event.getItem().getType();
        if ("guerrero".equals(baseClass) && isWarriorWeapon(item)) {
            event.setCancelled(true);
            useWarriorOverwhelmingStrike(player);
        } else if ("explorador".equals(baseClass) && item == Material.BOW) {
            event.setCancelled(true);
            useExplorerRoll(player);
        } else if ("mago".equals(baseClass) && item == Material.STICK) {
            event.setCancelled(true);
            useMageBarrier(player);
        } else if ("clerigo".equals(baseClass) && item == Material.BLAZE_ROD) {
            event.setCancelled(true);
            useClericBlessing(player);
        }
    }

    private void useWarriorDash(Player player) {
        int range = getConfig().getInt("base-class-abilities.warrior-dash.max-range", 7);
        if (!(player.getTargetEntity(range) instanceof LivingEntity target) || !isAbilityTarget(player, target)) {
            player.sendActionBar(Component.text("Dash: mira a un enemigo a menos de " + range + " bloques.",
                    NamedTextColor.YELLOW));
            return;
        }
        int cooldown = getConfig().getInt("base-class-abilities.warrior-dash.cooldown-seconds", 8);
        if (!startCooldown(player, "warrior-dash", cooldown)) {
            return;
        }
        showSkillCast(player, Component.text("Dash", NamedTextColor.GOLD));
        double velocity = getConfig().getDouble("base-class-abilities.warrior-dash.velocity", 1.45);
        Vector direction = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
        player.setVelocity(direction.multiply(velocity).setY(Math.max(0.18, direction.getY())));
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 0.5, 0), 18, 0.5, 0.35, 0.5, 0.03);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_SLIDE, 0.9f, 0.8f);
    }

    private void useWarriorDesolatingLeap(Player player) {
        int cooldown = getConfig().getInt("base-class-abilities.warrior-desolating-leap.cooldown-seconds", 6);
        if (!startCooldown(player, "warrior-desolating-leap", cooldown)) {
            return;
        }
        showSkillCast(player, Component.text("Salto Desolador", NamedTextColor.GOLD));
        double radius = getConfig().getDouble("base-class-abilities.warrior-desolating-leap.radius", 5.0);
        int slowTicks = getConfig().getInt("base-class-abilities.warrior-desolating-leap.slow-ticks", 70);
        int affected = 0;
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity mob && isAbilityTarget(player, mob)) {
                applySlow(mob, slowTicks);
                affected++;
            }
        }
        if ("guardian".equals(profiles.get(player.getUniqueId()).specialization())) {
            double bonus = getConfig().getDouble("base-class-abilities.warrior-desolating-leap.guardian-bonus-threat", 100.0);
            List<LivingEntity> taunted = threatManager.tauntNearby(player, radius, bonus);
            taunted.forEach(this::highlightTauntedMob);
        }
        player.getWorld().spawnParticle(Particle.GUST, player.getLocation().add(0, 0.25, 0), 18, radius / 2, 0.2, radius / 2, 0.04);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 0.25, 0), 24, radius / 2, 0.25, radius / 2, 0.03);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 0.9f, 0.7f);
        player.sendActionBar(Component.text("Salto Desolador: " + affected + " enemigo(s) ralentizados.",
                NamedTextColor.GOLD));
    }

    private void useWarriorOverwhelmingStrike(Player player) {
        useWarriorOverwhelmingStrike(player, null);
    }

    private void useWarriorOverwhelmingStrike(Player player, LivingEntity preferredTarget) {
        PlayerProfile profile = profiles.get(player.getUniqueId());
        if ("berserker".equals(profile.specialization())) {
            useBerserkerWhirlwind(player);
            return;
        }
        int range = getConfig().getInt("base-class-abilities.warrior-overwhelming-strike.range", 5);
        LivingEntity target = preferredTarget != null && isAbilityTarget(player, preferredTarget)
                ? preferredTarget
                : firstLivingTargetInSight(player, range, 1.1);
        if (target == null || !isAbilityTarget(player, target)) {
            player.sendMessage(Component.text("Mira a un enemigo cercano.", NamedTextColor.YELLOW));
            return;
        }
        int cooldown = getConfig().getInt("base-class-abilities.warrior-overwhelming-strike.cooldown-seconds", 15);
        if (!startCooldown(player, "warrior-overwhelming-strike", cooldown)) {
            return;
        }
        showSkillCast(player, Component.text("Golpe Abrumador", NamedTextColor.RED));
        boolean guardian = "guardian".equals(profile.specialization());
        double multiplier = getConfig().getDouble("base-class-abilities.warrior-overwhelming-strike."
                + (guardian ? "guardian-damage-multiplier" : "warrior-damage-multiplier"), guardian ? 1.5 : 1.7);
        double damage = weaponDamage(player.getInventory().getItemInMainHand()) * multiplier;
        double dealt = dealAbilityDamage(player, target, damage);
        applyOverwhelmingSlow(target);
        if (guardian) {
            healPlayer(player, dealt * getConfig().getDouble(
                    "base-class-abilities.warrior-overwhelming-strike.guardian-heal-ratio", 0.2));
        }
        target.setVelocity(target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(0.6));
        playHeavyStrikeEffect(player, target);
    }

    private LivingEntity firstLivingTargetInSight(Player player, int range, double hitboxRadius) {
        RayTraceResult trace = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                range,
                hitboxRadius,
                entity -> entity instanceof LivingEntity living && isAbilityTarget(player, living));
        if (trace == null || !(trace.getHitEntity() instanceof LivingEntity living)) {
            return null;
        }
        return living;
    }

    private void applyOverwhelmingSlow(LivingEntity target) {
        int ticks = getConfig().getInt("base-class-abilities.warrior-overwhelming-strike.slow-ticks", 70);
        applySlow(target, ticks);
    }

    private void applySlow(LivingEntity target, int ticks) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticks, 0, false, true, true));
        showFloatingText(target, Component.text("Ralentizado", NamedTextColor.AQUA), 24L);
        long highlightedUntil = System.currentTimeMillis() + ticks * 50L;
        slowHighlights.put(target.getUniqueId(), highlightedUntil);
        new BukkitRunnable() {
            @Override
            public void run() {
                Long currentUntil = slowHighlights.get(target.getUniqueId());
                if (!target.isValid() || currentUntil == null || currentUntil <= System.currentTimeMillis()) {
                    slowHighlights.remove(target.getUniqueId());
                    cancel();
                    return;
                }
                target.getWorld().spawnParticle(
                        Particle.DUST,
                        target.getLocation().add(0, 1.0, 0),
                        7,
                        0.45,
                        0.65,
                        0.45,
                        0.0,
                        new Particle.DustOptions(Color.AQUA, 1.0f));
            }
        }.runTaskTimer(this, 0L, 5L);
    }

    private double dealAbilityDamage(Player attacker, LivingEntity target, double amount) {
        double before = target.getHealth();
        target.damage(amount, attacker);
        return Math.max(0.0, before - target.getHealth());
    }

    private double dealMobSkillDamage(LivingEntity attacker, LivingEntity target, double amount) {
        double before = target.getHealth();
        target.damage(amount, attacker);
        return Math.max(0.0, before - target.getHealth());
    }

    private void showFloatingText(LivingEntity target, Component text, long durationTicks) {
        Location location = floatingTextLocation(target, true);
        ArmorStand indicator = target.getWorld().spawn(location, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setMarker(true);
            stand.setGravity(false);
            stand.setSmall(true);
            stand.customName(text);
            stand.setCustomNameVisible(true);
        });
        indicator.setVelocity(new Vector(ThreadLocalRandom.current().nextDouble(-0.01, 0.01), 0.04,
                ThreadLocalRandom.current().nextDouble(-0.01, 0.01)));
        Bukkit.getScheduler().runTaskLater(this, indicator::remove, durationTicks);
    }

    private void showSkillCast(LivingEntity caster, Component text) {
        Location location = floatingTextLocation(caster, false);
        ArmorStand indicator = caster.getWorld().spawn(location, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setMarker(true);
            stand.setGravity(false);
            stand.setSmall(true);
            stand.customName(text);
            stand.setCustomNameVisible(true);
        });
        indicator.setVelocity(new Vector(ThreadLocalRandom.current().nextDouble(-0.008, 0.008), 0.035,
                ThreadLocalRandom.current().nextDouble(-0.008, 0.008)));
        Bukkit.getScheduler().runTaskLater(this, indicator::remove, 24L);
    }

    private Location floatingTextLocation(LivingEntity entity, boolean wideOffset) {
        Location base = entity.getLocation().add(0, entity.getHeight() * (wideOffset ? 0.72 : 0.9), 0);
        Vector facing = entity.getLocation().getDirection().setY(0);
        if (facing.lengthSquared() < 0.0001) {
            facing = new Vector(0, 0, 1);
        } else {
            facing.normalize();
        }
        Vector sideways = new Vector(-facing.getZ(), 0, facing.getX()).normalize();
        double side = ThreadLocalRandom.current().nextBoolean() ? 1.0 : -1.0;
        double sideDistance = wideOffset ? ThreadLocalRandom.current().nextDouble(0.35, 0.65)
                : ThreadLocalRandom.current().nextDouble(0.25, 0.5);
        double forwardDrift = ThreadLocalRandom.current().nextDouble(-0.18, 0.18);
        double yOffset = wideOffset ? ThreadLocalRandom.current().nextDouble(-0.08, 0.12)
                : ThreadLocalRandom.current().nextDouble(0.0, 0.16);
        return base.add(sideways.multiply(side * sideDistance)).add(facing.multiply(forwardDrift)).add(0, yOffset, 0);
    }

    private void healPlayer(Player player, double amount) {
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHealth, player.getHealth() + amount));
    }

    private void playHeavyStrikeEffect(Player player, LivingEntity target) {
        Location start = player.getEyeLocation().add(0, -0.35, 0);
        Vector path = target.getLocation().add(0, 1, 0).toVector().subtract(start.toVector());
        int steps = 8;
        for (int step = 1; step <= steps; step++) {
            Location particle = start.clone().add(path.clone().multiply((double) step / steps));
            player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, particle, 1, 0, 0, 0, 0);
        }
        Location impact = target.getLocation().add(0, 1, 0);
        target.getWorld().spawnParticle(Particle.CRIT, impact, 22, 0.55, 0.75, 0.55, 0.04);
        target.getWorld().spawnParticle(Particle.SMALL_GUST, impact, 8, 0.45, 0.45, 0.45, 0.02);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.1f, 0.65f);
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.45f, 1.45f);
    }

    private void useExplorerRoll(Player player) {
        int cooldown = getConfig().getInt("base-class-abilities.explorer-roll.cooldown-seconds", 6);
        if (!startCooldown(player, "explorer-roll", cooldown)) {
            return;
        }
        showSkillCast(player, Component.text("Rodar", NamedTextColor.GREEN));
        double velocity = getConfig().getDouble("base-class-abilities.explorer-roll.velocity", 1.1);
        Vector direction = player.getLocation().getDirection().setY(0).normalize().multiply(velocity).setY(0.25);
        player.setVelocity(direction);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 16, 0.6, 0.2, 0.6, 0.02);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_HORSE_STEP, 0.8f, 1.4f);
    }

    private void useMageBarrier(Player player) {
        int cooldown = getConfig().getInt("base-class-abilities.mage-barrier.cooldown-seconds", 16);
        if (!startCooldown(player, "mage-barrier", cooldown)) {
            return;
        }
        showSkillCast(player, Component.text("Barrera", NamedTextColor.LIGHT_PURPLE));
        int hearts = getConfig().getInt("base-class-abilities.mage-barrier.absorption-hearts", 4);
        int duration = getConfig().getInt("base-class-abilities.mage-barrier.duration-seconds", 8);
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, duration * 20, Math.max(0, hearts / 2 - 1), false, true, true));
        player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1, 0), 28, 0.8, 1.0, 0.8, 0.0);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.9f, 1.0f);
    }

    private void useClericBlessing(Player player) {
        int range = getConfig().getInt("base-class-abilities.cleric-blessing.range", 12);
        Player target = player.getTargetEntity(range) instanceof Player selected ? selected : player;
        int cooldown = getConfig().getInt("base-class-abilities.cleric-blessing.cooldown-seconds", 14);
        if (!startCooldown(player, "cleric-blessing", cooldown)) {
            return;
        }
        showSkillCast(player, Component.text("Bendicion", NamedTextColor.YELLOW));
        int duration = getConfig().getInt("base-class-abilities.cleric-blessing.duration-seconds", 10);
        target.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration * 20, 0, false, true, true));
        target.getWorld().spawnParticle(Particle.END_ROD, target.getLocation().add(0, 1, 0), 14, 0.6, 0.8, 0.6, 0.0);
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.6f, 1.4f);
    }

    private void useBerserkerWhirlwind(Player player) {
        int cooldown = getConfig().getInt("base-class-abilities.warrior-overwhelming-strike.cooldown-seconds", 15);
        if (!startCooldown(player, "warrior-overwhelming-strike", cooldown)) {
            return;
        }
        showSkillCast(player, Component.text("Torbellino", NamedTextColor.RED));
        double radius = getConfig().getDouble("specialization-abilities.berserker-whirlwind.radius", 4.0);
        double mainDamage = weaponDamage(player.getInventory().getItemInMainHand());
        double offhandDamage = weaponDamage(player.getInventory().getItemInOffHand());
        double damage = mainDamage * getConfig().getDouble("specialization-abilities.berserker-whirlwind.main-hand-multiplier", 1.7)
                + offhandDamage * getConfig().getDouble("specialization-abilities.berserker-whirlwind.offhand-multiplier", 0.7);
        int affected = 0;
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof org.bukkit.entity.LivingEntity mob && isAbilityTarget(player, mob)) {
                dealAbilityDamage(player, mob, damage);
                applyOverwhelmingSlow(mob);
                affected++;
            }
        }
        if (affected == 0) {
            clearCooldown(player, "warrior-overwhelming-strike");
            player.sendMessage(Component.text("No hay enemigos cerca.", NamedTextColor.YELLOW));
            return;
        }
        healPlayer(player, offhandDamage * getConfig().getDouble(
                "specialization-abilities.berserker-whirlwind.offhand-heal-ratio", 0.7));
        playWhirlwindEffect(player, radius);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.8f);
    }

    private boolean isWarriorWeapon(Material material) {
        String name = material.name();
        return name.endsWith("_SWORD") || name.endsWith("_AXE");
    }

    private boolean isAbilityTarget(Player caster, LivingEntity target) {
        return target != caster && !(target instanceof ArmorStand) && !target.isDead();
    }

    private double weaponDamage(ItemStack item) {
        return switch (item.getType()) {
            case WOODEN_SWORD, GOLDEN_SWORD -> 4.0;
            case STONE_SWORD -> 5.0;
            case IRON_SWORD -> 6.0;
            case DIAMOND_SWORD -> 7.0;
            case NETHERITE_SWORD -> 8.0;
            case WOODEN_AXE, GOLDEN_AXE -> 7.0;
            case STONE_AXE, IRON_AXE, DIAMOND_AXE -> 9.0;
            case NETHERITE_AXE -> 10.0;
            default -> 0.0;
        };
    }

    private void playWhirlwindEffect(Player player, double abilityRadius) {
        int animationTicks = getConfig().getInt("specialization-abilities.berserker-whirlwind.animation-ticks", 10);
        int pointsPerRing = getConfig().getInt("specialization-abilities.berserker-whirlwind.particles-per-ring", 12);
        double visualRadius = Math.min(abilityRadius,
                getConfig().getDouble("specialization-abilities.berserker-whirlwind.visual-radius", 3.0));
        new BukkitRunnable() {
            int tick;

            @Override
            public void run() {
                if (!player.isOnline() || tick >= animationTicks) {
                    cancel();
                    return;
                }
                Location center = player.getLocation().add(0, 0.35, 0);
                for (int ring = 0; ring < 3; ring++) {
                    double y = ring * 0.55;
                    double ringRadius = visualRadius * (0.45 + ring * 0.24);
                    double phase = tick * 0.65 + ring * 0.85;
                    for (int point = 0; point < pointsPerRing; point++) {
                        double angle = phase + (Math.PI * 2 * point / pointsPerRing);
                        Location particle = center.clone().add(
                                Math.cos(angle) * ringRadius,
                                y,
                                Math.sin(angle) * ringRadius);
                        player.getWorld().spawnParticle(Particle.SMALL_GUST, particle, 1, 0, 0, 0, 0);
                    }
                }
                player.getWorld().spawnParticle(Particle.SWEEP_ATTACK,
                        center.clone().add(0, 0.8, 0), 3, visualRadius / 2, 0.3, visualRadius / 2, 0.0);
                tick += 2;
            }
        }.runTaskTimer(this, 0L, 2L);
    }

    private void useHunterMark(Player player) {
        int range = getConfig().getInt("specialization-abilities.hunter-mark.range", 18);
        if (!(player.getTargetEntity(range) instanceof org.bukkit.entity.LivingEntity mob)
                || !isAbilityTarget(player, mob)) {
            player.sendMessage(Component.text("Mira a una entidad viva.", NamedTextColor.YELLOW));
            return;
        }
        int cooldown = getConfig().getInt("specialization-abilities.hunter-mark.cooldown-seconds", 14);
        if (!startCooldown(player, "hunter-mark", cooldown)) {
            return;
        }
        showSkillCast(player, Component.text("Marca", NamedTextColor.GREEN));
        int duration = getConfig().getInt("specialization-abilities.hunter-mark.duration-seconds", 10);
        hunterMarks.put(mob.getUniqueId(), System.currentTimeMillis() + duration * 1000L);
        mob.getWorld().spawnParticle(Particle.CRIT, mob.getLocation().add(0, 1, 0), 20, 0.6, 0.8, 0.6, 0.0);
        mob.getWorld().playSound(mob.getLocation(), Sound.ITEM_TRIDENT_HIT, 0.7f, 1.5f);
        player.sendMessage(Component.text("Objetivo marcado durante " + duration + " s.", NamedTextColor.GREEN));
    }

    private void useRogueVanish(Player player) {
        int cooldown = getConfig().getInt("specialization-abilities.rogue-vanish.cooldown-seconds", 16);
        if (!startCooldown(player, "rogue-vanish", cooldown)) {
            return;
        }
        showSkillCast(player, Component.text("Desvanecer", NamedTextColor.GRAY));
        double reduction = getConfig().getDouble("specialization-abilities.rogue-vanish.threat-reduction", 0.80);
        threatManager.reduceThreat(player, reduction);
        int duration = getConfig().getInt("specialization-abilities.rogue-vanish.invisibility-seconds", 3);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, duration * 20, 0, false, false, true));
        player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1, 0), 25, 0.8, 0.8, 0.8, 0.02);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.4f);
    }

    private void usePyromancerFireball(Player player) {
        int range = getConfig().getInt("specialization-abilities.pyromancer-fireball.range", 18);
        if (!(player.getTargetEntity(range) instanceof org.bukkit.entity.LivingEntity target)
                || !isAbilityTarget(player, target)) {
            player.sendMessage(Component.text("Mira a una entidad viva.", NamedTextColor.YELLOW));
            return;
        }
        int cooldown = getConfig().getInt("specialization-abilities.pyromancer-fireball.cooldown-seconds", 9);
        if (!startCooldown(player, "pyromancer-fireball", cooldown)) {
            return;
        }
        showSkillCast(player, Component.text("Bola de Fuego", NamedTextColor.GOLD));
        double radius = getConfig().getDouble("specialization-abilities.pyromancer-fireball.radius", 3.0);
        double damage = getConfig().getDouble("specialization-abilities.pyromancer-fireball.damage", 6.0);
        int fireTicks = getConfig().getInt("specialization-abilities.pyromancer-fireball.fire-seconds", 4) * 20;
        for (org.bukkit.entity.Entity entity : target.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof org.bukkit.entity.LivingEntity mob && isAbilityTarget(player, mob)) {
                dealAbilityDamage(player, mob, damage);
                mob.setFireTicks(fireTicks);
            }
        }
        dealAbilityDamage(player, target, damage);
        target.setFireTicks(fireTicks);
        target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 35, radius / 2, 0.8, radius / 2, 0.03);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.9f);
    }

    private void useArcanistLink(Player player) {
        double radius = getConfig().getDouble("specialization-abilities.arcanist-link.radius", 12.0);
        Player tank = nearestTank(player, radius);
        if (tank == null) {
            player.sendMessage(Component.text("No hay un Guardian o Paladin cerca.", NamedTextColor.YELLOW));
            return;
        }
        int cooldown = getConfig().getInt("specialization-abilities.arcanist-link.cooldown-seconds", 14);
        if (!startCooldown(player, "arcanist-link", cooldown)) {
            return;
        }
        showSkillCast(player, Component.text("Vinculo", NamedTextColor.AQUA));
        double ratio = getConfig().getDouble("specialization-abilities.arcanist-link.transfer-ratio", 0.70);
        threatManager.transferThreat(player, tank, ratio);
        player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1, 0), 20, 0.8, 1.0, 0.8, 0.0);
        playArcanistLinkEffect(player, tank);
        player.sendMessage(Component.text("Has vinculado tu amenaza a " + tank.getName() + ".", NamedTextColor.GREEN));
        tank.sendActionBar(Component.text(player.getName() + " ha transferido amenaza hacia ti.", NamedTextColor.AQUA));
    }

    private void playArcanistLinkEffect(Player source, Player tank) {
        Location start = source.getLocation().add(0, 1, 0);
        Vector path = tank.getLocation().add(0, 1, 0).toVector().subtract(start.toVector());
        int steps = 14;
        for (int step = 0; step <= steps; step++) {
            Location particle = start.clone().add(path.clone().multiply((double) step / steps));
            source.getWorld().spawnParticle(Particle.ENCHANT, particle, 2, 0.12, 0.12, 0.12, 0.0);
        }
        source.getWorld().playSound(source.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 0.7f, 1.4f);
        tank.getWorld().playSound(tank.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.8f, 0.8f);
    }

    private Player nearestTank(Player source, double radius) {
        Player selected = null;
        double nearest = radius * radius;
        for (Player candidate : Bukkit.getOnlinePlayers()) {
            if (candidate == source || candidate.getWorld() != source.getWorld() || downed.containsKey(candidate.getUniqueId())) {
                continue;
            }
            String specialization = profiles.get(candidate.getUniqueId()).specialization();
            if (!"guardian".equals(specialization) && !"paladin".equals(specialization)) {
                continue;
            }
            double distance = candidate.getLocation().distanceSquared(source.getLocation());
            if (distance <= nearest) {
                selected = candidate;
                nearest = distance;
            }
        }
        return selected;
    }

    private void useDruidBloom(Player player) {
        int cooldown = getConfig().getInt("specialization-abilities.druid-bloom.cooldown-seconds", 14);
        if (!startCooldown(player, "druid-bloom", cooldown)) {
            return;
        }
        showSkillCast(player, Component.text("Floracion", NamedTextColor.GREEN));
        double radius = getConfig().getDouble("specialization-abilities.druid-bloom.radius", 8.0);
        int duration = getConfig().getInt("specialization-abilities.druid-bloom.regeneration-seconds", 6);
        int affected = 0;
        for (Player ally : Bukkit.getOnlinePlayers()) {
            if (ally.getWorld() == player.getWorld()
                    && ally.getLocation().distanceSquared(player.getLocation()) <= radius * radius
                    && !downed.containsKey(ally.getUniqueId())) {
                ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration * 20, 0, false, true, true));
                affected++;
            }
        }
        threatManager.addHealingThreat(player, affected * 2.0);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 30, radius / 2, 0.8, radius / 2, 0.0);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRASS_BREAK, 1.0f, 0.8f);
        player.sendMessage(Component.text("Floracion alcanza a " + affected + " aliado(s).", NamedTextColor.GREEN));
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (downed.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && downed.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().trim();
        String command = message.startsWith("/") ? message.substring(1) : message;
        String base = command.isBlank() ? "" : command.split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        if (downed.containsKey(player.getUniqueId()) && base.equals("rendirse")) {
            return;
        }
        if (downed.containsKey(player.getUniqueId())
                && !player.isOp()
                && !player.hasPermission("servidro.admin")) {
            event.setCancelled(true);
            player.sendMessage(Component.text("No puedes usar comandos mientras estas derribado.", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        DownedState state = downed.remove(event.getPlayer().getUniqueId());
        if (state != null) {
            state.cancel();
            event.getPlayer().setPose(Pose.STANDING, false);
            event.getPlayer().setSwimming(false);
            event.getPlayer().removePotionEffect(PotionEffectType.DARKNESS);
            event.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
            unhighlightDownedPlayer(event.getPlayer());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        refreshPlayerBaseStats(player);
        updateTab(player);
        int delay = getConfig().getInt("onboarding.join-guide-delay-ticks", 30);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!player.isOnline()) {
                return;
            }
            player.showTitle(net.kyori.adventure.title.Title.title(
                    Component.text("Servidro MX", NamedTextColor.GOLD),
                    Component.text("Reino Corrompido", NamedTextColor.DARK_RED)));
            sendGuide(player);
        }, delay);
    }

    private void updateTab(Player player) {
        PlayerProfile profile = profiles.get(player.getUniqueId());
        String baseClass = profile.baseClass();
        String className = displayClass(baseClass);
        NamedTextColor classColor = classColor(baseClass);
        player.playerListName(Component.text("[" + className + "] ", classColor)
                .append(Component.text(player.getName(), NamedTextColor.WHITE))
                .append(Component.text(" " + profile.level(), NamedTextColor.GRAY)));
        player.sendPlayerListHeaderAndFooter(
                Component.text(getConfig().getString("tab.header", "Servidro MX"), NamedTextColor.GOLD),
                Component.text(getConfig().getString("tab.footer", "Temporada Alfa"), NamedTextColor.GRAY));
        alignScoreboardTeam(player, baseClass);
    }

    private void alignScoreboardTeam(Player player, String baseClass) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = switch (baseClass == null ? "" : baseClass) {
            case "guerrero" -> "10_";
            case "explorador" -> "20_";
            case "mago" -> "30_";
            case "clerigo" -> "40_";
            default -> "90_";
        } + player.getUniqueId().toString().replace("-", "").substring(0, 12);
        for (Team team : scoreboard.getTeams()) {
            if (team.getName().matches("\\d{2}_[0-9a-f]{12}")) {
                team.removeEntry(player.getName());
            }
        }
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }
        PlayerProfile profile = profiles.get(player.getUniqueId());
        team.prefix(Component.text("[" + displayClass(baseClass) + "] ", classColor(baseClass)));
        team.suffix(Component.text(" " + profile.level() + " ", NamedTextColor.GRAY).append(playerHealthTag(player)));
        team.addEntry(player.getName());
    }

    private void updateNearbyHealthBars() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTab(player);
        }
        double radius = getConfig().getDouble("visuals.health-bars.radius", 12.0);
        double radiusSquared = radius * radius;
        Set<UUID> visible = new HashSet<>();
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            LivingEntity focus = focusedHealthBarTarget(viewer, radius);
            if (focus != null) {
                visible.add(focus.getUniqueId());
                applyHealthNameplate(focus);
            }
            for (LivingEntity target : viewer.getWorld().getLivingEntities()) {
                if (!isVisibleCombatHealthBarTarget(viewer, target, radiusSquared) || target == focus) {
                    continue;
                }
                visible.add(target.getUniqueId());
                applyHealthNameplate(target);
            }
        }
        Set<UUID> toRestore = new HashSet<>();
        for (UUID uuid : new HashSet<>(trackedNameplates.keySet())) {
            Entity entity = Bukkit.getEntity(uuid);
            if (!(entity instanceof LivingEntity target) || !target.isValid() || !visible.contains(uuid)) {
                toRestore.add(uuid);
            }
        }
        for (UUID uuid : toRestore) {
            Entity entity = Bukkit.getEntity(uuid);
            restoreTrackedNameplate(uuid, entity instanceof LivingEntity living ? living : null);
        }
    }

    private boolean isHealthBarTarget(Player viewer, LivingEntity target) {
        return target != viewer
                && !(target instanceof ArmorStand)
                && !(target instanceof Player)
                && !(target instanceof AbstractVillager)
                && !target.isDead()
                && target.isValid();
    }

    private boolean isVisibleCombatHealthBarTarget(Player viewer, LivingEntity target, double radiusSquared) {
        return isHealthBarTarget(viewer, target)
                && viewer.getLocation().distanceSquared(target.getLocation()) <= radiusSquared
                && viewer.hasLineOfSight(target)
                && shouldShowCombatHealthBar(target);
    }

    private LivingEntity focusedHealthBarTarget(Player viewer, double radius) {
        RayTraceResult trace = viewer.getWorld().rayTraceEntities(
                viewer.getEyeLocation(),
                viewer.getEyeLocation().getDirection(),
                radius,
                0.4,
                entity -> entity instanceof LivingEntity living
                && isHealthBarTarget(viewer, living)
                        && shouldShowCombatHealthBar(living)
                        && viewer.hasLineOfSight(living));
        return trace != null && trace.getHitEntity() instanceof LivingEntity target ? target : null;
    }

    private boolean shouldShowCombatHealthBar(LivingEntity target) {
        return isAggressiveCombatMob(target) || isCombatTracked(target);
    }

    private boolean isCombatTracked(LivingEntity target) {
        Long expiresAt = combatHealthBarUntil.get(target.getUniqueId());
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt < System.currentTimeMillis()) {
            combatHealthBarUntil.remove(target.getUniqueId());
            return false;
        }
        return true;
    }

    private boolean isAggressiveCombatMob(LivingEntity target) {
        if (target.hasMetadata("MythicMob") || target instanceof Monster) {
            return true;
        }
        if (target instanceof Mob mob && mob.getTarget() != null) {
            return true;
        }
        return false;
    }

    private void markCombatHealthBar(LivingEntity target) {
        combatHealthBarUntil.put(
                target.getUniqueId(),
                System.currentTimeMillis() + (getConfig().getInt("visuals.health-bars.combat-seconds", 4) * 1000L));
    }

    private void applyHealthNameplate(LivingEntity target) {
        trackedNameplates.computeIfAbsent(target.getUniqueId(), ignored -> target.customName());
        trackedNameplateVisibility.putIfAbsent(target.getUniqueId(), target.isCustomNameVisible());
        target.customName(baseNameplate(target).append(Component.space()).append(healthBarText(target)));
        target.setCustomNameVisible(true);
    }

    private Component healthBarText(LivingEntity target) {
        double maxHealth = target.getAttribute(Attribute.MAX_HEALTH) == null
                ? Math.max(1.0, target.getHealth())
                : target.getAttribute(Attribute.MAX_HEALTH).getValue();
        double ratio = Math.max(0.0, Math.min(1.0, target.getHealth() / maxHealth));
        int filled = (int) Math.ceil(ratio * 10);
        NamedTextColor color = ratio > 0.6 ? NamedTextColor.GREEN
                : ratio > 0.3 ? NamedTextColor.YELLOW : NamedTextColor.RED;
        String filledBars = "|".repeat(filled);
        String emptyBars = "|".repeat(10 - filled);
        return Component.text("[", NamedTextColor.DARK_GRAY)
                .append(Component.text(filledBars, color))
                .append(Component.text(emptyBars, NamedTextColor.DARK_GRAY))
                .append(Component.text("] " + Math.ceil(target.getHealth()) + "/" + Math.ceil(maxHealth),
                        NamedTextColor.WHITE));
    }

    private Component baseNameplate(LivingEntity target) {
        Component current = trackedNameplates.get(target.getUniqueId());
        if (current != null) {
            return current;
        }
        return target.customName() != null ? target.customName() : Component.text(displayEntityType(target.getType()), NamedTextColor.WHITE);
    }

    private void restoreTrackedNameplate(UUID uuid, LivingEntity target) {
        if (target != null && target.isValid()) {
            target.customName(trackedNameplates.get(uuid));
            target.setCustomNameVisible(trackedNameplateVisibility.getOrDefault(uuid, false));
        }
        trackedNameplates.remove(uuid);
        trackedNameplateVisibility.remove(uuid);
    }

    private void restoreTrackedNameplates() {
        for (UUID uuid : new HashSet<>(trackedNameplates.keySet())) {
            Entity entity = Bukkit.getEntity(uuid);
            restoreTrackedNameplate(uuid, entity instanceof LivingEntity living ? living : null);
        }
    }

    private Component playerHealthTag(Player player) {
        double maxHealth = Math.max(1.0, attributeValue(player, Attribute.MAX_HEALTH));
        double ratio = Math.max(0.0, Math.min(1.0, player.getHealth() / maxHealth));
        int filled = Math.max(0, Math.min(5, (int) Math.ceil(ratio * 5.0)));
        NamedTextColor color = ratio > 0.6 ? NamedTextColor.GREEN
                : ratio > 0.3 ? NamedTextColor.YELLOW : NamedTextColor.RED;
        return Component.text("[", NamedTextColor.DARK_GRAY)
                .append(Component.text("|".repeat(filled), color))
                .append(Component.text("|".repeat(5 - filled), NamedTextColor.DARK_GRAY))
                .append(Component.text("]", NamedTextColor.DARK_GRAY));
    }

    private boolean tryUseTotem(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.getType() == Material.TOTEM_OF_UNDYING) {
            consumeOne(player, true, mainHand);
            applyTotemRescue(player);
            return true;
        }
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand.getType() == Material.TOTEM_OF_UNDYING) {
            consumeOne(player, false, offHand);
            applyTotemRescue(player);
            return true;
        }
        return false;
    }

    private void consumeOne(Player player, boolean mainHand, ItemStack stack) {
        int remaining = stack.getAmount() - 1;
        if (remaining <= 0) {
            if (mainHand) {
                player.getInventory().setItemInMainHand(null);
            } else {
                player.getInventory().setItemInOffHand(null);
            }
            return;
        }
        stack.setAmount(remaining);
        if (mainHand) {
            player.getInventory().setItemInMainHand(stack);
        } else {
            player.getInventory().setItemInOffHand(stack);
        }
    }

    private void applyTotemRescue(Player player) {
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHealth, 1.0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 900, 1, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0, false, true, true));
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 30, 0.6, 0.9, 0.6, 0.0);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
        player.sendMessage(Component.text("El totem te ha salvado del derribo.", NamedTextColor.GOLD));
    }

    private double attributeValue(Player player, Attribute attribute) {
        AttributeInstance instance = player.getAttribute(attribute);
        return instance == null ? 0.0 : instance.getValue();
    }

    private void sendBaseClassInfo(Player player, String classId) {
        switch (classId) {
            case "guerrero" -> {
                player.sendMessage(Component.text("Guerrero | Rol: frente de combate y presion.", NamedTextColor.GOLD));
                player.sendMessage(Component.text("Dash: F | Golpe Abrumador: Shift + clic izquierdo | Salto Desolador: saltar + Shift", NamedTextColor.YELLOW));
                player.sendMessage(Component.text("Provocar: clic derecho con escudo.", NamedTextColor.YELLOW));
            }
            case "explorador" -> {
                player.sendMessage(Component.text("Explorador | Rol: movilidad, persecucion y precision.", NamedTextColor.GREEN));
                player.sendMessage(Component.text("Rodar: doble Shift | juego de distancia y reposicion.", NamedTextColor.YELLOW));
            }
            case "mago" -> {
                player.sendMessage(Component.text("Mago | Rol: dano magico y control.", NamedTextColor.LIGHT_PURPLE));
                player.sendMessage(Component.text("Barrera: F con foco. Especializaciones: Piromante y Arcanista.", NamedTextColor.YELLOW));
            }
            case "clerigo" -> {
                player.sendMessage(Component.text("Clerigo | Rol: apoyo, curacion y utilidad.", NamedTextColor.YELLOW));
                player.sendMessage(Component.text("Bendicion y Curacion Menor para sostener al grupo.", NamedTextColor.YELLOW));
            }
            default -> player.sendMessage(Component.text("No tienes una clase base elegida todavia.", NamedTextColor.RED));
        }
    }

    private void sendSpecializationInfo(Player player, String specialization) {
        switch (specialization) {
            case "guardian" -> player.sendMessage(Component.text("Guardian: tanque puro, provoca con escudo y Golpe Abrumador cura 20%.", NamedTextColor.BLUE));
            case "berserker" -> player.sendMessage(Component.text("Berserker: ofensiva en area con Torbellino y robo de vida parcial.", NamedTextColor.RED));
            case "cazador" -> player.sendMessage(Component.text("Cazador: Marca del Cazador para presionar un objetivo.", NamedTextColor.GREEN));
            case "picaro" -> player.sendMessage(Component.text("Picaro: Desvanecer para bajar amenaza y reposicionarte.", NamedTextColor.DARK_GREEN));
            case "piromante" -> player.sendMessage(Component.text("Piromante: Bola de Fuego con dano en area y quemadura.", NamedTextColor.RED));
            case "arcanista" -> player.sendMessage(Component.text("Arcanista: Vinculo para redirigir parte del dano.", NamedTextColor.AQUA));
            case "paladin" -> player.sendMessage(Component.text("Paladin: mezcla de tanqueo, desafio y soporte clerical.", NamedTextColor.GOLD));
            case "druida" -> player.sendMessage(Component.text("Druida: Floracion para regeneracion en area.", NamedTextColor.GREEN));
            default -> {
            }
        }
    }

    private String displayClass(String baseClass) {
        if (baseClass == null) {
            return "Sin Clase";
        }
        return switch (baseClass) {
            case "guerrero" -> "Guerrero";
            case "explorador" -> "Explorador";
            case "mago" -> "Mago";
            case "clerigo" -> "Clerigo";
            default -> baseClass;
        };
    }

    private NamedTextColor classColor(String baseClass) {
        if (baseClass == null) {
            return NamedTextColor.GRAY;
        }
        return switch (baseClass) {
            case "guerrero" -> NamedTextColor.RED;
            case "explorador" -> NamedTextColor.GREEN;
            case "mago" -> NamedTextColor.LIGHT_PURPLE;
            case "clerigo" -> NamedTextColor.YELLOW;
            default -> NamedTextColor.GRAY;
        };
    }

    private boolean movedBlock(Location from, Location to) {
        return to == null || from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ();
    }

    private boolean movedPosition(Location from, Location to) {
        return to == null || Math.abs(from.getX() - to.getX()) > 0.001
                || Math.abs(from.getY() - to.getY()) > 0.001
                || Math.abs(from.getZ() - to.getZ()) > 0.001;
    }

    private String display(String value) {
        return value == null ? "sin elegir" : value;
    }

    private void saveProfiles() {
        try {
            profiles.save();
        } catch (IOException exception) {
            getLogger().severe("No se pudieron guardar los perfiles: " + exception.getMessage());
        }
    }

    private void savePersonalChests() {
        try {
            personalChests.save();
        } catch (IOException exception) {
            getLogger().severe("No se pudieron guardar los cofres personales: " + exception.getMessage());
        }
    }

    private void saveDailyMissions() {
        try {
            dailyMissions.save();
        } catch (IOException exception) {
            getLogger().severe("No se pudieron guardar las misiones diarias: " + exception.getMessage());
        }
    }

    private record MythicRewardStat(String label, double amount, Attribute attribute) {
    }

    private static final class DownedState {
        private final Player player;
        private final BukkitTask autoReviveTask;
        private final BukkitTask displayTask;
        private final BossBar bossBar;
        private DownedPhase phase = DownedPhase.DOWNED;
        private BukkitTask reviveTask;
        private BukkitTask visualTask;
        private UUID helper;

        private DownedState(Player player, BukkitTask autoReviveTask, BukkitTask displayTask, BossBar bossBar) {
            this.player = player;
            this.autoReviveTask = autoReviveTask;
            this.displayTask = displayTask;
            this.bossBar = bossBar;
        }

        private void cancelReviveTask() {
            if (reviveTask != null) {
                reviveTask.cancel();
                reviveTask = null;
            }
        }

        private void cancel() {
            autoReviveTask.cancel();
            displayTask.cancel();
            if (visualTask != null) {
                visualTask.cancel();
            }
            player.hideBossBar(bossBar);
            player.setPose(Pose.STANDING, false);
            player.setSwimming(false);
            cancelReviveTask();
        }
    }

    private enum DownedPhase {
        DOWNED,
        REVIVING
    }
}
