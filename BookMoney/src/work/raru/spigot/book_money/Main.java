package work.raru.spigot.book_money;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class Main extends JavaPlugin {
	Logger logger = getLogger();
	private static Economy econ = null;
	@Override
	public void onEnable() {
		if (!setupEconomy() ) {
			logger.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		logger.info("Enabled");
	}

	@Override
	public void onDisable() {
		logger.info("Disabled");
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	private boolean create(Player player, Double amount) {
		PlayerInventory inv = player.getInventory();
		ItemStack books = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta book = (BookMeta) books.getItemMeta();
		book.setTitle("The money");
		book.setAuthor("Lemonation");
		book.setUnbreakable(true);
		book.setGeneration(BookMeta.Generation.COPY_OF_COPY);
		List<BaseComponent[]> pages = new ArrayList<>();
		BaseComponent Button = new TextComponent("この本を使って受け取る!\n(ここをクリックすると\n/bmuが実行されます)\n");
		Button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bmu"));
		Button.setColor(net.md_5.bungee.api.ChatColor.RED);
		Button.setUnderlined(true);
		Button.setBold(true);
		BaseComponent[] page1 = new BaseComponent[3];
		page1[0] = new TextComponent("金額: "+amount+"\n");
		page1[1] = Button;
		page1[2] = new TextComponent("\n1ページ目：目次\n2ページ目：金額(処理用)\n3ページ目：その他");
		pages.add(page1);
		BaseComponent[] page2 = new BaseComponent[1];
		page2[0] = new TextComponent(String.valueOf(amount));
		pages.add(page2);
		BaseComponent[] page3 = new BaseComponent[1];
		page3[0] = new TextComponent("Created by "+player.getName());
		pages.add(page3);
		book.spigot().setPages(pages);
		books.setItemMeta(book);
		EconomyResponse econRes = econ.withdrawPlayer(player, amount);
		if (econRes.transactionSuccess()) {
			inv.addItem(books);
			return true;
		} else {
			return false;
		}
	}
	
	private boolean use(Player player) {
		PlayerInventory inv = player.getInventory();
		ItemStack item = inv.getItemInMainHand();
		ItemStack books;
		if (item.getType() == Material.WRITTEN_BOOK) {
			books = item;
		} else {
			player.sendMessage("You must have a book in mainhand!");
			return false;
		}
		BookMeta book = (BookMeta) books.getItemMeta();
		if (!book.getTitle().equals("The money") ||
				!book.getAuthor().equals("Lemonation") ||
				!book.getGeneration().equals(BookMeta.Generation.COPY_OF_COPY)) {
			player.sendMessage("invalid book!");
			return false;
		}
		Double amount;
		try {
			amount = Double.valueOf(book.getPage(2))*books.getAmount();
		} catch (NumberFormatException e) {
			player.sendMessage("invalid book's amount!");
			return false;
		}
		EconomyResponse econRes = econ.depositPlayer(player,amount);
		if (econRes.transactionSuccess()) {
			inv.setItemInMainHand(null);
			player.sendMessage("Sucsess!");
			return true;
		} else {
			player.sendMessage("Error! ErrorCode:bm-103");
			return true;
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("bm")) {
			Player player;
			if (args.length < 1) {
				return false;
			}
			switch (args[0]) {
			case "create":
				if (!sender.hasPermission("book_money.create")) {
					sender.sendMessage("Sorry, you don't have permission.");
					return true;
				}
				if (args.length == 2) {
					if (sender instanceof Player) {
						player = (Player) sender;
					} else {
						return false;
					}
				} else {
					return false;
				}
				Double amount;
				try {
					amount = Double.valueOf(args[1]);
				} catch (NumberFormatException e) {
					return false;
				}
				if (amount <= 0) {
					sender.sendMessage("Amount must be greater than 0.");
					return false;
				}
				if (econ.getBalance(player) < amount) {
					sender.sendMessage("You don't have $"+String.valueOf(amount));
					return true;
				}
				if (create(player,amount)) {
					sender.sendMessage("Sucsess!");
					return true;
				} else {
					sender.sendMessage("Error! ErrorCode:bm-101");
					return true;
				}
			case "use":
				if (!sender.hasPermission("book_money.use")) {
					sender.sendMessage("Sorry, you don't have permission.");
					return true;
				}
				if (args.length == 2) {
					if (sender instanceof Player) {
						player = (Player) sender;
					} else {
						return false;
					}
				} else {
					return false;
				}
				if (use(player)) {
					return true;
				} else {
					return false;
				}
			default:
				return false;
			}
		}
		if (command.getName().equalsIgnoreCase("bmc")) {
			Player player;
			if (args.length == 1) {
				if (sender instanceof Player) {
					player = (Player) sender;
				} else {
					return false;
				}
			} else {
				return false;
			}
			Double amount;
			try {
				amount = Double.valueOf(args[0]);
			} catch (NumberFormatException e) {
				return false;
			}
			if (amount <= 0) {
				sender.sendMessage("Amount must be greater than 0.");
				return false;
			}
			if (econ.getBalance(player) < amount) {
				sender.sendMessage("You don't have $"+String.valueOf(amount));
				return true;
			}
			if (create(player,amount)) {
				sender.sendMessage("Sucsess!");
				return true;
			} else {
				sender.sendMessage("Error! ErrorCode:bm-102");
				return true;
			}
		}
		if (command.getName().equalsIgnoreCase("bmu")) {
			Player player;
			if (args.length == 0) {
				if (sender instanceof Player) {
					player = (Player) sender;
				} else {
					return false;
				}
			} else {
				return false;
			}
			if (use(player)) {
				sender.sendMessage("Sucsess!");
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
}
