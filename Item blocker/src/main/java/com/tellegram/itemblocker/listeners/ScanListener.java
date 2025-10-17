package com.tellegram.itemblocker.listeners;

import com.tellegram.itemblocker.ItemBlockerPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class ScanListener implements Listener {

    private final ItemBlockerPlugin plugin;

    public ScanListener(ItemBlockerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!plugin.isScanOnJoin()) return;
        Player p = e.getPlayer();
        if (!plugin.shouldEnforce(p)) return;
        plugin.scanPlayerInventories(p, true);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        if (!plugin.isScanOnWorldChange()) return;
        Player p = e.getPlayer();
        if (!plugin.shouldEnforce(p)) return;
        plugin.scanPlayerInventories(p, true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!plugin.isScanOnInventoryClose()) return;
        if (!(e.getPlayer() instanceof Player p)) return;
        if (!plugin.shouldEnforce(p)) return;
        plugin.scanPlayerInventories(p, true);
    }
}