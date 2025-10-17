package com.tellegram.itemblocker.util;

import com.tellegram.itemblocker.ItemBlockerPlugin;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class InventoryScanner {

    private InventoryScanner() {}

    public static void scanAll(ItemBlockerPlugin plugin, Player p, boolean includeOpenTopInventory) {
        if (p == null) return;

        cleanInventory(plugin, p, p.getInventory(), "player-inventory");

        ItemStack[] armor = p.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            ItemStack it = armor[i];
            ItemStack cleaned = cleanItem(plugin, p, it, "armor");
            armor[i] = cleaned;
        }
        p.getInventory().setArmorContents(armor);

        ItemStack off = p.getInventory().getItemInOffHand();
        p.getInventory().setItemInOffHand(cleanItem(plugin, p, off, "offhand"));

        cleanInventory(plugin, p, p.getEnderChest(), "ender-chest");

        if (includeOpenTopInventory && p.getOpenInventory() != null) {
            Inventory top = p.getOpenInventory().getTopInventory();
            if (top != null) cleanInventory(plugin, p, top, "open-top");
        }
    }

    private static void cleanInventory(ItemBlockerPlugin plugin, Player p, Inventory inv, String context) {
        if (inv == null) return;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack it = inv.getItem(i);
            inv.setItem(i, cleanItem(plugin, p, it, context));
        }
    }

    private static ItemStack cleanItem(ItemBlockerPlugin plugin, Player p, ItemStack it, String context) {
        if (it == null || it.getType() == Material.AIR) return it;

        if (plugin.isBlocked(it)) {
            plugin.logRemoval(p, it.getType(), context, it.getAmount());
            return null;
        }

        ItemMeta meta = it.getItemMeta();
        if (meta instanceof BlockStateMeta blockStateMeta && blockStateMeta.getBlockState() instanceof ShulkerBox box) {
            Inventory boxInv = box.getInventory();
            cleanInventory(plugin, p, boxInv, context + ":shulker");
            blockStateMeta.setBlockState(box);
            it.setItemMeta(blockStateMeta);
            return it;
        }

        try {
            Class<?> bundleMetaClass = Class.forName("org.bukkit.inventory.meta.BundleMeta", false, meta.getClass().getClassLoader());
            if (bundleMetaClass != null && bundleMetaClass.isInstance(meta)) {
                java.lang.reflect.Method getItems = meta.getClass().getMethod("getItems");
                java.lang.reflect.Method setItems = meta.getClass().getMethod("setItems", List.class);
                @SuppressWarnings("unchecked")
                List<ItemStack> items = new ArrayList<>((List<ItemStack>) getItems.invoke(meta));
                boolean changed = false;
                Iterator<ItemStack> iter = items.iterator();
                while (iter.hasNext()) {
                    ItemStack inside = iter.next();
                    if (plugin.isBlocked(inside)) {
                        plugin.logRemoval(p, inside.getType(), context + ":bundle", inside.getAmount());
                        iter.remove();
                        changed = true;
                    } else {
                        ItemStack cleaned = cleanItem(plugin, p, inside, context + ":bundle");
                        if (cleaned == null) {
                            iter.remove();
                            changed = true;
                        } else if (cleaned != inside) {
                            int idx = items.indexOf(inside);
                            if (idx >= 0) {
                                items.set(idx, cleaned);
                            }
                            changed = true;
                        }
                    }
                }
                if (changed) {
                    setItems.invoke(meta, items);
                    it.setItemMeta(meta);
                }
                return it;
            }
        } catch (ClassNotFoundException ignore) {

        } catch (Exception ex) {
            plugin.getLogger().log(java.util.logging.Level.WARNING, "Bundle scan failed", ex);
        }

        return it;
    }
}
