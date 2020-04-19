package work.raru.spigot.rankmanager.discord;

import java.util.UUID;

import org.bukkit.Bukkit;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import work.raru.spigot.rankmanager.Main;

/**
 * Hello world!
 *
 */
public class App extends ListenerAdapter
{
	TextChannel MinecraftLink = ConfigConvertUtils.getTextChannel("MinecraftLink");
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
    	if (event.isFromGuild()) {
    		if (event.getTextChannel().equals(MinecraftLink)) {
    			String rawContent = event.getMessage().getContentRaw();
    			if (rawContent.matches("!link [0-9A-Za-z]+")) {
    				String token = rawContent.substring(6);
    				long discordId = event.getAuthor().getIdLong();
					TextChannel channel = event.getTextChannel();
    				switch (UserLinkManager.useToken(discordId, token)) {
    				case 0:
    					UUID uuid = UserLinkManager.getMinecraftUUID(discordId);
    					new RoleUtils().addRole(discordId, Main.config.getLong("Discord.RoleID.Special.linked"));
    					channel.sendMessage(event.getAuthor().getAsMention()+" linked to "+Bukkit.getPlayer(uuid)+"("+uuid.toString()+")").queue();
    					break;
    				case 1:
    					channel.sendMessage("Token not found").queue();
    					break;
    				case 2:
    					channel.sendMessage("Token overlapping").queue();
    					break;
    				case 3:
    					channel.sendMessage("Failed to write database").queue();
    					break;
    				case 4:
    					channel.sendMessage("SQL error").queue();
    					break;
    				}
    			}
    		}
    	}
    }
    
    
}
