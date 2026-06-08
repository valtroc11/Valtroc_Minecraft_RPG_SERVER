package mx.servidro.rpg;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

final class PlayerProfile {
    private String baseClass;
    private String specialization;
    private int level = 1;
    private int classExperience;
    private int specializationLevel;
    private boolean starterKitClaimed;
    private final Map<String, Integer> professionLevels = new LinkedHashMap<>();
    private final Map<String, Integer> professionExperience = new LinkedHashMap<>();
    private final Set<String> claimedGuides = new LinkedHashSet<>();
    private String activeClassXpFlaskTier;
    private long activeClassXpFlaskExpiresAt;

    PlayerProfile() {
        for (Profession profession : Profession.values()) {
            professionLevels.put(profession.id(), 1);
            professionExperience.put(profession.id(), 0);
        }
    }

    String baseClass() {
        return baseClass;
    }

    void setBaseClass(String baseClass) {
        this.baseClass = baseClass;
    }

    String specialization() {
        return specialization;
    }

    void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    int level() {
        return level;
    }

    void addLevels(int amount, int maxLevel) {
        level = Math.min(maxLevel, Math.max(1, level + amount));
    }

    int classExperience() {
        return classExperience;
    }

    int addClassExperience(int amount, int baseRequired, int maxLevel) {
        if (amount <= 0) {
            return 0;
        }
        int levelsGained = 0;
        int experience = classExperience + amount;
        while (level < maxLevel && experience >= requiredClassExperience(level, baseRequired)) {
            experience -= requiredClassExperience(level, baseRequired);
            level++;
            levelsGained++;
        }
        if (level >= maxLevel) {
            experience = 0;
        }
        classExperience = experience;
        return levelsGained;
    }

    int requiredClassExperience(int baseRequired) {
        return requiredClassExperience(level, baseRequired);
    }

    private int requiredClassExperience(int level, int baseRequired) {
        return Math.max(1, baseRequired) * Math.max(1, level);
    }

    int specializationLevel() {
        return specializationLevel;
    }

    boolean starterKitClaimed() {
        return starterKitClaimed;
    }

    void setStarterKitClaimed(boolean starterKitClaimed) {
        this.starterKitClaimed = starterKitClaimed;
    }

    void resetSpecializationLevel() {
        specializationLevel = 0;
    }

    void resetForTesting() {
        baseClass = null;
        specialization = null;
        level = 1;
        classExperience = 0;
        specializationLevel = 0;
        starterKitClaimed = false;
        claimedGuides.clear();
        activeClassXpFlaskTier = null;
        activeClassXpFlaskExpiresAt = 0L;
    }

    int professionLevel(String profession) {
        return professionLevels.getOrDefault(profession, 1);
    }

    Map<String, Integer> professionLevels() {
        return Map.copyOf(professionLevels);
    }

    int professionExperience(String profession) {
        return professionExperience.getOrDefault(profession, 0);
    }

    boolean hasClaimedGuide(String guideId) {
        return claimedGuides.contains(guideId);
    }

    boolean claimGuide(String guideId) {
        return claimedGuides.add(guideId);
    }

    Set<String> claimedGuides() {
        return Set.copyOf(claimedGuides);
    }

    void setLoadedClaimedGuides(Set<String> guideIds) {
        claimedGuides.clear();
        claimedGuides.addAll(guideIds);
    }

    String activeClassXpFlaskTier() {
        return activeClassXpFlaskTier;
    }

    long activeClassXpFlaskExpiresAt() {
        return activeClassXpFlaskExpiresAt;
    }

    boolean hasActiveClassXpFlask(long now) {
        return activeClassXpFlaskTier != null && activeClassXpFlaskExpiresAt > now;
    }

    void activateClassXpFlask(String tierId, long expiresAt) {
        activeClassXpFlaskTier = tierId;
        activeClassXpFlaskExpiresAt = expiresAt;
    }

    void clearActiveClassXpFlask() {
        activeClassXpFlaskTier = null;
        activeClassXpFlaskExpiresAt = 0L;
    }

    void setLoadedClassXpFlask(String tierId, long expiresAt) {
        activeClassXpFlaskTier = tierId;
        activeClassXpFlaskExpiresAt = expiresAt;
    }

    void addProfessionLevels(String profession, int amount, int maxLevel) {
        professionLevels.computeIfPresent(profession,
                (ignored, level) -> Math.min(maxLevel, Math.max(1, level + amount)));
    }

    void setProfessionProgress(String profession, int level, int experience, int maxLevel) {
        if (professionLevels.containsKey(profession)) {
            int cappedLevel = Math.min(maxLevel, Math.max(1, level));
            professionLevels.put(profession, cappedLevel);
            professionExperience.put(profession, cappedLevel >= maxLevel ? 0 : Math.max(0, experience));
        }
    }

    void setLoadedProfessionLevel(String profession, int professionLevel, int maxLevel) {
        if (professionLevels.containsKey(profession)) {
            professionLevels.put(profession, Math.min(maxLevel, Math.max(1, professionLevel)));
        }
    }

    int addProfessionExperience(String profession, int amount, int baseRequired, int maxLevel) {
        if (!professionLevels.containsKey(profession) || amount <= 0) {
            return 0;
        }
        int levelsGained = 0;
        int experience = professionExperience.get(profession) + amount;
        int level = professionLevels.get(profession);
        while (level < maxLevel && experience >= requiredExperience(level, baseRequired)) {
            experience -= requiredExperience(level, baseRequired);
            level++;
            levelsGained++;
        }
        if (level >= maxLevel) {
            experience = 0;
        }
        professionLevels.put(profession, level);
        professionExperience.put(profession, experience);
        return levelsGained;
    }

    int requiredExperience(String profession, int baseRequired) {
        return requiredExperience(professionLevel(profession), baseRequired);
    }

    private int requiredExperience(int level, int baseRequired) {
        return Math.max(1, baseRequired) * Math.max(1, level);
    }

    void setLoadedValues(String baseClass, String specialization, int level, int classExperience, int specializationLevel,
            boolean starterKitClaimed, int maxLevel, int maxSpecializationLevel) {
        this.baseClass = baseClass;
        this.specialization = specialization;
        this.level = Math.min(maxLevel, Math.max(1, level));
        this.classExperience = this.level >= maxLevel ? 0 : Math.max(0, classExperience);
        this.specializationLevel = Math.min(maxSpecializationLevel, Math.max(0, specializationLevel));
        this.starterKitClaimed = starterKitClaimed;
    }
}
