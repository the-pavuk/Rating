package ru.the_pavuk.rating.utils;

import org.bukkit.Bukkit;
import org.bukkit.boss.*;
import org.bukkit.entity.Player;
import ru.the_pavuk.rating.Rating;

public class BossBarWorker {

    public static BossBar getBossBar(Player player, BossBar bossBar){
        if (Rating.bossBarMap.containsKey(player.getName())) {
            bossBar.removeFlag(BarFlag.CREATE_FOG);
            bossBar.setProgress(player.getHealth()/20);
            return bossBar;
        }
        bossBar = Bukkit.createBossBar(player.getName(), BarColor.YELLOW, BarStyle.SEGMENTED_10, BarFlag.CREATE_FOG);
        bossBar.removeFlag(BarFlag.CREATE_FOG);
        bossBar.setProgress(player.getHealth()/20);
        Rating.bossBarMap.put(player.getName(), bossBar);
        return bossBar;
    }

}
