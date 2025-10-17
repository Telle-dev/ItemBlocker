package com.tellegram.itemblocker;

import com.tellegram.itemblocker.commands.ItemBlockCommand;
import org.bukkit.Bukkit;
import com.tellegram.itemblocker.listeners.*;
import com.tellegram.itemblocker.util.InventoryScanner;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ItemBlockerPlugin extends JavaPlugin {

    private Set<Material> blockedMaterials = new HashSet<>();
    private String denyMessage = "This item is currently disabled!";
    private boolean logRemovals = true;
    private boolean scanOnJoin = true;
    private boolean scanOnWorldChange = true;
    private boolean scanOnInventoryClose = true;
    private String bypassPermission = "itemblocker.bypass";
    private boolean silentRemove = false;
    private List<String> allowedWorlds = new ArrayList<>();
    private List<String> blockedWorlds = new ArrayList<>();
    private boolean allowInCreative = false;

    private final Map<java.util.UUID, Long> lastLogByPlayer = new ConcurrentHashMap<>();
    private static final long LOG_THROTTLE_MS = 3000L;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadSettings();

        logAsciiHeader();

        getServer().getPluginManager().registerEvents(new CraftingListener(this), this);
        getServer().getPluginManager().registerEvents(new UsePlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new DropPickupListener(this), this);
        getServer().getPluginManager().registerEvents(new ScanListener(this), this);
        getServer().getPluginManager().registerEvents(new CreativeListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathHopperListener(this), this);
        getServer().getPluginManager().registerEvents(new DragDispenseListener(this), this);
        getServer().getPluginManager().registerEvents(new SwapHandListener(this), this);

        Objects.requireNonNull(getCommand("itemblock")).setExecutor(new ItemBlockCommand(this));
        Objects.requireNonNull(getCommand("itemblock")).setTabCompleter(new ItemBlockCommand(this));

        scanOnlinePlayersOnEnable();
    }

    public void reloadSettings() {
        reloadConfig();
        FileConfiguration cfg = getConfig();

        List<String> names = cfg.getStringList("blocked-items");
        Set<Material> newBlocked = new HashSet<>();
        for (String n : names) {
            try {
                Material m = Material.valueOf(n.trim().toUpperCase(Locale.ROOT));
                newBlocked.add(m);
            } catch (IllegalArgumentException ex) {
                getLogger().warning("Unknown material in config blocked-items: " + n);
            }
        }
        blockedMaterials = Collections.unmodifiableSet(newBlocked);

        denyMessage = cfg.getString("message", denyMessage);
        logRemovals = cfg.getBoolean("log-removals", logRemovals);

        ConfigurationSection scan = cfg.getConfigurationSection("scan-on");
        if (scan != null) {
            scanOnJoin = scan.getBoolean("join", scanOnJoin);
            scanOnWorldChange = scan.getBoolean("world-change", scanOnWorldChange);
            scanOnInventoryClose = scan.getBoolean("inventory-close", scanOnInventoryClose);
        }

        bypassPermission = cfg.getString("bypass-permission", bypassPermission);
        silentRemove = cfg.getBoolean("silent-remove", silentRemove);

        allowedWorlds = cfg.getStringList("allowed-worlds");
        blockedWorlds = cfg.getStringList("blocked-worlds");
        allowInCreative = cfg.getBoolean("allow-in-creative", allowInCreative);
    }

    public boolean isBlocked(ItemStack stack) {
        return stack != null && stack.getType() != Material.AIR && blockedMaterials.contains(stack.getType());
    }

    public boolean isBlocked(Material material) {
        return material != null && blockedMaterials.contains(material);
    }

    public boolean hasBypass(Player p) {
        return p != null && bypassPermission != null && !bypassPermission.isEmpty() && p.hasPermission(bypassPermission);
    }

    
    public boolean shouldEnforce(Player p) {
        if (p == null) return true;
        if (hasBypass(p)) return false;
        if (allowInCreative && p.getGameMode() == GameMode.CREATIVE) return false;
        String world = p.getWorld() != null ? p.getWorld().getName() : null;
        if (world == null) return true;
        if (allowedWorlds != null && !allowedWorlds.isEmpty()) {
            return allowedWorlds.contains(world);
        }
        if (blockedWorlds != null && !blockedWorlds.isEmpty()) {
            return blockedWorlds.contains(world);
        }
        return true;
    }

    public void sendDenyMessage(Player p) {
        if (!silentRemove && p != null) {
            p.sendMessage(net.md_5.bungee.api.ChatColor.RED + denyMessage);
        }
    }

    public void logRemoval(Player p, Material mat, String context, int amount) {
        if (!logRemovals) return;

        if (p != null) {
            long now = System.currentTimeMillis();
            Long last = lastLogByPlayer.get(p.getUniqueId());
            if (last != null && (now - last) < LOG_THROTTLE_MS) {
                return;
            }
            lastLogByPlayer.put(p.getUniqueId(), now);
        }
        try {
            String playerName = p != null ? p.getName() : "-";
            String world = p != null && p.getWorld() != null ? p.getWorld().getName() : "-";
            String coords = p != null && p.getLocation() != null
                    ? String.format(Locale.ROOT, "(%.1f, %.1f, %.1f)", p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ())
                    : "-";
            getLogger().info(String.format(Locale.ROOT,
                    "Removed: player=%s material=%s context=%s world=%s coords=%s amount=%d",
                    playerName, mat, context, world, coords, amount));
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Failed to log removal", e);
        }
    }

    public void scanPlayerInventories(Player p, boolean includeOpenTopInventory) {
        InventoryScanner.scanAll(this, p, includeOpenTopInventory);
    }

    public Set<String> getBlockedMaterialNames() {
        return blockedMaterials.stream().map(Enum::name).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public boolean isScanOnJoin() { return scanOnJoin; }
    public boolean isScanOnWorldChange() { return scanOnWorldChange; }
    public boolean isScanOnInventoryClose() { return scanOnInventoryClose; }
    public boolean isSilentRemove() { return silentRemove; }
    public boolean isAllowInCreative() { return allowInCreative; }

    private void logAsciiHeader() {
        final String border = "==============================================================";
        getLogger().info(border);
        getLogger().info("  _____ _                 _ ____  _            _             ");
        getLogger().info(" |_   _| |__   ___   ___ | | __ )| | ___   ___| | _____ _ __ ");
        getLogger().info("   | | | '_ \\ / _ \\ / _ \\| |  _ \\| |/ _ \\ / __| |/ / _ \\ '__|");
        getLogger().info("   | | | | | | (_) | (_) | | |_) | | (_) | (__|   <  __/ |   ");
        getLogger().info("   |_| |_| |_|\\___/ \\___/|_|____/|_|\\___/ \\___|_|\\_\\___|_|");
        getLogger().info(String.format("  Plugin: %s v%s | API: %s", getDescription().getName(), getDescription().getVersion(), getServer().getBukkitVersion()));
        getLogger().info(border);
    }

    private void scanOnlinePlayersOnEnable() {
        int count = 0;
        for (Player p : getServer().getOnlinePlayers()) {
            if (shouldEnforce(p)) {
                scanPlayerInventories(p, true);
                count++;
            }
        }
        if (count > 0) {
            getLogger().info("Startup scan: cleaned inventories for " + count + " online player(s).");
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            int laterCount = 0;
            for (Player p : getServer().getOnlinePlayers()) {
                if (shouldEnforce(p)) {
                    scanPlayerInventories(p, true);
                    laterCount++;
                }
            }
            if (laterCount > 0) {
                getLogger().info("Delayed scan: cleaned inventories for " + laterCount + " online player(s).");
            }
        }, 40L);
    }
}
