package com.blendycat.virtualshop.gui;

import com.blendycat.virtualshop.Main;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class SellGUI extends InventoryGUI {

    private static final String LORE1 = ChatColor.translateAlternateColorCodes('&', "&7Right click to &aadd");
    private static final String LORE2 = ChatColor.translateAlternateColorCodes('&', "&7Left click to &c subtract");
    private static final String LORE3 = ChatColor.translateAlternateColorCodes( '&', "&7Shift click for &b%s");
    private static final String ADJUST_PRICE = ChatColor.translateAlternateColorCodes('&', "&a+&7/&c-&b%s");

    private double price;
    private boolean sell = false;

    private Material currentMaterial;

    private Button confirmButton;

    public SellGUI() {
        super(36, "Sell items");

        Button back = new Button(Material.BOOK, ChatColor.WHITE + "Back");
        back.setAction((InventoryClickEvent e)-> e.getWhoClicked().openInventory(new MainGUI().getInventory()));
        addButton(3, 0, back);

        Button cancelButton = new Button(Material.LAVA_BUCKET, ChatColor.RED + "Cancel");
        cancelButton.setAction(x->cancel(this.getInventory().getViewers().get(0)));
        addButton(3, 1, cancelButton);

        confirmButton = new Button(Material.EMERALD, ChatColor.GREEN + "OK");
        confirmButton.setAction((InventoryClickEvent e)->confirm(e.getWhoClicked()));
        addButton(3, 8, confirmButton);

        setPrice(0);
    }

    private void createPriceAdjustmentButton(int row, int column, Material material, double value, double shiftValue) {
        Button button = new Button(material, ChatColor.WHITE + "Price: " + ChatColor.GREEN + Main.formatDollar(price));
        button.setLore(
                String.format(ADJUST_PRICE, Main.formatDollar(value)),
                LORE1,
                LORE2,
                String.format(LORE3, Main.formatDollar(shiftValue))
        );
        button.setAction((InventoryClickEvent e)-> {
            switch(e.getClick()) {
                case RIGHT:
                    if (price + value < 10000000)
                    setPrice(price + value);
                    break;
                case LEFT:
                    if (price - value >= 0) {
                        setPrice(price - value);
                    } else if (price > 0){
                        setPrice(0);
                    }
                    break;
                case SHIFT_RIGHT:
                    if (price + shiftValue < 10000000)
                    setPrice(price + shiftValue);
                    break;
                case SHIFT_LEFT:
                    if (price - shiftValue >= 0) {
                        setPrice(price - shiftValue);
                    } else if(price > 0) {
                        setPrice(0);
                    }
                    break;
            }
        });
        addButton(row, column, button);
    }

    private void setPrice(double newPrice) {
        price = newPrice;
        createPriceAdjustmentButton(3, 2, Material.PAPER, 100, 0.99);
        createPriceAdjustmentButton(3, 3, Material.PAPER, 50, 0.50);
        createPriceAdjustmentButton(3, 4, Material.PAPER, 20, 0.20);
        createPriceAdjustmentButton(3, 5, Material.PAPER, 10, 0.10);
        createPriceAdjustmentButton(3, 6, Material.PAPER, 5, 0.05);
        createPriceAdjustmentButton(3, 7, Material.PAPER, 1, 0.01);
    }


    @Override
    protected void onPlaceItem(boolean guiIsClickedInventory, InventoryClickEvent e) {
        if(guiIsClickedInventory) {
            ItemStack item = e.getCursor();
            e.setCancelled(moveItemToGUI(item));
        }
    }

    @Override
    protected void onPickupItem(boolean guiIsClickedInventory, InventoryClickEvent event) {
        if(guiIsClickedInventory) moveItemFromGUI();
    }

    @Override
    protected void onMoveToOtherInventory(boolean guiIsClickedInventory, InventoryClickEvent e) {
        if(guiIsClickedInventory) moveItemFromGUI();
        if(!guiIsClickedInventory) e.setCancelled(moveItemToGUI(e.getCurrentItem()));
    }

    @Override
    protected void onCollectToCursor(boolean guiIsClickedInventory, InventoryClickEvent event) {
        moveItemFromGUI();
    }

    @Override
    protected void onDrag(InventoryDragEvent e) {
        ItemStack item = e.getOldCursor();
        boolean isDraggedInTopInventory = false;
        InventoryView view = e.getView();

        Inventory top = view.getTopInventory();
        int topSize = top.getSize();

        for(int slot : e.getRawSlots()) {
            if(slot < topSize) {
                isDraggedInTopInventory = true;
            }
        }
        if(isDraggedInTopInventory) e.setCancelled(moveItemToGUI(item));
    }

    @Override
    protected void onClose(InventoryCloseEvent e) {
        if(!sell) cancel(e.getPlayer());
    }

    /**
     *
     * @param item the item stack in question
     * @return whether the event is cancelled
     */
    private boolean moveItemToGUI(ItemStack item) {
        if(item != null) {
            // Check that item does not have custom meta
            Material material = item.getType();
            if(item.isSimilar(new ItemStack(material, item.getAmount()))) {
                // allow if the item is the same type as already set
                if(currentMaterial == null || material == currentMaterial) {
                    currentMaterial = material;
                    return false;
                }
            }
        }
        return true;
    }

    // clear the material if there is nothing in the sell window
    private void moveItemFromGUI() {
        Inventory inv = this.getInventory();
        // Check that the area is clear. Delay by 1 tick by using a scheduler
        Bukkit.getScheduler().runTask(Main.instance, ()-> {
            for (int i = 0; i < inv.getSize() - 9; i++) {
                if (inv.getItem(i) != null) {
                    return;
                }
            }
            currentMaterial = null;
        });
    }

    private void cancel(HumanEntity player) {
        Inventory inv = this.getInventory();

        for (int i = 0; i < inv.getSize() - 9; i++) {
            if (inv.getItem(i) != null) {
                HashMap<Integer, ItemStack> returned =  player.getInventory().addItem(inv.getItem(i));
                World world = player.getWorld();
                if(returned.keySet().size() > 0) {
                    world.dropItem(player.getLocation(), returned.get(0));
                }
                inv.clear(i);
            }
        }
    }

    private void confirm(HumanEntity player) {
        Inventory inv = this.getInventory();
        int quantity = 0;

        for(int i = 0; i < inv.getSize() - 9; i++) {
            ItemStack item = inv.getItem(i);
            if(item != null) {
                quantity += item.getAmount();
            }
        }

        // Check that quantity is nice
        if(quantity > 0) {
            // Check that price is epic
            if(price > 0) {
                Economy eco = Main.getEconomy();
                double sellTax = Main.getSellTax();
                UUID uuid = player.getUniqueId();

                double balance = eco.getBalance((OfflinePlayer) player);

                double totalPrice = price * quantity;
                double tax = sellTax * totalPrice;

                if(balance >= tax) {

                    try {
                        Connection conn = Main.getConnection();

                        PreparedStatement stmt = conn.prepareStatement(Main.loadSQL("sql/select/listing_exists.sql"));
                        stmt.setString(1, uuid.toString());
                        stmt.setString(2, currentMaterial.name());
                        int prevQuantity = 0;
                        double prevPrice = 0;
                        double additionalTax = 0;

                        ResultSet rs = stmt.executeQuery();
                        // If there is an existing row, update it. Otherwise, create new row.
                        if(rs.next()) {
                            int id = rs.getInt(1);
                            prevQuantity = rs.getInt(2);
                            prevPrice = rs.getDouble(3);

                            if(prevPrice != price) {
                                // redundant parenthesis but whatever it's aesthetic
                                double prevTaxPayed = (prevPrice * prevQuantity) * sellTax;
                                // How much tax would be required on the amount already existing with new price
                                double newTax = (price * prevQuantity) * sellTax;

                                additionalTax = newTax - prevTaxPayed;
                            }

                            stmt = conn.prepareStatement(Main.loadSQL("sql/update/listing.sql"));

                            stmt.setInt(1, prevQuantity + quantity);
                            stmt.setDouble(2, price);
                            stmt.setInt(3, id);

                        } else {
                            stmt = conn.prepareStatement(Main.loadSQL("sql/insert/listing.sql"));

                            stmt.setString(1, uuid.toString());
                            stmt.setString(2, currentMaterial.name());
                            stmt.setDouble(3, price);
                            stmt.setInt(4, quantity);

                        }
                        if(balance >= tax + additionalTax) {
                            sell = true;
                            stmt.executeUpdate();
                            eco.withdrawPlayer((OfflinePlayer) player, tax + additionalTax);
                            player.sendMessage(String.format(ChatColor.translateAlternateColorCodes('&',
                                    "&fSelling &7%d &b%s &ffor &a%s &feach (You paid &c%s &fin seller's tax)."),
                                    prevQuantity + quantity, currentMaterial.name(), Main.formatDollar(price), Main.formatDollar(tax + additionalTax)));
                            player.closeInventory();
                        } else {
                            sell = false;
                            confirmButton.setLore(ChatColor.RED + "You do not have enough funds to pay seller's taxes!");
                            addButton(3, 8, confirmButton);
                        }
                        conn.close();
                    } catch (SQLException | IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    confirmButton.setLore(ChatColor.RED + "You do not have enough funds to pay seller's taxes!");
                    addButton(3, 8, confirmButton);
                }
            } else {
                confirmButton.setLore(ChatColor.RED + "You cannot sell items for free!");
                addButton(3, 8, confirmButton);
            }
        } else {
            confirmButton.setLore(ChatColor.RED + "You cannot sell 0 items!");
            addButton(3, 8, confirmButton);
        }
    }
}
