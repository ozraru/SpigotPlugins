package work.raru.spigot.user_switcher;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Main extends JavaPlugin {
	Logger logger = getLogger();
	@Override
	public void onEnable() {
		logger.info("Enabled");
	}
	
	@Override
	public void onDisable() {
		logger.info("Disabled");
	}


//	private File Logout(String player, CommandSender sender) {
//		sender.sendMessage("logout");
//		PermissionUser User = PermissionsEx.getUser(player);
//		if (User == null) {
//			return null;
//		}
//		@SuppressWarnings("deprecation")
//		PermissionGroup[] Groups = User.getGroups();
//		File BeforeData = null;
//		for (PermissionGroup permissionGroup : Groups) {
//			if (permissionGroup.getOption("Account") != null && permissionGroup.getOption("Account").equals("true")) {
//				BeforeData  = new File("accountdata/"+permissionGroup.getName()+".dat");
//				sender.sendMessage(BeforeData.getAbsolutePath());
//			}
//		}
//		if (BeforeData == null) {
//			sender.sendMessage("Not found before account");
//			return null;
//		}
//		econ.getBalance(Bukkit.getPlayer(player));
//		for (PermissionGroup permissionGroup : Groups) {
//			User.removeGroup(permissionGroup);
//		}
//		
//		return BeforeData;
//	}
//	
//	private boolean Login() {
//		if (getServer().getPluginManager().getPlugin("Vault") == null) {
//			return false;
//		}
//		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
//		if (rsp == null) {
//			return false;
//		}
//		econ = rsp.getProvider();
//		return econ != null;
//	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("login")) {
			sender.sendMessage("login.");
			
			if (args.length != 2) {
				return false;
			}
			
			PermissionManager PermissionManager = PermissionsEx.getPermissionManager();
			PermissionGroup LoginGroup = PermissionManager.getGroup(args[1]);

			if (Bukkit.getPlayer(args[0]) == null) {
				sender.sendMessage("Not found user");
				return false;
			} else if (LoginGroup.isVirtual()) {
				sender.sendMessage("Not found group");
				return false;
			} else if (LoginGroup.getOption("Account") != null && LoginGroup.getOption("Account").equals("true")) {
				PermissionUser User = PermissionsEx.getUser(args[0]);
				@SuppressWarnings("deprecation")
				PermissionGroup[] Groups = User.getGroups();
				Path BeforeData = null;
				for (PermissionGroup permissionGroup : Groups) {
					if (permissionGroup.getOption("Account") != null && permissionGroup.getOption("Account").equals("true")) {
						BeforeData = Paths.get("accountdata/"+permissionGroup.getName()+".dat");
						sender.sendMessage(BeforeData.toAbsolutePath().toString());
					}
					User.removeGroup(permissionGroup);
				}
				User.addGroup(LoginGroup);
				Path PlayerData = Paths.get(Bukkit.getWorlds().get(0).getName()+"/playerdata/"+Bukkit.getPlayer(args[0]).getUniqueId()+".dat");
				Path AfterData = Paths.get("accountdata/"+LoginGroup.getName()+".dat");
				sender.sendMessage(PlayerData.toAbsolutePath().toString());
				sender.sendMessage(AfterData.toAbsolutePath().toString());
				Bukkit.getPlayer(args[0]).kickPlayer("Logging in. Please reconnect.");
				if (BeforeData == null) {
					sender.sendMessage("Not found before account");
					BeforeData = Paths.get("accountdata/Before-bak.dat");
				}
				try {
					Files.move(PlayerData,BeforeData);
					sender.sendMessage("logout sucsess!");
				} catch (Exception e) {
					sender.sendMessage(e.getMessage());
					sender.sendMessage("logout failed!");
					return false;
				}
				try {
					Files.move(AfterData, PlayerData);
					sender.sendMessage("login sucsess!");
					return true;
				} catch (Exception e) {
					sender.sendMessage(e.getMessage());
					sender.sendMessage("login failed!");
					return false;
				}
			} else {
				sender.sendMessage(LoginGroup.getOption("Account"));
				sender.sendMessage("true");
				sender.sendMessage("Group is not account");
				return false;
			}
		}
		if (command.getName().equalsIgnoreCase("get_uuid")) {
			if (args.length > 0) {
				if (Bukkit.getServer().getPlayer(args[0]) != null) {
					sender.sendMessage(args[0]+"'s UUID is "+Bukkit.getServer().getPlayer(args[0]).getUniqueId().toString());
					return true;
				} else {
					sender.sendMessage("User not found");
					return true;
				}
			} else if (Bukkit.getPlayer(sender.getName()) != null){
				sender.sendMessage("Your UUID is "+sender.getServer().getPlayer(sender.getName()).getUniqueId().toString());
				return true;
			} else {
				return false;
			}
		}
		if (command.getName().equalsIgnoreCase("logout")) {
			sender.sendMessage("logout");
			if (args.length != 1) {
				return false;
			}
			PermissionUser User = PermissionsEx.getUser(args[0]);
			if (User == null) {
				return false;
			}
			@SuppressWarnings("deprecation")
			PermissionGroup[] Groups = User.getGroups();
			Path BeforeData = null;
			for (PermissionGroup permissionGroup : Groups) {
				if (permissionGroup.getOption("Account") != null && permissionGroup.getOption("Account").equals("true")) {
					BeforeData  = Paths.get("accountdata/"+permissionGroup.getName()+".dat");
					sender.sendMessage(BeforeData.toAbsolutePath().toString());
				}
				User.removeGroup(permissionGroup);
			}
			if (BeforeData == null) {
				sender.sendMessage("Not found before account");
				return false;
			}
			
			Path PlayerData = Paths.get(Bukkit.getWorlds().get(0).getName()+"/playerdata/"+Bukkit.getPlayer(args[0]).getUniqueId()+".dat");
			Bukkit.getPlayer(args[0]).kickPlayer("Logging out. Please reconnect.");
			try {
				Files.move(PlayerData, BeforeData);
				sender.sendMessage("logout sucsess!");
			} catch (Exception e) {
				sender.sendMessage(e.getMessage());
				sender.sendMessage("logout failed!");
				return false;
			}
			return true;
		}
		if (command.getName().equalsIgnoreCase("signup")) {
		}
		return false;
	}
}
