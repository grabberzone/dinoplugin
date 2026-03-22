package dk.dino.dinoplugin;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class DinoCommand implements CommandExecutor {

    private final DinoPlugin plugin;
    private final DinoManager manager;

    public DinoCommand(DinoPlugin plugin) {
        this.plugin = plugin;
        this.manager = plugin.getDinoManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Kun spillere kan bruge denne kommando!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "spawn" -> {
                if (!player.hasPermission("dino.admin")) {
                    player.sendMessage(ChatColor.RED + "Du har ikke tilladelse!");
                    return true;
                }
                DinoType type = args.length > 1 ? getDinoType(args[1]) : DinoType.values()[new java.util.Random().nextInt(DinoType.values().length)];
                manager.spawnDino(type, player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Spawnede en " + ChatColor.translateAlternateColorCodes('&', type.getColoredName()) + ChatColor.GREEN + " ved dig!");
            }
            case "list" -> {
                int count = 0;
                for (Map.Entry<UUID, DinoData> entry : manager.getAllDinos().entrySet()) {
                    if (entry.getValue().isOwnedBy(player.getUniqueId())) {
                        DinoData data = entry.getValue();
                        String status = data.isPegged() ? ChatColor.AQUA + "(bundet til pæl)" :
                                        data.isLeashed() ? ChatColor.YELLOW + "(i snor)" : ChatColor.GRAY + "(fri)";
                        player.sendMessage(ChatColor.GRAY + "- " + ChatColor.YELLOW + data.getType().getDisplayName() + " " + status);
                        count++;
                    }
                }
                if (count == 0) player.sendMessage(ChatColor.RED + "Du har ingen tamede dinosaurer.");
                else player.sendMessage(ChatColor.GREEN + "Du har " + ChatColor.YELLOW + count + ChatColor.GREEN + " tamede dinosaurer.");
            }
            case "release" -> {
                Entity nearest = null;
                double nearestDist = 6;
                for (Map.Entry<UUID, DinoData> entry : manager.getAllDinos().entrySet()) {
                    if (entry.getValue().isOwnedBy(player.getUniqueId())) {
                        Entity e = plugin.getServer().getEntity(entry.getKey());
                        if (e != null) {
                            double dist = e.getLocation().distance(player.getLocation());
                            if (dist < nearestDist) {
                                nearest = e;
                                nearestDist = dist;
                            }
                        }
                    }
                }
                if (nearest == null) {
                    player.sendMessage(ChatColor.RED + "Ingen af dine dinosaurer er inden for 5 blokke!");
                } else {
                    manager.removeDino(nearest.getUniqueId());
                    player.sendMessage(ChatColor.RED + "Du frigav din dinosaur.");
                }
            }
            case "count" -> {
                if (!player.hasPermission("dino.admin")) {
                    player.sendMessage(ChatColor.RED + "Du har ikke tilladelse!");
                    return true;
                }
                long total = manager.getAllDinos().size();
                long tamed = manager.getAllDinos().values().stream().filter(DinoData::isTamed).count();
                player.sendMessage(ChatColor.YELLOW + "Der er " + ChatColor.GREEN + total + ChatColor.YELLOW + " dinosaurer (" + ChatColor.GREEN + tamed + ChatColor.YELLOW + " tamed).");
            }
            case "clear" -> {
                if (!player.hasPermission("dino.admin")) {
                    player.sendMessage(ChatColor.RED + "Du har ikke tilladelse!");
                    return true;
                }
                int count = 0;
                for (UUID uuid : manager.getAllDinos().keySet()) {
                    DinoData data = manager.getDinoData(uuid);
                    if (!data.isTamed()) {
                        Entity e = plugin.getServer().getEntity(uuid);
                        if (e != null) e.remove();
                        count++;
                    }
                }
                manager.getAllDinos().entrySet().removeIf(e -> !e.getValue().isTamed());
                player.sendMessage(ChatColor.RED + "Fjernede " + ChatColor.YELLOW + count + ChatColor.RED + " vilde dinosaurer!");
            }
            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.YELLOW + "=== Dino Kommandoer ===");
        player.sendMessage(ChatColor.GRAY + "/dino list " + ChatColor.WHITE + "- Se dine dinosaurer");
        player.sendMessage(ChatColor.GRAY + "/dino release " + ChatColor.WHITE + "- Frigiv din nærmeste dino");
        if (player.hasPermission("dino.admin")) {
            player.sendMessage(ChatColor.GRAY + "/dino spawn [type] " + ChatColor.WHITE + "- Spawn en dino");
            player.sendMessage(ChatColor.GRAY + "/dino count " + ChatColor.WHITE + "- Tæl dinosaurer");
            player.sendMessage(ChatColor.GRAY + "/dino clear " + ChatColor.WHITE + "- Slet vilde dinos");
        }
    }

    private DinoType getDinoType(String name) {
        for (DinoType type : DinoType.values()) {
            if (type.name().equalsIgnoreCase(name) || type.getDisplayName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return DinoType.values()[new java.util.Random().nextInt(DinoType.values().length)];
    }
}
