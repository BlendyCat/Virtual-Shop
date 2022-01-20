package com.blendycat.virtualshop;

import com.blendycat.virtualshop.commands.ShopCommand;
import com.blendycat.virtualshop.commands.UpdateCategoriesCommand;
import com.blendycat.virtualshop.gui.InventoryGUI;
import com.mysql.cj.jdbc.MysqlDataSource;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

    int coins = 0;
    private static FileConfiguration config;
    private static double sellTax = 0;
    private static double buyTax = 0;
    public static Main instance;

    private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;

    @Override
    public void onEnable() {

        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Runtime runtime = Runtime.getRuntime();
        getLogger().info("Memory in use:" + (runtime.totalMemory() - runtime.freeMemory()));

        saveDefaultConfig();

        config = getConfig();

        sellTax = config.getDouble("sell-tax");
        buyTax = config.getDouble("buy-tax");

        PluginCommand shop = getCommand("shop");
        PluginCommand updatecategories = getCommand("updatecategories");

        if(shop != null) {
            shop.setExecutor(new ShopCommand());
        }

        if(updatecategories != null) {
            updatecategories.setExecutor(new UpdateCategoriesCommand());
        }

        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new InventoryGUI.EventsHandler(), this);
        instance = this;

        setupMYSQL();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public void onDisable() {

    }

    public static Connection getConnection() throws SQLException {
        MysqlDataSource source = new MysqlDataSource();
        source.setUser(config.getString("mysql.user"));
        source.setDatabaseName(config.getString("mysql.database"));
        source.setCreateDatabaseIfNotExist(true);
        source.setServerName(config.getString("mysql.host"));
        source.setUseSSL(config.getBoolean("mysql.use-ssl"));
        source.setPassword(config.getString("mysql.pass"));
        source.setPort(config.getInt("mysql.port"));
        return source.getConnection();
    }

    public static void setupMYSQL() {
        try {
            Connection connection = getConnection();
            Statement stmt = connection.createStatement();
            stmt.addBatch(loadSQL("sql/tables/categories.sql"));
            stmt.addBatch(loadSQL("sql/tables/material_index.sql"));
            stmt.addBatch(loadSQL("sql/tables/listings.sql"));
            stmt.executeBatch();
            connection.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadSQL(String filename) throws IOException {
        // Get the file resource
        Reader reader = instance.getTextResource(filename);
        // Make sure it's not null
        if(reader != null) {
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            StringBuilder builder = new StringBuilder();
            while((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        }
        // If the file does not exist, return a file not found exception
        throw new FileNotFoundException(filename);
    }

    public static String formatDollar(double value) {
        if(Math.floor(value) == value) {
            return "$" + (int) value;
        }
        return "$" + String.format("%.2f", value);
    }

    public static double getSellTax() {
        return sellTax;
    }

    public static double getBuyTax() {
        return buyTax;
    }

    public static Economy getEconomy() {
        return econ;
    }
}
