package com.blendycat.virtualshop.commands;

import com.blendycat.virtualshop.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.sql.*;
import java.util.List;

public class UpdateCategoriesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender.hasPermission("shops.command.updatecategories")) {
            try {
                Connection conn = Main.getConnection();
                long before = System.currentTimeMillis();
                // Drop and create new tables
                Statement stmt = conn.createStatement();
                stmt.addBatch("DROP TABLE `categories`;");
                stmt.addBatch("DROP TABLE `material_index`;");
                stmt.addBatch(Main.loadSQL("sql/tables/categories.sql"));
                stmt.addBatch(Main.loadSQL("sql/tables/material_index.sql"));
                stmt.executeBatch();

                FileConfiguration config = Main.instance.getConfig();
                ConfigurationSection categories = config.getConfigurationSection("categories");
                if(categories != null) {
                    PreparedStatement materialStmt = conn.prepareStatement(Main.loadSQL("sql/insert/material_index.sql"));
                    PreparedStatement categoryStmt = conn.prepareStatement(Main.loadSQL("sql/insert/categories.sql"));
                    int materialBatchSize = 0;
                    for (String category : categories.getKeys(false)) {
                        String icon = categories.getString(category + ".icon");
                        if(icon == null) {
                            conn.close();
                            sender.sendMessage(ChatColor.RED + "icon is not defined for category '" + category + "'!");
                            return true;
                        }
                        categoryStmt.setString(1, category);
                        categoryStmt.setString(2, icon.toUpperCase());
                        categoryStmt.addBatch();

                        List<String> items = categories.getStringList(category + ".items");
                        for(String material : items) {
                            if(Material.getMaterial(material) == null) {
                                Main.instance.getLogger().info(material + " is not a valid material! Check config and update categories");
                            }
                            materialStmt.setString(1, material.toUpperCase());
                            materialStmt.setString(2, category);
                            materialStmt.addBatch();
                            materialBatchSize++;
                            if(materialBatchSize >= 80) {
                                materialStmt.executeBatch();
                                materialStmt.clearBatch();
                                materialBatchSize = 0;
                            }
                        }
                    }
                    categoryStmt.executeBatch();
                    materialStmt.executeBatch();
                    conn.close();
                    long after = System.currentTimeMillis();
                    long ms = after - before;
                    sender.sendMessage(ChatColor.GREEN + "Complete in " + ms + " ms!");
                } else {
                    sender.sendMessage(ChatColor.RED + "Categories not defined in config!");
                    conn.close();
                    return true;
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        } else {
            sender.sendMessage(ChatColor.RED + "No permission!");
        }
        return true;
    }
}
