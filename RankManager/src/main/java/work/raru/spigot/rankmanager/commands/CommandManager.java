package work.raru.spigot.rankmanager.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import work.raru.spigot.rankmanager.discord.LinkExecuter;

public class CommandManager {
	public boolean CommandExec(CommandSender sender, Command command, String label, String[] args) {
		switch (command.getName().toLowerCase()) {
		case "rankmanager":
			return NormalCommand.Command(sender, args);
		case "promote":
			return PromoteDemote.promote(sender, args);
		case "demote":
			return PromoteDemote.demote(sender, args);
		case "link":
			return LinkExecuter.minecraft(sender, args);
		default:
			sender.sendMessage("Error, command not found");
			return false;
		}
	}
}
