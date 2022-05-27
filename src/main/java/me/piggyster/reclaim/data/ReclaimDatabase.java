package me.piggyster.reclaim.data;

import me.piggyster.reclaim.ReclaimPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ReclaimDatabase {

    private String type;
    private ReclaimPlugin plugin;
    private Connection connection;

    public ReclaimDatabase(ReclaimPlugin plugin) {
        this.plugin = plugin;
        type = plugin.getConfigManager().getConfig("config").getString("database.type");
        try {
            initialize();
        } catch(SQLException | NullPointerException | ClassNotFoundException ex) {
            ex.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }



    public void loadReclaims(UUID player, Map<UUID, Set<String>> map) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement pt = connection.prepareStatement("SELECT * FROM reclaims WHERE uuid = ?;");
                pt.setString(1, player.toString());
                ResultSet rs = pt.executeQuery();
                Set<String> set = new HashSet<>();
                while(rs.next()) {
                    String reclaim = rs.getString("reclaim");
                    set.add(reclaim);
                }
                if(!set.isEmpty()) {
                    map.put(player, set);
                }
            } catch(SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    public void insertReclaim(UUID player, String reclaim) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
           try {
               PreparedStatement ps = connection.prepareStatement("INSERT INTO reclaims(uuid, reclaim) VALUES(?, ?);");
               ps.setString(1, player.toString());
               ps.setString(2, reclaim);
               ps.execute();
           } catch(SQLException ex) {
               ex.printStackTrace();
           }
        });
    }

    public void close() {
        try {
            connection.close();
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void initialize() throws SQLException, NullPointerException, ClassNotFoundException {
        if(type.equalsIgnoreCase("mysql")) {
            ConfigurationSection section = plugin.getConfigManager().getConfig("config").getConfigurationSection("database.credentials");
            String host = section.getString("host");
            int port = section.getInt("port");
            String database = section.getString("database");
            String username = section.getString("username");
            String password = section.getString("password");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username, password);
        } else if(type.equalsIgnoreCase("h2")) {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:" + plugin.getDataFolder().getAbsolutePath() + "/storage");

        }

        if(connection == null) {
            throw new SQLException("Cannot create connection.");
        }
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS reclaims(uuid VARCHAR(36), reclaim VARCHAR(32));").execute();
    }
}
