package dk.dino.dinoplugin;

import org.bukkit.entity.EntityType;

public enum DinoType {
    T_REX("T-Rex", EntityType.RAVAGER, 200, 15, 0.35, false, "&c"),
    TRICERATOPS("Triceratops", EntityType.IRON_GOLEM, 150, 10, 0.25, false, "&e"),
    RAPTOR("Raptor", EntityType.WOLF, 80, 8, 0.45, false, "&6"),
    BRACHIOSAURUS("Brachiosaurus", EntityType.CAMEL, 300, 5, 0.15, false, "&a"),
    PTERODACTYL("Pterodactyl", EntityType.PHANTOM, 60, 6, 0.5, true, "&b"),
    QUETZALCOATLUS("Quetzalcoatlus", EntityType.PHANTOM, 150, 12, 0.55, true, "&5");

    private final String displayName;
    private final EntityType entityType;
    private final double health;
    private final double damage;
    private final double speed;
    private final boolean canFly;
    private final String color;

    DinoType(String displayName, EntityType entityType, double health, double damage, double speed, boolean canFly, String color) {
        this.displayName = displayName;
        this.entityType = entityType;
        this.health = health;
        this.damage = damage;
        this.speed = speed;
        this.canFly = canFly;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public EntityType getEntityType() { return entityType; }
    public double getHealth() { return health; }
    public double getDamage() { return damage; }
    public double getSpeed() { return speed; }
    public boolean canFly() { return canFly; }
    public String getColoredName() { return color + displayName; }
}
