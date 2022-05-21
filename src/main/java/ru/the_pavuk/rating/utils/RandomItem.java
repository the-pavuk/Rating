package ru.the_pavuk.rating.utils;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.the_pavuk.rating.Rating;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RandomItem {
    private List<ItemStack> items = new ArrayList<>();

    public RandomItem(){
        for(Material m : Material.values()) {
            if(m.getMaxDurability() > 1) {
                ItemStack itemStack = null;
                for(int i = 0; i < m.getMaxDurability(); i++) {
                    itemStack = new ItemStack(m, 1, (byte) i);
                }
                items.add(itemStack);
            } else {
                items.add(new ItemStack(m, 1));
            }
        }
    }


    public ItemStack getItem() {
        return new ItemStack(items.get(new Random().nextInt(items.size())));
    }
}
