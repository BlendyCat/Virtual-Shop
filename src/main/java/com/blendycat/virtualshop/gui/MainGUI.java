package com.blendycat.virtualshop.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MainGUI extends InventoryGUI {

    public MainGUI() {
        super(9, "Virtual Shop");

        Button buy = new Button(Material.EMERALD, ChatColor.WHITE + "Buy");
        buy.setAction((InventoryClickEvent e) -> {
            e.getWhoClicked().openInventory(new CategoriesGUI().getInventory());
        });

        Button sell = new Button(Material.CHEST, ChatColor.WHITE + "Sell");
        sell.setAction((InventoryClickEvent e)-> {
            e.getWhoClicked().openInventory(new SellGUI().getInventory());
        });

        addButton(0, 3, buy);
        addButton(0, 5, sell);
    }
}
