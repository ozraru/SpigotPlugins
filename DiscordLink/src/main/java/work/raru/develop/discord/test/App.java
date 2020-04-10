package work.raru.develop.discord.test;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Hello world!
 *
 */
public class App extends ListenerAdapter
{
    public static void main( String[] args )
    {
    	System.out.println("Ready1");
		JDABuilder b = new JDABuilder();
		JDA jda = null;
    	System.out.println("Ready2");
		try {
			jda = b.setToken("Njk3ODM1MTU4NDI2MDkxNzAw.Xo9pMg.vO8hqV9IuiU7dRQ9swcaXISntYA").build();
		} catch (LoginException e) {
			e.printStackTrace();
		}
    	System.out.println("Ready3");
		jda.addEventListener(new App());
    	System.out.println("Ready4");
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent msg) {
    	System.out.println("msg1");
    	try {
    		String string = msg.getMessage().getContentRaw();
        	System.out.println("msg2");
    		if (msg.getTextChannel().getName().equalsIgnoreCase("BotTest")) {
    	    	System.out.println("msg3");
    			if (string.contains("Hello, bot")) {
    		    	System.out.println("msg4");
    				msg.getTextChannel().sendMessage("Hello, "+msg.getAuthor().getName()).queue();
    			}
    	    	System.out.println("msg5");
    			msg.getGuild().addRoleToMember(msg.getMember(), msg.getJDA().getRolesByName("Helloer", true).get(0)).queue();
    		}
        	System.out.println("msg6");
    		if (string.startsWith("!linkmc")) {
    	    	System.out.println("msg7");
    			msg.getMember();
    		}
    	} catch (Exception e) {
        	System.out.println("msg8");
    		e.printStackTrace();
    	}
    }
}
