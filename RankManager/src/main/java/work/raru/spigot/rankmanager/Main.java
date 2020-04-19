package work.raru.spigot.rankmanager;

import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import work.raru.spigot.rankmanager.commands.CommandManager;
import work.raru.spigot.rankmanager.discord.App;

public class Main extends JavaPlugin {
	public static Logger logger;
	public static FileConfiguration config;
	public static JDA jda = null;
	public static LuckPerms LPapi;
	public void initJDA() {
		try {
			jda = new JDABuilder().setToken(config.getString("Discord.BotLoginToken")).build().awaitReady();
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
		logger = getLogger();
		this.saveDefaultConfig();
		config = this.getConfig();
		config.options().copyDefaults(true);
		logger.info("Config Ready");
		LPapi = LuckPermsProvider.get();
		logger.info("LuckPerms Ready");
		int sqlResult = DatabaseManager.init(getDataFolder());
		if (sqlResult == 0) {
			int cleanup = DatabaseManager.cleanup();
			switch (cleanup) {
			case -1:
				logger.warning("Failed to cleanup token");
				break;
			case 0:
				logger.info("Not found row to cleanup");
				break;
			default:
				logger.info("Cleanup "+cleanup+" row(s)");
			}
			logger.info("SQLite Ready");
		} else {
			logger.warning("SQLite initialize Failed. Error: "+sqlResult);
		}
		new Thread(this::initJDA).start();
	}
	@Override
	public void onDisable() {
		if (DatabaseManager.disconnect()) {
			logger.info("Disconnected Database");
		} else {
			logger.warning("Failed to disconnect Database");
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return new CommandManager().CommandExec(sender, command, label, args);
	}
}
