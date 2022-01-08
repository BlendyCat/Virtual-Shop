package com.blendycat.virtualshop.gui;


import com.blendycat.virtualshop.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriesGUI extends PaginatedGUI {

    public CategoriesGUI() {
        super(1, "Categories", loadCategories(), 0);

        Button back = new Button(Material.BOOK, ChatColor.WHITE + "Back");
        back.setAction((InventoryClickEvent e)->{
            e.getWhoClicked().openInventory(new MainGUI().getInventory());
        });
        addStaticButton(1, 0, back);
    }

    private static List<Button> loadCategories () {
        List<Button> buttons = new ArrayList<>();
        try {
            Connection conn = Main.getConnection();
            // To select all from categories
            CallableStatement stmt = conn.prepareCall(Main.loadSQL("sql/select/categories.sql"));
            ResultSet rs = stmt.executeQuery();
            // loops through result set to get categories
            while(rs.next()) {
                String name = rs.getString(1);
                String material_name = rs.getString(2);

                // get the material from the material name
                Material display = Material.getMaterial(material_name);

                Button button = new Button(display, ChatColor.WHITE + name);

                // This will make the button open the category gui for the corresponding cateogory
                button.setAction((InventoryClickEvent e)-> {
                    e.getWhoClicked().openInventory(new CategoryGUI(name).getInventory());
                });

                buttons.add(button);
            }
            conn.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return buttons;
    }
}
