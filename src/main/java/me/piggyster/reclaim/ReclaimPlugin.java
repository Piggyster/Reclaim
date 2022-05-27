package me.piggyster.reclaim;

import org.bukkit.plugin.java.JavaPlugin;
import me.piggyster.api.config.ConfigManager;
import me.piggyster.api.service.Service;
import me.piggyster.reclaim.general.ReclaimCommand;
import me.piggyster.reclaim.general.ReclaimService;
import me.piggyster.reclaim.listener.PlayerListener;

public class ReclaimPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private ReclaimService service;

    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.createConfig("config");
        configManager.setMessageSection("messages");
        configManager.setSoundSection("sounds");
        new ReclaimCommand(this);
        service = Service.provide(ReclaimService.class, new ReclaimService(this));
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    public void onDisable() {
        service.shutdown();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ReclaimService getService() {
        return service;
    }

}
