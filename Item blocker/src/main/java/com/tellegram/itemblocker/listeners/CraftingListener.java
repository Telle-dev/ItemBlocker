package com.tellegram.itemblocker.listeners;

import com.tellegram.itemblocker.ItemBlockerPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.*;

public class CraftingListener implements Listener {

    private final ItemBlockerPlugin plugin;

    public CraftingListener(ItemBlockerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent e) {
        ItemStack result = e.getInventory().getResult();
        if (result != null && plugin.isBlocked(result)) {
            e.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!plugin.shouldEnforce(p)) return;
        ItemStack current = e.getCurrentItem();
        if (plugin.isBlocked(current)) {
            e.setCancelled(true);
            e.getInventory().setResult(null);
            plugin.sendDenyMessage(p);
            plugin.logRemoval(p, current.getType(), "craft", current.getAmount());
        }
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent e) {
        ItemStack result = e.getResult();
        if (result != null && plugin.isBlocked(result)) {
            e.setResult(null);
        }
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent e) {
        ItemStack result = e.getResult();
        if (result != null && plugin.isBlocked(result)) {
            e.setResult(null);
        }
    }

    @EventHandler
    public void onStonecutterClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!plugin.shouldEnforce(p)) return;
        InventoryView view = e.getView();
        if (view == null || view.getTopInventory() == null) return;
        if (view.getTopInventory().getType() != InventoryType.STONECUTTER) return;

        ItemStack current = e.getCurrentItem();
        if (plugin.isBlocked(current)) {
            e.setCancelled(true);
            e.setCurrentItem(null);
            plugin.sendDenyMessage(p);
            plugin.logRemoval(p, current.getType(), "stonecutter-click", current.getAmount());
        }
    }

    @EventHandler
    public void onBrew(BrewEvent e) {
        BrewerInventory inv = e.getContents();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack it = inv.getItem(i);
            if (plugin.isBlocked(it)) {
                inv.setItem(i, null);
            }
        }
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent e) {
        ItemStack result = e.getResult();
        if (result != null && plugin.isBlocked(result)) {
            e.setResult(new ItemStack(Material.AIR));
        }
    }

    @EventHandler
    public void onFurnaceExtract(FurnaceExtractEvent e) {
        if (plugin.isBlocked(e.getItemType())) {
            Player p = e.getPlayer();
            int amount = e.getItemAmount();
            if (p != null && !plugin.hasBypass(p)) {
                removeFromInventory(p, e.getItemType(), amount);
                plugin.sendDenyMessage(p);
                plugin.logRemoval(p, e.getItemType(), "furnace-extract", amount);
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
