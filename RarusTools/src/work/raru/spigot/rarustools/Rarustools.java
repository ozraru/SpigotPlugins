package work.raru.spigot.rarustools;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Rarustools extends JavaPlugin {
	Logger logger = getLogger();
	FileConfiguration config;
	public void onEnable() {
		logger.info("Enabled");
		this.saveDefaultConfig();
		config = this.getConfig();
		config.options().copyDefaults(true);
	}
	
	public void onDisable() {
		logger.info("Disabled");
	}
	
	private boolean CheckValidItem(@Nonnull Inventory inv, @Nonnull Material type,@Nullable String name,@Nullable List<String> lore,@Nullable Enchantment ench, int enchLv) {
		HashMap<Integer, ? extends ItemStack> items = inv.all(type);
		System.out.println("Checking");
		for (Integer key : items.keySet()) {
			System.out.println("CheckingItem");
			ItemStack item = items.get(key);
			ItemMeta ItemMeta = item.getItemMeta();
			System.out.println("CheckingItem1");
			if (name!=null && !ItemMeta.getDisplayName().equals(name)) {
				break;
			}
			System.out.println("CheckingItem2");
			if (lore!=null && !ItemMeta.getLore().equals(lore)) {
				break;
			}
			System.out.println("CheckingItem3");
			if (ench!=null && !(ItemMeta.getEnchantLevel(ench)==enchLv)) {
				break;
			}
			System.out.println("CheckingItem4");
			return true;
		}
		return false;
	}
	
	private ItemStack CreateItem(@Nonnull Material type,@Nullable String name,@Nullable List<String> lore,@Nullable Enchantment ench, int enchLv, boolean hideEnch) {
		ItemStack items = new ItemStack(type);
		ItemMeta item = items.getItemMeta();
		item.setDisplayName(name);
		item.setLore(lore);
		item.addEnchant(ench, enchLv, true);
		if (hideEnch) {
			item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		items.setItemMeta(item);
		return items;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("rarustools")) {
			if (args.length < 1) {
				return false;
			}
			switch (args[0].toLowerCase()) {
			case "inventory":
			case "inv":
			case "i":
				if (args.length < 2) {
					return false;
				}
				Player Source;
				Player Target;
				boolean other;
				switch (args.length) {
				case 2:
					if (sender instanceof Player) {
						Source = (Player) sender;
						Target = (Player) sender;
					} else {
						return false;
					}
					other = false;
					break;
				case 3:
					if (sender instanceof Player) {
						Target = (Player) sender;
					} else {
						return false;
					}
					Source = Bukkit.getPlayer(args[2]);
					other = true;
					break;
				case 4:
					Source = Bukkit.getPlayer(args[2]);
					Target = Bukkit.getPlayer(args[3]);
					other = true;
					break;
				default:
					return false;
				}
				if (Source == null) {
					sender.sendMessage("Source not found.");
					return false;
				}
				if (Target == null) {
					sender.sendMessage("Target not found.");
					return false;
				}
				switch (args[1].toLowerCase()) {
				case "crafting_table":
				case "craftingtable":
				case "craft_table":
				case "crafttable":
				case "crafting":
				case "craft":
				case "ct":
				case "c":
				case "workbench":
				case "work":
				case "wb":
				case "w":
					if (other) {
						if (!sender.hasPermission("rarustools.inventorys.crafttable.other")) {
							sender.sendMessage("You don't have permission for other.");
							return true;
						}
					} else {
						if (sender.hasPermission("rarustools.inventorys.crafttable")) {
							if (!(sender.hasPermission("rarustools.inventorys.crafttable.bypass") ||
									CheckValidItem(Source.getInventory(), Material.WORKBENCH, config.getString("Inventorys.Items.Workbench.name"), config.getStringList("Inventorys.Items.Workbench.lore"), Enchantment.getByName(config.getString("Inventorys.Items.Workbench.ench")), config.getInt("Inventorys.Items.Workbench.enchLevel")))) {
								sender.sendMessage("You don't have Portable Crafting Table and bypass permission.");
								return true;
							}
						} else {
							sender.sendMessage("You don't have permission for this command.");
							return true;
						}
					}
					Target.openWorkbench(null, true);
					return true;
				
				case "ender_chest":
				case "enderchest":
				case "ender":
				case "end":
				case "ec":
				case "e":
					if (other) {
						if (!sender.hasPermission("rarustools.inventorys.enderchest.other")) {
							sender.sendMessage("You don't have permission for other.");
							return true;
						}
					} else {
						if (sender.hasPermission("rarustools.inventorys.enderchest")) {
							if (!(sender.hasPermission("rarustools.inventorys.enderchest.bypass") ||
									CheckValidItem(Source.getInventory(), Material.ENDER_CHEST, config.getString("Inventorys.Items.EnderChest.name"), config.getStringList("Inventorys.Items.EnderChest.lore"), Enchantment.getByName(config.getString("Inventorys.Items.EnderChest.ench")), config.getInt("Inventorys.Items.EnderChest.enchLevel")))) {
								sender.sendMessage("You don't have Portable Ender Chest and bypass permission.");
								return true;
							}
						} else {
							sender.sendMessage("You don't have permission for this command.");
							return true;
						}
					}
					Target.openInventory(Source.getEnderChest());
					return true;
				
				case "inventory":
				case "inv":
				case "i":
					if (!sender.hasPermission("rarustools.inventorys.inventory.other")) {
						sender.sendMessage("You don't have permission for this command.");
						return true;
					}
					Target.openInventory(Source.getInventory());
					return true;
				}
			case "items":
			case "item":
				Player Recieve;
				ItemStack item;
				switch (args.length) {
				case 2:
					if (sender instanceof Player) {
						Recieve = (Player) sender;
					} else {
						return false;
					}
					break;
				case 3:
					Recieve = Bukkit.getPlayer(args[2]);
					break;
				default:
					return false;
				}
				switch (args[1].toLowerCase()) {
				case "crafting_table":
				case "craftingtable":
				case "craft_table":
				case "crafttable":
				case "crafting":
				case "craft":
				case "ct":
				case "c":
				case "workbench":
				case "work":
				case "wb":
				case "w":
					if (!sender.hasPermission("rarustools.items.crafttable")) {
						sender.sendMessage("You don't have permission for this command.");
						return true;
					}
					item = CreateItem(Material.WORKBENCH, config.getString("Inventorys.Items.Workbench.name"), config.getStringList("Inventorys.Items.Workbench.lore"), Enchantment.getByName(config.getString("Inventorys.Items.Workbench.ench")), config.getInt("Inventorys.Items.Workbench.enchLevel"), config.getBoolean("Inventorys.Items.Workbench.hideench"));
					break;
				case "ender_chest":
				case "enderchest":
				case "ender":
				case "end":
				case "ec":
				case "e":
					if (!sender.hasPermission("rarustools.items.enderchest")) {
						sender.sendMessage("You don't have permission for this command.");
						return true;
					}
					item = CreateItem(Material.ENDER_CHEST, config.getString("Inventorys.Items.EnderChest.name"), config.getStringList("Inventorys.Items.EnderChest.lore"), Enchantment.getByName(config.getString("Inventorys.Items.EnderChest.ench")), config.getInt("Inventorys.Items.EnderChest.enchLevel"), config.getBoolean("Inventorys.Items.EnderChest.hideench"));
					break;
				default:
					return false;
				}
				Recieve.getInventory().addItem(item);
				sender.sendMessage("Complete!");
				return true;
			case "reload":
				sender.sendMessage("reloading...");
				this.reloadConfig();
				sender.sendMessage("reloaded");
				return true;
			case "debug":
				Player debugger = (Player) sender;
				sender.sendMessage(debugger.getInventory().getItemInMainHand().getEnchantments().toString());
				sender.sendMessage(config.getString("Inventorys.Items.Workbench.name"));
				sender.sendMessage(config.getString("Inventorys.Items.Workbench.ench"));
				sender.sendMessage(Enchantment.getByName(config.getString("Inventorys.Items.Workbench.ench")).getName());
				sender.sendMessage(Enchantment.getByName(config.getString("Inventorys.Items.Workbench.ench")).toString());
			}
		}
		return false;
	}
}
