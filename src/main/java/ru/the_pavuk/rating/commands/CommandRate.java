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
            sender.sendMessage("������ ����� ����� ��������� ��� �������!");
            return true;
        }
        DbHandler dbHandler = new DbHandler(plugin);
        try {
            if (args[0].equals("admin")) {
                if (!sender.hasPermission("rating.admin")) {
                    sender.sendMessage("���������� ������������� �������:");
                    return false;
                } else {
                    try {
                        if (args[1].equals("reset")) {
                            Player resetablePlayer = Bukkit.getPlayer(args[2]);
                            if (resetablePlayer == null) {
                                sender.sendMessage(ChatColor.RED + "����� �� ������!");
                                return true;
                            } else {
                                try {
                                    dbHandler.resetPlayer(resetablePlayer);
                                    sender.sendMessage(ChatColor.GREEN + "������� ������ " + ChatColor.RESET + args[2] + ChatColor.GREEN + " ������� �������!");
                                    return true;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            sender.sendMessage("���������� ������������� �������:");
                            return false;
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        sender.sendMessage("���������� ������������� �������:");
                        return false;
                    }
                }
                return true;
            }
            try {
                if (args[1].equals("")) {
                    sender.sendMessage("���������� ������������� �������:");
                    return false;
                }
                Player p = Bukkit.getPlayer(args[0]);
                if (p == null) {
                    sender.sendMessage(ChatColor.RED + "����� �� ������!");
                } else {
                    if (p.getName().equals(sender.getName())) {
                        sender.sendMessage(ChatColor.RED + "�� �� ������ ������� ������ ����!");
                        return true;
                    }
                    Cooldown cooldown = new Cooldown(plugin);
                    if (cooldown.isInList((Player) sender, p)) {
                        sender.sendMessage(ChatColor.AQUA + "�� ��� � ��������!");
                        return true;
                    }
                    try {
                        if (Integer.parseInt(args[1]) >= 1 && Integer.parseInt(args[1]) <= 5) {
                            Boolean bonus = false;
                            if (dbHandler.getRating((Player) sender) >= 4.5 && dbHandler.getRatesCount((Player) sender) > 0) bonus = true;
                            PlayerRate fromPlayer = new PlayerRate(sender.getName(), Integer.valueOf(args[1]), bonus);
                            try {
                                dbHandler.addRate(p, fromPlayer);
                                sender.sendMessage(ChatColor.GREEN + "�� ������� ������ " + ChatColor.RESET + p.getName() + ChatColor.GREEN + " � �������: " + ChatColor.RESET + args[1] + "\u2605");
                                p.sendMessage(ChatColor.GREEN + "��� ������ ����� " + ChatColor.RESET + sender.getName() + ChatColor.GREEN + " ������� �: " + ChatColor.RESET + args[1] + "\u2605");
                                sender.sendMessage("�������� ������ ����� ������ �������� �����: 12 �����.");
                                cooldown.schedulePlayer((Player) sender, 43200 * 20, p);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "������ ������ ���� ����� ������ � ���������� �� ������� �� ����!");
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage("������ ������ ���� ����� ������!");
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                sender.sendMessage("���������� ������������� �������:");
                return false;
            }
        } catch (ArrayIndexOutOfBoundsException e){
            sender.sendMessage("���������� ������������� �������:");
            return false;
        }
        sender.sendMessage("���������� ������������� �������:");
        return false;
    }

}
