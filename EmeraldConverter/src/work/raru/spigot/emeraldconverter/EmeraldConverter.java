package work.raru.spigot.emeraldconverter;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class EmeraldConverter extends JavaPlugin {
	Logger logger = getLogger();
	private static Economy econ = null;
	FileConfiguration config;
	public void onEnable() {
		if (!setupEconomy() ) {
			logger.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		this.saveDefaultConfig();
		config = this.getConfig();
		config.options().copyDefaults(true);
		logger.info("Enabled");
	}
	
	public void onDisable() {
		logger.info("Disabled");
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	private boolean deposit(Player player, int amount) {
		PlayerInventory inv = player.getInventory();
		if (!inv.contains(Material.EMERALD, amount)) {
			player.sendMessage("You don't have "+amount+" emerald");
			return false;
		}
		ItemStack delete_emerald = new ItemStack(Material.EMERALD, amount);
		EconomyResponse res = econ.depositPlayer(player, amount*config.getDouble("dollar_per_emerald"));
		if (!res.transactionSuccess()) {
			player.sendMessage("Error! Code:ec-101");
			return false;
		}
		inv.removeItem(delete_emerald);
		return true;
	}
	private boolean withdraw(Player player, int amount) {
		PlayerInventory inv = player.getInventory();
		double amountDollar = amount*config.getDouble("dollar_per_emerald");
		if (!econ.has(player, amountDollar)) {
			player.sendMessage("You don't have "+amountDollar+" dollar");
			return false;
		}
		ItemStack add_emerald = new ItemStack(Material.EMERALD, amount);
		EconomyResponse res = econ.withdrawPlayer(player, amountDollar);
		if (!res.transactionSuccess()) {
			player.sendMessage("Error! Code:ec-102");
			return false;
		}
		inv.addItem(add_emerald);
		return true;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		ArrayList<String> subcommand = new ArrayList<String>();
		if (command.getName().equalsIgnoreCase("emeraldconverter")) {
			if (args.length < 1) {
				return false;
			}
			switch (args[0].toLowerCase()) {
			case "deposit":
			case "dep":
			case "d":
				if (args.length == 2) {
					subcommand.add("deposit");
					subcommand.add(args[1]);
				} else {
					return false;
				}
			case "withdraw":
			case "with":
			case "wd":
			case "w":
				if (args.length == 2) {
					subcommand.add("withdraw");
					subcommand.add(args[1]);
				} else {
					return false;
				}
			default:
				return false;
			}
		}
		if (command.getName().equalsIgnoreCase("emeraldconverterdeposit")) {
			if (args.length == 1) {
				subcommand.add("deposit");
				subcommand.add(args[0]);
			} else {
				return false;
			}
		}
		if (command.getName().equalsIgnoreCase("emeraldconverterwithdraw")) {
			if (args.length == 1) {
				subcommand.add("withdraw");
				subcommand.add(args[0]);
			} else {
				return false;
			}
		}
		if (subcommand.get(0) == null) {
			sender.sendMessage("Error! Code:ec-103");
			return false;
		} else {
			if (subcommand.get(0).equals("deposit")) {
				Player player;
				int amount;
				if (!sender.hasPermission("emeraldconverter.deposit")) {
					sender.sendMessage("You don't have permission.");
					return true;
				}
				if (sender instanceof Player) {
					player = (Player) sender;
				} else {
					sender.sendMessage("This command can only call by Player");
					return true;
				}
				try {
					amount = Integer.valueOf(subcommand.get(1));
				} catch (NumberFormatException e) {
					return false;
				}
				if (!(amount > 0)) {
					sender.sendMessage("You can use greater than 0 for amount");
					return false;
				}
				if (deposit(player, amount)) {
					sender.sendMessage("Sucsess!");
					return true;
				} else {
					return true;
				}
			}
			if (subcommand.get(0).equals("withdraw")) {
				Player player;
				int amount;
				if (!sender.hasPermission("emeraldconverter.withdraw")) {
					sender.sendMessage("You don't have permission.");
					return true;
				}
				if (sender instanceof Player) {
					player = (Player) sender;
				} else {
					sender.sendMessage("This command can only call by Player");
					return true;
				}
				try {
					amount = Integer.valueOf(subcommand.get(1));
				} catch (NumberFormatException e) {
					return false;
				}
				if (!(amount > 0)) {
					sender.sendMessage("You can use greater than 0 for amount");
					return false;
				}
				if (withdraw(player, amount)) {
					sender.sendMessage("Sucsess!");
					return true;
				} else {
					return true;
				}
			}
			sender.sendMessage("Error! Code:ec-104");
			return false;
		}
	}
}
