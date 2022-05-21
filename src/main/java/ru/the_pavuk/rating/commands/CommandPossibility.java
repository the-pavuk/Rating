package ru.the_pavuk.rating.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.the_pavuk.rating.Rating;
import ru.the_pavuk.rating.database.DbHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class CommandPossibility implements @Nullable CommandExecutor {
    private final Rating plugin;

    public CommandPossibility(Rating rating) {
        this.plugin = rating;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            DbHandler dbHandler = new DbHandler(plugin);
            if (p.hasPermission("rating.admin")) {
                sender.sendMessage(ChatColor.AQUA + "�� ���������! �� ��� �� ���������� ����� ��� �������.");
                return true;
            }
            HashMap<Double, String> debuffHashMap = new HashMap<>();
            HashMap<Double, String> buffHashMap = new HashMap<>();
            // �������
            debuffHashMap.put(1.5, "�������������� ��� �������� (���� ������ 1.5). ���� �� ��� ������, ��� ��� �������������� \u263A");
            debuffHashMap.put(1.7, "������ ��������, � �������� �������������� ��� ������ �������");
            debuffHashMap.put(1.9, "��� ������� ������� �������� ��� ������� ����, ����� � 0.5 ������");
            debuffHashMap.put(2.0, "������� ���������: ��������� ��������� ������� �� ��������� ��� � 5 �����");
            debuffHashMap.put(2.1, "������ ��������� ����");
            debuffHashMap.put(2.2, "����������� ���� �� ���� �� 50%");
            debuffHashMap.put(2.3, "������ ��������������");
            debuffHashMap.put(2.4, "����� ��� ���� ������ �������, ������������� 10 ������");
            debuffHashMap.put(2.5, "������ ������ � ������� �����");
            debuffHashMap.put(2.6, "������ ������������ ���������");
            debuffHashMap.put(2.7, "������ ������������ ������� (����� ����� ������)");
            debuffHashMap.put(2.8, "������ ���� �����");
            debuffHashMap.put(2.9, "������ ��������� �������");
            debuffHashMap.put(3.0, "������ ������������ ���� ����������� � ����������");
            debuffHashMap.put(3.15, "������ ������ � ���");
            debuffHashMap.put(3.2, "������ ����������������� � ���������� ��������");
            debuffHashMap.put(3.35, "������ ������������ ������� ��������� �������");
            debuffHashMap.put(3.5, "������ ��������� ����� ������ � ���");
            debuffHashMap.put(3.75, "������ ���� �������");
            debuffHashMap.put(3.9, "������ ��������� � ��������");
            debuffHashMap.put(4.0, "������ ��������� ����� ������ � �����");
            //�����
            buffHashMap.put(4.1, "������ ����� 3�� ������");
            buffHashMap.put(4.2, "������� ��� � �����");
            buffHashMap.put(4.3, "�������� ����� ��������������� 1 ������");
            buffHashMap.put(4.4, "��������� �� ���� �������� ������ �����");
            buffHashMap.put(4.5, "����� � ������ �������");
            buffHashMap.put(4.7, "������������� �� ����� ����� �� 50%");
            buffHashMap.put(4.8, "������� ���������: ��� � ������ �������� ��������� �������, ���� ��������� �� ��������");
            buffHashMap.put(4.9, "��� ��� ����� � �������� ��������� �������");

            sender.sendMessage(ChatColor.AQUA + "���� ������� �������:");
            SortedSet<Double> debuff = new TreeSet<>(debuffHashMap.keySet());
            for (Double d: debuff){
                try {
                    if (d >= dbHandler.getRating(p) && dbHandler.getRating(p) <= 4.0) {
                        sender.sendMessage(" - " + ChatColor.RED + d + "\u2605 | " + ChatColor.RESET + debuffHashMap.get(d));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (dbHandler.getRating(p) > 4.0) sender.sendMessage(" - �������� ���!");
            } catch (IOException e) {
                e.printStackTrace();
            }
            sender.sendMessage(ChatColor.AQUA + "���� ������� ����� (��� ��������, ���� ���� ���������� ������� ������ ����):");
            SortedSet<Double> buff = new TreeSet<>(buffHashMap.keySet());
            for (Double d: buff){
                try {
                    if (d <= dbHandler.getRating(p) && dbHandler.getRating(p) >= 4.1 && d >= 4.1 && dbHandler.getRatesCount(p) > 0) {
                        sender.sendMessage(" - " + ChatColor.GREEN + d + "\u2605 | " + ChatColor.RESET + buffHashMap.get(d));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (dbHandler.getRating(p) < 4.1) sender.sendMessage(" - ������ ���!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
