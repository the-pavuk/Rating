package ru.the_pavuk.rating.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.the_pavuk.rating.Rating;

public class CommandFeedback implements CommandExecutor {
    private final Rating plugin;

    public CommandFeedback(Rating rating) {
        this.plugin = rating;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player){
            TextComponent text = new TextComponent("Нашли баг? Напишите мне в ");
            text.setColor(ChatColor.DARK_AQUA);
            TextComponent text2 = new TextComponent("VK (клик)");
            text2.setColor(ChatColor.BLUE);
            text2.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://vk.com/the__pavuk"));
            text.addExtra(text2);
            text.addExtra("!");
            Player p = (Player) sender;
            p.spigot().sendMessage(text);
        }
        return true;
    }
}
