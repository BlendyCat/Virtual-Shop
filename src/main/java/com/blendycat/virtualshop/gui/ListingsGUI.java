package com.blendycat.virtualshop.gui;

import com.blendycat.virtualshop.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ListingsGUI extends PaginatedGUI {

    public ListingsGUI(String category, Material material) {
        super(1, material.name(), loadItems(material.name(), category), 0);

        Button back = new Button(Material.BOOK, ChatColor.WHITE + "Back");
        back.setAction((InventoryClickEvent e)-> {
            e.getWhoClicked().openInventory(new CategoryGUI(category).getInventory());
        });
        addStaticButton(1, 0, back);
    }

    private static List<Button> loadItems(String materialString, String categoryName) {
        List<Button> buttons = new ArrayList<>();
        try {
            Connection conn = Main.getConnection();
            PreparedStatement stmt = conn.prepareStatement(Main.loadSQL("sql/select/listings_per_material.sql"));
            stmt.setString(1, materialString);

            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                int id = rs.getInt(1);
                String uuid = rs.getString(2);
                OfflinePlayer seller = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                String materialName = rs.getString(3);
                double price = rs.getDouble(4);
                int quantity = rs.getInt(5);

                Material material = Material.getMaterial(materialName);

                Button button = new Button(material);
                button.setLore(
                        String.format(ChatColor.translateAlternateColorCodes('&', "&7Seller&8: &6%s"), seller.getName()),
                        String.format(ChatColor.translateAlternateColorCodes('&', "&7Price&8: &a%s&8/&7ea."), Main.formatDollar(price)),
                        String.format(ChatColor.translateAlternateColorCodes('&', "&7Quantity&8: &b%s"), quantity)
                );
                button.setAction((e -> {
                    HumanEntity player = e.getWhoClicked();

                    player.openInventory(new BuyGUI(id, material, categoryName, price, quantity).getInventory());
                }));
                buttons.add(button);
            }
            conn.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return buttons;
    }
}