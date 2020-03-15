package work.raru.spigot.nationsystem;

import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.World.Spigot;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class NationSystem extends JavaPlugin {
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
	
	private boolean setToKing(World world,Player player,long time) {
		PermissionUser user = PermissionsEx.getUser(player); 
		user.addGroup(config.getString("Worlds."+world.getName()+".kingGroupName"), null, time);
		return true;
	}
	
	private double getChangeWorldBoarderCost(World world,double size,Player king) {
		WorldBorder border = world.getWorldBorder();
		Double initialCost = border.getSize()*config.getDouble("Worlds."+world.getName()+".BorderCosts.AlreadyBorderMultiplier");
		double changeAmount = size-border.getSize();
		if (changeAmount == 0) {
			king.sendMessage("Border size did not change!");
			return 0;
		}
		if (changeAmount > 0) {
			king.sendMessage("You will add "+changeAmount+" blocks to WorldBoarder size.");
			initialCost += changeAmount*config.getDouble("Worlds."+world.getName()+".BorderCosts.ChangeBorderMultiplier.Positive");
		} else {
			king.sendMessage("You will remove "+changeAmount+" blocks from WorldBoarder size.");
			initialCost += changeAmount*config.getDouble("Worlds."+world.getName()+".BorderCosts.ChangeBorderMultiplier.Negative");
		}
		king.sendMessage("You need "+initialCost+" dollars for Temporarily cost.");
		return initialCost;
	}
	
	private double getHoldingCost(World world, Player king, int TimeUnitAmount, int BorderSize) {
		double holdingCostPerTimeUnit = BorderSize*config.getDouble("Worlds."+world.getName()+".HoldingCosts.BorderMultiplier");
		double holdingCost = (holdingCostPerTimeUnit*TimeUnitAmount)*Math.pow(config.getDouble("Worlds."+world.getName()+".HoldingCosts.TimeUnitMultiplier"),TimeUnitAmount);
		return holdingCost;
	}
}
