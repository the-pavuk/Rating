package ru.the_pavuk.rating.datatypes;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PlayerRate implements ConfigurationSerializable {
    private String nickname;
    private Integer rate;
    private boolean bonus;
    public PlayerRate(String nickname, Integer rate, boolean bonus){
        this.nickname = nickname;
        this.rate = rate;
        this.bonus = bonus;
    }

    public String getNickname() {
        return nickname;
    }

    public Integer getRate() {
        return rate;
    }

    public boolean isBonus() {
        return bonus;
    }

    public void setNickname(String nickname){
        this.nickname = nickname;
    }
    public void setRate(Integer rate){
        this.rate = rate;
    }
    public void setBonus(Boolean bonus){
        this.bonus = bonus;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("nickname", nickname);
        serialized.put("rate", rate);
        serialized.put("bonus", bonus);
        return serialized;
    }
    public static PlayerRate deserialize(Map<String, Object> deserialize){
        return new PlayerRate(String.valueOf(deserialize.get("nickname")), NumberConversions.toInt("rate"), Boolean.parseBoolean("bonus"));
    }
}
