# ItemBlocker  
by **Tellegram**

Prevents players from using, crafting, dropping, or picking up specific items. Cleans blocked materials from all inventories, including nested shulkers. Works safely without NMS and supports a bypass permission.

---

## Features
- Blocks by `Material` only (no NBT or enchant checks)  
- Prevents crafting in all stations (workbench, anvil, smithing, stonecutter, loom, cartography, brewing, furnace outputs)  
- Blocks use, place, eat, interact, and removes held item  
- Prevents dropping and picking up blocked items  
- Cleans on join, world change, and inventory close  
- Filters death drops and hoppers  
- Protects creative inventory  
- Logs removals (optional)  
- Handles inventory drag and dispenser dispensing  
- Per-world and per-gamemode settings  

---

## Compatibility
| Component | Supported |
|------------|------------|
| Servers | Paper, Purpur, Pufferfish |
| Versions | 1.20.x – 1.21.x |
| Java | 17+ |
| Dependencies | None |
| API | paper-api 1.20.6 |
| NMS | Not used |

---

## Installation
1. Download the latest JAR from:  
   [ItemBlocker Releases](https://github.com/Tellegram/ItemBlocker/releases/latest/download/ItemBlocker.jar)  
2. Move it to your `plugins/` folder.  
3. Restart the server or use `/plugman load ItemBlocker`.  
4. Adjust `plugins/ItemBlocker/config.yml` as needed.  
5. Reload with `/itemblock reload`.

## Example Configuration
```bash
wget https://github.com/Tellegram/ItemBlocker/releases/latest/download/ItemBlocker.jar -O plugins/ItemBlocker.jar
Example Configuration
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

allowed-worlds: []
blocked-worlds: []
allow-in-creative: false
Commands
Command	Description
/itemblock reload	Reload configuration
/itemblock check <ITEM>	Check if material is blocked
`/itemblock scan <player	*>`

Alias: /blockitem (same subcommands)

Permissions
Permission	Description
itemblocker.bypass	Exempts user from blocking and removals

Notes
Scans run in O(n) per inventory; recursion depth is limited.

Messages only appear when silent-remove is false.

Actions are canceled before removal to prevent duplication.

Maintained by Tellegram
