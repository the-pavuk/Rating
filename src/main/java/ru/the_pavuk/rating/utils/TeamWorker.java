package ru.the_pavuk.rating.utils;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TeamWorker {
    public static void createTeam(Player p){
        Scoreboard score = p.getScoreboard();
        Team t = score.getTeam(p.getName());
        if (!isTeamExist(t)){
            t = score.registerNewTeam(p.getName());
        }
        if (!playerHasEntry(t, p)){
            t.addEntry(p.getName());
        }

    }
    public static void setPrefix(Team t, String prefix){
        t.setPrefix(prefix);
    }
    public static void deleteTeam(Player p) {
        Scoreboard score = p.getScoreboard();
        Team t = score.getTeam(p.getName());
        if (t != null)
            t.unregister();
    }
    public static Team getTeam(Player p){
        return p.getScoreboard().getTeam(p.getName());
    }
    private static boolean isTeamExist(Team t){
        return t != null;
    }
    private static boolean playerHasEntry(Team t, Player p){
        return t.hasEntry(p.getName());
    }

}
