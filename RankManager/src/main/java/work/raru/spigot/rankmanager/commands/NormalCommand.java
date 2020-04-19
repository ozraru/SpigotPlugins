package work.raru.spigot.rankmanager.commands;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import work.raru.spigot.rankmanager.discord.UserLinkManager;

public class NormalCommand {
	static boolean Command(CommandSender sender, String[] args) {
		if (args.length < 1) {
			return false;
		}
		switch (args[0]) {
		case "sync":
			if (2 >= args.length && args.length >= 1) {
				UUID uuid;
				if (args.length == 1) {
					uuid = ArgsConvertUtils.GenerateUUID(sender, null);
				} else {
					uuid = ArgsConvertUtils.GenerateUUID(sender, args[1]);
				}
				if (uuid == null) {
					sender.sendMessage("Not found user");
					return false;
				}
				if (UserLinkManager.sync(uuid)) {
					sender.sendMessage("Sucsess!");
					return true;
				} else {
					sender.sendMessage("Not found link");
					return true;
				}
			} else {
				return false;
			}
		default:
			return false;
		}
	}
}
