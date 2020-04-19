package work.raru.spigot.rankmanager.commands;

import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import work.raru.spigot.rankmanager.Main;

public class PromoteDemote {
	static private String GenerateTrackName(CommandSender sender, @Nonnull String worldName) {
		World world;
		String trackName;
		if (worldName == null) {
			if (sender instanceof Player) {
				world = ((Player) sender).getWorld();
				trackName = Main.config.getString("Worlds."+world.getName()+".TrackName");
				if (trackName == null) {
					sender.sendMessage("You can't use that world");
					return null;
				}
			} else {
				return null;
			}
		} else {
			trackName = Main.config.getString("Worlds."+worldName+".TrackName");
			if (trackName == null) {
				sender.sendMessage("Not found world");
				return null;
			}
		}
		return trackName;
	}
	static boolean promote(CommandSender sender, String[] args) {
		UUID uuid;
		String trackName;
		switch (args.length) {
		case 1:
			trackName = GenerateTrackName(sender, null);
			break;
		case 2:
			trackName = GenerateTrackName(sender, args[1]);
			break;
		default:
			sender.sendMessage("Insufficient arguments");
			return false;
		}
		uuid = ArgsConvertUtils.GenerateUUID(sender, args[0]);
		if (uuid == null) {
			sender.sendMessage("Not found user");
			return false;
		}
		if (trackName == null) {
			return false;
		}
		int result = new GroupUtils().promote(trackName, uuid);
		if (result == 1) {
			sender.sendMessage("Error!");
		}
		if (result == 2) {
			sender.sendMessage("That user didn't link discord");
		}
		return true;
	}
	static boolean demote(CommandSender sender, String[] args) {
		UUID uuid;
		switch (args.length) {
		case 1:
			uuid = ArgsConvertUtils.GenerateUUID(sender, null);
			break;
		case 2:
			uuid = ArgsConvertUtils.GenerateUUID(sender, args[0]);
			break;
		default:
			sender.sendMessage("Insufficient arguments");
			return false;
		}
		if (uuid == null) {
			sender.sendMessage("Not found user");
			return false;
		}
		String trackName = GenerateTrackName(sender, args[1]);
		if (trackName == null) {
			return false;
		}
		int result = new GroupUtils().demote(trackName, uuid);
		if (result == 1) {
			sender.sendMessage("That user didn't link discord");
		}
		if (result == 2) {
			sender.sendMessage("Error!");
		}
		return true;
	}
}
