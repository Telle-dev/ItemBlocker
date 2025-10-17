# ItemBlocker by `Tellegram`

Blocks crafting, using, placing, dropping, and picking up configured materials. Cleans items from all inventories, including nested containers (`shulker`-in-`shulker`, `Inventory`). Configurable denial message. Bypass permission supported.

## Features
- Block by `Material` only; names, NBT, enchants ignored.
- Prevent crafting on all stations: workbench, anvil, smithing, stonecutter, loom, cartography, brewing, furnace outputs.
- Prevent use/place/eat/interact and remove held item.
- Prevent dropping and picking up; delete world drops.
- Clean on join, world change, inventory close.
- Protect creative inventory.
- Filter death drops and block hopper transfers.
- Optional removal logging.
- Extra coverage: inventory drag and dispenser dispensing.
- Per-world and gamemode flags (`allowed-worlds`, `blocked-worlds`, `allow-in-creative`).

## Compatibility
- Servers: `Paper`, `Purpur`, `Pufferfish`
- Versions: `1.20.x – 1.21.x`
- Java: `17+`
- No `NMS`/`CraftBukkit`
- Compiles against `paper-api` `1.20.6`

## Download & Install
1. Download the latest JAR:  
   `https://github.com/Tellegram/ItemBlocker/releases/latest/download/ItemBlocker.jar`
2. Put it into `plugins/`.
3. Restart the server.  
   With PlugMan: `/plugman load ItemBlocker`
4. Edit `plugins/ItemBlocker/config.yml` if needed, then run `/itemblock reload`.

### CLI (optional)
```bash
wget https://github.com/Tellegram/ItemBlocker/releases/latest/download/ItemBlocker.jar -o plugins/ItemBlocker.jar
Configuration
blocked-items:
  - BUNDLE
  - BEDROCK
  - COMMAND_BLOCK
  - YOUR_ITEM

message: "This item is currently disabled!"
log-removals: true

scan-on:
  join: true
  world-change: true
  inventory-close: true

bypass-permission: "itemblocker.bypass"
silent-remove: false

# World/game mode flags
allowed-worlds: []
blocked-worlds: []
allow-in-creative: false
Commands
/itemblock reload — reload configuration

/itemblock check <ITEM> — report if a material is blocked

/itemblock scan <player|*> — clean inventories now; use `*` to scan all online players
Alias: /blockitem (same subcommands)

Permission
itemblocker.bypass — exempt from blocking and removals

Notes
Scans are O(n) per inventory; recursion limited by container nesting.

Messages show only for player-triggered actions and when silent-remove=false.

Cancel first, then remove, to avoid dupes.

Maintained by Tellegram