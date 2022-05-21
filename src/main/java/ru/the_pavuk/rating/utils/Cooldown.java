package ru.the_pavuk.rating.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.the_pavuk.rating.Rating;

public class Cooldown {
    private final Rating plugin;

    public Cooldown(Rating plugin){
        this.plugin = plugin;
    }
    public void schedulePlayer(Player player, Integer ticksTime, Player secondPlayer){
        Rating.cooldownMap.put(player.getName(), secondPlayer.getName());
        new BukkitRunnable(){
            @Override
            public void run() {
                Rating.cooldownMap.remove(player.getName(), secondPlayer.getName());
                player.sendMessage(ChatColor.AQUA + "Время кулдауна истекло! (Вы можете изменить оценку игроку: " + secondPlayer.getName() + ")");
            }
        }.runTaskLaterAsynchronously(plugin, ticksTime);
    }
    public boolean isInList(Player p, Player p2){
        return Rating.cooldownMap.containsKey(p.getName()) && Rating.cooldownMap.containsValue(p2.getName());
    }
}
