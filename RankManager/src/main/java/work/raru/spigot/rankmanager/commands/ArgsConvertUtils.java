package work.raru.spigot.rankmanager.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArgsConvertUtils {
	public static UUID GenerateUUID(CommandSender sender, String PlayerIdentity) {
		UUID uuid;
		if (PlayerIdentity == null) {
			if (sender instanceof Player) {
				return ((Player) sender).getUniqueId();
			} else {
				return null;
			}
		} else {
			try {
				uuid = UUID.fromString(PlayerIdentity);
			} catch (IllegalArgumentException e) {
				uuid = null;
			}
			if (uuid == null || Bukkit.getOfflinePlayer(uuid) == null) {
				Player player = Bukkit.getPlayer(PlayerIdentity);
				if (player == null) {
					return null;
				} else {
					uuid = player.getUniqueId();
				}
			}
			return uuid;
		}
	}
}
