// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CommandRestart.java

package org.croniks.autorestart.commands;

import de.inventivegames.util.title.Title;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.croniks.autorestart.core.Main;

public class CommandRestart implements CommandExecutor {

	public CommandRestart() {
		config = Main.config;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
		if (args.length == 0)
			sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append(cmd.getUsage()).toString());
		else if (args[0].equalsIgnoreCase("help")) {
			if (sender.hasPermission("autorestart.help")) {
				sender.sendMessage((new StringBuilder()).append(ChatColor.GRAY).append("AutoRestart - Help").toString());
				sender.sendMessage((new StringBuilder()).append(ChatColor.GRAY).append("- /autore help - Shows This Help Screen").toString());
				if (sender.hasPermission("autorestart.now"))
					sender.sendMessage((new StringBuilder()).append(ChatColor.GRAY).append("- /autore now - Restart The Server Now!").toString());
				if (sender.hasPermission("autorestart.time"))
					sender.sendMessage((new StringBuilder()).append(ChatColor.GRAY).append("- /autore time - Time Left Until Server Restart").toString());
				if (sender.hasPermission("autorestart.reload"))
					sender.sendMessage((new StringBuilder()).append(ChatColor.GRAY).append("- /autore reload - Reloads Config File").toString());
				if (sender.hasPermission("autorestart.in"))
					sender.sendMessage((new StringBuilder()).append(ChatColor.GRAY).append("- /autore in <minutes> - Sets when the server will restart (In minutes!)").toString());
				if (sender.hasPermission("autorestart.pause"))
					sender.sendMessage((new StringBuilder()).append(ChatColor.GRAY).append("- /autore pause - Pauses the AutoRestart countdown!").toString());
				if (sender.hasPermission("autorestart.start"))
					sender.sendMessage((new StringBuilder()).append(ChatColor.GRAY).append("- /autore start - Starts the AutoRestart countdown!").toString());
			}
		} else if (args[0].equalsIgnoreCase("now")) {
			if (sender.hasPermission("autorestart.now"))
				Main.shutdownServer();
			else
				sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You do not have permission for this sub command!").toString());
		} else if (args[0].equalsIgnoreCase("time")) {
			if (sender.hasPermission("autorestart.time")) {
				Integer hours = Integer.valueOf((Main.timeEnd.intValue() - Main.time.intValue()) / 3600);
				Integer minutes = Integer.valueOf((Main.timeEnd.intValue() - Main.time.intValue()) / 60 - hours.intValue() * 60);
				Integer seconds = Integer.valueOf(Main.timeEnd.intValue() - Main.time.intValue() - minutes.intValue() * 60 - hours.intValue() * 3600);
				if (config.getBoolean("config.popup-enabled.time")) {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						new Title(
								//title
								config.getString("config.popup-messages.time.title").replace("%h", (new StringBuilder()).append(hours).toString())
								.replace("%m", (new StringBuilder()).append(minutes).toString()).replace("%s", (new StringBuilder()).append(seconds).toString()),
								//subtitle
								config.getString("config.popup-messages.time.subtitle").replace("%h", (new StringBuilder()).append(hours).toString())
								.replace("%m", (new StringBuilder()).append(minutes).toString()).replace("%s", (new StringBuilder()).append(seconds).toString()),
								5, 100, 20).send(player);
					} else {
						sender.sendMessage((new StringBuilder(String.valueOf(Main.getPrefix()))).append(
								ChatColor.translateAlternateColorCodes(
										'&',
										config.getString("config.messages.change").replace("%h", (new StringBuilder()).append(hours).toString())
												.replace("%m", (new StringBuilder()).append(minutes).toString()).replace("%s", (new StringBuilder()).append(seconds).toString()))).toString());
					}
				} else {
					sender.sendMessage((new StringBuilder(String.valueOf(Main.getPrefix()))).append(
							ChatColor.translateAlternateColorCodes(
									'&',
									config.getString("config.messages.change").replace("%h", (new StringBuilder()).append(hours).toString())
											.replace("%m", (new StringBuilder()).append(minutes).toString()).replace("%s", (new StringBuilder()).append(seconds).toString()))).toString());
				}
			} else {
				sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You do not have permission for this sub command!").toString());
			}
		} else if (args[0].equalsIgnoreCase("reload")) {
			if (!config.getBoolean("config.multicraft")) {
				if (sender.hasPermission("autorestart.reload")) {
					Main.reloadConfigFile();
					Main.timeEnd = Integer.valueOf((int) (config.getDouble("config.time") * 3600D));
					Main.time = Integer.valueOf(0);
					sender.sendMessage((new StringBuilder()).append(ChatColor.GRAY).append("AutoRestart config has been reloaded! Timer has bee reseted!").toString());
				} else {
					sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You do not have permission for this sub command!").toString());
				}
			} else {
				sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("This command is not supported with MultiCraft!").toString());
			}
		} else if (args[0].equalsIgnoreCase("in")) {
			if (!config.getBoolean("config.multicraft")) {
				if (sender.hasPermission("autorestart.in")) {
					if (args.length != 2)
						sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("Usage: /restart in <minutes>").toString());
					else
						try {
							Integer min = new Integer(args[1]);
							Main.time = Integer.valueOf(Main.timeEnd.intValue() - min.intValue() * 60);
							Integer hours = Integer.valueOf((Main.timeEnd.intValue() - Main.time.intValue()) / 3600);
							Integer minutes = Integer.valueOf((Main.timeEnd.intValue() - Main.time.intValue()) / 60 - hours.intValue() * 60);
							Integer seconds = Integer.valueOf(Main.timeEnd.intValue() - Main.time.intValue() - minutes.intValue() * 60 - hours.intValue() * 3600);
							if (config.getBoolean("config.popup-enabled.change")) {
								Bukkit.getConsoleSender().sendMessage(
										(new StringBuilder("Server will restart in ")).append(hours).append("h ").append(min).append("m ").append(seconds).append("s!").toString());
								
								Title t = new Title(
										//title
										config.getString("config.popup-messages.change.title").replace("%h", (new StringBuilder()).append(hours).toString())
										.replace("%m", (new StringBuilder()).append(minutes).toString()).replace("%s", (new StringBuilder()).append(seconds).toString()),
										//subtitle
										config.getString("config.popup-messages.change.subtitle").replace("%h", (new StringBuilder()).append(hours).toString())
										.replace("%m", (new StringBuilder()).append(minutes).toString()).replace("%s", (new StringBuilder()).append(seconds).toString()),
										5, 100, 20);
								for (Player p : Bukkit.getOnlinePlayers()){
									t.send(p);
								}

							} else {
								Bukkit.broadcastMessage((new StringBuilder(String.valueOf(Main.getPrefix()))).append(
										ChatColor.translateAlternateColorCodes(
												'&',
												config.getString("config.messages.change").replace("%h", (new StringBuilder()).append(hours).toString())
														.replace("%m", (new StringBuilder()).append(minutes).toString()).replace("%s", (new StringBuilder()).append(seconds).toString()))).toString());
							}
						} catch (NumberFormatException ex) {
							sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("Enter <minutes> as a number!").toString());
							sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("Usage: /restart in <minutes>").toString());
						}
				} else {
					sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You do not have permission for this sub command!").toString());
				}
			} else {
				sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("This command is not supported with MultiCraft!").toString());
			}
		} else if (args[0].equalsIgnoreCase("pause")) {
			if (!config.getBoolean("config.multicraft")) {
				if (sender.hasPermission("autorestart.pause")) {
					if (Main.running.booleanValue()) {
						Main.running = Boolean.valueOf(false);
						if (config.getBoolean("config.popup-enabled.status")) {
							Bukkit.getConsoleSender().sendMessage((new StringBuilder()).append(ChatColor.RED).append("The AutoRestart counter has stopped!").toString());
							
							Title t = new Title(
									//title
									config.getString("config.popup-messages.status.pause.title"),
									//subtitle
									config.getString("config.popup-messages.status.pause.subtitle"),
									5, 100, 20);
							for (Player p : Bukkit.getOnlinePlayers()){
								t.send(p);
							}

						} else {
							Bukkit.broadcastMessage((new StringBuilder(String.valueOf(Main.getPrefix()))).append(
									ChatColor.translateAlternateColorCodes('&', config.getString("config.messages.status.pause"))).toString());
						}
					} else {
						sender.sendMessage("AutoRestart is already paused!");
					}
				} else {
					sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You do not have permission for this sub command!").toString());
				}
			} else {
				sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("This command is not supported with MultiCraft!").toString());
			}
		} else if (args[0].equalsIgnoreCase("start")) {
			if (!config.getBoolean("config.multicraft")) {
				if (sender.hasPermission("autorestart.start")) {
					if (!Main.running.booleanValue()) {
						Main.running = Boolean.valueOf(true);
						if (config.getBoolean("config.popup-enabled.status")) {
							Bukkit.getConsoleSender().sendMessage((new StringBuilder()).append(ChatColor.RED).append("The AutoRestart counter has started again!").toString());
							
							Title t = new Title(
									//title
									config.getString("config.popup-messages.status.start.title"),
									//subtitle
									config.getString("config.popup-messages.status.start.subtitle"),
									5, 100, 20);
							for (Player p : Bukkit.getOnlinePlayers()){
								t.send(p);
							}

						} else {
							Bukkit.broadcastMessage((new StringBuilder(String.valueOf(Main.getPrefix()))).append(
									ChatColor.translateAlternateColorCodes('&', config.getString("config.messages.status.start"))).toString());
						}
					} else {
						sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("AutoRestart is already running!").toString());
					}
				} else {
					sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You do not have permission for this sub command!").toString());
				}
			} else {
				sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("This command is not supported with MultiCraft!").toString());
			}
		} else {
			sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append(cmd.getUsage()).toString());
		}
		return true;
	}

	FileConfiguration config;
}
