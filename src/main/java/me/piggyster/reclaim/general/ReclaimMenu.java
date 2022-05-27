package me.piggyster.reclaim.general;

import me.piggyster.reclaim.ReclaimPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import me.piggyster.api.color.ColorAPI;
import me.piggyster.api.menu.Menu;
import me.piggyster.api.menu.item.SimpleItem;
import me.piggyster.api.service.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReclaimMenu extends Menu {

    private static int size;
    private static SimpleItem spacer;
    private static String title;
    private static boolean loaded;
    private static String itemName;
    private static List<String> itemLore;
    private static String itemElement;
    private static SimpleItem notAvailable;
    private static SimpleItem notAvailableTemp;
    private static long notAvailableTempDuration;
    private static SimpleItem alreadyUsedTemp;
    private static long alreadyUsedTempDuration;
    private static Map<ReclaimStatus, String> statuses;
    private ReclaimPlugin plugin;

    public ReclaimMenu(ReclaimPlugin plugin, Player owner) {
        super(plugin, owner);
        this.plugin = plugin;
        if(!loaded) {
            loaded = true;
            ConfigurationSection menuSection = plugin.getConfigManager().getConfig("config").getConfigurationSection("menu");
            size = menuSection.getInt("size");
            title = menuSection.getString("title");
            ConfigurationSection itemSection = menuSection.getConfigurationSection("items");
            spacer = SimpleItem.fromSection(itemSection.getConfigurationSection("spacer"));
            itemName = itemSection.getString("reclaim.name");
            itemLore = itemSection.getStringList("reclaim.lore");
            itemElement = itemSection.getString("reclaim.element");
            notAvailable = SimpleItem.fromSection(itemSection.getConfigurationSection("not_available"));
            alreadyUsedTemp = SimpleItem.fromSection(itemSection.getConfigurationSection("already_used_temp"));
            alreadyUsedTempDuration = itemSection.getLong("already_used_temp.duration");
            notAvailableTemp = SimpleItem.fromSection(itemSection.getConfigurationSection("not_available_temp"));
            notAvailableTempDuration = itemSection.getLong("not_available_temp.duration");
            ConfigurationSection statusSection = plugin.getConfigManager().getConfig("config").getConfigurationSection("settings.status");
            statuses = new HashMap<>();
            for(String node : statusSection.getKeys(false)) {
                statuses.put(ReclaimStatus.valueOf(node), statusSection.getString(node));
            }
        }
    }

    @Override
    public void formatItems() {
        for(int i = 0; i < getSize(); i++) {
            setItem(spacer, null, i);
        }
        ReclaimService service = Service.load(ReclaimService.class);
        for(ReclaimCategory category : service.getCategories()) {
            ReclaimStatus status = service.getStatus(owner, category.getName());
            if(status == ReclaimStatus.LOCKED) {
                setItem(notAvailable.clone().setPlaceholder("%category%", category.getCapitalized()), "locked", category.getSlot());
            } else {
                Reclaim reclaim = service.getUnlocked(owner, category.getName());
                SimpleItem item = reclaim.getItem().setName(itemName);
                List<String> rewards = new ArrayList<>();
                for(Map.Entry<String, Integer> entry : reclaim.getRewards().entrySet()) {
                    rewards.add(itemElement.replace("%color%", reclaim.getColor()).replace("%altcolor%", reclaim.getAltColor()).replace("%reward%", entry.getKey()).replace("%amount%", entry.getValue() + ""));
                }
                rewards = ColorAPI.process(rewards);
                for (String string : itemLore) {
                    if (string.equals("%elements%")) {
                        for (String reward : rewards) {
                            item.addLore(reward);
                        }
                    } else {
                        item.addLore(string);
                    }
                }
                item.setPlaceholder("%color%", reclaim.getColor()).setPlaceholder("%altcolor%", reclaim.getAltColor()).setPlaceholder("%name%", reclaim.getName()).setPlaceholder("%status%", statuses.getOrDefault(status, status.getDefaultDisplay()));
                String key = status == ReclaimStatus.USED ? "used" : category.getName() + "_" + reclaim.getName();
                setItem(item, key, category.getSlot());
            }
        }
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if(event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }
        String key = keys.get(event.getSlot());
        if(key == null) {
            return;
        }
        if(key.equals("locked")) {
            setTempItem(notAvailableTemp, event.getSlot(), notAvailableTempDuration);
            return;
        }
        if(key.equals("used")) {
            setTempItem(alreadyUsedTemp, event.getSlot(), alreadyUsedTempDuration);
            return;
        }

        ReclaimService service = Service.grab(ReclaimService.class);

        String stringCategory = key.split("_")[0];
        String stringReclaim = key.split("_")[1];

        ReclaimCategory category = service.getCategory(stringCategory);
        Reclaim reclaim = category.getReclaim(stringReclaim);
        service.redeem(owner, stringCategory);
        for(String command : reclaim.getCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", owner.getName()));
        }
        plugin.getConfigManager().getMessage("claimed").setPlaceholder("%color%", reclaim.getColor()).setPlaceholder("%altcolor%", reclaim.getAltColor()).setPlaceholder("%reclaim%", reclaim.getName()).send(owner);
        owner.closeInventory();
    }
}
