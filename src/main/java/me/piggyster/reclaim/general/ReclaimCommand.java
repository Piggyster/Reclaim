package me.piggyster.reclaim.general;

import me.piggyster.reclaim.ReclaimPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.piggyster.api.command.Command;

public class ReclaimCommand extends Command {

    private ReclaimPlugin plugin;

    public ReclaimCommand(ReclaimPlugin plugin) {
        super(plugin, "reclaim");
        this.plugin = plugin;
    }

    public void run(CommandSender sender, String[] args) {
        if(!(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;
        ReclaimMenu menu = new ReclaimMenu(plugin, player);
        menu.open();
    }
}
