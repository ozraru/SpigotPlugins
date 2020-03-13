package work.raru.spigot.rarustools;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class Rarustools extends JavaPlugin {
	Logger logger = getLogger();
	public void onEnable() {
		logger.info("Enabled");
	}
	
	public void onDisable() {
		logger.info("Disabled");
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
				Inventory inv;
				switch (args.length) {
				case 2:
					if (sender instanceof Player) {
						Source = (Player) sender;
						Target = (Player) sender;
					} else {
						return false;
					}
					break;
				case 3:
					if (sender instanceof Player) {
						Target = (Player) sender;
					} else {
						return false;
					}
					Source = Bukkit.getPlayer(args[2]);
					break;
				case 4:
					Source = Bukkit.getPlayer(args[2]);
					Target = Bukkit.getPlayer(args[3]);
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
					Target.openWorkbench(null, true);
					return true;
				
				case "ender_chest":
				case "enderchest":
				case "ender":
				case "end":
				case "ec":
				case "e":
					Target.openInventory(Source.getEnderChest());
					return true;
				
				case "inventory":
				case "inv":
				case "i":
					Target.openInventory(Source.getInventory());
					return true;
				}
			}
		}
		return false;
	}
}
