package fr.olympa.bot.teamspeak;

import fr.olympa.bot.bungee.TeamspeakCommand;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class TeamspeakListener implements Listener {

	@EventHandler
	public void PlayerQuitEvent(final PlayerDisconnectEvent event) {
		final ProxiedPlayer player = event.getPlayer();
		TeamspeakCommand.remove(player);
	}
}
