package dk.dino.dinoplugin;

import java.util.UUID;

public class DinoData {
    private final UUID entityUUID;
    private final DinoType type;
    private UUID ownerUUID;
    private boolean tamed;
    private boolean leashed;
    private boolean pegged;
    private int tameProgress;

    public DinoData(UUID entityUUID, DinoType type) {
        this.entityUUID = entityUUID;
        this.type = type;
        this.tamed = false;
        this.leashed = false;
        this.pegged = false;
        this.tameProgress = 0;
    }

    public UUID getEntityUUID() { return entityUUID; }
    public DinoType getType() { return type; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public void setOwnerUUID(UUID ownerUUID) { this.ownerUUID = ownerUUID; }
    public boolean isTamed() { return tamed; }
    public void setTamed(boolean tamed) { this.tamed = tamed; }
    public boolean isLeashed() { return leashed; }
    public void setLeashed(boolean leashed) { this.leashed = leashed; }
    public boolean isPegged() { return pegged; }
    public void setPegged(boolean pegged) { this.pegged = pegged; }
    public int getTameProgress() { return tameProgress; }
    public void setTameProgress(int tameProgress) { this.tameProgress = tameProgress; }
    public void incrementTameProgress() { this.tameProgress++; }
    public boolean isOwnedBy(UUID uuid) { return ownerUUID != null && ownerUUID.equals(uuid); }
}
