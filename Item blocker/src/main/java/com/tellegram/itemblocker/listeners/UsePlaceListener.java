package com.tellegram.itemblocker.listeners;

import com.tellegram.itemblocker.ItemBlockerPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class UsePlaceListener implements Listener {

    private final ItemBlockerPlugin plugin;

    public UsePlaceListener(ItemBlockerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!plugin.shouldEnforce(p)) return;
        ItemStack it = e.getItem();
        if (plugin.isBlocked(it)) {
            e.setCancelled(true);
            removeHeldItem(p, e.getHand());
            plugin.sendDenyMessage(p);
            plugin.logRemoval(p, it.getType(), "interact", it.getAmount());
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (!plugin.shouldEnforce(p)) return;
        ItemStack it = e.getItemInHand();
        if (plugin.isBlocked(it)) {
            e.setCancelled(true);
            plugin.sendDenyMessage(p);
            plugin.logRemoval(p, it.getType(), "place", it.getAmount());
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();
        if (!plugin.shouldEnforce(p)) return;
        ItemStack it = e.getItem();
        if (plugin.isBlocked(it)) {
            e.setCancelled(true);
            removeHeldItem(p, EquipmentSlot.HAND);
            plugin.sendDenyMessage(p);
            plugin.logRemoval(p, it.getType(), "consume", it.getAmount());
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Player p = e.getPlayer();
        if (!plugin.shouldEnforce(p)) return;
        ItemStack it = new ItemStack(e.getBucket());
        if (plugin.isBlocked(it)) {
            e.setCancelled(true);
            plugin.sendDenyMessage(p);
            plugin.logRemoval(p, it.getType(), "bucket-empty", it.getAmount());
        }
    }

    private void removeHeldItem(Player p, EquipmentSlot hand) {
        if (hand == EquipmentSlot.OFF_HAND) {
            p.getInventory().setItemInOffHand(null);
        } else {
            p.getInventory().setItemInMainHand(null);
        }
    }
}