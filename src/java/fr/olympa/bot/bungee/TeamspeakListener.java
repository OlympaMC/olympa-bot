package fr.olympa.bot.bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class TeamspeakListener implements Listener {

	@EventHandler
	public void onPlayerQuitEvent(final PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		TeamspeakCommand.remove(player);
	}
}
