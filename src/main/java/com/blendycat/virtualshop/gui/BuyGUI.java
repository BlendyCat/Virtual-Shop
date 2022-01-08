package com.blendycat.virtualshop.gui;

import com.blendycat.virtualshop.Main;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class BuyGUI extends InventoryGUI {

    private static final String TITLE = ChatColor.translateAlternateColorCodes('&', "&fAmount&8: &b%d");
    private static final String LORE = ChatColor.translateAlternateColorCodes('&', "&a+&7/&c-&b%d");
    private static final String LORE1 = ChatColor.translateAlternateColorCodes('&', "&7Right click to &aadd");
    private static final String LORE2 = ChatColor.translateAlternateColorCodes('&', "&7Left click to &c subtract");

    private static final String P = ChatColor.translateAlternateColorCodes('&', "&7Amount&8: &b%d");
    private static final String P1 = ChatColor.translateAlternateColorCodes('&', "&7Cost per&8: &3%s");
    private static final String P2 = ChatColor.translateAlternateColorCodes('&', "&7Total&8: &a%s");

    private static final String BUYER_MESSAGE = ChatColor.translateAlternateColorCodes('&',
            "&fSuccessfully bought &7%d &b%s &ffrom &6%s &ffor &a%s &f(You paid &c%s &fin sales tax)");
    private static final String SELLER_MESSAGE = ChatColor.translateAlternateColorCodes('&',
            "&6%s &fjust bought &7%d &b%s &ffrom you for &a%s&f!");

    private int quantity = 0;
    private int maxQuantity;
    private double price;
    private Material material;
    private int id;
    private Economy eco;

    private Button purchase;

    public BuyGUI(int id, Material material, String category, double price, int maxQuantity) {
        super(9, "Select purchase amount:");

        this.maxQuantity = maxQuantity;
        this.price = price;
        this.id = id;
        this.material = material;
        this.eco = Main.getEconomy();

        Button back = new Button(Material.BOOK, ChatColor.WHITE + "Back");
        back.setAction(e ->
                e.getWhoClicked().openInventory(new ListingsGUI(category, material).getInventory())
        );
        addButton(0, back);

        purchase = new Button(Material.EMERALD, ChatColor.GREEN + "Purchase");
        purchase.setLore(
                String.format(P, quantity),
                String.format(P1, Main.formatDollar(price)),
                String.format(P2, Main.formatDollar(price * quantity))
        );
        addButton(8, purchase);

        purchase.setAction(e-> {
            HumanEntity buyer = e.getWhoClicked();
            try {
                Connection conn = Main.getConnection();
                PreparedStatement stmt = conn.prepareStatement(Main.loadSQL("sql/select/listing_by_id.sql"));
                stmt.setInt(1, id);

                ResultSet rs = stmt.executeQuery();
                if(rs.next()) {
                    String uuidString = rs.getString(1);
                    OfflinePlayer seller = Bukkit.getOfflinePlayer(UUID.fromString(uuidString));
                    double updatedPrice = rs.getDouble(3);
                    int updatedMaxQuantity = rs.getInt(4);

                    int quant = Math.min(quantity, updatedMaxQuantity);
                    double balance = eco.getBalance((OfflinePlayer) buyer);
                    double cost = updatedPrice * quant;
                    double tax = cost * Main.getBuyTax();
                    if (balance > cost) {
                        if(balance > cost + tax) {
                            // delete listing
                            if(quant == updatedMaxQuantity) {
                                stmt = conn.prepareStatement(Main.loadSQL("sql/update/delete_listing.sql"));
                                stmt.setInt(1, id);
                            } else { // update listing
                                stmt = conn.prepareStatement(Main.loadSQL("sql/update/listing_quantity.sql"));
                                stmt.setInt(1, maxQuantity - quant);
                                stmt.setInt(2, id);
                            }
                            stmt.execute();
                            HashMap<Integer, ItemStack> returned = buyer.getInventory().addItem(new ItemStack(material, quant));
                            for(ItemStack i : returned.values()) {
                                Location location = buyer.getLocation();
                                buyer.getWorld().dropItem(location, i);
                            }
                            eco.withdrawPlayer((OfflinePlayer) buyer, cost + tax);
                            eco.depositPlayer(seller, cost);

                            buyer.sendMessage(String.format(BUYER_MESSAGE, quant, material.name(), seller.getName(), Main.formatDollar(cost + tax), Main.formatDollar(tax)));
                            buyer.closeInventory();
                            if(seller.isOnline()) {
                                Player sellp = seller.getPlayer();
                                if(sellp != null) {
                                    sellp.sendMessage(String.format(SELLER_MESSAGE, buyer.getName(), quant, material.name(), Main.formatDollar(cost)));
                                }
                            }

                        } else {
                            buyer.closeInventory();
                            buyer.sendMessage(ChatColor.RED + "You don't have enough funds to cover sales tax for this amount!");
                        }
                    } else {
                        buyer.closeInventory();
                        buyer.sendMessage(ChatColor.RED + "The listing price has been updated. You do not have enough funds to purchase that quantity anymore!");
                }
                } else {
                    buyer.closeInventory();
                    buyer.sendMessage(ChatColor.RED + "The listing you were trying to purchase no longer exists!");
                }
                conn.close();
            } catch (SQLException | IOException ex) {
                ex.printStackTrace();
            }
        });

        setQuantity(0);
    }

    private void createAdjustmentButton(int row, int column, int value) {
        Button button = new Button(Material.DARK_PRISMARINE_SLAB, String.format(TITLE, quantity));
        button.setLore(
                String.format(LORE, value),
                LORE1,
                LORE2
        );
        button.setAmount(value);

        button.setAction(e -> {
            switch(e.getClick()) {
                case RIGHT:
                case SHIFT_RIGHT:
                    // This makes sure that the quantity can't be increased past the amount listed as well or past the amount the player can buy
                    setQuantity(Math.min(Math.min(quantity + value, maxQuantity),(int) (eco.getBalance((OfflinePlayer) e.getWhoClicked()) / price)));
                    break;
                case LEFT:
                case SHIFT_LEFT:
                    setQuantity(Math.max(quantity - value, 0));
                    break;
            }
        });

        addButton(row, column, button);
    }

    private void setQuantity(int newQuantity) {
        quantity = newQuantity;
        createAdjustmentButton(0, 1, 64);
        createAdjustmentButton(0, 2, 32);
        createAdjustmentButton(0, 3, 16);
        createAdjustmentButton(0, 4, 8);
        createAdjustmentButton(0, 5, 4);
        createAdjustmentButton(0, 6, 2);
        createAdjustmentButton(0, 7, 1);

        purchase.setLore(
                String.format(P, quantity),
                String.format(P1, Main.formatDollar(price)),
                String.format(P2, Main.formatDollar(price * quantity))
        );
        addButton(8, purchase);
    }
}
