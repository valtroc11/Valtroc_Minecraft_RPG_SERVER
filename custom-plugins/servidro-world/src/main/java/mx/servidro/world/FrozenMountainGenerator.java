package mx.servidro.world;

import java.util.List;
import java.util.Random;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

public final class FrozenMountainGenerator extends ChunkGenerator {
    private static final int SEA_LEVEL = 64;
    private static final int SNOW_LINE = 158;
    private static final int GLACIER_LINE = 210;
    private static final int MAX_TARGET_HEIGHT = 315;
    private static final int MIN_Y = -64;
    private static final int CAVE_CEILING = 118;

    private final BiomeProvider biomeProvider = new FrozenBiomeProvider();

    @Override
    public boolean isParallelCapable() {
        return true;
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return biomeProvider;
    }

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        long seed = worldInfo.getSeed();
        int minY = Math.max(chunkData.getMinHeight(), MIN_Y);
        int maxY = Math.min(chunkData.getMaxHeight(), 320);

        for (int localX = 0; localX < 16; localX++) {
            int worldX = chunkX * 16 + localX;
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldZ = chunkZ * 16 + localZ;
                int height = terrainHeight(seed, worldX, worldZ);
                for (int y = minY; y <= height && y < maxY; y++) {
                    Material material = baseMaterial(y, height);
                    if (y > minY + 4 && y < CAVE_CEILING && isCave(seed, worldX, y, worldZ, height)) {
                        continue;
                    }
                    chunkData.setBlock(localX, y, localZ, material);
                }
                addSurface(chunkData, seed, localX, worldX, localZ, worldZ, height);
            }
        }
    }

    @Override
    public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        int minY = chunkData.getMinHeight();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunkData.setBlock(x, minY, z, Material.BEDROCK);
                chunkData.setBlock(x, minY + 1, z, Material.BEDROCK);
            }
        }
    }

    @Override
    public int getBaseHeight(WorldInfo worldInfo, Random random, int x, int z, HeightMap heightMap) {
        return terrainHeight(worldInfo.getSeed(), x, z);
    }

    @Override
    public boolean canSpawn(World world, int x, int z) {
        return terrainHeight(world.getSeed(), x, z) > SEA_LEVEL;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        int y = terrainHeight(world.getSeed(), 0, 0) + 3;
        return new Location(world, 0.5, y, 0.5);
    }

    private int terrainHeight(long seed, int x, int z) {
        double continental = fbm(seed + 11, x * 0.0016, z * 0.0016, 5, 0.52);
        double ridges = ridged(seed + 23, x * 0.0038, z * 0.0038, 5);
        double rangeMask = mountainRangeMask(seed, x, z);
        double secondary = fbm(seed + 37, x * 0.009, z * 0.009, 4, 0.50);
        double detail = fbm(seed + 41, x * 0.025, z * 0.025, 3, 0.45);
        double valley = glacialValleyMask(seed, x, z);

        double height = 86.0 + smoothstep(-0.45, 0.75, continental) * 28.0;
        height += rangeMask * 136.0;
        height += Math.pow(ridges, 3.35) * rangeMask * 205.0;
        height += Math.copySign(Math.pow(Math.abs(secondary), 1.35), secondary) * 42.0;
        height += detail * 13.0;
        height -= valley * 66.0;
        if (valley > 0.35) {
            double valleyFloor = 92.0 + noise(seed + 72, z * 0.0025, 0.0) * 12.0;
            height = lerp(height, valleyFloor, Math.min(0.72, valley * 0.82));
        }

        if (height > 235.0) {
            height += (height - 235.0) * 0.55;
        }
        if (height > 286.0) {
            height += (height - 286.0) * 0.12;
        }
        double localCap = 292.0
                + noise(seed + 81, x * 0.006, z * 0.006) * 13.0
                + noise(seed + 82, x * 0.021, z * 0.021) * 5.0;
        if (height > localCap) {
            height = localCap + (height - localCap) * 0.18;
        }
        return clamp((int) Math.round(height), 78, MAX_TARGET_HEIGHT);
    }

    private double mountainRangeMask(long seed, int x, int z) {
        double center = rangeCenter(seed, z);
        double distance = Math.abs(x - center);
        double main = 1.0 - smoothstep(90.0, 620.0, distance);

        double westDistance = Math.abs(x - (center - 360.0 - noise(seed + 61, z * 0.0022, 0.0) * 90.0));
        double eastDistance = Math.abs(x - (center + 330.0 + noise(seed + 62, z * 0.0020, 0.0) * 110.0));
        double west = 0.78 * (1.0 - smoothstep(70.0, 300.0, westDistance));
        double east = 0.70 * (1.0 - smoothstep(75.0, 340.0, eastDistance));

        double passNoise = noise(seed + 63, x * 0.0012, z * 0.0012);
        double passCut = smoothstep(0.58, 0.88, passNoise) * 0.45;
        return clamp01(Math.max(main, Math.max(west, east)) - passCut);
    }

    private double glacialValleyMask(long seed, int x, int z) {
        double center = rangeCenter(seed, z);
        double westValley = 1.0 - smoothstep(55.0, 180.0, Math.abs(x - (center - 185.0)));
        double eastValley = 1.0 - smoothstep(60.0, 210.0, Math.abs(x - (center + 175.0)));
        double longitudinal = smoothstep(-0.25, 0.85, noise(seed + 71, z * 0.0035, x * 0.0008));
        return clamp01(Math.max(westValley, eastValley) * longitudinal);
    }

    private double rangeCenter(long seed, int z) {
        double broad = noise(seed + 51, z * 0.00115, 0.0) * 460.0;
        double medium = noise(seed + 52, z * 0.0032, 0.0) * 145.0;
        double drift = Math.sin((z + seed % 10000L) * 0.0017) * 180.0;
        return broad + medium + drift;
    }

    private Material baseMaterial(int y, int surfaceHeight) {
        if (y < -8) {
            return Material.DEEPSLATE;
        }
        if (surfaceHeight - y <= 4 && y < SNOW_LINE) {
            return Material.DIRT;
        }
        return Material.STONE;
    }

    private void addSurface(ChunkData chunkData, long seed, int localX, int worldX, int localZ, int worldZ, int height) {
        double slope = localSlope(seed, worldX, worldZ);
        if (height >= 250) {
            if (slope > 34.0) {
                chunkData.setBlock(localX, height, localZ, Material.STONE);
                chunkData.setBlock(localX, height - 1, localZ, Material.PACKED_ICE);
            } else {
                chunkData.setBlock(localX, height, localZ, Material.SNOW_BLOCK);
                chunkData.setBlock(localX, height - 1, localZ, Material.PACKED_ICE);
                chunkData.setBlock(localX, height - 2, localZ, Material.BLUE_ICE);
            }
        } else if (height >= GLACIER_LINE) {
            chunkData.setBlock(localX, height, localZ, slope > 28.0 ? Material.STONE : Material.SNOW_BLOCK);
            chunkData.setBlock(localX, height - 1, localZ, Material.PACKED_ICE);
            chunkData.setBlock(localX, height - 2, localZ, Material.STONE);
        } else if (height >= 190) {
            chunkData.setBlock(localX, height, localZ, slope > 24.0 ? Material.STONE : Material.SNOW_BLOCK);
            chunkData.setBlock(localX, height - 1, localZ, slope > 24.0 ? Material.STONE : Material.PACKED_ICE);
        } else if (height >= SNOW_LINE) {
            Material top = slope > 18.0 || noise(seed + 1401, worldX * 0.07, worldZ * 0.07) > 0.32
                    ? Material.STONE
                    : Material.SNOW_BLOCK;
            chunkData.setBlock(localX, height, localZ, top);
            chunkData.setBlock(localX, height - 1, localZ, Material.STONE);
        } else if (height >= 130) {
            Material top = slope > 16.0 ? Material.STONE : Material.GRASS_BLOCK;
            chunkData.setBlock(localX, height, localZ, top);
            if (top == Material.GRASS_BLOCK || Math.floorMod(worldX + worldZ, 3) == 0) {
                chunkData.setBlock(localX, height + 1, localZ, Material.SNOW);
            }
        } else {
            chunkData.setBlock(localX, height, localZ, Material.GRASS_BLOCK);
            chunkData.setBlock(localX, height + 1, localZ, Material.SNOW);
        }

        addOreVeins(chunkData, seed, localX, worldX, localZ, worldZ, height);
        addGlacierTongue(chunkData, seed, localX, worldX, localZ, worldZ, height, slope);
        addFrozenLake(chunkData, seed, localX, worldX, localZ, worldZ, height);
        addSnowField(chunkData, seed, localX, worldX, localZ, worldZ, height, slope);
        addIceSpikes(chunkData, seed, localX, worldX, localZ, worldZ, height);
        addHydrothermalSpring(chunkData, seed, localX, worldX, localZ, worldZ, height);
        addColdTree(chunkData, seed, localX, worldX, localZ, worldZ, height);
        addCaveDetails(chunkData, seed, localX, worldX, localZ, worldZ, height);
    }

    private void addOreVeins(ChunkData chunkData, long seed, int localX, int worldX, int localZ, int worldZ, int height) {
        for (int y = -42; y < Math.min(60, height - 10); y++) {
            double deepOre = noise(seed + 701, worldX * 0.135, y * 0.135, worldZ * 0.135);
            if (!isOreReplaceable(chunkData.getType(localX, y, localZ))) {
                continue;
            }
            if (deepOre > 0.78) {
                chunkData.setBlock(localX, y, localZ, y < -8 ? Material.DEEPSLATE_IRON_ORE : Material.IRON_ORE);
            }
        }
    }

    private void addHydrothermalSpring(ChunkData chunkData, long seed, int localX, int worldX, int localZ, int worldZ, int height) {
        double spring = noise(seed + 901, worldX * 0.012, worldZ * 0.012);
        double detail = noise(seed + 902, worldX * 0.07, worldZ * 0.07);
        if (spring < 0.962 || detail < 0.25 || height > 155 || height < 86) {
            return;
        }
        int radius = 2 + Math.floorMod(worldX * 31 + worldZ * 17, 2);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int ax = localX + dx;
                int az = localZ + dz;
                if (ax < 0 || ax >= 16 || az < 0 || az >= 16) {
                    continue;
                }
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > radius + 0.15) {
                    continue;
                }
                if (distance > radius - 0.9) {
                    chunkData.setBlock(ax, height, az, Material.STONE);
                    chunkData.setBlock(ax, height + 1, az, Material.AIR);
                    continue;
                }
                chunkData.setBlock(ax, height - 1, az, Material.MAGMA_BLOCK);
                chunkData.setBlock(ax, height, az, Material.WATER);
                chunkData.setBlock(ax, height + 1, az, Material.AIR);
            }
        }
    }

    private void addSnowField(ChunkData chunkData, long seed, int localX, int worldX, int localZ, int worldZ, int height, double slope) {
        if (height < 186 && slope < 24.0) {
            return;
        }
        double field = noise(seed + 1101, worldX * 0.018, worldZ * 0.018);
        if (height > 228 || field > -0.18 || slope > 28.0) {
            int depth = height > 260 ? 4 : height > 228 ? 3 : 2;
            for (int y = 0; y < depth; y++) {
                chunkData.setBlock(localX, height - y, localZ, Material.SNOW_BLOCK);
            }
            if ((height > 245 || slope > 30.0) && Math.floorMod(worldX * 17 + worldZ * 23, 7) == 0) {
                chunkData.setBlock(localX, height + 1, localZ, Material.POWDER_SNOW);
            }
        }
    }

    private void addGlacierTongue(ChunkData chunkData, long seed, int localX, int worldX, int localZ, int worldZ, int height, double slope) {
        if (height < 128 || height > 252) {
            return;
        }
        double valley = glacialValleyMask(seed, worldX, worldZ);
        double flow = noise(seed + 1501, worldX * 0.012, worldZ * 0.004);
        double fracture = noise(seed + 1502, worldX * 0.12, worldZ * 0.12);
        if (valley < 0.28 || flow < -0.10) {
            return;
        }
        Material ice = fracture > 0.48 ? Material.BLUE_ICE : Material.PACKED_ICE;
        chunkData.setBlock(localX, height, localZ, ice);
        chunkData.setBlock(localX, height - 1, localZ, Material.PACKED_ICE);
        if (height < 220 && fracture < -0.70 && slope < 20.0) {
            chunkData.setBlock(localX, height + 1, localZ, Material.AIR);
            chunkData.setBlock(localX, height, localZ, Material.BLACKSTONE);
        } else if (Math.floorMod(worldX * 5 + worldZ * 11, 6) == 0) {
            chunkData.setBlock(localX, height + 1, localZ, Material.SNOW);
        }
        if (height < 138 && valley > 0.5 && Math.abs(noise(seed + 1503, worldX * 0.018, worldZ * 0.018)) < 0.16) {
            chunkData.setBlock(localX, height, localZ, Material.BLUE_ICE);
            chunkData.setBlock(localX, height - 1, localZ, Material.WATER);
        }
    }

    private void addFrozenLake(ChunkData chunkData, long seed, int localX, int worldX, int localZ, int worldZ, int height) {
        if (height < 78 || height > 118) {
            return;
        }
        double basin = noise(seed + 1201, worldX * 0.010, worldZ * 0.010);
        double edge = noise(seed + 1202, worldX * 0.045, worldZ * 0.045);
        if (basin < 0.54 || edge < -0.18) {
            return;
        }

        Material ice = Material.PACKED_ICE;
        double type = noise(seed + 1203, worldX * 0.08, worldZ * 0.08);
        if (type > 0.35) {
            ice = Material.PACKED_ICE;
        } else if (type < -0.35) {
            ice = Material.BLUE_ICE;
        }

        chunkData.setBlock(localX, height, localZ, ice);
        chunkData.setBlock(localX, height - 1, localZ, Material.WATER);
        chunkData.setBlock(localX, height - 2, localZ, Material.GRAVEL);
        if (Math.floorMod(worldX + worldZ, 5) == 0) {
            chunkData.setBlock(localX, height + 1, localZ, Material.SNOW);
        }
    }

    private void addIceSpikes(ChunkData chunkData, long seed, int localX, int worldX, int localZ, int worldZ, int height) {
        if (height < 132 || height > 238) {
            return;
        }
        double field = noise(seed + 1301, worldX * 0.026, worldZ * 0.026);
        double point = noise(seed + 1302, worldX * 0.41, worldZ * 0.41);
        if (field < 0.26 || point < 0.88) {
            return;
        }
        if (localX < 2 || localX > 13 || localZ < 2 || localZ > 13) {
            return;
        }

        int spikeHeight = 3 + Math.floorMod(worldX * 19 + worldZ * 29, 7);
        Material material = point > 0.94 ? Material.BLUE_ICE : Material.PACKED_ICE;
        for (int y = 1; y <= spikeHeight; y++) {
            int radius = y < spikeHeight / 2 ? 1 : 0;
            chunkData.setBlock(localX, height + y, localZ, material);
            if (radius > 0) {
                chunkData.setBlock(localX + 1, height + y, localZ, material);
                chunkData.setBlock(localX - 1, height + y, localZ, material);
                chunkData.setBlock(localX, height + y, localZ + 1, material);
                chunkData.setBlock(localX, height + y, localZ - 1, material);
            }
        }
        chunkData.setBlock(localX, height + spikeHeight + 1, localZ, Material.PACKED_ICE);
    }

    private void addColdTree(ChunkData chunkData, long seed, int localX, int worldX, int localZ, int worldZ, int height) {
        if (height < 82 || height > 178) {
            return;
        }
        double forest = noise(seed + 1001, worldX * 0.028, worldZ * 0.028);
        double scatter = noise(seed + 1002, worldX * 0.31, worldZ * 0.31);
        double valley = glacialValleyMask(seed, worldX, worldZ);
        double requiredForest = valley > 0.45 ? -0.12 : valley > 0.25 ? 0.02 : 0.18;
        double requiredScatter = valley > 0.45 ? 0.68 : valley > 0.25 ? 0.75 : 0.84;
        if (forest < requiredForest || scatter < requiredScatter) {
            return;
        }
        if (localX < 3 || localX > 12 || localZ < 3 || localZ > 12) {
            return;
        }

        int trunkHeight = 5 + Math.floorMod(worldX * 13 + worldZ * 7, valley > 0.35 ? 5 : 3);
        for (int y = 1; y <= trunkHeight; y++) {
            chunkData.setBlock(localX, height + y, localZ, Material.SPRUCE_LOG);
        }
        int top = height + trunkHeight;
        for (int y = top - 3; y <= top + 1; y++) {
            int radius = Math.max(1, 3 - (y - (top - 3)));
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int ax = localX + dx;
                    int az = localZ + dz;
                    if (ax < 0 || ax >= 16 || az < 0 || az >= 16) {
                        continue;
                    }
                    if (Math.abs(dx) + Math.abs(dz) > radius + 1) {
                        continue;
                    }
                    if (dx == 0 && dz == 0 && y <= top) {
                        continue;
                    }
                    chunkData.setBlock(ax, y, az, Material.SPRUCE_LEAVES);
                }
            }
        }
    }

    private boolean isCave(long seed, int x, int y, int z, int surfaceHeight) {
        if (y > surfaceHeight - 12) {
            return false;
        }
        double tunnelA = Math.abs(noise(seed + 301, x * 0.018, y * 0.026, z * 0.018));
        double tunnelB = Math.abs(noise(seed + 302, x * 0.020, y * 0.023, z * 0.020));
        double tunnelMask = noise(seed + 303, x * 0.006, z * 0.006);
        boolean tunnel = tunnelMask > -0.35 && tunnelA < 0.105 && tunnelB < 0.34;

        double chamber = noise(seed + 401, x * 0.0055, y * 0.010, z * 0.0055);
        double chamberGate = noise(seed + 402, x * 0.018, z * 0.018);
        boolean largeChamber = y < 58 && chamberGate > 0.62 && chamber > 0.72;

        double pillarNoise = noise(seed + 403, x * 0.035, z * 0.035);
        boolean keepPillar = largeChamber && pillarNoise > 0.58;
        return (tunnel || largeChamber) && !keepPillar;
    }

    private void addCaveDetails(ChunkData chunkData, long seed, int localX, int worldX, int localZ, int worldZ, int surfaceHeight) {
        for (int y = -48; y < Math.min(CAVE_CEILING, surfaceHeight - 14); y++) {
            Material current = chunkData.getType(localX, y, localZ);
            if (current != Material.AIR) {
                continue;
            }
            Material above = chunkData.getType(localX, y + 1, localZ);
            Material below = chunkData.getType(localX, y - 1, localZ);
            if (isOreReplaceable(above) && noise(seed + 1601, worldX * 0.16, y * 0.11, worldZ * 0.16) > 0.84) {
                chunkData.setBlock(localX, y, localZ, Material.PACKED_ICE);
            }
            if (isOreReplaceable(below) && noise(seed + 1602, worldX * 0.14, y * 0.10, worldZ * 0.14) > 0.86) {
                chunkData.setBlock(localX, y, localZ, Material.PACKED_ICE);
            }
            if (y < 30 && isOreReplaceable(below) && noise(seed + 1603, worldX * 0.035, y * 0.08, worldZ * 0.035) > 0.91) {
                chunkData.setBlock(localX, y - 1, localZ, Material.MAGMA_BLOCK);
                chunkData.setBlock(localX, y, localZ, Material.WATER);
            }
        }
    }

    private double localSlope(long seed, int x, int z) {
        int west = terrainHeight(seed, x - 3, z);
        int east = terrainHeight(seed, x + 3, z);
        int north = terrainHeight(seed, x, z - 3);
        int south = terrainHeight(seed, x, z + 3);
        return Math.max(Math.abs(east - west), Math.abs(south - north));
    }

    private boolean isOreReplaceable(Material material) {
        return material == Material.STONE
                || material == Material.DEEPSLATE
                || material == Material.TUFF
                || material == Material.GRANITE
                || material == Material.DIORITE
                || material == Material.ANDESITE;
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

    private static double noise(long seed, double x, double y, double z) {
        int x0 = fastFloor(x);
        int y0 = fastFloor(y);
        int z0 = fastFloor(z);
        double tx = x - x0;
        double ty = y - y0;
        double tz = z - z0;
        double sx = fade(tx);
        double sy = fade(ty);
        double sz = fade(tz);
        double x00 = lerp(randomUnit(seed, x0, y0, z0), randomUnit(seed, x0 + 1, y0, z0), sx);
        double x10 = lerp(randomUnit(seed, x0, y0 + 1, z0), randomUnit(seed, x0 + 1, y0 + 1, z0), sx);
        double x01 = lerp(randomUnit(seed, x0, y0, z0 + 1), randomUnit(seed, x0 + 1, y0, z0 + 1), sx);
        double x11 = lerp(randomUnit(seed, x0, y0 + 1, z0 + 1), randomUnit(seed, x0 + 1, y0 + 1, z0 + 1), sx);
        return lerp(lerp(x00, x10, sy), lerp(x01, x11, sy), sz);
    }

    private static double randomUnit(long seed, int x, int z) {
        long h = seed ^ (x * 341873128712L) ^ (z * 132897987541L);
        h = (h ^ (h >>> 33)) * 0xff51afd7ed558ccdL;
        h = (h ^ (h >>> 33)) * 0xc4ceb9fe1a85ec53L;
        h = h ^ (h >>> 33);
        return ((h >>> 11) * 0x1.0p-53) * 2.0 - 1.0;
    }

    private static double randomUnit(long seed, int x, int y, int z) {
        long h = seed ^ (x * 341873128712L) ^ (y * 42317861L) ^ (z * 132897987541L);
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

    private static double smoothstep(double edge0, double edge1, double x) {
        double t = Math.max(0.0, Math.min(1.0, (x - edge0) / (edge1 - edge0)));
        return t * t * (3.0 - 2.0 * t);
    }

    private static int fastFloor(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static final class FrozenBiomeProvider extends BiomeProvider {
        @Override
        public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
            if (y < 50) {
                return Biome.DRIPSTONE_CAVES;
            }
            if (y > GLACIER_LINE) {
                return Biome.FROZEN_PEAKS;
            }
            return Biome.SNOWY_SLOPES;
        }

        @Override
        public List<Biome> getBiomes(WorldInfo worldInfo) {
            return List.of(Biome.SNOWY_SLOPES, Biome.FROZEN_PEAKS, Biome.DRIPSTONE_CAVES);
        }
    }
}
