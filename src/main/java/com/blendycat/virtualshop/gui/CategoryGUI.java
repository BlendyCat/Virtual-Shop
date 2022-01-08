package com.blendycat.virtualshop.gui;


import com.blendycat.virtualshop.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryGUI extends PaginatedGUI {

    private static final String LORE = ChatColor.translateAlternateColorCodes('&', "&b%d &7listings");
    private static final String LORE1 = ChatColor.translateAlternateColorCodes('&', "&7starting at &a%s");

    public CategoryGUI(String categoryName) {
        super(5, categoryName, loadCategories(categoryName), 0);

        Button back = new Button(Material.BOOK, ChatColor.WHITE + "Back");
        back.setAction((InventoryClickEvent e)->{
            e.getWhoClicked().openInventory(new CategoriesGUI().getInventory());
        });
        addStaticButton(5, 0, back);
    }

    private static List<Button> loadCategories (String categoryName) {
        List<Button> buttons = new ArrayList<>();
        try {
            Connection conn = Main.getConnection();
            // To select all from categories
            CallableStatement stmt = conn.prepareCall(Main.loadSQL("sql/select/materials_in_category.sql"));
            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();
            // loops through result set to get categories
            while(rs.next()) {
                String material_name = rs.getString(1);
                int count = rs.getInt(2);
                double lowestPrice = rs.getDouble(3);
                // get the material from the material name
                Material display = Material.getMaterial(material_name);

                Button button = new Button(display);
                button.setLore(
                        String.format(LORE, count),
                        String.format(LORE1, Main.formatDollar(lowestPrice))
                );
                if(display != null) {
                    // This will make the button open the category gui for the corresponding cateogory
                    button.setAction((InventoryClickEvent e) -> {
                        e.getWhoClicked().openInventory(new ListingsGUI(categoryName, display).getInventory());
                    });

                    buttons.add(button);
                }
            }
            conn.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return buttons;
    }
}
