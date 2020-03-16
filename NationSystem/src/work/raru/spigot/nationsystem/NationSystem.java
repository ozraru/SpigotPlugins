package work.raru.spigot.nationsystem;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class NationSystem extends JavaPlugin {
	Logger logger = getLogger();
	private static Economy econ = null;
	FileConfiguration config;
	FileConfiguration data;
	File dataFile = new File(getDataFolder(), "NationData.yml");
	public void onEnable() {
		if (!setupEconomy() ) {
			logger.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		this.saveDefaultConfig();
		config = this.getConfig();
		config.options().copyDefaults(true);
		data = YamlConfiguration.loadConfiguration(dataFile);
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
	
	private long getTime(long second) {
		long NowTime = System.currentTimeMillis() * 1000; //Seconds
		long EndTime = NowTime + second;
		return EndTime;
	}
	
	private boolean saveData() {
		try {
			data.save(dataFile);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	private boolean setToKing(World world,Player player,long seconds) {
		PermissionUser user = PermissionsEx.getUser(player);
		PermissionUser Beforeuser = PermissionsEx.getUser((Player) data.getOfflinePlayer("Worlds."+world.getName()+".King.player"));
		user.addGroup(config.getString("Worlds."+world.getName()+".kingGroupName"), null, seconds);
		Beforeuser.removeGroup(config.getString("Worlds."+world.getName()+".kingGroupName"));
		data.set("Worlds."+world.getName()+".King.player", (OfflinePlayer) player);
		data.set("Worlds."+world.getName()+".King.endtime", getTime(seconds));
		return true;
	}
	
	private void setKingTime(World world,long seconds) {
		PermissionUser user = PermissionsEx.getUser((Player) data.getOfflinePlayer("Worlds."+world.getName()+".King.player"));
		user.addGroup(config.getString("Worlds."+world.getName()+".kingGroupName"), null, seconds);
		data.set("Worlds."+world.getName()+".King.endtime", getTime(seconds));
	}
	
	private void setShield(World world, int setAmount) {
		data.set("Worlds."+world.getName()+".Shields", setAmount);
	}
	private int getShield(World world) {
		long NowTime = getTime(0);
		double kingTime = data.getDouble("Worlds."+world.getName()+".King.endtime");
		if (kingTime < NowTime) {
			setShield(world, 0);
			return 0;
		}
		return data.getInt("Worlds."+world.getName()+".Shields");
	}
	
	private double getChangeWorldBoarderCost(World world,double size,Player payer) {
		WorldBorder border = world.getWorldBorder();
		double initialCost = border.getSize()*config.getDouble("Worlds."+world.getName()+".BorderCosts.AlreadyBorderMultiplier");
		double changeAmount = size-border.getSize();
		if (changeAmount == 0) {
			payer.sendMessage("Border size did not change!");
			return 0;
		}
		if (changeAmount > 0) {
			payer.sendMessage("You will add "+changeAmount+" blocks to WorldBoarder size.");
			initialCost += changeAmount*config.getDouble("Worlds."+world.getName()+".BorderCosts.ChangeBorderMultiplier.Positive");
		} else {
			payer.sendMessage("You will remove "+changeAmount+" blocks from WorldBoarder size.");
			initialCost += changeAmount*config.getDouble("Worlds."+world.getName()+".BorderCosts.ChangeBorderMultiplier.Negative");
		}
		payer.sendMessage("You need "+initialCost+" dollars for Temporarily cost.");
		return initialCost;
	}
	
	private double getHoldingCost(World world, Player king, int TimeUnitAmount, double BorderSize) {
		double holdingCostPerTimeUnit = BorderSize*config.getDouble("Worlds."+world.getName()+".HoldingCosts.BorderMultiplier");
		double holdingCost = (holdingCostPerTimeUnit*TimeUnitAmount)*Math.pow(config.getDouble("Worlds."+world.getName()+".HoldingCosts.TimeUnitMultiplier"),TimeUnitAmount);
		king.sendMessage("You need "+holdingCost+" dollars for Holding cost.");
		return holdingCost;
	}
	
	private double getAllCost(World world, Player payer, int TimeUnitAmount, double BorderSize) {
		double TempCost = 0;
		double HoldingCost = 0;
		if (BorderSize == 0) {
			BorderSize = world.getWorldBorder().getSize();
		} else {
			TempCost += getChangeWorldBoarderCost(world, BorderSize, payer);
		}
		HoldingCost += getHoldingCost(world, payer, TimeUnitAmount, BorderSize);
		double AllCosts = TempCost + HoldingCost;
		payer.sendMessage("You need "+AllCosts+" dollars for all costs.");
		return AllCosts;
	}
	
	private boolean pay(Player payer, double cost) {
		if (!econ.has(payer, cost)) {
			payer.sendMessage("You don't have Required dollars.");
			return false;
		}
		EconomyResponse econRes = econ.withdrawPlayer(payer, cost);
		if (econRes.transactionSuccess()) {
			return true;
		} else {
			payer.sendMessage("Error, failed to transaction. Code: ns-101");
			payer.sendMessage(econRes.errorMessage);
			return false;
		}
	}
	
	private boolean payCostCheck(Player payer, int TimeUnitAmount, double BorderSize, World world) {
		if (!(payer.hasPermission("nationsystem.pay."+world.getName())||payer.hasPermission("nationsystem.take.*"))) {
			payer.sendMessage("You don't have Permission");
			return false;
		}
		int seconds = TimeUnitAmount * config.getInt("SecondsPerTimeAmount");
		if (data.getLong("Worlds."+world.getName()+".King.endtime") <= getTime(seconds)) {
			if (BorderSize == 0) {
				payer.sendMessage("Time did not update!");
				return false;
			}
		}
		return true;
	}
	private boolean payCost(Player payer, int TimeUnitAmount, double BorderSize, World world) {
		double Costs = getAllCost(world, payer, TimeUnitAmount, BorderSize);
		if (!pay(payer, Costs)) {
			return true;
		}
		if (BorderSize != 0) {
		world.getWorldBorder().setSize(BorderSize, config.getLong("ChangeWorldBoarderDuration"));
		}
		int seconds = TimeUnitAmount * config.getInt("SecondsPerTimeAmount");
		setKingTime(world, seconds);
		saveData();
		return true;
	}

	private boolean get(Player player, int TimeUnitAmount, double BorderSize, World world) {
		double Costs = getAllCost(world, player, TimeUnitAmount, BorderSize);
		if (!pay(player, Costs)) {
			return true;
		}
		if (BorderSize != 0) {
		world.getWorldBorder().setSize(BorderSize, config.getLong("ChangeWorldBoarderDuration"));
		}
		int seconds = TimeUnitAmount * config.getInt("SecondsPerTimeAmount");
		setToKing(world, player, seconds);
		saveData();
		return true;
	}
	
	private boolean takeKingCheck(Player player, int useEgg, World world) {
		if (!(player.hasPermission("nationsystem.take."+world.getName())||player.hasPermission("nationsystem.take.*"))) {
			player.sendMessage("You don't have Permission");
			return false;
		}
		if (data.getInt("Worlds."+world.getName()+".Shields") >= useEgg) {
			player.sendMessage("Require more than "+data.getInt("Worlds."+world.getName()+".Shields")+" Eggs");
			return false;
		}
		return true;
	}
	private boolean takeKing(Player player, int TimeUnitAmount, double BorderSize, int useEgg, World world) {
		PlayerInventory inv = player.getInventory();
		if (!inv.contains(Material.DRAGON_EGG, useEgg)) {
			player.sendMessage("You don't have Required DragonEggs");
			return true;
		}
		double Costs = getAllCost(world, player, TimeUnitAmount, BorderSize);
		if (!pay(player, Costs)) {
			return true;
		}
		if (BorderSize != 0) {
		world.getWorldBorder().setSize(BorderSize, config.getLong("ChangeWorldBoarderDuration"));
		}
		int seconds = TimeUnitAmount * config.getInt("SecondsPerTimeAmount");
		setToKing(world, player, seconds);
		setShield(world, useEgg - getShield(world));
		saveData();
		return true;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		data = YamlConfiguration.loadConfiguration(dataFile);
		HashMap<String,String> subcommands = new HashMap<String, String>();
		boolean dry = true;
		if (subcommands.get("subcommand").equalsIgnoreCase("pay")||subcommands.get("subcommand").equalsIgnoreCase("take")||subcommands.get("subcommand").equalsIgnoreCase("get")) {
			Player payer;
			if (sender instanceof Player) {
				payer = (Player) sender;
			} else {
				sender.sendMessage("This command only call from Player");
				return true;
			}
			
			String sWorld = subcommands.get("World");
			World world;
			if (sWorld == null) {
				world = payer.getWorld();
			} else {
				world = Bukkit.getWorld(sWorld);
			}
			if (world == null) {
				sender.sendMessage("Invalid world");
				return false;
			}
			if (config.get("Worlds."+world.getName()) == null) {
				sender.sendMessage("Sorry, you can't use " + world.getName());
				return false;
			}
			
			int TimeUnitAmount;
			try {
				TimeUnitAmount = Integer.valueOf(subcommands.get("TimeUnit"));
			} catch (NumberFormatException e) {
				sender.sendMessage("Invalid TimeUnitAmount");
				return false;
			}
			if (TimeUnitAmount < 1) {
				sender.sendMessage("TimeUnitAmount must be at least 1.");
				return false;
			}
			
			double BorderSize;
			try {
				BorderSize = Double.valueOf(subcommands.get("newBorder"));
			} catch (NumberFormatException e) {
				sender.sendMessage("Invalid BorderSize");
				return false;
			}
			if (BorderSize < 0) {
				sender.sendMessage("BorderSize must be at least 0.");
			}

			if (subcommands.get("subcommand").equalsIgnoreCase("pay")) {
				if (!payCostCheck(payer, TimeUnitAmount, BorderSize, world)) {
					return true;
				}
				if (dry) {
					getAllCost(world, payer, TimeUnitAmount, BorderSize);
					return true;
				} else {
					return payCost(payer, TimeUnitAmount, BorderSize, world);
				}
			}
			if (subcommands.get("subcommand").equalsIgnoreCase("get")) {
				if (!(payer.hasPermission("nationsystem.get."+world.getName())||payer.hasPermission("nationsystem.get.*"))) {
					payer.sendMessage("You didn't given");
					return false;
				}
				if (dry) {
					getAllCost(world, payer, TimeUnitAmount, BorderSize);
					return true;
				} else {
					return get(payer, TimeUnitAmount, BorderSize, world);
				}
			}
			if (subcommands.get("subcommand").equalsIgnoreCase("take")) {
				int useEgg;
				try {
					useEgg = Integer.valueOf(subcommands.get("useEgg"));
				} catch (NumberFormatException e) {
					sender.sendMessage("Invalid useEgg");
					return false;
				}
				if (!takeKingCheck(payer, useEgg, world)) {
					return true;
				}
				if (dry) {
					getAllCost(world, payer, TimeUnitAmount, BorderSize);
					return true;
				} else {
					return takeKing(payer, TimeUnitAmount, BorderSize, useEgg, world);
				}
			}
		}
		if (subcommands.get("subcommand").equalsIgnoreCase("give")) {
			Player player = null;
			if (sender instanceof Player) {
				player = (Player) sender;
			}
			
			String sWorld = subcommands.get("World");
			World world;
			if (sWorld == null) {
				if (player != null) {
					world = player.getWorld();
				} else {
					return false;
				}
			} else {
				world = Bukkit.getWorld(sWorld);
			}
			if (world == null) {
				sender.sendMessage("Invalid world");
				return false;
			}
			if (config.get("Worlds."+world.getName()) == null) {
				sender.sendMessage("Sorry, you can't use " + world.getName());
				return false;
			}
			PermissionUser giveto = PermissionsEx.getUser(subcommands.get("giveto"));
			if (giveto == null) {
				sender.sendMessage("Invalid Player to give");
				return true;
			}
			giveto.addTimedPermission("nationsystem.get."+world.getName(), null, config.getInt("giveSecond"));
			return true;
		}
	}
}
