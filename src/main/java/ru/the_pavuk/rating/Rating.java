package ru.the_pavuk.rating;

import org.bukkit.boss.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ru.the_pavuk.rating.commands.CommandFeedback;
import ru.the_pavuk.rating.commands.CommandPossibility;
import ru.the_pavuk.rating.commands.CommandRate;
import ru.the_pavuk.rating.database.DbHandler;
import ru.the_pavuk.rating.datatypes.PlayerRate;
import ru.the_pavuk.rating.listeners.EventListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Logger;

public final class Rating extends JavaPlugin {
    public static HashMap<UUID, Inventory> Inventories = new HashMap<>();
    public static HashMap<String, String> cooldownMap = new HashMap<>();
    public static HashMap<String, String> commandMap = new HashMap<>();
    public static HashMap<String, BossBar> bossBarMap = new HashMap<>();
    private DbHandler dbHandler = new DbHandler(this);
    public static BukkitTask bukkitRunnableRandItem;
    public static BukkitTask bukkitRunnableBossBar;
    public static BukkitTask bukkitRunnableDelItem;
    public static BukkitTask bukkitTaskMain;
    public FileConfiguration ratings = null;
    Logger logger = Logger.getLogger("Rating");


    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(PlayerRate.class);
        File fileRatings = new File(getDataFolder() + File.separator + "ratings.yml");
        Bukkit.getPluginManager().registerEvents(new EventListener(this), this);
        Objects.requireNonNull(getCommand("rate")).setExecutor(new CommandRate(this));
        Objects.requireNonNull(getCommand("feedback")).setExecutor(new CommandFeedback(this));
        Objects.requireNonNull(getCommand("reward")).setExecutor(new CommandPossibility(this));
        //Objects.requireNonNull(getCommand("rate")).setTabCompleter(new RateCompleter(this));
        try {
            ratings = loadCustomConfig("ratings.yml", fileRatings);
        } catch (IOException e){
            e.printStackTrace();
        }
        EventListener.giveRandomItem();
        EventListener.deleteRandomItem();
        try {
            EventListener.bossBar();
            EventListener.effects();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            logger.info("Saving the ratings-file...");
            dbHandler.save();
        } catch (IOException e) {
            logger.warning("Something went wrong while saving!");
            logger.warning(e.getMessage());
        }
        bukkitRunnableRandItem.cancel();
        bukkitRunnableBossBar.cancel();
        bukkitRunnableDelItem.cancel();
        bukkitTaskMain.cancel();
    }
    public FileConfiguration loadCustomConfig(String resourceName, File out) throws IOException {
        InputStream in = getResource(resourceName);
        if (!out.exists()){
            out.getParentFile().mkdirs();
            logger.warning("Ratings-file does not exist! Creating a new file...");
        }
        FileConfiguration file = YamlConfiguration.loadConfiguration(out);
        if (in != null){
            InputStreamReader inReader = new InputStreamReader(in);
            file.setDefaults(YamlConfiguration.loadConfiguration(inReader));
            file.options().copyDefaults(true);
            file.save(out);
        }
        return file;
    }
}
