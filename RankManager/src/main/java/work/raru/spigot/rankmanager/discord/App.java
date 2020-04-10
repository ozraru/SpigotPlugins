package work.raru.spigot.rankmanager.discord;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Hello world!
 *
 */
public class App extends ListenerAdapter
{
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
    	System.out.println("way");
    	if (event.getTextChannel().getName().contains("bot")) {
    		if (!event.getAuthor().isBot()) {
    			event.getTextChannel().sendMessage(event.getAuthor().getAsMention()+": "+event.getMessage().getContentRaw()).queue();
    			event.getMessage().delete().queue();
    		}
    	}
    }
    
    
}
