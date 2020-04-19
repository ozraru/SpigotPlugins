package work.raru.spigot.rankmanager.discord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import work.raru.spigot.rankmanager.DatabaseManager;
import work.raru.spigot.rankmanager.Main;

public class UserLinkManager {
	public static boolean LinkQueue(@Nonnull UUID minecraftUUID, @Nonnull String token, @Nonnull int expireSeconds) {
//		String value = "'"+minecraftUUID.toString()+"','"+token+"','"+LocalDateTime.now().plusSeconds(Main.config.getInt("Discord.TokenExpirationSeconds"))+"'";
		String value = "'" + minecraftUUID.toString() + "','" + token + "','"
				+ LocalDateTime.now().plusSeconds(expireSeconds) + "'";
		try {
			DatabaseManager.DELETE("LinkToken", "minecraft = '" + minecraftUUID.toString() + "'");
			Statement insert = DatabaseManager.INSERT("LinkToken", value, null);
			if (insert.getUpdateCount() == 1) {
				insert.close();
				return DatabaseManager.confirm();
			} else {
				insert.close();
				DatabaseManager.cancel();
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			DatabaseManager.cancel();
			return false;
		}
	}

	public static int useToken(@Nonnull long DiscordId, @Nonnull String token) {
		try {
			ResultSet result = DatabaseManager.SELECT("LinkToken", "minecraft",
					"token = '" + token + "' AND expireTimeStamp > '" + Timestamp.valueOf(LocalDateTime.now()) + "'",
					null);
			if (!result.next()) {
				return 1;
			}
			String minecraft = result.getString("minecraft");
			if (result.next()) {
				return 2;
			}
			result.close();
			if (LinkWrite(UUID.fromString(minecraft), DiscordId)) {
				return 0;
			} else {
				return 3;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			DatabaseManager.cancel();
			return 4;
		}
	}

	public static boolean LinkWrite(@Nonnull UUID minecraftUUID, @Nonnull long DiscordId) {
		String FILTER = "minecraft = '" + minecraftUUID.toString() + "' OR discord = " + DiscordId;
		try {
			DatabaseManager.DELETE("LinkList", FILTER).close();
			Statement result = DatabaseManager.INSERT("LinkList",
					new String[] { "'" + minecraftUUID.toString() + "'", String.valueOf(DiscordId) }, null);
			if (result.getUpdateCount() == 1) {
				result.close();
				return DatabaseManager.confirm();
			} else {
				result.close();
				DatabaseManager.cancel();
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			DatabaseManager.cancel();
			return false;
		}
	}

	public static boolean sync(long discordId) {
		ConfigurationSection roles = Main.config.getConfigurationSection("Discord.RoleID.Group");
		RoleUtils ru = new RoleUtils();

		UUID uuid = UserLinkManager.getMinecraftUUID(discordId);
		if (uuid == null) {
			return false;
		}
		Player minecraft = (Player) Bukkit.getOfflinePlayer(uuid);
		for (String key : roles.getKeys(false)) {
			long roleId = roles.getLong(key);
			if (minecraft == null) {
				ru.removeRole(discordId, roleId);
			} else {
				if (minecraft.hasPermission("group." + key)) {
					ru.addRole(discordId, roleId);
				} else {
					ru.removeRole(discordId, roleId);
				}
			}
		}
		return true;
	}

	public static boolean sync(UUID minecraft) {
		Long discordId = UserLinkManager.getDiscordId(minecraft);
		if (discordId == null) {
			return false;
		}
		return sync(discordId);
	}

	public static Long getDiscordId(UUID minecraftUUID) {
		try {
			ResultSet result = DatabaseManager.SELECT("LinkList", "discord",
					"minecraft = '" + minecraftUUID.toString() + "'", null);
			if (!result.next()) {
				return null;
			}
			Long discordId = result.getLong("discord");
			if (result.next()) {
				return null;
			}
			result.close();
			if (DatabaseManager.confirm()) {
				return discordId;
			} else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			DatabaseManager.cancel();
			return null;
		}
	}

	public static UUID getMinecraftUUID(long discordId) {
		try {
			ResultSet result = DatabaseManager.SELECT("LinkList", "minecraft", "discord = " + discordId, null);
			if (!result.next()) {
				return null;
			}
			String minecraftUUID = result.getString("minecraft");
			if (result.next()) {
				return null;
			}
			result.close();
			if (DatabaseManager.confirm()) {
				return UUID.fromString(minecraftUUID);
			} else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			DatabaseManager.cancel();
			return null;
		}
	}
}
