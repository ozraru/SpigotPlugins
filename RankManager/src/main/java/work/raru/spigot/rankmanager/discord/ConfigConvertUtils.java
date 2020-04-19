package work.raru.spigot.rankmanager.discord;

import org.bukkit.configuration.file.FileConfiguration;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import work.raru.spigot.rankmanager.Main;

public class ConfigConvertUtils {
	static JDA jda = Main.jda;
	static FileConfiguration config = Main.config;
	static Guild getGuild() {
		long guildID = config.getLong("Discord.GuildID");
		return jda.getGuildById(guildID);
	}
	static TextChannel getTextChannel(String ChannelConfig) {
		long channelID = config.getLong("Discord.ChannelID."+ChannelConfig);
		return getGuild().getTextChannelById(channelID);
	}
	static Role getRole(String groupName) {
		long groupID = config.getLong("Discord.RoleID.Group."+groupName);
		return getGuild().getRoleById(groupID);
	}
}
