package com.tellegram.itemblocker.listeners;

import com.tellegram.itemblocker.ItemBlockerPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class DropPickupListener implements Listener {

    private final ItemBlockerPlugin plugin;

    public DropPickupListener(ItemBlockerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        ItemStack dropped = e.getItemDrop().getItemStack();
        if (!plugin.shouldEnforce(p)) return;
        if (plugin.isBlocked(dropped)) {
            e.setCancelled(true);

            int toRemove = dropped.getAmount();
            removeFromInventory(p, dropped.getType(), toRemove);
            plugin.sendDenyMessage(p);
            plugin.logRemoval(p, dropped.getType(), "drop", toRemove);

            e.getItemDrop().remove();
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        Entity ent = e.getEntity();
        ItemStack stack = e.getItem().getItemStack();
        if (plugin.isBlocked(stack)) {
            if (ent instanceof Player p && !plugin.shouldEnforce(p)) {
                return;
            }
            e.setCancelled(true);
            e.getItem().remove();
            if (ent instanceof Player p) {
                plugin.sendDenyMessage(p);
                plugin.logRemoval(p, stack.getType(), "pickup", stack.getAmount());
            }
        }
    }

    private void removeFromInventory(Player p, org.bukkit.Material type, int amount) {
        ItemStack[] contents = p.getInventory().getContents();
        for (int i = 0; i < contents.length && amount > 0; i++) {
            ItemStack it = contents[i];
            if (it != null && it.getType() == type) {
                int take = Math.min(amount, it.getAmount());
                it.setAmount(it.getAmount() - take);
                if (it.getAmount() <= 0) contents[i] = null;
                amount -= take;
            }
        }
        p.getInventory().setContents(contents);
    }
}
