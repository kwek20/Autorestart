// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PlayerJoin.java

package org.croniks.autorestart.events;

import de.inventivegames.util.title.Title;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.croniks.autorestart.core.Main;

public class PlayerJoin implements Listener {

	public PlayerJoin() {
	}

	public static void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (!Main.updated.booleanValue() && player.isOp()) {
			player.sendMessage((new StringBuilder()).append(ChatColor.RED).append("AutoRestart has an update!").toString());
			new Title(
					//title
					(new StringBuilder()).append(ChatColor.RED).append("AutoRestart Alert!").toString(),
					//subtitle
					(new StringBuilder()).append(ChatColor.RED).append("AutoRestart has an update").toString(),
					5, 100, 10).send(player);;
		}
	}
}
