package me.piggyster.reclaim.general;

import me.piggyster.reclaim.ReclaimPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import me.piggyster.api.service.PluginService;
import me.piggyster.reclaim.data.ReclaimDatabase;

import java.util.*;

public class ReclaimService implements PluginService<ReclaimPlugin> {

    private ReclaimPlugin plugin;
    private ReclaimDatabase database;
    private Map<Integer, ReclaimCategory> categories;
    private Map<UUID, Set<String>> data;

    public ReclaimService(ReclaimPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        database = new ReclaimDatabase(plugin);
        data = new HashMap<>();
        FileConfiguration config = plugin.getConfigManager().getConfig("config");
        ConfigurationSection reclaim = config.getConfigurationSection("reclaims");
        categories = new HashMap<>();
        for(String node : reclaim.getKeys(false)) {
            ConfigurationSection categorySection = reclaim.getConfigurationSection(node);
            ReclaimCategory category = new ReclaimCategory(categorySection);
            categories.put(category.getSlot(), category);
        }
    }

    public ReclaimCategory getCategory(String name) {
        for(ReclaimCategory category : categories.values()) {
            if(category.getName().equals(name)) return category;
        }
        return null;
    }

    public List<ReclaimCategory> getCategories() {
        return new ArrayList<>(categories.values());
    }

    public Reclaim getUnlocked(Player player, String category) {
        for(Reclaim reclaim : getCategory(category).getReclaims()) {
            if(player.hasPermission("reclaim." + reclaim.getName().toLowerCase())) {
                return reclaim;
            }
        }
        return null;
    }

    public ReclaimStatus getStatus(Player player, String category) {
        if(getUnlocked(player, category) == null) return ReclaimStatus.LOCKED;

        if(data.containsKey(player.getUniqueId()) && data.get(player.getUniqueId()).contains(category)) {
            return ReclaimStatus.USED;
        }
        for(Reclaim reclaim : getCategory(category).getReclaims()) {
            if(player.hasPermission("reclaim." + reclaim.getName().toLowerCase())) {
                return ReclaimStatus.UNLOCKED;
            }
        }
        return ReclaimStatus.LOCKED;
    }

    public void redeem(Player player, String category) {
        database.insertReclaim(player.getUniqueId(), category);
        Set<String> set = data.getOrDefault(player.getUniqueId(), new HashSet<>());
        set.add(category);
        data.put(player.getUniqueId(), set);
    }

    public void shutdown() {
        database.close();
    }

    public ReclaimPlugin getPlugin() {
        return plugin;
    }

    public void loadUser(UUID player) {
        database.loadReclaims(player, data);
    }

    public void unloadUser(UUID player) {
        data.remove(player);
    }
}
