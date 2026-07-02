package mx.servidro.world;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class ServidroWorldPlugin extends JavaPlugin implements Listener {
    public static final String FROZEN_WORLD_NAME = "servidro_helado";
    private static final String FROZEN_POC_WORLD_NAME = "world_servidro_poc_helado";
    private static final int FROZEN_DEPTH_MAX_Y = 48;
    private static final int MIN_SURFACE_HEIGHT = 112;
    private static final int HIGH_OUTCROP_MIN_Y = 184;
    private static final int HIGH_SILVER_MIN_Y = 210;

    private final FrozenMountainGenerator frozenGenerator = new FrozenMountainGenerator();
    private int fogTaskId = -1;
    private int faunaTaskId = -1;
    private NamespacedKey overworldFrozenChunkKey;
    private NamespacedKey frozenOreChunkKey;
    private Biome frozenRidgeBiome;
    private Biome frozenDepthsBiome;
    private Method oraxenPlaceMethod;
    private boolean oraxenBridgeResolved;
    private boolean missingOraxenWarningLogged;

    @Override
    public void onEnable() {
        overworldFrozenChunkKey = new NamespacedKey(this, "overworld_frozen_biomes_v1");
        frozenOreChunkKey = new NamespacedKey(this, "frozen_ore_pass_v1");
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTask(this, this::ensureFrozenWorld);
        Bukkit.getScheduler().runTask(this, this::processLoadedChunks);
        fogTaskId = Bukkit.getScheduler().runTaskTimer(this, this::tickFrozenFog, 40L, 20L).getTaskId();
        faunaTaskId = Bukkit.getScheduler().runTaskTimer(this, this::tickFrozenFauna, 200L, 600L).getTaskId();
    }

    @Override
    public void onDisable() {
        if (fogTaskId != -1) {
            Bukkit.getScheduler().cancelTask(fogTaskId);
        }
        if (faunaTaskId != -1) {
            Bukkit.getScheduler().cancelTask(faunaTaskId);
        }
    }

    private void ensureFrozenWorld() {
        World world = Bukkit.getWorld(FROZEN_WORLD_NAME);
        if (world == null) {
            WorldCreator creator = new WorldCreator(FROZEN_WORLD_NAME);
            creator.generator(frozenGenerator);
            creator.environment(World.Environment.NORMAL);
            world = creator.createWorld();
        }
        if (world != null) {
            world.setStorm(true);
            world.setThundering(true);
            world.setWeatherDuration(Integer.MAX_VALUE);
            getLogger().info("Mundo helado activo: " + world.getName());
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        if (FROZEN_WORLD_NAME.equals(worldName)) {
            return frozenGenerator;
        }
        return null;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        if (FROZEN_WORLD_NAME.equals(world.getName())) {
            applyFrozenZoneOres(event.getChunk(), true);
            return;
        }
        if (!isEligibleOverworld(world)) {
            return;
        }
        applyFrozenOverworldBiomes(event.getChunk());
        applyFrozenZoneOres(event.getChunk(), false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("buscarbiomahelado")) {
            return handleFrozenBiomeSearch(sender, args);
        }
        if (!command.getName().equalsIgnoreCase("mundohelado")) {
            return false;
        }
        if (!sender.hasPermission("servidro.admin")) {
            sender.sendMessage(Component.text("No tienes permiso.", NamedTextColor.RED));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Ejecuta este comando dentro del juego.");
            return true;
        }
        World world = Bukkit.getWorld(FROZEN_WORLD_NAME);
        if (world == null) {
            player.sendMessage(Component.text("El mundo helado no esta cargado.", NamedTextColor.RED));
            return true;
        }
        Location target = new Location(world, 0.5, 245.0, 0.5, player.getLocation().getYaw(), player.getLocation().getPitch());
        world.getChunkAtAsync(target).thenRun(() -> Bukkit.getScheduler().runTask(this, () -> {
            Location safe = world.getHighestBlockAt(target).getLocation().add(0.5, 3.0, 0.5);
            safe.setYaw(target.getYaw());
            safe.setPitch(target.getPitch());
            player.teleport(safe);
            player.sendMessage(Component.text("Entraste a las Cordilleras Heladas generadas por ServidroWorld.", NamedTextColor.AQUA));
        }));
        return true;
    }

    private boolean handleFrozenBiomeSearch(CommandSender sender, String[] args) {
        if (!sender.hasPermission("servidro.admin")) {
            sender.sendMessage(Component.text("No tienes permiso.", NamedTextColor.RED));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Ejecuta este comando dentro del juego.");
            return true;
        }
        if (!resolveFrozenBiomeTargets()) {
            player.sendMessage(Component.text("El bioma servidro:cordilleras_heladas no esta registrado. Revisa datapacks.", NamedTextColor.RED));
            return true;
        }

        int radius = 96;
        boolean teleport = false;
        if (args.length >= 1) {
            try {
                radius = Math.max(8, Math.min(384, Integer.parseInt(args[0])));
            } catch (NumberFormatException ex) {
                player.sendMessage(Component.text("Uso: /buscarbiomahelado [radioChunks] [tp]", NamedTextColor.YELLOW));
                return true;
            }
        }
        if (args.length >= 2) {
            teleport = args[1].equalsIgnoreCase("tp") || args[1].equalsIgnoreCase("teleport");
        }

        World world = player.getWorld();
        if (!isEligibleOverworld(world)) {
            player.sendMessage(Component.text("Ejecuta este comando en el Overworld.", NamedTextColor.RED));
            return true;
        }

        int centerChunkX = player.getLocation().getBlockX() >> 4;
        int centerChunkZ = player.getLocation().getBlockZ() >> 4;
        List<int[]> chunks = buildChunkSearchOrder(centerChunkX, centerChunkZ, radius);
        player.sendMessage(Component.text("Buscando cordilleras heladas en " + chunks.size() + " chunks...", NamedTextColor.AQUA));
        searchFrozenBiomeChunk(player, world, chunks, 0, teleport);
        return true;
    }

    private List<int[]> buildChunkSearchOrder(int centerChunkX, int centerChunkZ, int radius) {
        List<int[]> chunks = new ArrayList<>((radius * 2 + 1) * (radius * 2 + 1));
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                chunks.add(new int[] {centerChunkX + dx, centerChunkZ + dz, dx * dx + dz * dz});
            }
        }
        chunks.sort(Comparator.comparingInt(value -> value[2]));
        return chunks;
    }

    private void searchFrozenBiomeChunk(Player player, World world, List<int[]> chunks, int index, boolean teleport) {
        if (!player.isOnline()) {
            return;
        }
        if (index >= chunks.size()) {
            player.sendMessage(Component.text("No encontre cordilleras heladas en ese radio. Prueba /buscarbiomahelado 192", NamedTextColor.YELLOW));
            return;
        }

        int[] entry = chunks.get(index);
        int chunkX = entry[0];
        int chunkZ = entry[1];
        world.getChunkAtAsync(chunkX, chunkZ).thenRun(() -> Bukkit.getScheduler().runTask(this, () -> {
            Location found = findFrozenBiomeInChunk(world, chunkX, chunkZ);
            if (found != null) {
                player.sendMessage(Component.text("Cordilleras Heladas encontradas en X "
                        + found.getBlockX() + " Y " + found.getBlockY() + " Z " + found.getBlockZ(), NamedTextColor.GREEN));
                if (teleport) {
                    player.teleport(found);
                    player.sendMessage(Component.text("Teletransportado al bioma helado.", NamedTextColor.AQUA));
                } else {
                    player.sendMessage(Component.text("Para ir directo usa: /buscarbiomahelado "
                            + Math.max(8, (int) Math.sqrt(entry[2]) + 2) + " tp", NamedTextColor.GRAY));
                }
                return;
            }
            if (index > 0 && index % 512 == 0) {
                player.sendMessage(Component.text("Buscando... " + index + "/" + chunks.size() + " chunks revisados.", NamedTextColor.GRAY));
            }
            searchFrozenBiomeChunk(player, world, chunks, index + 1, teleport);
        }));
    }

    private Location findFrozenBiomeInChunk(World world, int chunkX, int chunkZ) {
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        for (int x = baseX + 2; x < baseX + 16; x += 4) {
            for (int z = baseZ + 2; z < baseZ + 16; z += 4) {
                int surfaceY = world.getHighestBlockYAt(x, z);
                if (world.getBiome(x, Math.min(surfaceY, world.getMaxHeight() - 1), z) == frozenRidgeBiome
                        || world.getBiome(x, 160, z) == frozenRidgeBiome) {
                    return world.getHighestBlockAt(x, z).getLocation().add(0.5, 2.0, 0.5);
                }
            }
        }
        return null;
    }

    private void tickFrozenFog() {
        World world = Bukkit.getWorld(FROZEN_WORLD_NAME);
        if (world == null) {
            return;
        }
        for (Player player : world.getPlayers()) {
            Location location = player.getLocation();
            int y = location.getBlockY();
            int cloudAmount = y > 220 ? 54 : y > 170 ? 38 : 22;
            double radius = y > 220 ? 13.0 : y > 170 ? 10.0 : 7.0;
            player.spawnParticle(Particle.CLOUD, location.clone().add(0, 1.6, 0), cloudAmount, radius, 2.8, radius, 0.008);
            if (y > 170) {
                int snowAmount = y > 240 ? 42 : 24;
                player.spawnParticle(Particle.SNOWFLAKE, location.clone().add(0, 1.2, 0), snowAmount, radius * 0.8, 2.4, radius * 0.8, 0.025);
            }
            if (y > 235 && Math.random() < 0.08) {
                player.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.28f, 0.55f);
            }
        }
    }

    private void tickFrozenFauna() {
        World world = Bukkit.getWorld(FROZEN_WORLD_NAME);
        if (world == null) {
            return;
        }
        for (Player player : world.getPlayers()) {
            long nearbyFoxes = player.getNearbyEntities(64, 32, 64).stream()
                    .filter(entity -> entity.getType() == EntityType.FOX)
                    .count();
            if (nearbyFoxes >= 5 || Math.random() > 0.35) {
                continue;
            }

            Location base = player.getLocation().clone().add(
                    -48 + Math.random() * 96,
                    0,
                    -48 + Math.random() * 96);
            Location spawn = world.getHighestBlockAt(base).getLocation().add(0.5, 1.0, 0.5);
            int y = spawn.getBlockY();
            if (y < 90 || y > 190) {
                continue;
            }
            Fox fox = (Fox) world.spawnEntity(spawn, EntityType.FOX);
            fox.setFoxType(Fox.Type.SNOW);
        }
    }

    private void processLoadedChunks() {
        resolveFrozenBiomeTargets();
        for (World world : Bukkit.getWorlds()) {
            if (FROZEN_WORLD_NAME.equals(world.getName())) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    applyFrozenZoneOres(chunk, true);
                }
                continue;
            }
            if (!isEligibleOverworld(world)) {
                continue;
            }
            for (Chunk chunk : world.getLoadedChunks()) {
                applyFrozenOverworldBiomes(chunk);
                applyFrozenZoneOres(chunk, false);
            }
        }
    }

    private boolean isEligibleOverworld(World world) {
        return world.getEnvironment() == World.Environment.NORMAL
                && !FROZEN_WORLD_NAME.equals(world.getName())
                && !FROZEN_POC_WORLD_NAME.equals(world.getName());
    }

    private void applyFrozenOverworldBiomes(Chunk chunk) {
        if (chunk.getPersistentDataContainer().has(overworldFrozenChunkKey, PersistentDataType.BYTE)) {
            return;
        }
        if (!resolveFrozenBiomeTargets()) {
            return;
        }

        World world = chunk.getWorld();
        long seed = world.getSeed();
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();
        int baseX = chunk.getX() << 4;
        int baseZ = chunk.getZ() << 4;
        boolean processed = false;

        for (int cellX = 0; cellX < 4; cellX++) {
            int sampleX = baseX + cellX * 4 + 2;
            for (int cellZ = 0; cellZ < 4; cellZ++) {
                int sampleZ = baseZ + cellZ * 4 + 2;
                int surfaceY = world.getHighestBlockYAt(sampleX, sampleZ);
                Biome sourceBiome = world.getBiome(sampleX, surfaceY, sampleZ);
                if (!isFrozenSourceBiome(sourceBiome, surfaceY)) {
                    continue;
                }
                if (!shouldConvertFrozenCell(seed, sampleX, sampleZ, surfaceY, sourceBiome)) {
                    continue;
                }

                processed = true;
                for (int y = minY; y < maxY; y += 4) {
                    world.setBiome(sampleX, y, sampleZ, y <= FROZEN_DEPTH_MAX_Y ? frozenDepthsBiome : frozenRidgeBiome);
                }
            }
        }

        chunk.getPersistentDataContainer().set(overworldFrozenChunkKey, PersistentDataType.BYTE, processed ? (byte) 1 : (byte) 0);
    }

    private void applyFrozenZoneOres(Chunk chunk, boolean wholeChunkFrozen) {
        if (chunk.getPersistentDataContainer().has(frozenOreChunkKey, PersistentDataType.BYTE)) {
            return;
        }

        boolean targetChunk = wholeChunkFrozen || chunkHasFrozenBiomeSignature(chunk);
        if (!targetChunk) {
            chunk.getPersistentDataContainer().set(frozenOreChunkKey, PersistentDataType.BYTE, (byte) 0);
            return;
        }

        World world = chunk.getWorld();
        long salt = 0x5F3759DFL ^ ((long) chunk.getX() << 32) ^ (chunk.getZ() & 0xffffffffL);
        Random random = new Random(world.getSeed() ^ salt);

        placeSurfaceOutcrops(world, chunk, random);
        placeDeepTinVeins(world, chunk, random);
        placeDeepIronVeins(world, chunk, random);

        chunk.getPersistentDataContainer().set(frozenOreChunkKey, PersistentDataType.BYTE, (byte) 1);
    }

    private boolean chunkHasFrozenBiomeSignature(Chunk chunk) {
        Byte frozenFlag = chunk.getPersistentDataContainer().get(overworldFrozenChunkKey, PersistentDataType.BYTE);
        if (frozenFlag != null) {
            return frozenFlag == (byte) 1;
        }
        if (!resolveFrozenBiomeTargets()) {
            return false;
        }

        World world = chunk.getWorld();
        int baseX = chunk.getX() << 4;
        int baseZ = chunk.getZ() << 4;
        for (int cellX = 0; cellX < 4; cellX++) {
            int sampleX = baseX + cellX * 4 + 2;
            for (int cellZ = 0; cellZ < 4; cellZ++) {
                int sampleZ = baseZ + cellZ * 4 + 2;
                if (world.getBiome(sampleX, 160, sampleZ) == frozenRidgeBiome
                        || world.getBiome(sampleX, 0, sampleZ) == frozenDepthsBiome) {
                    return true;
                }
            }
        }
        return false;
    }

    private void placeSurfaceOutcrops(World world, Chunk chunk, Random random) {
        int tinAttempts = 6 + random.nextInt(4);
        int silverAttempts = 2 + random.nextInt(2);
        for (int i = 0; i < tinAttempts; i++) {
            tryPlaceSurfaceOutcrop(world, chunk, random, "tin_ore", HIGH_OUTCROP_MIN_Y, 4 + random.nextInt(4));
        }
        for (int i = 0; i < silverAttempts; i++) {
            tryPlaceSurfaceOutcrop(world, chunk, random, "silver_ore", HIGH_SILVER_MIN_Y, 3 + random.nextInt(3));
        }
    }

    private void tryPlaceSurfaceOutcrop(World world, Chunk chunk, Random random, String oreId, int minHeight, int nodes) {
        int worldX = (chunk.getX() << 4) + random.nextInt(16);
        int worldZ = (chunk.getZ() << 4) + random.nextInt(16);
        int surfaceY = world.getHighestBlockYAt(worldX, worldZ);
        if (surfaceY < minHeight) {
            return;
        }

        Block anchor = findSurfaceOutcropAnchor(world, worldX, worldZ, surfaceY);
        if (anchor == null) {
            return;
        }

        for (int n = 0; n < nodes; n++) {
            int x = anchor.getX() + random.nextInt(5) - 2;
            int z = anchor.getZ() + random.nextInt(5) - 2;
            int y = anchor.getY() + random.nextInt(3) - 1;
            Block candidate = findNearbyExposedRock(world, x, y, z, 2);
            if (candidate == null) {
                continue;
            }
            placeOraxenOre(oreId, candidate);
        }
    }

    private Block findSurfaceOutcropAnchor(World world, int x, int z, int surfaceY) {
        for (int y = surfaceY; y >= Math.max(world.getMinHeight(), surfaceY - 8); y--) {
            Block block = world.getBlockAt(x, y, z);
            if (isSurfaceOutcropReplaceable(block.getType()) && hasExposedFace(block)) {
                return block;
            }
        }
        return null;
    }

    private Block findNearbyExposedRock(World world, int x, int y, int z, int verticalRadius) {
        for (int dy = 0; dy <= verticalRadius; dy++) {
            int[] offsets = dy == 0 ? new int[] {0} : new int[] {dy, -dy};
            for (int offset : offsets) {
                int targetY = y + offset;
                if (targetY <= world.getMinHeight() || targetY >= world.getMaxHeight()) {
                    continue;
                }
                Block block = world.getBlockAt(x, targetY, z);
                if (isSurfaceOutcropReplaceable(block.getType()) && hasExposedFace(block)) {
                    return block;
                }
            }
        }
        return null;
    }

    private void placeDeepTinVeins(World world, Chunk chunk, Random random) {
        int attempts = 6 + random.nextInt(4);
        for (int i = 0; i < attempts; i++) {
            int x = (chunk.getX() << 4) + random.nextInt(16);
            int z = (chunk.getZ() << 4) + random.nextInt(16);
            int y = -24 + random.nextInt(73);
            placeBuriedOraxenVein(world, x, y, z, "tin_ore", 4 + random.nextInt(4), random);
        }
    }

    private void placeDeepIronVeins(World world, Chunk chunk, Random random) {
        int attempts = 7 + random.nextInt(5);
        for (int i = 0; i < attempts; i++) {
            int x = (chunk.getX() << 4) + random.nextInt(16);
            int z = (chunk.getZ() << 4) + random.nextInt(16);
            int y = -32 + random.nextInt(89);
            Material oreType = y < 0 ? Material.DEEPSLATE_IRON_ORE : Material.IRON_ORE;
            placeVanillaVein(world, x, y, z, oreType, 4 + random.nextInt(5), random);
        }
    }

    private void placeBuriedOraxenVein(World world, int x, int y, int z, String oreId, int nodes, Random random) {
        for (int i = 0; i < nodes; i++) {
            int px = x + random.nextInt(5) - 2;
            int py = y + random.nextInt(5) - 2;
            int pz = z + random.nextInt(5) - 2;
            if (py <= world.getMinHeight() || py >= world.getMaxHeight()) {
                continue;
            }
            Block block = world.getBlockAt(px, py, pz);
            if (!isUndergroundOreReplaceable(block.getType())) {
                continue;
            }
            placeOraxenOre(oreId, block);
        }
    }

    private void placeVanillaVein(World world, int x, int y, int z, Material oreType, int nodes, Random random) {
        for (int i = 0; i < nodes; i++) {
            int px = x + random.nextInt(5) - 2;
            int py = y + random.nextInt(5) - 2;
            int pz = z + random.nextInt(5) - 2;
            if (py <= world.getMinHeight() || py >= world.getMaxHeight()) {
                continue;
            }
            Block block = world.getBlockAt(px, py, pz);
            if (!isUndergroundOreReplaceable(block.getType())) {
                continue;
            }
            block.setType(oreType, false);
        }
    }

    private void placeOraxenOre(String oreId, Block block) {
        if (!isUndergroundOreReplaceable(block.getType()) && !isSurfaceOutcropReplaceable(block.getType())) {
            return;
        }
        if (!ensureOraxenBridge()) {
            return;
        }
        try {
            oraxenPlaceMethod.invoke(null, oreId, block.getLocation());
        } catch (ReflectiveOperationException ex) {
            getLogger().warning("Fallo colocando mena Oraxen " + oreId + " en " + block.getLocation());
        }
    }

    private boolean ensureOraxenBridge() {
        if (oraxenBridgeResolved) {
            return oraxenPlaceMethod != null;
        }
        oraxenBridgeResolved = true;

        Plugin oraxen = Bukkit.getPluginManager().getPlugin("Oraxen");
        if (oraxen == null || !oraxen.isEnabled()) {
            if (!missingOraxenWarningLogged) {
                missingOraxenWarningLogged = true;
                getLogger().warning("Oraxen no esta disponible; se omitiran estaño/plata custom en biomas helados.");
            }
            return false;
        }
        try {
            Class<?> blocksClass = Class.forName("io.th0rgal.oraxen.api.OraxenBlocks", true, oraxen.getClass().getClassLoader());
            oraxenPlaceMethod = blocksClass.getMethod("place", String.class, Location.class);
            return true;
        } catch (ReflectiveOperationException ex) {
            if (!missingOraxenWarningLogged) {
                missingOraxenWarningLogged = true;
                getLogger().warning("No pude resolver el bridge de OraxenBlocks; se omitiran estaño/plata custom en biomas helados.");
            }
            return false;
        }
    }

    private boolean resolveFrozenBiomeTargets() {
        if (frozenRidgeBiome != null && frozenDepthsBiome != null) {
            return true;
        }
        frozenRidgeBiome = Registry.BIOME.get(NamespacedKey.fromString("servidro:cordilleras_heladas"));
        frozenDepthsBiome = Registry.BIOME.get(NamespacedKey.fromString("servidro:profundidades_heladas"));
        if (frozenRidgeBiome == null || frozenDepthsBiome == null) {
            getLogger().warning("No pude resolver los biomas custom servidro:cordilleras_heladas / servidro:profundidades_heladas. Verifica que el datapack este cargado.");
            return false;
        }
        return true;
    }

    private boolean isFrozenSourceBiome(Biome biome, int surfaceY) {
        if (surfaceY < MIN_SURFACE_HEIGHT) {
            return false;
        }
        return biome == Biome.FROZEN_PEAKS
                || biome == Biome.JAGGED_PEAKS
                || biome == Biome.SNOWY_SLOPES
                || biome == Biome.GROVE
                || biome == Biome.STONY_PEAKS
                || biome == Biome.WINDSWEPT_HILLS
                || biome == Biome.WINDSWEPT_GRAVELLY_HILLS
                || biome == Biome.ICE_SPIKES;
    }

    private boolean isSurfaceOutcropReplaceable(Material type) {
        return type == Material.STONE
                || type == Material.ANDESITE
                || type == Material.DIORITE
                || type == Material.GRANITE
                || type == Material.TUFF
                || type == Material.CALCITE
                || type == Material.DEEPSLATE;
    }

    private boolean isUndergroundOreReplaceable(Material type) {
        return type == Material.STONE
                || type == Material.ANDESITE
                || type == Material.DIORITE
                || type == Material.GRANITE
                || type == Material.TUFF
                || type == Material.CALCITE
                || type == Material.DEEPSLATE;
    }

    private boolean hasExposedFace(Block block) {
        return isAirLike(block.getRelative(BlockFace.UP).getType())
                || isAirLike(block.getRelative(BlockFace.NORTH).getType())
                || isAirLike(block.getRelative(BlockFace.SOUTH).getType())
                || isAirLike(block.getRelative(BlockFace.EAST).getType())
                || isAirLike(block.getRelative(BlockFace.WEST).getType());
    }

    private boolean isAirLike(Material type) {
        return type == Material.AIR
                || type == Material.CAVE_AIR
                || type == Material.VOID_AIR
                || type == Material.SNOW;
    }

    private boolean shouldConvertFrozenCell(long seed, int x, int z, int surfaceY, Biome sourceBiome) {
        double macro = normalize01(fbm(seed + 2001, x * 0.0024, z * 0.0024, 4, 0.54));
        double ridges = ridged(seed + 2002, x * 0.0058, z * 0.0058, 4);
        double detail = normalize01(noise(seed + 2003, x * 0.028, z * 0.028));
        double elevation = clamp01((surfaceY - MIN_SURFACE_HEIGHT) / 72.0);
        double sourceBonus = sourceBiome == Biome.FROZEN_PEAKS || sourceBiome == Biome.JAGGED_PEAKS
                ? 0.18
                : sourceBiome == Biome.SNOWY_SLOPES || sourceBiome == Biome.GROVE
                ? 0.10
                : 0.05;
        double score = macro * 0.32 + ridges * 0.36 + detail * 0.08 + elevation * 0.24 + sourceBonus;
        return score >= 0.69;
    }

    private static double normalize01(double value) {
        return (value + 1.0) * 0.5;
    }

    private static double fbm(long seed, double x, double z, int octaves, double persistence) {
        double total = 0.0;
        double amplitude = 1.0;
        double frequency = 1.0;
        double max = 0.0;
        for (int i = 0; i < octaves; i++) {
            total += noise(seed + i * 101L, x * frequency, z * frequency) * amplitude;
            max += amplitude;
            amplitude *= persistence;
            frequency *= 2.0;
        }
        return total / max;
    }

    private static double ridged(long seed, double x, double z, int octaves) {
        double value = 0.0;
        double amplitude = 0.65;
        double frequency = 1.0;
        double max = 0.0;
        for (int i = 0; i < octaves; i++) {
            double n = Math.abs(noise(seed + i * 157L, x * frequency, z * frequency));
            value += (1.0 - n) * amplitude;
            max += amplitude;
            amplitude *= 0.55;
            frequency *= 2.05;
        }
        return value / max;
    }

    private static double noise(long seed, double x, double z) {
        int x0 = fastFloor(x);
        int z0 = fastFloor(z);
        double tx = x - x0;
        double tz = z - z0;
        double sx = fade(tx);
        double sz = fade(tz);
        double a = randomUnit(seed, x0, z0);
        double b = randomUnit(seed, x0 + 1, z0);
        double c = randomUnit(seed, x0, z0 + 1);
        double d = randomUnit(seed, x0 + 1, z0 + 1);
        return lerp(lerp(a, b, sx), lerp(c, d, sx), sz);
    }

    private static double randomUnit(long seed, int x, int z) {
        long h = seed ^ (x * 341873128712L) ^ (z * 132897987541L);
        h = (h ^ (h >>> 33)) * 0xff51afd7ed558ccdL;
        h = (h ^ (h >>> 33)) * 0xc4ceb9fe1a85ec53L;
        h = h ^ (h >>> 33);
        return ((h >>> 11) * 0x1.0p-53) * 2.0 - 1.0;
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static int fastFloor(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
