package ru.the_pavuk.rating.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RatingChangeEvent extends Event {
    private final Player player;
    private static final HandlerList handlerList = new HandlerList();

    public RatingChangeEvent(Player p){
        this.player = p;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
    public static @NotNull HandlerList getHandlerList() {
        return handlerList;
    }
}
