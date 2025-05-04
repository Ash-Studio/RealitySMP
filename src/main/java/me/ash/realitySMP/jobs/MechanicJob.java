package me.ash.realitySMP.jobs;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MechanicJob extends Job {
    private static final String GUI_TITLE = ChatColor.DARK_GRAY + "Mechanic Services";
    private final ConcurrentHashMap<UUID, Long> repairCooldowns = new ConcurrentHashMap<>();

    public MechanicJob(RealitySMP plugin) {
        super(plugin, "Mechanic", "Repair vehicles and craft tools", 45.0);
        this.displayItem = new ItemStack(Material.ANVIL);
        ItemMeta meta = displayItem.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GRAY + "Mechanic");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Repair vehicles and craft tools",
                ChatColor.GOLD + "Hourly Pay: $" + hourlyPay
        ));
        displayItem.setItemMeta(meta);
    }

    @Override
    public void performDuty(Player player) {
        if (!isEmployed(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are not employed as a mechanic!");
            return;
        }

        openJobGUI(player);
    }

    @Override
    public void openJobGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

        // Mechanic information
        ItemStack info = createGuiItem(Material.BOOK,
                ChatColor.GOLD + "Mechanic Information",
                ChatColor.GRAY + "Level: " + getPlayerLevel(player.getUniqueId()),
                ChatColor.GRAY + "Experience: " + getPlayerExperience(player.getUniqueId()) + "/" + getExperienceForNextLevel(getPlayerLevel(player.getUniqueId())),
                ChatColor.GRAY + "Pay Rate: $" + hourlyPay + "/hour");
        gui.setItem(4, info);

        // Services
        gui.setItem(10, createGuiItem(Material.MINECART,
                ChatColor.YELLOW + "Repair Vehicle",
                ChatColor.GRAY + "Repair a nearby vehicle",
                ChatColor.GOLD + "Cost for client: $30",
                ChatColor.GREEN + "Click to select"));

        gui.setItem(12, createGuiItem(Material.IRON_PICKAXE,
                ChatColor.YELLOW + "Tool Repair",
                ChatColor.GRAY + "Repair a tool or weapon",
                ChatColor.GOLD + "Cost for client: $25",
                ChatColor.GREEN + "Click to select"));

        gui.setItem(14, createGuiItem(Material.IRON_BLOCK,
                ChatColor.YELLOW + "Custom Crafting",
                ChatColor.GRAY + "Craft specialized items",
                ChatColor.GOLD + "Cost: Varies by item",
                ChatColor.GREEN + "Click to select"));

        gui.setItem(16, createGuiItem(Material.DIAMOND_PICKAXE,
                ChatColor.AQUA + "Tool Enchanting",
                ChatColor.GRAY + "Add efficiency to tools",
                ChatColor.GOLD + "Cost for client: $50",
                ChatColor.GREEN + "Click to select"));

        // Fill empty slots with gray glass
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        player.openInventory(gui);
    }

    public boolean repairVehicle(Player mechanic, Vehicle vehicle, Player client) {
        if (!isEmployed(mechanic.getUniqueId())) {
            mechanic.sendMessage(ChatColor.RED + "You are not employed as a mechanic!");
            return false;
        }

        // Check cooldown
        if (repairCooldowns.containsKey(mechanic.getUniqueId())) {
            long lastRepair = repairCooldowns.get(mechanic.getUniqueId());
            if (System.currentTimeMillis() - lastRepair < 60000) { // 1 minute cooldown
                mechanic.sendMessage(ChatColor.RED + "You must wait before repairing another vehicle!");
                return false;
            }
        }

        // Calculate cost based on mechanic's level (higher level = more commission)
        int mechanicLevel = getPlayerLevel(mechanic.getUniqueId());
        double commission = 1.0 + (mechanicLevel * 0.05); // 5% increase per level
        double repairCost = 30 * commission;

        if (client != null) {
            if (plugin.getEconomy().getBalance(client) < repairCost) {
                mechanic.sendMessage(ChatColor.RED + "Client " + client.getName() + " cannot afford this repair!");
                return false;
            }

            // Process payment
            plugin.getEconomy().withdrawPlayer(client, repairCost);
            plugin.getEconomy().depositPlayer(mechanic, repairCost);

            // Messages
            mechanic.sendMessage(ChatColor.GREEN + "You have repaired " + client.getName() + "'s vehicle and earned $" + repairCost);
            client.sendMessage(ChatColor.GREEN + "Mechanic " + mechanic.getName() + " has repaired your vehicle. ($" + repairCost + ")");
        } else {
            // If no client, mechanic gets experience but no payment
            mechanic.sendMessage(ChatColor.GREEN + "You have repaired a vehicle.");
        }

        // Add experience and set cooldown
        addExperience(mechanic.getUniqueId(), 40);
        repairCooldowns.put(mechanic.getUniqueId(), System.currentTimeMillis());

        return true;
    }

    public boolean repairTool(Player mechanic, ItemStack tool, Player client) {
        if (!isEmployed(mechanic.getUniqueId())) {
            mechanic.sendMessage(ChatColor.RED + "You are not employed as a mechanic!");
            return false;
        }

        if (tool == null || !isTool(tool.getType())) {
            mechanic.sendMessage(ChatColor.RED + "This item cannot be repaired!");
            return false;
        }

        // Check if the tool is already at full durability
        if (!(tool.getItemMeta() instanceof Damageable)) {
            mechanic.sendMessage(ChatColor.RED + "This item cannot be repaired!");
            return false;
        }

        Damageable damageable = (Damageable) tool.getItemMeta();
        if (!damageable.hasDamage()) {
            mechanic.sendMessage(ChatColor.RED + "This item doesn't need repairs!");
            return false;
        }

        // Calculate cost based on mechanic's level and tool material
        int mechanicLevel = getPlayerLevel(mechanic.getUniqueId());
        double commission = 1.0 + (mechanicLevel * 0.05); // 5% increase per level
        double repairCost = calculateToolRepairCost(tool) * commission;

        if (client != null) {
            if (plugin.getEconomy().getBalance(client) < repairCost) {
                mechanic.sendMessage(ChatColor.RED + "Client " + client.getName() + " cannot afford this repair!");
                return false;
            }

            // Process payment
            plugin.getEconomy().withdrawPlayer(client, repairCost);
            plugin.getEconomy().depositPlayer(mechanic, repairCost);

            // Repair the tool
            damageable.setDamage(0);
            tool.setItemMeta((ItemMeta) damageable);

            // Messages
            mechanic.sendMessage(ChatColor.GREEN + "You have repaired " + client.getName() + "'s tool and earned $" + repairCost);
            client.sendMessage(ChatColor.GREEN + "Mechanic " + mechanic.getName() + " has repaired your tool. ($" + repairCost + ")");

            // Add experience
            addExperience(mechanic.getUniqueId(), 25);
        } else {
            // If repairing own tool, reduced cost and experience
            if (plugin.getEconomy().getBalance(mechanic) < repairCost / 2) {
                mechanic.sendMessage(ChatColor.RED + "You cannot afford to repair this tool!");
                return false;
            }

            // Process payment (half cost for self-repair)
            plugin.getEconomy().withdrawPlayer(mechanic, repairCost / 2);

            // Repair the tool
            damageable.setDamage(0);
            tool.setItemMeta((ItemMeta) damageable);

            // Messages
            mechanic.sendMessage(ChatColor.GREEN + "You have repaired your own tool for $" + (repairCost / 2));

            // Add experience (less for self-repair)
            addExperience(mechanic.getUniqueId(), 10);
        }

        return true;
    }

    public void openCustomCraftingGUI(Player mechanic) {
        if (!isEmployed(mechanic.getUniqueId())) {
            mechanic.sendMessage(ChatColor.RED + "You are not employed as a mechanic!");
            return;
        }

        Inventory craftingGui = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Mechanic Crafting");

        // Add available custom recipes based on mechanic level
        int mechanicLevel = getPlayerLevel(mechanic.getUniqueId());

        // Basic recipes (level 1+)
        craftingGui.setItem(10, createGuiItem(Material.IRON_AXE,
                ChatColor.WHITE + "Enhanced Iron Axe",
                ChatColor.GRAY + "A more durable iron axe",
                ChatColor.GOLD + "Cost: $35",
                ChatColor.YELLOW + "Requirements: 3x Iron Ingot, 2x Stick",
                ChatColor.GREEN + "Click to craft"));

        craftingGui.setItem(11, createGuiItem(Material.IRON_PICKAXE,
                ChatColor.WHITE + "Enhanced Iron Pickaxe",
                ChatColor.GRAY + "A more durable iron pickaxe",
                ChatColor.GOLD + "Cost: $35",
                ChatColor.YELLOW + "Requirements: 3x Iron Ingot, 2x Stick",
                ChatColor.GREEN + "Click to craft"));

        // Advanced recipes (level 3+)
        if (mechanicLevel >= 3) {
            craftingGui.setItem(13, createGuiItem(Material.DIAMOND_AXE,
                    ChatColor.AQUA + "Enhanced Diamond Axe",
                    ChatColor.GRAY + "A more durable diamond axe",
                    ChatColor.GOLD + "Cost: $75",
                    ChatColor.YELLOW + "Requirements: 3x Diamond, 2x Stick",
                    ChatColor.GREEN + "Click to craft"));

            craftingGui.setItem(14, createGuiItem(Material.DIAMOND_PICKAXE,
                    ChatColor.AQUA + "Enhanced Diamond Pickaxe",
                    ChatColor.GRAY + "A more durable diamond pickaxe",
                    ChatColor.GOLD + "Cost: $75",
                    ChatColor.YELLOW + "Requirements: 3x Diamond, 2x Stick",
                    ChatColor.GREEN + "Click to craft"));
        }

        // Expert recipes (level 5+)
        if (mechanicLevel >= 5) {
            craftingGui.setItem(16, createGuiItem(Material.NETHERITE_PICKAXE,
                    ChatColor.DARK_PURPLE + "Enhanced Netherite Pickaxe",
                    ChatColor.GRAY + "A more durable netherite pickaxe",
                    ChatColor.GOLD + "Cost: $150",
                    ChatColor.YELLOW + "Requirements: 3x Netherite Ingot, 2x Stick",
                    ChatColor.GREEN + "Click to craft"));

            craftingGui.setItem(19, createGuiItem(Material.ELYTRA,
                    ChatColor.LIGHT_PURPLE + "Repaired Elytra",
                    ChatColor.GRAY + "Repair a damaged elytra",
                    ChatColor.GOLD + "Cost: $200",
                    ChatColor.YELLOW + "Requirements: Damaged Elytra, 4x Phantom Membrane",
                    ChatColor.GREEN + "Click to craft"));
        }

        // Master recipes (level 10+)
        if (mechanicLevel >= 10) {
            craftingGui.setItem(25, createGuiItem(Material.NETHERITE_SWORD,
                    ChatColor.DARK_RED + "Master's Blade",
                    ChatColor.GRAY + "A specially crafted netherite sword",
                    ChatColor.GOLD + "Cost: $300",
                    ChatColor.YELLOW + "Requirements: 4x Netherite Ingot, 2x Blaze Rod, 1x Diamond",
                    ChatColor.GREEN + "Click to craft"));
        }

        // Fill empty slots with gray glass
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < craftingGui.getSize(); i++) {
            if (craftingGui.getItem(i) == null) {
                craftingGui.setItem(i, filler);
            }
        }

        mechanic.openInventory(craftingGui);
    }

    public boolean craftItem(Player mechanic, String itemName, Inventory playerInventory) {
        if (!isEmployed(mechanic.getUniqueId())) {
            mechanic.sendMessage(ChatColor.RED + "You are not employed as a mechanic!");
            return false;
        }

        int mechanicLevel = getPlayerLevel(mechanic.getUniqueId());

        // Check requirements and craft the item
        switch (itemName) {
            case "Enhanced Iron Axe":
                if (hasItems(playerInventory, Material.IRON_INGOT, 3) && hasItems(playerInventory, Material.STICK, 2)) {
                    if (plugin.getEconomy().getBalance(mechanic) < 35) {
                        mechanic.sendMessage(ChatColor.RED + "You cannot afford to craft this item!");
                        return false;
                    }

                    // Remove required items
                    removeItems(playerInventory, Material.IRON_INGOT, 3);
                    removeItems(playerInventory, Material.STICK, 2);

                    // Process payment
                    plugin.getEconomy().withdrawPlayer(mechanic, 35);

                    // Create enhanced tool with better durability
                    ItemStack enhancedAxe = new ItemStack(Material.IRON_AXE);
                    ItemMeta axeMeta = enhancedAxe.getItemMeta();
                    axeMeta.setDisplayName(ChatColor.WHITE + "Enhanced Iron Axe");
                    List<String> axeLore = new ArrayList<>();
                    axeLore.add(ChatColor.GRAY + "Crafted by " + mechanic.getName());
                    axeLore.add(ChatColor.GRAY + "Durability: 125% of normal");
                    axeMeta.setLore(axeLore);
                    enhancedAxe.setItemMeta(axeMeta);

                    // Add item to player's inventory
                    playerInventory.addItem(enhancedAxe);

                    // Add experience
                    addExperience(mechanic.getUniqueId(), 30);

                    mechanic.sendMessage(ChatColor.GREEN + "You have crafted an Enhanced Iron Axe!");
                    return true;
                } else {
                    mechanic.sendMessage(ChatColor.RED + "You don't have the required materials!");
                    return false;
                }

            case "Enhanced Iron Pickaxe":
                if (hasItems(playerInventory, Material.IRON_INGOT, 3) && hasItems(playerInventory, Material.STICK, 2)) {
                    if (plugin.getEconomy().getBalance(mechanic) < 35) {
                        mechanic.sendMessage(ChatColor.RED + "You cannot afford to craft this item!");
                        return false;
                    }

                    // Remove required items
                    removeItems(playerInventory, Material.IRON_INGOT, 3);
                    removeItems(playerInventory, Material.STICK, 2);

                    // Process payment
                    plugin.getEconomy().withdrawPlayer(mechanic, 35);

                    // Create enhanced tool with better durability
                    ItemStack enhancedPickaxe = new ItemStack(Material.IRON_PICKAXE);
                    ItemMeta pickaxeMeta = enhancedPickaxe.getItemMeta();
                    pickaxeMeta.setDisplayName(ChatColor.WHITE + "Enhanced Iron Pickaxe");
                    List<String> pickaxeLore = new ArrayList<>();
                    pickaxeLore.add(ChatColor.GRAY + "Crafted by " + mechanic.getName());
                    pickaxeLore.add(ChatColor.GRAY + "Durability: 125% of normal");
                    pickaxeMeta.setLore(pickaxeLore);
                    enhancedPickaxe.setItemMeta(pickaxeMeta);

                    // Add item to player's inventory
                    playerInventory.addItem(enhancedPickaxe);

                    // Add experience
                    addExperience(mechanic.getUniqueId(), 30);

                    mechanic.sendMessage(ChatColor.GREEN + "You have crafted an Enhanced Iron Pickaxe!");
                    return true;
                } else {
                    mechanic.sendMessage(ChatColor.RED + "You don't have the required materials!");
                    return false;
                }

            case "Enhanced Diamond Axe":
                if (mechanicLevel < 3) {
                    mechanic.sendMessage(ChatColor.RED + "You need to be level 3 or higher to craft this item!");
                    return false;
                }

                if (hasItems(playerInventory, Material.DIAMOND, 3) && hasItems(playerInventory, Material.STICK, 2)) {
                    if (plugin.getEconomy().getBalance(mechanic) < 75) {
                        mechanic.sendMessage(ChatColor.RED + "You cannot afford to craft this item!");
                        return false;
                    }

                    // Remove required items
                    removeItems(playerInventory, Material.DIAMOND, 3);
                    removeItems(playerInventory, Material.STICK, 2);

                    // Process payment
                    plugin.getEconomy().withdrawPlayer(mechanic, 75);

                    // Create enhanced tool
                    ItemStack enhancedAxe = new ItemStack(Material.DIAMOND_AXE);
                    ItemMeta axeMeta = enhancedAxe.getItemMeta();
                    axeMeta.setDisplayName(ChatColor.AQUA + "Enhanced Diamond Axe");
                    List<String> axeLore = new ArrayList<>();
                    axeLore.add(ChatColor.GRAY + "Crafted by " + mechanic.getName());
                    axeLore.add(ChatColor.GRAY + "Durability: 150% of normal");
                    axeMeta.setLore(axeLore);
                    enhancedAxe.setItemMeta(axeMeta);

                    // Add item to player's inventory
                    playerInventory.addItem(enhancedAxe);

                    // Add experience
                    addExperience(mechanic.getUniqueId(), 50);

                    mechanic.sendMessage(ChatColor.GREEN + "You have crafted an Enhanced Diamond Axe!");
                    return true;
                } else {
                    mechanic.sendMessage(ChatColor.RED + "You don't have the required materials!");
                    return false;
                }

                // Add additional cases for other craftable items

            default:
                mechanic.sendMessage(ChatColor.RED + "Unknown item: " + itemName);
                return false;
        }
    }

    public boolean enchantTool(Player mechanic, ItemStack tool, Player client) {
        if (!isEmployed(mechanic.getUniqueId())) {
            mechanic.sendMessage(ChatColor.RED + "You are not employed as a mechanic!");
            return false;
        }

        int mechanicLevel = getPlayerLevel(mechanic.getUniqueId());
        if (mechanicLevel < 2) {
            mechanic.sendMessage(ChatColor.RED + "You must be at least level 2 to enchant tools!");
            return false;
        }

        if (tool == null || !isTool(tool.getType())) {
            mechanic.sendMessage(ChatColor.RED + "This item cannot be enchanted!");
            return false;
        }

        // Calculate cost based on mechanic's level and tool material
        double commission = 1.0 + (mechanicLevel * 0.05); // 5% increase per level
        double enchantCost = 50 * commission;

        if (client != null) {
            if (plugin.getEconomy().getBalance(client) < enchantCost) {
                mechanic.sendMessage(ChatColor.RED + "Client " + client.getName() + " cannot afford this enchantment!");
                return false;
            }

            // Process payment
            plugin.getEconomy().withdrawPlayer(client, enchantCost);
            plugin.getEconomy().depositPlayer(mechanic, enchantCost);

            // Add enchantment to the tool
            // This would typically use the Enchantment API to add enchantments
            // For this example, we'll just update the lore to simulate it

            ItemMeta meta = tool.getItemMeta();
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add(ChatColor.GRAY + "Enhanced by " + mechanic.getName());
            lore.add(ChatColor.GRAY + "Efficiency bonus: +" + mechanicLevel);
            meta.setLore(lore);
            tool.setItemMeta(meta);

            // Messages
            mechanic.sendMessage(ChatColor.GREEN + "You have enhanced " + client.getName() + "'s tool and earned $" + enchantCost);
            client.sendMessage(ChatColor.GREEN + "Mechanic " + mechanic.getName() + " has enhanced your tool. ($" + enchantCost + ")");

            // Add experience
            addExperience(mechanic.getUniqueId(), 45);

            return true;
        } else {
            // If enhancing own tool, reduced cost and experience
            if (plugin.getEconomy().getBalance(mechanic) < enchantCost / 2) {
                mechanic.sendMessage(ChatColor.RED + "You cannot afford to enhance this tool!");
                return false;
            }

            // Process payment (half cost for self-enhancement)
            plugin.getEconomy().withdrawPlayer(mechanic, enchantCost / 2);

            // Add enchantment to the tool
            ItemMeta meta = tool.getItemMeta();
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add(ChatColor.GRAY + "Self-enhanced");
            lore.add(ChatColor.GRAY + "Efficiency bonus: +" + (mechanicLevel - 1));
            meta.setLore(lore);
            tool.setItemMeta(meta);

            // Messages
            mechanic.sendMessage(ChatColor.GREEN + "You have enhanced your own tool for $" + (enchantCost / 2));

            // Add experience (less for self-enhancement)
            addExperience(mechanic.getUniqueId(), 20);

            return true;
        }
    }

    private double calculateToolRepairCost(ItemStack tool) {
        Material material = tool.getType();
        double baseCost = 25.0; // Default cost for iron tools

        if (material.name().contains("DIAMOND")) {
            baseCost = 50.0;
        } else if (material.name().contains("NETHERITE")) {
            baseCost = 100.0;
        } else if (material.name().contains("GOLD")) {
            baseCost = 15.0;
        } else if (material.name().contains("STONE")) {
            baseCost = 10.0;
        } else if (material.name().contains("WOODEN")) {
            baseCost = 5.0;
        }

        // Adjust cost based on damage percentage
        Damageable damageable = (Damageable) tool.getItemMeta();
        int maxDurability = tool.getType().getMaxDurability();
        int damage = damageable.getDamage();
        double damageRatio = (double) damage / maxDurability;

        return baseCost * damageRatio;
    }

    private boolean isTool(Material material) {
        String name = material.name();
        return name.endsWith("_PICKAXE") ||
                name.endsWith("_AXE") ||
                name.endsWith("_SHOVEL") ||
                name.endsWith("_HOE") ||
                name.endsWith("_SWORD") ||
                name.equals("FISHING_ROD") ||
                name.equals("BOW") ||
                name.equals("CROSSBOW") ||
                name.equals("SHEARS") ||
                name.equals("FLINT_AND_STEEL") ||
                name.equals("ELYTRA");
    }

    private boolean hasItems(Inventory inventory, Material material, int amount) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count >= amount;
    }

    private void removeItems(Inventory inventory, Material material, int amount) {
        int remaining = amount;
        for (int i = 0; i < inventory.getSize() && remaining > 0; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == material) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    inventory.setItem(i, null);
                    remaining -= itemAmount;
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
            }
        }
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(line);
        }

        meta.setLore(loreList);
        item.setItemMeta(meta);

        return item;
    }

    public static boolean isMechanicGUI(Inventory inventory) {
        return inventory != null && (inventory.getTitle().equals(GUI_TITLE) ||
                inventory.getTitle().equals(ChatColor.DARK_GRAY + "Mechanic Crafting"));
    }
}