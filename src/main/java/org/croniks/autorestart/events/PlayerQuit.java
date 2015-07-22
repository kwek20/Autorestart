// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PlayerQuit.java

package org.croniks.autorestart.events;

import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.croniks.autorestart.core.Main;

public class PlayerQuit implements Listener {

	public PlayerQuit() {
	}

	public void onPlayerQuit(PlayerQuitEvent event) {
		if (max_player_wait.booleanValue() && Bukkit.getOnlinePlayers().size() - 1 <= Main.config.getInt("config.max-players.amount")) {
			Bukkit.broadcastMessage((new StringBuilder(String.valueOf(Main.getPrefix()))).append(
					ChatColor.translateAlternateColorCodes('&',
							Main.config.getString("config.max-players.shutdown").replace("%d", (new StringBuilder(String.valueOf(Main.config.getInt("config.max-players.delay")))).toString())))
					.toString());
			Main.startShutdownServer();
			max_player_wait = Boolean.valueOf(false);
		}
	}

	public static Boolean max_player_wait = Boolean.valueOf(false);

}
