package work.raru.spigot.user_switcher;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.query.QueryOptions;

public class Main extends JavaPlugin {
	Logger logger = getLogger();
	LuckPerms LPapi;
	@Override
	public void onEnable() {
		logger.info("Enabled");
		RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
		if (provider != null) {
		    LPapi = provider.getProvider();
		}
	}
	
	@Override
	public void onDisable() {
		logger.info("Disabled");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("login")) {
			sender.sendMessage("login.");
			
			if (args.length != 2) {
				return false;
			}
			
			User User = LPapi.getUserManager().getUser(args[0]);
			Player player = Bukkit.getPlayer(args[0]);
			String sAfterGroup = args[1];
			Group AfterGroup = LPapi.getGroupManager().getGroup(sAfterGroup);

			if (User == null) {
				sender.sendMessage("Not found user");
				return false;
			}
			if (player == null) {
				sender.sendMessage("Player must be online");
				return true;
			}
			if (AfterGroup == null) {
				sender.sendMessage("Not found group");
				return false;
			}
			QueryOptions query = LPapi.getContextManager().getStaticQueryOptions();
			String isAccountAfter = AfterGroup.getCachedData().getMetaData(query).getMetaValue("Account");
			if (isAccountAfter != null && isAccountAfter.equals("true")) {
				String sBeforeGroup = User.getPrimaryGroup();
				Group BeforeGroup = LPapi.getGroupManager().getGroup(sBeforeGroup);
				Path BeforeData = null;
				String isAccountBefore = BeforeGroup.getCachedData().getMetaData(query).getMetaValue("Account");
				if (isAccountBefore != null && isAccountBefore.equals("true")) {
					BeforeData = Paths.get("accountdata/"+sBeforeGroup+".dat");
					sender.sendMessage(BeforeData.toAbsolutePath().toString());
				}
				User.data().remove(Node.builder("group."+sBeforeGroup).build());
				User.data().add(Node.builder("group."+sAfterGroup).build());
				User.setPrimaryGroup(sAfterGroup);
				Path PlayerData = Paths.get(Bukkit.getWorlds().get(0).getName()+"/playerdata/"+Bukkit.getPlayer(args[0]).getUniqueId()+".dat");
				Path AfterData = Paths.get("accountdata/"+sAfterGroup+".dat");
				Bukkit.getPlayer(args[0]).kickPlayer("Logging in. Please reconnect.");
				if (BeforeData == null) {
					sender.sendMessage("Not found before account");
					BeforeData = Paths.get("accountdata/Before-bak.dat");
				}
				sender.sendMessage(BeforeData.toAbsolutePath().toString());
				sender.sendMessage(PlayerData.toAbsolutePath().toString());
				sender.sendMessage(AfterData.toAbsolutePath().toString());
				try {
					Files.move(PlayerData,BeforeData);
					sender.sendMessage("logout sucsess!");
				} catch (Exception e) {
					sender.sendMessage(e.getClass().getName());
					sender.sendMessage(e.getMessage());
					sender.sendMessage("logout failed!");
					return false;
				}
				try {
					Files.move(AfterData, PlayerData);
					sender.sendMessage("login sucsess!");
					LPapi.getUserManager().saveUser(User);
					return true;
				} catch (Exception e) {
					sender.sendMessage("login failed!");
					sender.sendMessage(e.getClass().getName());
					sender.sendMessage(e.getMessage());
					return true;
				}
			} else {
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
			sender.sendMessage("logout.");
			
			if (args.length != 1) {
				return false;
			}
			User User = LPapi.getUserManager().getUser(args[0]);

			if (User == null) {
				sender.sendMessage("Not found user");
				return false;
			}
			QueryOptions query = LPapi.getContextManager().getStaticQueryOptions();
			String sBeforeGroup = User.getPrimaryGroup();
			Group BeforeGroup = LPapi.getGroupManager().getGroup(sBeforeGroup);
			Path BeforeData = null;
			String isAccountBefore = BeforeGroup.getCachedData().getMetaData(query).getMetaValue("Account");
			if (isAccountBefore != null && isAccountBefore.equals("true")) {
				BeforeData = Paths.get("accountdata/"+sBeforeGroup+".dat");
				sender.sendMessage(BeforeData.toAbsolutePath().toString());
			}
			User.data().remove(Node.builder("group."+sBeforeGroup).build());
			Path PlayerData = Paths.get(Bukkit.getWorlds().get(0).getName()+"/playerdata/"+Bukkit.getPlayer(args[0]).getUniqueId()+".dat");
			sender.sendMessage(PlayerData.toAbsolutePath().toString());
			if (BeforeData == null) {
				sender.sendMessage("Not found before account");
				BeforeData = Paths.get("accountdata/Before-bak.dat");
			}
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
