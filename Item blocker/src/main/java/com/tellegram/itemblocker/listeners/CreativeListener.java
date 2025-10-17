package com.tellegram.itemblocker.listeners;

import com.tellegram.itemblocker.ItemBlockerPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;

public class CreativeListener implements Listener {

    private final ItemBlockerPlugin plugin;

    public CreativeListener(ItemBlockerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCreativeSet(InventoryCreativeEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            if (!plugin.shouldEnforce(p)) return;
        }
        ItemStack cursor = e.getCursor();
        ItemStack current = e.getCurrentItem();
        if (plugin.isBlocked(cursor) || plugin.isBlocked(current)) {
            e.setCancelled(true);
            e.setCursor(null);
            e.setCurrentItem(null);
        }
    }
}