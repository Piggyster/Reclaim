package me.piggyster.reclaim.listener;

import me.piggyster.reclaim.ReclaimPlugin;
import me.piggyster.reclaim.general.ReclaimCategory;
import me.piggyster.reclaim.general.ReclaimService;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import me.piggyster.api.config.ConfigManager;
import me.piggyster.api.service.Service;
import me.piggyster.reclaim.general.ReclaimStatus;

public class PlayerListener implements Listener {

    private ReclaimPlugin plugin;

    public PlayerListener(ReclaimPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getOnlinePlayers().forEach(player -> plugin.getService().loadUser(player.getUniqueId()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getService().loadUser(event.getPlayer().getUniqueId());
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            ReclaimService service = Service.load(ReclaimService.class);
            for(ReclaimCategory category : service.getCategories()) {
                if(service.getStatus(event.getPlayer(), category.getName()) == ReclaimStatus.UNLOCKED) {
                    ConfigManager configManager = plugin.getConfigManager();
                    configManager.getSound("reminder").play(event.getPlayer());
                    configManager.getMessage("reminder").send(event.getPlayer());
                    return;
                }
            }
        }, 400);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getService().unloadUser(event.getPlayer().getUniqueId());
    }
}
