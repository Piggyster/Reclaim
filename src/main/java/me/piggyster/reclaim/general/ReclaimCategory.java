package me.piggyster.reclaim.general;

import org.apache.commons.lang.WordUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ReclaimCategory {

    private String name;
    private TreeMap<Integer, Reclaim> reclaims;
    private int slot;

    ReclaimCategory(ConfigurationSection section) {
        reclaims = new TreeMap<>();
        name = section.getName();
        for(String node : section.getKeys(false)) {
            if(node.equalsIgnoreCase("slot")) {
                slot = section.getInt(node);
            } else {
                ConfigurationSection reclaimSection = section.getConfigurationSection(node);
                Reclaim reclaim = new Reclaim(reclaimSection);
                reclaims.put(reclaim.getWeight(), reclaim);
            }
        }
    }

    public String getName() {
        return name;
    }

    public int getSlot() {
        return slot;
    }

    public Reclaim getReclaim(String string) {
        for(Reclaim reclaim : reclaims.values()) {
            if(reclaim.getName().equals(string)) return reclaim;
        }
        return null;
    }

    public String getCapitalized() {
        return WordUtils.capitalizeFully(getName());
    }

    public List<Reclaim> getReclaims() {
        return new ArrayList<>(reclaims.values());
    }
}
