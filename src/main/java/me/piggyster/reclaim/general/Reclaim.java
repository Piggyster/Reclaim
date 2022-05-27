package me.piggyster.reclaim.general;

import org.bukkit.configuration.ConfigurationSection;
import me.piggyster.api.color.ColorAPI;
import me.piggyster.api.menu.item.SimpleItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reclaim {

    private String name;
    private int weight;
    private String color;
    private String altColor;
    private List<String> commands;
    private Map<String, Integer> rewards;
    private SimpleItem item;


    Reclaim(ConfigurationSection section) {
        name = section.getName();
        weight = section.getInt("weight");
        color = ColorAPI.process(section.getString("color"));
        altColor = ColorAPI.process(section.getString("alt_color"));
        rewards = new HashMap<>();
        for(String string : section.getStringList("rewards")) {
            String[] split = string.split(";");
            if(split.length == 2) {
                int amount = Integer.parseInt(split[1]);
                rewards.put(split[0], amount);
            } else {
                rewards.put(string, 1);
            }
        }
        commands = section.getStringList("commands");
        item = SimpleItem.fromSection(section.getConfigurationSection("item"));
    }

    public int getWeight() {
        return weight;
    }

    public String getName() {
        return name;
    }

    public List<String> getCommands() {
        return commands;
    }

    public Map<String, Integer> getRewards() {
        return rewards;
    }

    public String getAltColor() {
        return altColor;
    }

    public SimpleItem getItem() {
        return item.clone();
    }

    public String getColor() {
        return color;
    }
}
