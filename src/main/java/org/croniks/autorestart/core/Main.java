// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Main.java

package org.croniks.autorestart.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.croniks.autorestart.commands.CommandRestart;
import org.croniks.autorestart.events.PlayerJoin;
import org.croniks.autorestart.events.PlayerQuit;
import org.mcstats.Metrics;

import de.inventivegames.util.title.Title;

public class Main extends JavaPlugin {

	public Main() {
	}

	public void onEnable() {
		
		try {
			if (!getDataFolder().exists())
				saveDefaultConfig();
			
			Metrics metrics = new Metrics(this);
			metrics.start();
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ConfigV = Integer.valueOf(getConfig().getDefaults().getInt("version"));
		if (getConfig().getInt("version") != ConfigV.intValue()) {
			File backup = new File(getDataFolder(), "config_bk.yml");
			if (backup.exists())
				backup.delete();
			(new File(getDataFolder(), "config.yml")).renameTo(backup);
		}
		if (!(new File("getDataFolder")).exists())
			saveResource("config.yml", false);
		timeEnd = Integer.valueOf((int) (getConfig().getDouble("config.time") * 3600D));
		reminders = getConfig().getIntegerList("config.reminder");
		shutdownMessage = getConfig().getString("config.messages.shutdown");
		config = getConfig();
		plugin = this;
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new PlayerJoin(), this);
		pm.registerEvents(new PlayerQuit(), this);
		getCommand("autore").setExecutor(new CommandRestart());
		
		new BukkitRunnable() {
			@Override
			public void run() {
				if (Main.running.booleanValue())
					if (Main.time.intValue() < Main.timeEnd.intValue()) {
						Main.time = Integer.valueOf(Main.time.intValue() + 1);
						for (int i = 0; i < reminders.size(); i++)
							if (Main.time.intValue() == Main.timeEnd.intValue() - ((Integer) reminders.get(i)).intValue() * 60)
								if (getConfig().getBoolean("config.popup-enabled.minutes")) {
									
									Title t = new Title(
											//title
											getConfig().getString("config.popup-messages.minutes.title").replace("%m",
													(new StringBuilder(String.valueOf((Main.timeEnd.intValue() - Main.time.intValue()) / 60))).toString()),
											//subtitle
											getConfig().getString("config.popup-messages.minutes.subtitle").replace("%m",
													(new StringBuilder(String.valueOf((Main.timeEnd.intValue() - Main.time.intValue()) / 60))).toString()),
											5, 100, 10);
									for (Player p : Bukkit.getOnlinePlayers()){
										t.send(p);
									}

									System.out.println((new StringBuilder("[AutoRestart] Server restart in ")).append((Main.timeEnd.intValue() - Main.time.intValue()) / 60).append(" minutes!")
											.toString());
								} else {
									Bukkit.broadcastMessage((new StringBuilder(String.valueOf(Main.getPrefix()))).append(
											ChatColor.translateAlternateColorCodes(
													'&',
													getConfig().getString("config.messages.minutes").replace("%m",
															(new StringBuilder(String.valueOf((Main.timeEnd.intValue() - Main.time.intValue()) / 60))).toString()))).toString());
								}

						if (getConfig().getBoolean("config.remind-seconds") && Main.timeEnd.intValue() - Main.time.intValue() < getConfig().getInt("config.seconds-countdown") + 1)
							if (getConfig().getBoolean("config.popup-enabled.seconds")) {
								
								Title t = new Title(
										//title
										getConfig().getString("config.popup-messages.seconds.title").replace("%s",
											(new StringBuilder(String.valueOf(Main.timeEnd.intValue() - Main.time.intValue()))).toString()),
										//subtitle
										getConfig().getString("config.popup-messages.seconds.subtitle").replace("%s",
											(new StringBuilder(String.valueOf(Main.timeEnd.intValue() - Main.time.intValue()))).toString()),
										5, 100, 10);
								for (Player p : Bukkit.getOnlinePlayers()){
									t.send(p);
								}

								System.out.println((new StringBuilder("[AutoRestart] Server restart in ")).append(Main.timeEnd.intValue() - Main.time.intValue()).append(" seconds").append("!")
										.toString());
							} else {
								Bukkit.broadcastMessage((new StringBuilder(String.valueOf(Main.getPrefix()))).append(
										ChatColor.translateAlternateColorCodes(
												'&',
												getConfig().getString("config.messages.seconds").replace("%s",
														(new StringBuilder(String.valueOf(Main.timeEnd.intValue() - Main.time.intValue()))).toString()))).toString());
							}
						if (getConfig().getBoolean("config.commands-enabled")) {
							List<?> commands = getConfig().getStringList("config.commands");
							if (Main.timeEnd.intValue() - Main.time.intValue() < getConfig().getInt("config.commands-time")) {
								String cmd;
								for (Iterator<?> iterator2 = commands.iterator(); iterator2.hasNext(); Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd))
									cmd = (String) iterator2.next();

							}
						}
					} else {
						Main.shutdownServer();
						Main.running = Boolean.valueOf(false);
					}
			}
		}.runTaskTimer(this, 20, 20);
	}

	public static void shutdownServer() {
		if (config.getBoolean("config.multicraft"))
			Bukkit.broadcastMessage((new StringBuilder()).append(ChatColor.RED).append("Waiting for MutliCraft to restart server!").toString());
		else if (config.getBoolean("config.max-players.enabled")) {
			if (!PlayerQuit.max_player_wait.booleanValue()) {
				if (Bukkit.getOnlinePlayers().size() <= config.getInt("config.max-players.amount")) {
					forceShutdownServer();
				} else {
					Bukkit.broadcastMessage((new StringBuilder(String.valueOf(getPrefix()))).append(
							ChatColor.translateAlternateColorCodes('&',
									config.getString("config.max-players.message").replace("%a", (new StringBuilder(String.valueOf(config.getInt("config.max-players.amount")))).toString())))
							.toString());
					PlayerQuit.max_player_wait = Boolean.valueOf(true);
				}
			} else {
				Bukkit.broadcastMessage((new StringBuilder(String.valueOf(getPrefix()))).append(ChatColor.AQUA).append("Force restarting server, starting ")
						.append(config.getInt("config.max-players.delay")).append(" timer!").toString());
				startShutdownServer();
			}
		} else {
			forceShutdownServer();
		}
	}

	private static void forceShutdownServer() {
		for (Iterator<?> iterator = Bukkit.getOnlinePlayers().iterator(); iterator.hasNext();) {
			Player oplayer = (Player) iterator.next();
			Player player = Bukkit.getPlayer(oplayer.getName());
			try {
				player.kickPlayer(ChatColor.translateAlternateColorCodes('&', shutdownMessage));
			} catch (IllegalStateException e) {
				player.sendMessage("Server restarting!");
			}
		}

		World world;
		for (Iterator<?> iterator1 = Bukkit.getWorlds().iterator(); iterator1.hasNext(); System.out.println((new StringBuilder("[AutoRestart] ")).append(world.getName()).append(" has been saved!")
				.toString())) {
			world = (World) iterator1.next();
			world.save();
		}

		Bukkit.getServer().shutdown();
	}

	public static void startShutdownServer() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

			public void run() {
				Main.forceShutdownServer();
			}

		}, 20L * (long) config.getInt("config.max-players.delay"));
	}

	public static String getPrefix() {
		return ChatColor.translateAlternateColorCodes('&', config.getString("config.messages.prefix"));
	}

	private void checkUpdate(final Plugin plugin) {
		Thread check = new Thread(new Runnable() {

			public void run() {
				try {
					HttpURLConnection con = (HttpURLConnection) (new URL(org.croniks.autorestart.core.Main.URL)).openConnection();
					con.setRequestMethod("GET");
					con.setRequestProperty("User-Agent", "Mozilla/5.0");
					BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String version = null;
					String line;
					while ((line = reader.readLine()) != null)
						if (line.contains("<span class=\"muted\">") && line.contains("h1"))
							version = line.split("<span class=\"muted\">")[1].split("<")[0];
					reader.close();
					if (version.equalsIgnoreCase(plugin.getDescription().getVersion())) {
						System.out.println("[AutoRestart] AutoRestart is up to date!");
					} else {
						System.out.println((new StringBuilder("[AutoRestart] AutoRestart came out with a new version! v")).append(version).toString());
						System.out.println(org.croniks.autorestart.core.Main.URL);
						Main.updated = Boolean.valueOf(false);
					}
				} catch (IOException e) {
					System.out.println("[AutoRestart] has failed to check for updates! Please check your firewall, or internet connection.");
					System.out.println("[AutoRestart] if everything works, then Spigotmc is unable to be reached.");
				}
			}
		});
		check.start();
	}

	public static void reloadConfigFile() {
		plugin.reloadConfig();
	}

	private static Main plugin;
	public static Integer time = Integer.valueOf(0);
	public static Integer timeEnd = Integer.valueOf(0);
	private List<?> reminders;
	public static String shutdownMessage;
	public static Boolean updated = Boolean.valueOf(true);
	public static String URL = "http://www.spigotmc.org/resources/autorestart.2538/";
	public static Boolean running = Boolean.valueOf(true);
	public static Integer ConfigV;
	public static FileConfiguration config;

}
