package ru.the_pavuk.rating.database;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import ru.the_pavuk.rating.Rating;
import ru.the_pavuk.rating.datatypes.PlayerRate;
import ru.the_pavuk.rating.events.RatingChangeEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class DbHandler {

    private final Rating plugin;
    private FileConfiguration ratings;
    public DbHandler(Rating rating) {
        this.plugin = rating;
        this.ratings = rating.ratings;
    }

    public void update(Player who) throws IOException {
        double defRating = ratings.getDouble(who.getName()+".defaultRating");
        List<Map<?, ?>> rates = ratings.getMapList(who.getName()+".rates");
        List<Integer> numbers = new ArrayList<>();
        double average = defRating;
        if (rates != null) {
            for (Map<?, ?> o: rates) {
                Integer rate = NumberConversions.toInt(o.get("rate"));
                if (Boolean.parseBoolean(o.get("bonus").toString())){
                    if (this.getRating(who) > rate) rate = rate -1;
                    if (this.getRating(who) < rate) rate = rate +1;
                    if (this.getRating(who) == Double.parseDouble(rate.toString())) rate = rate;
                }
                numbers.add(rate);
            }
            for (int i : numbers) {
                average = average + i;
            }
            int size = rates.size()+1;
            average = average/size;
            if (average > 5){
                average = 5;
            }
        }
        ratings.set(who.getName() + ".rating", average);
        Bukkit.getPluginManager().callEvent(new RatingChangeEvent(who));
        save();
        ratings = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder()+ File.separator + "ratings.yml"));
    }

    public void addRate(Player to, PlayerRate from) throws IOException {

        List<Map<?, ?>> rates = ratings.getMapList(to.getName()+".rates");
        if (rates == null) rates = new ArrayList<>();
        if (!isPlayerInList(rates, from)){
            rates.add(from.serialize());
        } else {
            rates = replaceRate(rates, from);
        }
        ratings.set(to.getName()+".rates", rates);
        update(to);
    }
    public Double getRating(Player who) throws IOException {
        return (Double) ratings.get(who.getName()+".rating");
    }

    public void setRating(Player to, Double amount) throws IOException {
        ratings.set(to.getName()+".defaultRating", amount);
        update(to);
    }
    public void deleteRate(Player who, String nickname) throws IOException {
        List<Map<?, ?>> mapList = new CopyOnWriteArrayList<>();
        mapList.addAll(ratings.getMapList(who.getName()+".rates"));
        for (Map<?, ?> o: mapList){
            if(o.get("nickname").equals(nickname)) {
                mapList.remove(o);
            }
        }
        ratings.set(who.getName()+".rates", mapList);
        update(who);
    }

    public Integer getRatesCount(Player who){
        int count;
        try {
            count = ratings.getMapList(who.getName() + ".rates").size();
        } catch (NullPointerException e){
            count = 0;
        }
        return count;
    }

    public ChatColor getRateColor(Player who) throws IOException {
        ChatColor color = null;
        Double rate = getRating(who);
        if (rate >= 4.2){
            color = ChatColor.GREEN;
        }
        if (rate < 4.2 && rate > 3.2){
            color = ChatColor.YELLOW;
        }
        if (rate <= 3.2 && rate > 2.8){
            color = ChatColor.GOLD;
        }
        if (rate <= 2.8 && rate > 1.0) {
            color = ChatColor.DARK_RED;
        }
        return color;
    }

    public void save() throws IOException {
        plugin.ratings.save(plugin.getDataFolder()+File.separator+"ratings.yml");
    }

    private boolean isPlayerInList(@NotNull List<Map<?, ?>> playerRates, PlayerRate who){
        for (Map<?,?> o: playerRates){
            try {
                if (o.get("nickname").equals(who.getNickname())) {
                    return true;
                }
            } catch (NullPointerException e){
                return false;
            }
        }
        return false;
    }

    private List<Map<?, ?>> replaceRate(List<Map<?, ?>> playerRates, PlayerRate who){
        List<Map<?, ?>> mapList = new CopyOnWriteArrayList<>();
        mapList.addAll(playerRates);
        for (Map<?, ?> o: mapList){
            if (o.get("nickname").equals(who.getNickname())){
                mapList.remove(o);
                mapList.add(who.serialize());
            }
        }
        return mapList;
    }

    public void deleteEntry(Player who) {
        ratings.set(who.getName(), null);
    }

    public void resetPlayer(Player who) throws IOException {
        double defRating = ratings.getDouble(who.getName()+".defaultRating");
        ratings.set(who.getName()+".rating", defRating);
        ratings.set(who.getName()+".rates", null);
        update(who);
    }
}
