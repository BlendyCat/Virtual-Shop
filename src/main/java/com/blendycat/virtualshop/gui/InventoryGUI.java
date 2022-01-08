package com.blendycat.virtualshop.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public abstract class InventoryGUI implements InventoryHolder {

    private Inventory inventory;
    private HashMap<Integer, Button> buttons;

    public InventoryGUI(int size, String title) {
        inventory = Bukkit.createInventory(this, size, title);
        buttons = new HashMap<>();
    }

    protected int getRow(int slot) {
        return slot / 9;
    }

    protected int getColumn (int slot) {
        return slot % 9;
    }

    protected int getSlot(int row, int column) {
        return row * 9 + column;
    }

    private void click(InventoryClickEvent e) {
        Inventory clickedInventory = e.getClickedInventory();

        if(clickedInventory != null) {
            // This will be used for the other inventory click events
            boolean guiIsClickedInventory = false;
            if(clickedInventory.getHolder() != null && clickedInventory.getHolder() instanceof InventoryGUI) {
                int slot = e.getSlot();
                guiIsClickedInventory = true;
                Button button = buttons.get(slot);
                if(button != null) {
                    button.action(e);
                    return;
                }
            }

            switch (e.getAction()) {
                case PLACE_ALL:
                case PLACE_SOME:
                case PLACE_ONE:
                    onPlaceItem(guiIsClickedInventory, e);
                    break;
                case PICKUP_ALL:
                case PICKUP_ONE:
                case PICKUP_HALF:
                case PICKUP_SOME:
                    onPickupItem(guiIsClickedInventory, e);
                    break;
                case COLLECT_TO_CURSOR:
                    onCollectToCursor(guiIsClickedInventory, e);
                    break;
                case MOVE_TO_OTHER_INVENTORY:
                    onMoveToOtherInventory(guiIsClickedInventory, e);
                    break;
                case SWAP_WITH_CURSOR:

                    break;
                case DROP_ALL_CURSOR:
                case DROP_ALL_SLOT:
                case DROP_ONE_CURSOR:
                case DROP_ONE_SLOT:
                case HOTBAR_SWAP:
                    e.setCancelled(true);
                    break;
            }
        }
    }

    protected void onDrag(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    /**
     * You can override this to redefine what happens
     * @param guiIsClickedInventory This is true if the gui is the clicked inventory
     * @param event this is the inventory click event. You can modify whatever you please
     */
    protected void onPlaceItem(boolean guiIsClickedInventory, InventoryClickEvent event) {
        if(guiIsClickedInventory) {
            event.setCancelled(true);
        }
    }

    /**
     * You can override this to redefine what happens
     * @param guiIsClickedInventory This is true if the gui is the clicked inventory
     * @param event this is the inventory click event. You can modify whatever you please
     */
    protected void onPickupItem(boolean guiIsClickedInventory, InventoryClickEvent event) {
        if(guiIsClickedInventory) {
            event.setCancelled(true);
        }
    }

    /**
     * This is what happens when a player triple clicks an item to collect
     * @param guiIsClickedInventory This is true if the gui is the clicked inventory
     * @param event this is the inventory click event. You can modify whatever you please
     */
    protected void onCollectToCursor(boolean guiIsClickedInventory, InventoryClickEvent event) {
        event.setCancelled(true);
    }
    /**
     * You can override this to redefine what happens
     * @param guiIsClickedInventory This is true if the gui is the clicked inventory
     * @param event this is the inventory click event. You can modify whatever you please
     */
    protected void onMoveToOtherInventory(boolean guiIsClickedInventory, InventoryClickEvent event) {
        event.setCancelled(true);
    }

    /**
     * You can override this to redefine what happens
     * @param guiIsClickedInventory This is true if the gui is the clicked inventory
     * @param event this is the inventory click event. You can modify whatever you please
     */
    protected void onSwapWithCursor(boolean guiIsClickedInventory, InventoryClickEvent event) {
        if(guiIsClickedInventory) {
            event.setCancelled(true);
        }
    }

    protected void onClose(InventoryCloseEvent event) {

    }


    protected void addButton(int row, int column, Button button) {
        int slot = getSlot(row, column);
        buttons.put(slot, button);
        inventory.setItem(slot, button.getItem());
    }

    protected void addButton(int slot, Button button) {
        buttons.put(slot, button);
        inventory.setItem(slot, button.getItem());
    }

    protected void clearGUI() {
        buttons.clear();
        inventory.clear();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static class EventsHandler implements Listener {

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            Inventory inventory = e.getInventory();

            if(inventory.getHolder() != null) {
                if (inventory.getHolder() instanceof InventoryGUI) {
                    InventoryGUI gui = (InventoryGUI) inventory.getHolder();
                    gui.click(e);
                }
            }
        }

        @EventHandler
        public void onInventoryDrag(InventoryDragEvent e) {
            Inventory inventory = e.getInventory();

            if(inventory.getHolder() != null) {
                if (inventory.getHolder() instanceof InventoryGUI) {
                    InventoryGUI gui = (InventoryGUI) inventory.getHolder();
                    gui.onDrag(e);
                }
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            Inventory inventory = e.getInventory();

            if(inventory.getHolder() != null) {
                if (inventory.getHolder() instanceof InventoryGUI) {
                    InventoryGUI gui = (InventoryGUI) inventory.getHolder();
                    gui.onClose(e);
                }
            }
        }
    }

    public static class Button {

        protected static Button BACK_BUTTON = new Button(Material.BOOK, ChatColor.WHITE + "Back");

        private Material material;
        private String name;
        private List<String> lore;
        private ButtonRunnable runnable;
        private int amount = 1;

        public Button(Material material) {
            this.material = material;
        }

        public Button(Material material, String name) {
            this.material = material;
            this.name = name;
        }

        public Button(Material material, String name, String... lore) {
            this.material = material;
            this.name = name;
            this.lore = Arrays.asList(lore);
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public void setLore(String... strings) {
            lore = Arrays.asList(strings);
        }

        public void setAction(ButtonRunnable action) {
            this.runnable = action;
        }

        public ItemStack getItem() {
            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();
            if(meta != null) {
                if(lore != null) meta.setLore(lore);
                if(name != null) meta.setDisplayName(name);
            }
            item.setItemMeta(meta);
            return item;
        }

        public void action(InventoryClickEvent e) {
            e.setCancelled(true);
            if(runnable != null) runnable.run(e);
        }

        public interface ButtonRunnable {
            void run(InventoryClickEvent e);
        }
    }
}
