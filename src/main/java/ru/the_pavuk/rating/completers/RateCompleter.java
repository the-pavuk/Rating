package ru.the_pavuk.rating.completers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.the_pavuk.rating.Rating;

import java.util.ArrayList;
import java.util.List;

public class RateCompleter implements TabCompleter {
    private final Rating plugin;

    public RateCompleter(Rating rating) {
        this.plugin = rating;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (command.equals("rate")) {
            List<String> completer = new ArrayList<>();
            if (sender.hasPermission("rate.admin")) {
                completer.add("admin");
            }
            for (Player p: Bukkit.getOnlinePlayers()){
                completer.add(p.getName());
            }
            return completer;
        }
        if (args[1].equals("")){
            List<String> completer = new ArrayList<>();
            completer.add("1");
            completer.add("2");
            completer.add("3");
            completer.add("4");
            completer.add("5");
            return completer;
        }
        return null;
    }
}
