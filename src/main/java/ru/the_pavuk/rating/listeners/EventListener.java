package ru.the_pavuk.rating.listeners;

import org.bukkit.boss.*;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.the_pavuk.rating.Rating;
import ru.the_pavuk.rating.utils.BossBarWorker;
import ru.the_pavuk.rating.utils.RandomItem;
import ru.the_pavuk.rating.utils.TeamWorker;
import ru.the_pavuk.rating.database.DbHandler;
import ru.the_pavuk.rating.events.RatingChangeEvent;
import ru.the_pavuk.rating.inventories.PlayerInventory;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.StreamSupport;

public class EventListener implements Listener {

    private static Rating plugin = null;

    public EventListener(Rating rating) {
        this.plugin = rating;
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEntityEvent event) throws IOException {
        DbHandler dbHandler = new DbHandler(plugin);
        if (event.getRightClicked() instanceof Player && event.getPlayer().isSneaking()) {
            Player secondPlayer = (Player) event.getRightClicked();
            Inventory inv = new PlayerInventory(plugin).createInventory(secondPlayer);
            event.getPlayer().openInventory(inv);
            Rating.Inventories.put(event.getPlayer().getUniqueId(), inv);
        }
        if (event.getRightClicked().getType().equals(EntityType.VILLAGER) && dbHandler.getRating(event.getPlayer()) <= 3.9 && !event.getPlayer().hasPermission("rating.admin")){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void inventoryInteract(InventoryClickEvent event) throws IOException {
        Inventory playerInventory = Rating.Inventories.get(event.getWhoClicked().getUniqueId());
        DbHandler dbHandler = new DbHandler(plugin);
        if (dbHandler.getRating((Player) event.getWhoClicked()) <= 3.2 && !event.getWhoClicked().hasPermission("rating.admin")) {
            if (event.getInventory().getType().equals(InventoryType.PLAYER) || event.getInventory().getType().equals(InventoryType.CREATIVE)) return;
            try {
                if (event.getInventory().getType().equals(InventoryType.CHEST) && !playerInventory.equals(event.getInventory())) {
                    event.setCancelled(true);
                    return;
                }
            } catch (NullPointerException e){
                event.setCancelled(true);
            }
        }
        if (event.getInventory().getType().equals(InventoryType.PLAYER) || event.getInventory().getType().equals(InventoryType.CREATIVE)) return;
        try {
            if (playerInventory.equals(event.getInventory())) {
                if (event.getSlot() == 16) {
                    event.getWhoClicked().closeInventory();
                    String secondPlayerString = event.getCurrentItem().getItemMeta().getDisplayName().replace("Оценить игрока ", "");
                    event.getWhoClicked().sendMessage(ChatColor.AQUA + "Введите целое число от 1 до 5, которым хотите оценить игрока " + secondPlayerString + ".");
                    event.getWhoClicked().sendMessage(ChatColor.AQUA + "Или ОТМЕНА!, для отменения операции.");
                    Player player = (Player) event.getWhoClicked();
                    Rating.commandMap.put(player.getName(), secondPlayerString);
                }
                event.setCancelled(true);
            }
        } catch (NullPointerException e){
            return;
        }
    }

    @EventHandler
    @SuppressWarnings("deprecated")
    public void onChatMessage (PlayerChatEvent event) throws IOException {
        DbHandler dbHandler = new DbHandler(plugin);
        if (dbHandler.getRating(event.getPlayer()) <= 3.15 && !Rating.commandMap.containsKey(event.getPlayer().getName())){
            event.setCancelled(true);
        }
        if (Rating.commandMap.containsKey(event.getPlayer().getName())){
            if (event.getMessage().equals("ОТМЕНА!")) {
                event.getPlayer().sendMessage(ChatColor.RED + "Операция отменена!");
                Rating.commandMap.remove(event.getPlayer().getName());
                event.setCancelled(true);
                return;
            }
            event.getPlayer().performCommand("rate " + Rating.commandMap.get(event.getPlayer().getName()) + " " + event.getMessage());
            Rating.commandMap.remove(event.getPlayer().getName());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerLogin(PlayerLoginEvent event) throws IOException {
        DbHandler dbHandler = new DbHandler(plugin);
        if (dbHandler.getRating(event.getPlayer()) == null && !event.getPlayer().isBanned())
            dbHandler.setRating(event.getPlayer(), 5.0);
        if (!event.getPlayer().isBanned() && dbHandler.getRating(event.getPlayer()) != null){
            Player p = event.getPlayer();
            Double ratingValue = dbHandler.getRating(event.getPlayer());
            DecimalFormat decimalFormat = new DecimalFormat( "#.##" );
            String ratingString = decimalFormat.format(ratingValue);
            String prefix = ChatColor.BOLD + "" + dbHandler.getRateColor(p) + ratingString + "\u2605" + ChatColor.RESET + " ";
            if (p.hasPermission("rating.admin")) prefix = ChatColor.STRIKETHROUGH + prefix;
            TeamWorker.createTeam(p);
            Team playerTeam = TeamWorker.getTeam(p);
            TeamWorker.setPrefix(playerTeam, prefix);
        }
    }
    @EventHandler
    public void playerOnLeave(PlayerQuitEvent event){
        Player p = event.getPlayer();
        TeamWorker.deleteTeam(p);
    }
    @EventHandler
    @SuppressWarnings("deprecated")
    public void ratingChange(RatingChangeEvent event) throws IOException {
        DbHandler dbHandler = new DbHandler(plugin);
        Player p = event.getPlayer();
        Double ratingValue = dbHandler.getRating(event.getPlayer());
        DecimalFormat decimalFormat = new DecimalFormat( "#.##" );
        String ratingString = decimalFormat.format(ratingValue);
        String prefix =  ChatColor.BOLD + "" + dbHandler.getRateColor(p) + ratingString + "\u2605" + ChatColor.RESET + " ";
        if (p.hasPermission("rating.admin")) prefix = ChatColor.STRIKETHROUGH + prefix;
        TeamWorker.deleteTeam(p);
        TeamWorker.createTeam(p);
        Team playerTeam = TeamWorker.getTeam(p);
        TeamWorker.setPrefix(playerTeam,prefix);


        if (event.getPlayer().hasPermission("rating.admin")) return; //Если игрок с ОПкой, или имеет пермишен "rating.admin", то ивенты на него не работают.

        // Автоматический бан, если меньше 1.5 рейтинга

        if (ratingValue < 1.5){
            Bukkit.getBanList(BanList.Type.NAME).addBan(event.getPlayer().getName(), new TextComponent(ChatColor.RED + "Ваш рейтинг ниже 1.5\u2605").getText() , null, "console");
            event.getPlayer().kickPlayer(ChatColor.RED + "Вы были забанены!\n" + ChatColor.RESET + "Ваш рейтинг опустился ниже: " + ChatColor.DARK_RED + "1.5\u2605");
            dbHandler.deleteEntry(event.getPlayer());
            return;
        }

    }
    @EventHandler
    @SuppressWarnings("deprecated")
    public void playerBan(PlayerQuitEvent event) throws IOException {
        DbHandler dbHandler = new DbHandler(plugin);
        if (event.getPlayer().isBanned()){
            event.setQuitMessage(event.getPlayer().getName() + " был отмёнен в твиттере.");
            for (Player p: Bukkit.getOnlinePlayers()){
                dbHandler.deleteRate(p, event.getPlayer().getName());
            }
        }
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) throws IOException {
        DbHandler dbHandler = new DbHandler(plugin);
        if (dbHandler.getRating(event.getPlayer()) <= 1.9 && !event.getPlayer().hasPermission("rating.admin")) {
            event.getPlayer().damage(0.5);
            event.setCancelled(true);
        }
        if (dbHandler.getRating(event.getPlayer()) <= 2.5 && !event.getPlayer().hasPermission("rating.admin")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) throws IOException {
        DbHandler dbHandler = new DbHandler(plugin);
        if (dbHandler.getRating(event.getPlayer()) <= 1.9 && !event.getPlayer().hasPermission("rating.admin")) {
            event.getPlayer().damage(0.5);
            event.setCancelled(true);
        }
        if (dbHandler.getRating(event.getPlayer()) <= 2.5 && !event.getPlayer().hasPermission("rating.admin")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClickBlock(PlayerInteractEvent event) throws IOException {
        DbHandler dbHandler = new DbHandler(plugin);
        if (dbHandler.getRating(event.getPlayer()) <= 2.6 && !event.getPlayer().hasPermission("rating.admin")){
            if (event.hasBlock())
                if (event.getClickedBlock().getType().equals(Material.CRAFTING_TABLE)) event.setCancelled(true);
        }
        if (dbHandler.getRating(event.getPlayer()) <= 2.7 && !event.getPlayer().hasPermission("rating.admin")) {
            if (event.hasBlock())
                if (event.getClickedBlock().getType().equals(Material.FURNACE) || event.getClickedBlock().getType().equals(Material.BLAST_FURNACE) || event.getClickedBlock().getType().equals(Material.SMOKER)) event.setCancelled(true);
        }
        if (dbHandler.getRating(event.getPlayer()) <= 2.9 && !event.getPlayer().hasPermission("rating.admin")){
            if(event.hasBlock())
                if (event.getClickedBlock().getType().equals(Material.CHEST) || event.getClickedBlock().getType().equals(Material.ENDER_CHEST)) event.setCancelled(true);
        }
        if (dbHandler.getRating(event.getPlayer()) <= 3.35 && !event.getPlayer().hasPermission("rating.admin")){
            if(event.hasBlock()){
                if (event.getClickedBlock().getType().equals(Material.CARTOGRAPHY_TABLE) || event.getClickedBlock().getType().equals(Material.SMITHING_TABLE) || event.getClickedBlock().getType().equals(Material.FLETCHING_TABLE) || event.getClickedBlock().getType().equals(Material.BREWING_STAND)) event.setCancelled(true);
            }
        }
        if (dbHandler.getRating(event.getPlayer()) <= 3.0 && !event.getPlayer().hasPermission("rating.admin")) {
            if(event.hasBlock()){
                if (event.getClickedBlock().getType().equals(Material.ANVIL)|| event.getClickedBlock().getType().equals(Material.CHIPPED_ANVIL) || event.getClickedBlock().getType().equals(Material.DAMAGED_ANVIL)) event.setCancelled(true);
                if (event.getClickedBlock().getType().equals(Material.ENCHANTING_TABLE) || event.getClickedBlock().getType().equals(Material.LECTERN)) event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamageMob(EntityDamageByEntityEvent event) throws IOException {
        DbHandler dbHandler = new DbHandler(plugin);
        if (event.getDamager() instanceof Player){
            Player p = (Player) event.getDamager();
            if (dbHandler.getRating((Player) event.getDamager()) <= 1.9 && !event.getDamager().hasPermission("rating.admin")) {
                ((Player) event.getDamager()).damage(1);
                event.setCancelled(true);
            }
            if (dbHandler.getRating((Player)event.getDamager()) <= 2.8 && !event.getDamager().hasPermission("rating.admin")) event.setCancelled(true);
            if (dbHandler.getRating((Player) event.getDamager()) <= 3.5 && event.getEntity() instanceof Player && !event.getDamager().hasPermission("rating.admin")) event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerDamage(EntityDamageEvent event) throws IOException {
        DbHandler dbHandler = new DbHandler(plugin);
        if (event.getEntity() instanceof Player){
            Player p = (Player) event.getEntity();
            if (dbHandler.getRating(p) <= 2.2) {
                event.setDamage(event.getDamage()*1.5);
            }
            if (dbHandler.getRating(p) >= 4.7 && dbHandler.getRatesCount(p) > 0){
                event.setDamage(event.getDamage()/1.5);
                return;
            }
        }
    }

    @EventHandler
    public void onChangeDimension(PlayerPortalEvent event) throws IOException {
        DbHandler dbHandler = new DbHandler(plugin);
        if (dbHandler.getRating(event.getPlayer()) <= 3.5 && event.getTo().getWorld().getName().equals("world_the_end") && !event.getPlayer().hasPermission("rating.admin")) event.setCancelled(true);
        if (dbHandler.getRating(event.getPlayer()) <= 4.0 && event.getTo().getWorld().getName().equals("world_nether") && !event.getPlayer().hasPermission("rating.admin")) event.setCancelled(true);
    }

    @EventHandler
    public void onKillEntity(EntityDeathEvent event) throws IOException {
        DbHandler dbHandler = new DbHandler(plugin);
        Player p = null;
        if (event.getEntity().getKiller() != null) {
            p = event.getEntity().getKiller();
        } else {
            return;
        }
        if (dbHandler.getRating(p) >= 4.2 && !p.hasPermission("rating.admin") && !(event.getEntity() instanceof Player) && dbHandler.getRatesCount(p) > 0) {
            List<ItemStack> drops = event.getDrops();
            for (ItemStack itemStack : drops) {
                itemStack.setAmount(itemStack.getAmount() * 2);
            }
        }
        if (dbHandler.getRating(p) >= 4.3 && dbHandler.getRatesCount(p) > 0) {
            if (p.getHealth() < p.getMaxHealth())
                p.setHealth(p.getHealth()+2);
        }
    }
    public static void giveRandomItem(){
        DbHandler dbHandler = new DbHandler(plugin);
        Rating.bukkitRunnableRandItem = new BukkitRunnable(){
            @Override
            public void run() {
                for (Player p: Bukkit.getOnlinePlayers()){
                    try {
                        if (dbHandler.getRating(p) >= 4.8 && !p.hasPermission("rating.admin") && dbHandler.getRatesCount(p) > 0){
                            ItemStack randomItem = new RandomItem().getItem();
                            if (!(p.getInventory().firstEmpty() == -1)){
                                p.getInventory().addItem(randomItem);
                                p.sendMessage(ChatColor.GREEN + "Награда за хорошее поведение \u263A \n" + ChatColor.GREEN + "Выдан предмет: " + ChatColor.RESET + randomItem.getType());
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0,60*20);
    }
    public static void deleteRandomItem(){
        DbHandler dbHandler = new DbHandler(plugin);
        Rating.bukkitRunnableDelItem = new BukkitRunnable(){
            @Override
            public void run() {
                for (Player p: Bukkit.getOnlinePlayers()){
                    try {
                        if (dbHandler.getRating(p) <= 2.0 && !p.hasPermission("rating.admin")){
                            @Nullable ItemStack @NotNull [] inventory = p.getInventory().getContents();
                            ItemStack randomItem = inventory[new Random().nextInt(inventory.length)];
                            p.getInventory().remove(randomItem);
                            p.sendMessage(ChatColor.RED + "Наказание за плохое поведение \u263A \n" + ChatColor.RED + "Изъят предмет: " + ChatColor.RESET + randomItem.getType());

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0,60*20*5);
    }

    @EventHandler
    public void onPickUp(PlayerAttemptPickupItemEvent event) throws IOException {
        Player p = event.getPlayer();
        DbHandler dbHandler = new DbHandler(plugin);
        if (dbHandler.getRating(p) <= 2.1) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerConsume(PlayerItemConsumeEvent event) throws IOException {
        Player p = event.getPlayer();
        DbHandler dbHandler = new DbHandler(plugin);
        if (dbHandler.getRating(p) <= 2.4) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20*10,1,false,false));
        }
    }

    public static void bossBar() throws IOException {
        DbHandler dbHandler = new DbHandler(plugin);
        Rating.bukkitRunnableBossBar = new BukkitRunnable(){
            BossBar bossBar = null;
            @Override
            public void run() {
                for (Player p: Bukkit.getOnlinePlayers()){

                        try {
                            if (dbHandler.getRating(p) < 4.9) {
                                if (bossBar != null) {
                                    for (Player player : bossBar.getPlayers()) {
                                        bossBar.removePlayer(player);
                                    }
                                    bossBar.setVisible(false);
                                }
                            }
                            if (dbHandler.getRating(p) >= 4.9 && dbHandler.getRatesCount(p) > 0 && !p.hasPermission("rating.admin")) {
                                if (p.isSneaking() || p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                                    bossBar.setVisible(false);
                                    return;
                                }
                                bossBar = BossBarWorker.getBossBar(p, Rating.bossBarMap.get(p.getName()));
                                List<Entity> nearEntities = p.getNearbyEntities(10, 10, 10);
                                for (Entity nearEntity : nearEntities) {
                                    if (nearEntity instanceof Player) {
                                        Player nearPlayer = (Player) nearEntity;
                                        if (!bossBar.getPlayers().contains(nearPlayer)) {
                                            bossBar.addPlayer(nearPlayer);
                                        }
                                    }
                                }
                                for (Player player : bossBar.getPlayers()) {
                                    if (bossBar.getPlayers().contains(player) && !nearEntities.contains(player)) {
                                        bossBar.removePlayer(player);
                                    }
                                }
                                bossBar.setVisible(true);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
        }.runTaskTimer(plugin, 0,0);
    }
    public static void effects(){
        DbHandler dbHandler = new DbHandler(plugin);
        Rating.bukkitTaskMain = new BukkitRunnable(){

            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    try {
                        if (dbHandler.getRating(p) > 4.4 && dbHandler.getRatesCount(p) > 0) {
                            PotionEffectType.Category category = PotionEffectType.Category.HARMFUL;
                            for (PotionEffect effect: p.getActivePotionEffects()){
                                PotionEffectType effectType = effect.getType();
                                if (category.equals(effectType.getEffectCategory())) {
                                    p.removePotionEffect(effectType);
                                }
                            }
                        }
                        if (dbHandler.getRating(p) >= 4.1 && dbHandler.getRatesCount(p) > 0 && !p.hasPermission("rating.admin")) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 30, 2, false, false));
                        }
                        if (dbHandler.getRating(p) >= 4.6 && dbHandler.getRatesCount(p) > 0 && !p.hasPermission("rating.admin")) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 1, false, false));
                        }
                        if (dbHandler.getRating(p) <= 2.3 && !p.hasPermission("rating.admin")) {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 1, false, true));
                        }
                        if (dbHandler.getRating(p) <= 1.7 && !p.hasPermission("rating.admin")) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 30, 1, false, false));
                        }
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskTimer(plugin,0,0);
    }
}
