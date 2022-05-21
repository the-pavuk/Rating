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
                sender.sendMessage(ChatColor.AQUA + "Вы неуязвимы! На вас не действуйют баффы или дебаффы.");
                return true;
            }
            HashMap<Double, String> debuffHashMap = new HashMap<>();
            HashMap<Double, String> buffHashMap = new HashMap<>();
            // Дебаффы
            debuffHashMap.put(1.5, "Автоматический бан навсегда (если меньше 1.5). Если вы это видите, мои вам соболезнования \u263A");
            debuffHashMap.put(1.7, "Эффект свечения, в качестве предупреждения для других игроков");
            debuffHashMap.put(1.9, "При попытке ударить сущность или сломать блок, дамаг в 0.5 сердца");
            debuffHashMap.put(2.0, "Система наказания: изымается рандомный предмет из инвентаря раз в 5 минут");
            debuffHashMap.put(2.1, "Нельзя поднимать вещи");
            debuffHashMap.put(2.2, "Увеличивает урон по тебе на 50%");
            debuffHashMap.put(2.3, "Эффект медлительности");
            debuffHashMap.put(2.4, "Любая еда дает эффект тошноты, длительностью 10 секунд");
            debuffHashMap.put(2.5, "Нельзя ломать и ставить блоки");
            debuffHashMap.put(2.6, "Нельзя пользоваться верстаком");
            debuffHashMap.put(2.7, "Нельзя пользоваться печками (всеми кроме костра)");
            debuffHashMap.put(2.8, "Нельзя бить мобов");
            debuffHashMap.put(2.9, "Нельзя открывать сундуки");
            debuffHashMap.put(3.0, "Нельзя использовать стол зачарований и наковальню");
            debuffHashMap.put(3.15, "Нельзя писать в чат");
            debuffHashMap.put(3.2, "Нельзя взаимодействовать с содержимым сундуков");
            debuffHashMap.put(3.35, "Нельзя пользоваться блоками профессий жителей");
            debuffHashMap.put(3.5, "Нельзя проходить через портал в энд");
            debuffHashMap.put(3.75, "Нельзя бить игроков");
            debuffHashMap.put(3.9, "Нельзя торговать с жителями");
            debuffHashMap.put(4.0, "Нельзя проходить через портал в Незер");
            //Баффы
            buffHashMap.put(4.1, "Эффект удачи 3го уровня");
            buffHashMap.put(4.2, "Двойной лут с мобов");
            buffHashMap.put(4.3, "Убийство мобов восстанавливает 1 сердце");
            buffHashMap.put(4.4, "Иммунитет ко всем эффектам плохих зелий");
            buffHashMap.put(4.5, "Бонус к оценке игроков");
            buffHashMap.put(4.7, "Сопротивление ко всему урону на 50%");
            buffHashMap.put(4.8, "Система поощрения: раз в минуту выдается рандомный предмет, если инвентарь не заполнен");
            buffHashMap.put(4.9, "Ваш ник виден в боссбаре ближайшим игрокам");

            sender.sendMessage(ChatColor.AQUA + "Ваши текущие дебаффы:");
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
                if (dbHandler.getRating(p) > 4.0) sender.sendMessage(" - Дебаффов нет!");
            } catch (IOException e) {
                e.printStackTrace();
            }
            sender.sendMessage(ChatColor.AQUA + "Ваши текущие баффы (они работают, если ваше количество голосов больше нуля):");
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
                if (dbHandler.getRating(p) < 4.1) sender.sendMessage(" - Баффов нет!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
