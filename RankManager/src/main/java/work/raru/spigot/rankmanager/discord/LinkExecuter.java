package work.raru.spigot.rankmanager.discord;

import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import work.raru.spigot.rankmanager.Main;
import work.raru.spigot.rankmanager.commands.ArgsConvertUtils;

public class LinkExecuter {
	public static boolean minecraft(CommandSender sender, String[] args) {
		UUID uuid;
		switch (args.length) {
		case 0:
			if (sender instanceof Player) {
				Player target = (Player) sender;
				uuid = target.getUniqueId();
				break;
			} else {
				return false;
			}
		case 1:
			uuid = ArgsConvertUtils.GenerateUUID(sender, args[0]);
			if (uuid == null) {
				sender.sendMessage("Not found User");
				return false;
			}
			break;
		default:
			return false;
		}
		String token = RandomStringUtils.randomAlphanumeric(6);
		int expirationSeconds = Main.config.getInt("Discord.TokenExpirationSeconds");
		if (UserLinkManager.LinkQueue(uuid, token, expirationSeconds)) {
			sender.sendMessage("Please send '!link "+token+"' in discord channnel #"+ConfigConvertUtils.getTextChannel("MinecraftLink").getName()+" in "+expirationSeconds+" seconds");
			return true;
		} else {
			sender.sendMessage("Error Link queue");
			return true;
		}
	}
}
