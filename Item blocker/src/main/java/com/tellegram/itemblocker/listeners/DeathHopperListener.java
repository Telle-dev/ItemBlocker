package com.tellegram.itemblocker.listeners;

import com.tellegram.itemblocker.ItemBlockerPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DeathHopperListener implements Listener {

    private final ItemBlockerPlugin plugin;

    public DeathHopperListener(ItemBlockerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (e.getDrops() == null) return;
        Player p = e.getEntity();
        if (!plugin.shouldEnforce(p)) return;

        for (ItemStack stack : new java.util.ArrayList<>(e.getDrops())) {
            if (plugin.isBlocked(stack)) {
                plugin.logRemoval(p, stack.getType(), "death-drops", stack.getAmount());
                e.getDrops().remove(stack);
            }
        }
    }

    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent e) {
        ItemStack it = e.getItem();
        if (plugin.isBlocked(it)) {
            e.setCancelled(true);

            Inventory src = e.getSource();
            for (int i = 0; i < src.getSize(); i++) {
                ItemStack s = src.getItem(i);
                if (s != null && plugin.isBlocked(s)) {
                    src.setItem(i, null);
                }
            }
        }
    }
}
