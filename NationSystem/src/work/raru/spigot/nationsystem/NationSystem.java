package work.raru.spigot.nationsystem;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.data.TemporaryNodeMergeStrategy;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class NationSystem extends JavaPlugin {
	Logger logger = getLogger();
	private static Economy econ = null;
	FileConfiguration config;
	FileConfiguration data;
	File dataFile = new File(getDataFolder(), "NationData.yml");
	LuckPerms LPapi;
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
		RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
		if (provider != null) {
		    LPapi = provider.getProvider();
		}
		if (config.getBoolean("debug", false)) {
			logger.setLevel(Level.FINEST);
			logger.info("Enable debug mode");
		}
		logger.fine("This is fine!");
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
		long NowTime = System.currentTimeMillis() / 1000; //Seconds
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
	
	private OfflinePlayer getking(World world) {
		OfflinePlayer king = data.getOfflinePlayer("Worlds."+world.getName()+".King.player");
		long endtime = data.getLong("Worlds."+world.getName()+".King.endtime");
		if (endtime < getTime(0)) {
			return null;
		} else {
			return king;
		}
	}
	
	private boolean setToKing(World world,Player player,long seconds) {
		User user = LPapi.getUserManager().getUser(player.getUniqueId());
		OfflinePlayer BeforeKing = getking(world);
		if (BeforeKing != null) {
			player.sendMessage(BeforeKing.getName());
			User Beforeuser = LPapi.getUserManager().getUser(BeforeKing.getUniqueId());
			Beforeuser.data().remove(Node.builder("group."+config.getString("Worlds."+world.getName()+".kingGroupName")).expiry(data.getLong("Worlds."+world.getName()+".King.endtime")*1000).build());
			LPapi.getUserManager().saveUser(Beforeuser);
		} else {
			player.sendMessage("Not found BeforeKing");
		}
		user.data().add(Node.builder("group."+config.getString("Worlds."+world.getName()+".kingGroupName")).expiry(seconds, TimeUnit.SECONDS).build());
		data.set("Worlds."+world.getName()+".King.player", (OfflinePlayer) player);
		data.set("Worlds."+world.getName()+".King.endtime", getTime(seconds));
		LPapi.getUserManager().saveUser(user);
		return true;
	}
	
	private boolean setKingTime(World world,long seconds) {
		OfflinePlayer king = getking(world);
		if (king == null) {
			return false;
		}
		User user = LPapi.getUserManager().getUser(king.getUniqueId());
		user.data().remove(Node.builder("group."+config.getString("Worlds."+world.getName()+".kingGroupName")).build());
		user.data().add(Node.builder("group."+config.getString("Worlds."+world.getName()+".kingGroupName")).expiry(seconds,TimeUnit.SECONDS).build());
		data.set("Worlds."+world.getName()+".King.endtime", getTime(seconds));
		LPapi.getUserManager().saveUser(user);
		return true;
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
		payer.sendMessage("Update expire time to "+new Date(getTime(seconds)*1000).toString());
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
		payer.sendMessage("Sucsess!");
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
		player.sendMessage("Update expire time to "+new Date(getTime(seconds)*1000).toString());
		setToKing(world, player, seconds);
		saveData();
		return true;
	}
	
	private boolean takeKingCheck(Player player, int TimeUnitAmount, int useEgg, World world) {
		if (!(player.hasPermission("nationsystem.take."+world.getName())||player.hasPermission("nationsystem.take.*"))) {
			player.sendMessage("You don't have Permission");
			return false;
		}
		if (data.getInt("Worlds."+world.getName()+".Shields") >= useEgg) {
			player.sendMessage("Require more than "+data.getInt("Worlds."+world.getName()+".Shields")+" Eggs");
			return false;
		}
		int seconds = TimeUnitAmount * config.getInt("SecondsPerTimeAmount");
		player.sendMessage("Update expire time to "+new Date(getTime(seconds)*1000).toString());
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
		inv.removeItem(new ItemStack(Material.DRAGON_EGG, useEgg));
		if (BorderSize != 0) {
		world.getWorldBorder().setSize(BorderSize, config.getLong("ChangeWorldBoarderDuration"));
		}
		int seconds = TimeUnitAmount * config.getInt("SecondsPerTimeAmount");
		setToKing(world, player, seconds);
		setShield(world, useEgg - getShield(world));
		saveData();
		player.sendMessage("Sucsess!");
		return true;
	}
	private boolean CommandExec(CommandSender sender,HashMap<String,String> subcommands,boolean dry) {
		if (subcommands.get("subcommand").toLowerCase().matches("pay|take|get")) {
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
			
			String sBorderSize = subcommands.get("newBorder");
			double BorderSize;
			if (sBorderSize == null) {
				BorderSize = 0;
			} else {
				try {
					BorderSize = Double.valueOf(subcommands.get("newBorder"));
				} catch (NumberFormatException e) {
					sender.sendMessage("Invalid BorderSize");
					return false;
				}
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
				if (!takeKingCheck(payer, TimeUnitAmount, useEgg, world)) {
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
					logger.fine("Not found world");
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
			User giveto = LPapi.getUserManager().getUser(subcommands.get("giveto"));
			if (giveto == null) {
				sender.sendMessage("Invalid Player to give");
				return true;
			}
			giveto.data().add(Node.builder("nationsystem.get."+world.getName()).expiry(config.getInt("giveSecond"),TimeUnit.SECONDS).build(),TemporaryNodeMergeStrategy.REPLACE_EXISTING_IF_DURATION_LONGER);
			LPapi.getUserManager().saveUser(giveto);
			sender.sendMessage("Sucsess");
			return true;
		}
		if (subcommands.get("subcommand").equalsIgnoreCase("guard")) {
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
					logger.fine("Not found world");
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
			int useEgg;
			try {
				useEgg = Integer.valueOf(subcommands.get("useEgg"));
			} catch (NumberFormatException e) {
				sender.sendMessage("Invalid useEgg");
				return false;
			}
			PlayerInventory inv = player.getInventory();
			if (!inv.contains(Material.DRAGON_EGG, useEgg)) {
				player.sendMessage("You don't have Required DragonEggs");
				return true;
			}
			inv.removeItem(new ItemStack(Material.DRAGON_EGG, useEgg));
			setShield(world, getShield(world)+useEgg);
		}
		if (subcommands.get("subcommand").equalsIgnoreCase("info")) {
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
					logger.fine("Not found world");
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
			sender.sendMessage("World name: "+world.getName());
			OfflinePlayer king = getking(world);
			if (king == null) {
				sender.sendMessage("King: None");
				sender.sendMessage("King expire unixtime: None");
				sender.sendMessage("King expire time: None");
				sender.sendMessage("Shields: 0");
			} else {
				sender.sendMessage("King: "+king.getName());
				sender.sendMessage("King expire unixtime: "+data.getLong("Worlds."+world.getName()+".King.endtime"));
				sender.sendMessage("King expire time: "+new Date(data.getLong("Worlds."+world.getName()+".King.endtime")*1000));
				sender.sendMessage("Shields: "+getShield(world));
			}
			sender.sendMessage("Size: "+world.getWorldBorder().getSize());
			return true;
		}
		logger.fine("Not found subcommand");
		return false;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		data = YamlConfiguration.loadConfiguration(dataFile);
		HashMap<String,String> subcommands = new HashMap<String, String>();
		boolean dry = false;
		if (command.getName().equalsIgnoreCase("nationsystem")) {
			if (args.length < 1) {
				logger.fine("Too small args");
				return false;
			}
			if (args[0].toLowerCase().matches("pay|paydry")) {
				if (args[0].toLowerCase().matches("paydry")) {
					dry = true;
				}
				if (args.length >= 2 && args.length <= 4) {
					subcommands.put("subcommand", "pay");
					subcommands.put("TimeUnit", args[1]);
					if (args.length >= 3) {
						subcommands.put("newBorder", args[2]);
					}
					if (args.length >= 4) {
						subcommands.put("World", args[3]);
					}
					return CommandExec(sender, subcommands, dry);
				} else {
					logger.fine("args invalid for pay");
					return false;
				}
			}
			if (args[0].toLowerCase().matches("take|takedry")) {
				if (args[0].toLowerCase().matches("takedry")) {
					dry = true;
				}
				if (args.length >= 3 && args.length <= 5) {
					subcommands.put("subcommand", "take");
					subcommands.put("TimeUnit", args[1]);
					subcommands.put("useEgg", args[2]);
					if (args.length >= 4) {
						subcommands.put("newBorder", args[3]);
					}
					if (args.length >= 5) {
						subcommands.put("World", args[4]);
					}
					return CommandExec(sender, subcommands, dry);
				} else {
					logger.fine("args invalid for take");
					return false;
				}
			}
			if (args[0].equalsIgnoreCase("give")) {
				if (args.length >= 2 && args.length <= 3) {
					subcommands.put("subcommand", "give");
					subcommands.put("giveto", args[1]);
					if (args.length >= 3) {
						subcommands.put("World", args[2]);
					}
					return CommandExec(sender, subcommands, dry);
				} else {
					logger.fine("args invalid for give");
					return false;
				}
			}
			if (args[0].equalsIgnoreCase("get")) {
				if (args.length >= 2 && args.length <= 4) {
					subcommands.put("subcommand", "get");
					subcommands.put("TimeUnit", args[1]);
					if (args.length >= 3) {
						subcommands.put("newBorder", args[2]);
					}
					if (args.length >= 4) {
						subcommands.put("World", args[3]);
					}
					return CommandExec(sender, subcommands, dry);
				} else {
					logger.fine("args invalid for get");
					return false;
				}
			}
			if (args[0].equalsIgnoreCase("info")) {
				if (args.length >= 1 && args.length <= 2) {
					subcommands.put("subcommand", "info");
					if (args.length >= 2) {
						subcommands.put("World", args[1]);
					}
					return CommandExec(sender, subcommands, dry);
				} else {
					logger.fine("args invalid for give");
					return false;
				}
			}
			if (args[0].equalsIgnoreCase("reload")) {
				if (sender.hasPermission("nationsystem.reload")) {
					this.reloadConfig();
					sender.sendMessage("Sucsess");
					return true;
				} else {
					sender.sendMessage("You don't have permission");
					return true;
				}
			}
			logger.fine("Not found args[0]");
			return false;
		}
		sender.sendMessage("Error. Code:ns-001");
		return true;
	}
}
