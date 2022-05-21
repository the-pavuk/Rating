package ru.the_pavuk.rating.inventories;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.data.type.Sign;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import ru.the_pavuk.rating.Rating;
import ru.the_pavuk.rating.database.DbHandler;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class PlayerInventory {
    private final Rating plugin;
    private final DbHandler dbHandler;

    public PlayerInventory(Rating plugin) {
        this.plugin = plugin;
        this.dbHandler = new DbHandler(plugin);
    }

    public Inventory createInventory(Player p) throws IOException {
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.YELLOW + "" + p.getName());
        Double ratingValue = dbHandler.getRating(p);
        Integer count = dbHandler.getRatesCount(p);
        DecimalFormat decimalFormat = new DecimalFormat( "#.##" );
        String ratingString = decimalFormat.format(ratingValue);
        for (int i = 0; i <= 26; i++) {
            inventory.setItem(i, createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", ""));
        }
        inventory.setItem(10, createGuiItem(Material.NETHER_STAR, "Рейтинг игрока " + p.getName(), "Рейтинг этого игрока составляет: " + dbHandler.getRateColor(p) + ratingString , "Этот рейтинг был составлен на основе голосов " + count + " игроков"));
        inventory.setItem(16, createGuiItem(Material.BIRCH_SIGN, "Оценить игрока " + p.getName()));
        return inventory;
    }

    public Inventory adminInventory() throws IOException {
        Integer i = 0;
        Inventory adminInventory = Bukkit.createInventory(null, 27, ChatColor.AQUA + "Админ-панель");
        for (Player p: Bukkit.getOnlinePlayers()){

            if (i<25) {
                Double ratingValue = dbHandler.getRating(p);
                Integer count = dbHandler.getRatesCount(p);
                DecimalFormat decimalFormat = new DecimalFormat( "#.##" );
                String ratingString = decimalFormat.format(ratingValue);
                ItemStack item = createPlayerHead(p.getName(), p, "Рейтинг этого игрока составляет: " + dbHandler.getRateColor(p) + ratingString , "Этот рейтинг был составлен на основе голосов " + count + " игроков");
                adminInventory.setItem(i, item);
            } else { break; }
            i++;
        }
        adminInventory.setItem(25, new ItemStack(Material.AIR));
        adminInventory.setItem(26, new ItemStack(Material.AIR));
        return adminInventory;
    }

    protected ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.setDisplayName(name);

        // Set the lore of the item
        meta.setLore(Arrays.asList(lore));

        item.setItemMeta(meta);

        return item;
    }
    protected ItemStack createPlayerHead(final String name, Player p, String... lore){
        final ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        final ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
        skullMeta.setOwningPlayer(p);

        item.setItemMeta(meta);
        return item;
    }
}