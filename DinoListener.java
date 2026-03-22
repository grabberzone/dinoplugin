package dk.dino.dinoplugin;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class DinoListener implements Listener {

    private final DinoPlugin plugin;
    private final DinoManager manager;

    public DinoListener(DinoPlugin plugin) {
        this.plugin = plugin;
        this.manager = plugin.getDinoManager();
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        DinoData data = manager.getDinoData(entity.getUniqueId());
        if (data == null) return;

        event.setCancelled(true);

        // Hvis den er tamed og ejet af en anden
        if (data.isTamed() && !data.isOwnedBy(player.getUniqueId())) {
            player.sendActionBar(net.kyori.adventure.text.Component.text(
                ChatColor.RED + "Denne dinosaur tilhører en anden spiller!"
            ));
            return;
        }

        // Snor i hånden = leash
        if (player.getInventory().getItemInMainHand().getType() == org.bukkit.Material.LEAD) {
            if (!data.isTamed() || !data.isOwnedBy(player.getUniqueId())) return;
            if (data.isLeashed()) {
                player.sendActionBar(net.kyori.adventure.text.Component.text(
                    ChatColor.RED + "Din dinosaur er allerede i snor!"
                ));
                return;
            }
            data.setLeashed(true);
            player.sendActionBar(net.kyori.adventure.text.Component.text(
                ChatColor.GREEN + "Du satte en snor på din " + data.getType().getDisplayName() + "!"
            ));
            player.playSound(player.getLocation(), Sound.ENTITY_LEASH_KNOT_PLACE, 1f, 1f);
            return;
        }

        // Tom hånd = fjern snor
        if (player.getInventory().getItemInMainHand().getType() == org.bukkit.Material.AIR
                && data.isTamed() && data.isOwnedBy(player.getUniqueId()) && data.isLeashed()) {
            data.setLeashed(false);
            data.setPegged(false);
            player.sendActionBar(net.kyori.adventure.text.Component.text(
                ChatColor.YELLOW + "Du tog snoren af din dinosaur."
            ));
            player.playSound(player.getLocation(), Sound.ENTITY_LEASH_KNOT_BREAK, 1f, 1f);
            return;
        }

        // Kød i hånden = forsøg på taming
        if (!data.isTamed() && player.getInventory().getItemInMainHand().getType() == org.bukkit.Material.COOKED_BEEF) {
            player.getInventory().getItemInMainHand().setAmount(
                player.getInventory().getItemInMainHand().getAmount() - 1
            );
            boolean tamed = manager.tryTame(player, entity);
            if (!tamed && manager.getDinoData(entity.getUniqueId()) != null) {
                int progress = manager.getDinoData(entity.getUniqueId()).getTameProgress();
                if (progress == manager.getDinoData(entity.getUniqueId()).getTameProgress()) {
                    // Afvist
                    player.sendActionBar(net.kyori.adventure.text.Component.text(
                        ChatColor.RED + "Dinosauren afviste maden! Prøv igen..."
                    ));
                    player.playSound(player.getLocation(), Sound.ENTITY_WOLF_GROWL, 1f, 1f);
                }
            }
            return;
        }

        // Ingen kød ved utamed dino
        if (!data.isTamed()) {
            player.sendActionBar(net.kyori.adventure.text.Component.text(
                ChatColor.RED + "Hold " + ChatColor.YELLOW + "kogt kød" + ChatColor.RED + " i hånden for at tame! " +
                ChatColor.GRAY + "(" + data.getTameProgress() + "/" + manager.getTameRequired() + ")"
            ));
            return;
        }

        // Shift + højreklik = ride/stig af
        if (player.isSneaking() && data.isTamed() && data.isOwnedBy(player.getUniqueId())) {
            if (data.isPegged()) {
                player.sendActionBar(net.kyori.adventure.text.Component.text(
                    ChatColor.RED + "Fjern snoren før du kan ride!"
                ));
                return;
            }

            if (manager.isRiding(player)) {
                // Stig af
                player.leaveVehicle();
                manager.removeRiding(player);
                if (data.getType().canFly()) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }
                player.sendActionBar(net.kyori.adventure.text.Component.text(
                    ChatColor.YELLOW + "Du steg af din dinosaur."
                ));
            } else {
                // Stig på
                entity.addPassenger(player);
                manager.setRiding(player, entity.getUniqueId());
                player.playSound(player.getLocation(), Sound.ENTITY_HORSE_SADDLE, 1f, 1f);

                if (data.getType().canFly()) {
                    player.setAllowFlight(true);
                    player.sendActionBar(net.kyori.adventure.text.Component.text(
                        ChatColor.GREEN + "Du flyver på din " + data.getType().getDisplayName() + "! " +
                        ChatColor.GRAY + "Shift for at lande og stige af."
                    ));
                    player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1f, 1f);
                } else {
                    player.sendActionBar(net.kyori.adventure.text.Component.text(
                        ChatColor.GREEN + "Du rider på din " + data.getType().getDisplayName() + "! " +
                        ChatColor.GRAY + "Shift for at stige af."
                    ));
                }
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        DinoData data = manager.getDinoData(event.getEntity().getUniqueId());
        if (data == null) return;

        if (!data.isTamed()) {
            // Vild dino dræbt
            Player killer = event.getEntity().getKiller();
            if (killer != null) {
                killer.sendTitle(
                    ChatColor.GOLD + "" + ChatColor.BOLD + "DINOSAUR DRÆBT!",
                    ChatColor.YELLOW + "Du dræbte en " + data.getType().getDisplayName() + "!",
                    10, 60, 10
                );
                killer.setLevel(killer.getLevel() + 10);
            }
        } else {
            // Tamed dino dræbt - find ejeren
            if (data.getOwnerUUID() != null) {
                Player owner = Bukkit.getPlayer(data.getOwnerUUID());
                if (owner != null && owner.isOnline()) {
                    if (data.getType().canFly()) {
                        owner.setAllowFlight(false);
                        owner.setFlying(false);
                    }
                    manager.removeRiding(owner);
                    owner.sendTitle(
                        ChatColor.RED + "" + ChatColor.BOLD + "DIN DINOSAUR DØDE!",
                        ChatColor.GRAY + data.getType().getDisplayName() + " er væk for altid...",
                        10, 60, 10
                    );
                    owner.playSound(owner.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 1f);
                }
            }
        }

        manager.removeDino(event.getEntity().getUniqueId());
    }
}
