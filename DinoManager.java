package dk.dino.dinoplugin;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DinoManager {

    private final DinoPlugin plugin;
    private final Map<UUID, DinoData> dinos = new HashMap<>();
    private final Map<UUID, UUID> playerRiding = new HashMap<>();

    private static final int TAME_REQUIRED = 15;
    private static final int MAX_DINOS = 20;
    private static final int MIN_DISTANCE = 30;
    private static final int MAX_DISTANCE = 100;
    private static final int SPAWN_INTERVAL_TICKS = 20 * 60 * 5;

    public DinoManager(DinoPlugin plugin) {
        this.plugin = plugin;
        startSpawnLoop();
    }

    private void startSpawnLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                spawnRandomDino();
            }
        }.runTaskTimer(plugin, SPAWN_INTERVAL_TICKS, SPAWN_INTERVAL_TICKS);
    }

    public void spawnRandomDino() {
        long count = dinos.values().stream().filter(d -> !d.isTamed()).count();
        if (count >= MAX_DINOS) return;

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) return;

        Player target = players.get(new Random().nextInt(players.size()));
        DinoType type = DinoType.values()[new Random().nextInt(DinoType.values().length)];

        Location loc = getRandomLocation(target.getLocation());
        if (loc == null) return;

        spawnDino(type, loc);
        target.sendActionBar(net.kyori.adventure.text.Component.text(
            ChatColor.RED + "" + ChatColor.BOLD + "⚠ En " + type.getDisplayName() + " er dukket op i nærheden!"
        ));
    }

    public Entity spawnDino(DinoType type, Location loc) {
        Entity entity = loc.getWorld().spawnEntity(loc, type.getEntityType());

        if (entity instanceof LivingEntity living) {
            living.setCustomName(ChatColor.translateAlternateColorCodes('&', type.getColoredName()));
            living.setCustomNameVisible(true);

            // Sæt liv og hastighed via generisk attribut opslag
            for (var attribute : living.getAttributes()) {
                String key = attribute.getAttribute().getKey().getKey();
                if (key.equals("max_health") || key.equals("generic.max_health")) {
                    attribute.setBaseValue(type.getHealth());
                    living.setHealth(type.getHealth());
                }
                if (key.equals("movement_speed") || key.equals("generic.movement_speed")) {
                    attribute.setBaseValue(type.getSpeed());
                }
            }

            if (living instanceof Wolf wolf) {
                wolf.setAngry(true);
            }
        }

        DinoData data = new DinoData(entity.getUniqueId(), type);
        dinos.put(entity.getUniqueId(), data);

        return entity;
    }

    private Location getRandomLocation(Location center) {
        Random rand = new Random();
        double angle = rand.nextDouble() * 2 * Math.PI;
        double dist = MIN_DISTANCE + rand.nextInt(MAX_DISTANCE - MIN_DISTANCE);
        double x = center.getX() + dist * Math.cos(angle);
        double z = center.getZ() + dist * Math.sin(angle);
        Location loc = new Location(center.getWorld(), x, center.getY(), z);
        return center.getWorld().getHighestBlockAt(loc).getLocation().add(0, 1, 0);
    }

    public boolean tryTame(Player player, Entity entity) {
        DinoData data = dinos.get(entity.getUniqueId());
        if (data == null || data.isTamed()) return false;

        if (new Random().nextInt(10) < 3) return false;

        data.incrementTameProgress();
        int progress = data.getTameProgress();

        if (progress >= TAME_REQUIRED) {
            data.setTamed(true);
            data.setOwnerUUID(player.getUniqueId());

            if (entity instanceof LivingEntity living) {
                living.setCustomName(ChatColor.GREEN + "[Tamed] " + ChatColor.WHITE + data.getType().getDisplayName());
            }

            player.sendTitle(
                ChatColor.GREEN + "" + ChatColor.BOLD + "DINOSAUR TAMED!",
                ChatColor.YELLOW + "Du har nu en " + data.getType().getDisplayName() + "!",
                10, 60, 10
            );
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            return true;
        }

        int filled = (int)((double) progress / TAME_REQUIRED * 10);
        StringBuilder bar = new StringBuilder(ChatColor.GOLD + "Taming: ");
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? ChatColor.GREEN + "█" : ChatColor.GRAY + "█");
        }
        bar.append(" " + ChatColor.YELLOW + progress + ChatColor.GRAY + "/" + ChatColor.YELLOW + TAME_REQUIRED);
        player.sendActionBar(net.kyori.adventure.text.Component.text(bar.toString()));
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WOLF_WHINE, 1f, 1f);
        return false;
    }

    public void setRiding(Player player, UUID dinoUUID) { playerRiding.put(player.getUniqueId(), dinoUUID); }
    public void removeRiding(Player player) { playerRiding.remove(player.getUniqueId()); }
    public boolean isRiding(Player player) { return playerRiding.containsKey(player.getUniqueId()); }

    public DinoData getDinoData(UUID uuid) { return dinos.get(uuid); }
    public Map<UUID, DinoData> getAllDinos() { return dinos; }
    public void removeDino(UUID uuid) { dinos.remove(uuid); }
    public int getTameRequired() { return TAME_REQUIRED; }
}
