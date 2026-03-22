package dk.dino.dinoplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class DinoPlugin extends JavaPlugin {

    private static DinoPlugin instance;
    private DinoManager dinoManager;

    @Override
    public void onEnable() {
        instance = this;
        dinoManager = new DinoManager(this);

        getServer().getPluginManager().registerEvents(new DinoListener(this), this);
        getCommand("dino").setExecutor(new DinoCommand(this));

        getLogger().info("DinoPlugin aktiveret! 🦖");
    }

    @Override
    public void onDisable() {
        getLogger().info("DinoPlugin deaktiveret!");
    }

    public static DinoPlugin getInstance() { return instance; }
    public DinoManager getDinoManager() { return dinoManager; }
}
