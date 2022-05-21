package ru.the_pavuk.rating.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.the_pavuk.rating.Rating;
import ru.the_pavuk.rating.database.DbHandler;
import ru.the_pavuk.rating.datatypes.PlayerRate;
import ru.the_pavuk.rating.inventories.PlayerInventory;
import ru.the_pavuk.rating.utils.Cooldown;

import java.io.IOException;

public class CommandRate implements @Nullable CommandExecutor {
    Rating plugin;
    public CommandRate(Rating rating) {
        this.plugin = rating;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Только игрок может выполнять эту команду!");
            return true;
        }
        DbHandler dbHandler = new DbHandler(plugin);
        try {
            if (args[0].equals("admin")) {
                if (!sender.hasPermission("rating.admin")) {
                    sender.sendMessage("Правильное использование команды:");
                    return false;
                } else {
                    try {
                        if (args[1].equals("reset")) {
                            Player resetablePlayer = Bukkit.getPlayer(args[2]);
                            if (resetablePlayer == null) {
                                sender.sendMessage(ChatColor.RED + "Игрок не найден!");
                                return true;
                            } else {
                                try {
                                    dbHandler.resetPlayer(resetablePlayer);
                                    sender.sendMessage(ChatColor.GREEN + "Рейтинг игрока " + ChatColor.RESET + args[2] + ChatColor.GREEN + " успешно обнулен!");
                                    return true;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            sender.sendMessage("Правильное использование команды:");
                            return false;
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        sender.sendMessage("Правильное использование команды:");
                        return false;
                    }
                }
                return true;
            }
            try {
                if (args[1].equals("")) {
                    sender.sendMessage("Правильное использование команды:");
                    return false;
                }
                Player p = Bukkit.getPlayer(args[0]);
                if (p == null) {
                    sender.sendMessage(ChatColor.RED + "Игрок не найден!");
                } else {
                    if (p.getName().equals(sender.getName())) {
                        sender.sendMessage(ChatColor.RED + "Вы не можете оценить самого себя!");
                        return true;
                    }
                    Cooldown cooldown = new Cooldown(plugin);
                    if (cooldown.isInList((Player) sender, p)) {
                        sender.sendMessage(ChatColor.AQUA + "Вы ещё в кулдауне!");
                        return true;
                    }
                    try {
                        if (Integer.parseInt(args[1]) >= 1 && Integer.parseInt(args[1]) <= 5) {
                            Boolean bonus = false;
                            if (dbHandler.getRating((Player) sender) >= 4.5 && dbHandler.getRatesCount((Player) sender) > 0) bonus = true;
                            PlayerRate fromPlayer = new PlayerRate(sender.getName(), Integer.valueOf(args[1]), bonus);
                            try {
                                dbHandler.addRate(p, fromPlayer);
                                sender.sendMessage(ChatColor.GREEN + "Вы оценили игрока " + ChatColor.RESET + p.getName() + ChatColor.GREEN + " с оценкой: " + ChatColor.RESET + args[1] + "\u2605");
                                p.sendMessage(ChatColor.GREEN + "Вас оценил игрок " + ChatColor.RESET + sender.getName() + ChatColor.GREEN + " оценкой в: " + ChatColor.RESET + args[1] + "\u2605");
                                sender.sendMessage("Изменить оценку этому игроку возможно через: 12 часов.");
                                cooldown.schedulePlayer((Player) sender, 43200 * 20, p);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Оценка должна быть целым числом в промежутке от единицы до пяти!");
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Оценка должна быть целым числом!");
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                sender.sendMessage("Правильное использование команды:");
                return false;
            }
        } catch (ArrayIndexOutOfBoundsException e){
            sender.sendMessage("Правильное использование команды:");
            return false;
        }
        sender.sendMessage("Правильное использование команды:");
        return false;
    }

}
