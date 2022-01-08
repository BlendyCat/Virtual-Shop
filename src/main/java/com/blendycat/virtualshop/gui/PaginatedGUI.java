package com.blendycat.virtualshop.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple way to create a paginated GUI for a
 */
public abstract class PaginatedGUI extends InventoryGUI {

    private int page;
    private int pageSize;
    private int maxPage;
    private int dataRows;

    private List<Button> dataSet;
    private List<StaticButton> staticButtons;

    private Button previousPage;
    private Button currentPage;
    private Button nextPage;

    public PaginatedGUI(int rows, String title, List<Button> buttons, int page) {
        // add one more row than given for buttons
        super((rows + 1) * 9, title);

        dataRows = rows;
        dataSet = buttons;

        // useful for calculations
        pageSize = rows * 9;
        // calculate the largest page
        maxPage = (Math.max(0, dataSet.size() - 1)) / pageSize;

        this.page = page;

        // Initialize buttons
        previousPage = new Button(Material.PAPER, ChatColor.WHITE + "Previous Page");
        previousPage.setAction((InventoryClickEvent e)->
            previousPage()
        );

        currentPage = new Button(Material.BEACON, ChatColor.WHITE + "Page " + ChatColor.AQUA + (page + 1));

        nextPage = new Button(Material.ARROW, ChatColor.WHITE + "Next Page");
        nextPage.setAction((InventoryClickEvent e)->
            nextPage()
        );

        // initialize static buttons list
        staticButtons = new ArrayList<>();

        loadPage();
    }

    public void addStaticButton(int row, int column, Button button) {
        super.addButton(row, column, button);
        staticButtons.add(new StaticButton(getSlot(row, column), button));
    }

    private void loadPage() {
        clearGUI();

        int nextPageMaxIndex = (page + 1) * pageSize;

        List<Button> currentPageData = dataSet.subList(page * pageSize, Math.min(nextPageMaxIndex, dataSet.size()));

        if(maxPage > 0) {
            if(page > 0) addButton(dataRows, 3, previousPage);
            if(page < maxPage) addButton(dataRows, 5, nextPage);
            currentPage.setName(ChatColor.WHITE + "Page " + ChatColor.AQUA + (page + 1));
            addButton(dataRows, 4, currentPage);
        }

        for(int slot = 0; slot < currentPageData.size(); slot++) {
            addButton(slot, currentPageData.get(slot));
        }

        for(StaticButton staticButton : staticButtons) {
            addButton(staticButton.getSlot(), staticButton.getButton());
        }
    }

    private void nextPage() {
        page++;
        loadPage();
    }

    private void previousPage() {
        page--;
        loadPage();
    }

    private static class StaticButton {
        private Button button;
        private int slot;

        protected StaticButton(int slot, Button button) {
            this.slot = slot;
            this.button = button;
        }

        protected int getSlot() {
            return slot;
        }

        protected Button getButton() {
            return button;
        }
    }
}
