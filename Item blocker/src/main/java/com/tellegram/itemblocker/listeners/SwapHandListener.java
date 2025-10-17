package com.tellegram.itemblocker.listeners;

import com.tellegram.itemblocker.ItemBlockerPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class SwapHandListener implements Listener {

    private final ItemBlockerPlugin plugin;

    public SwapHandListener(ItemBlockerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent e) {
        Player p = e.getPlayer();
        if (!plugin.shouldEnforce(p)) return;

        ItemStack main = e.getMainHandItem();
        ItemStack off = e.getOffHandItem();

        boolean blockedMain = plugin.isBlocked(main);
        boolean blockedOff = plugin.isBlocked(off);

        if (blockedMain || blockedOff) {
            e.setCancelled(true);
            if (blockedMain && main != null) {
                p.getInventory().setItemInMainHand(null);
                plugin.logRemoval(p, main.getType(), "swap-hand", main.getAmount());
            }
            if (blockedOff && off != null) {
                p.getInventory().setItemInOffHand(null);
                plugin.logRemoval(p, off.getType(), "swap-hand", off.getAmount());
            }
            plugin.sendDenyMessage(p);
        }
    }
}