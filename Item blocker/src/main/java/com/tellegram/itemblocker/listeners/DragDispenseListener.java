package com.tellegram.itemblocker.listeners;

import com.tellegram.itemblocker.ItemBlockerPlugin;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class DragDispenseListener implements Listener {

    private final ItemBlockerPlugin plugin;

    public DragDispenseListener(ItemBlockerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!plugin.shouldEnforce(p)) return;

        ItemStack cursor = e.getCursor();
        boolean blocked = plugin.isBlocked(cursor);
        Material blockedMat = blocked ? cursor.getType() : null;
        int blockedAmount = blocked ? cursor.getAmount() : 0;

        if (!blocked) {
            for (Map.Entry<Integer, ItemStack> entry : e.getNewItems().entrySet()) {
                ItemStack it = entry.getValue();
                if (plugin.isBlocked(it)) {
                    blocked = true;
                    blockedMat = it.getType();
                    blockedAmount += it.getAmount();
                    break;
                }
            }
        }

        if (blocked) {
            e.setCancelled(true);
            plugin.sendDenyMessage(p);
            plugin.logRemoval(p, blockedMat, "inventory-drag", blockedAmount);

            p.setItemOnCursor(null);
        }
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent e) {
        ItemStack it = e.getItem();
        if (!plugin.isBlocked(it)) return;
        e.setCancelled(true);

        BlockState state = e.getBlock().getState();
        if (state instanceof Dispenser disp) {
            Inventory inv = disp.getInventory();
            int removed = 0;
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack s = inv.getItem(i);
                if (s != null && plugin.isBlocked(s)) {
                    removed += s.getAmount();
                    inv.setItem(i, null);
                }
            }
            plugin.logRemoval(null, it.getType(), "dispenser", removed > 0 ? removed : it.getAmount());
        } else {
            plugin.logRemoval(null, it.getType(), "dispenser", it.getAmount());
        }
    }
}
