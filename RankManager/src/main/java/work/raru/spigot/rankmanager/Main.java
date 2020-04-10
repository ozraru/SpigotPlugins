package work.raru.spigot.rankmanager;

import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.pablo67340.SQLiteLib.Main.SQLiteLib;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import work.raru.spigot.rankmanager.discord.App;

public class Main extends JavaPlugin{
	Logger logger = getLogger();
	FileConfiguration config;
	public SQLiteLib sqlLib;
	JDA jda = null;
	public void init() {
		try {
			jda = new JDABuilder().setToken(config.getString("Discord.Token")).build().awaitReady();
			jda.addEventListener(new App());
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.info("Enabled");
	}
	
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		config = this.getConfig();
		config.options().copyDefaults(true);
		logger.info("Config Ready");
       sqlLib = SQLiteLib.hookSQLiteLib(this);
		logger.info("SQLite Ready");
		new Thread(this::init).start();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		sender.sendMessage(jda.getStatus().toString());
		return true;
	}
}
