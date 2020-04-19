package work.raru.spigot.rankmanager.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import work.raru.spigot.rankmanager.Main;

public class RoleUtils {
	JDA jda = Main.jda;
	Guild guild = ConfigConvertUtils.getGuild();
	public void addRole(Member user, Role role) {
		guild.addRoleToMember(user, role).queue();
	}
	public void addRole(long discordId, long roleId) {
		Member user = guild.getMemberById(discordId);
		Role role = guild.getRoleById(roleId);
		addRole(user, role);
	}
	public void addRole(long discordId, String groupName) {
		Member user = guild.getMemberById(discordId);
		Role role = ConfigConvertUtils.getRole(groupName);
		addRole(user, role);
	}
	public void removeRole(Member user, Role role) {
		guild.removeRoleFromMember(user, role).queue();
	}
	public void removeRole(long discordId, long roleId) {
		Member user = guild.getMemberById(discordId);
		Role role = guild.getRoleById(roleId);
		removeRole(user, role);
	}
	public void removeRole(long discordId, String groupName) {
		Member user = guild.getMemberById(discordId);
		Role role = ConfigConvertUtils.getRole(groupName);
		removeRole(user, role);
	}
}
