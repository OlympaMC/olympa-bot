package fr.olympa.bot.bungee;

import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.bot.discord.link.LinkHandler;
import fr.olympa.core.bungee.api.customevent.OlympaGroupChangeEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LinkBungeListener implements Listener {
	
	@EventHandler
	public void onOlympaGroupChange(OlympaGroupChangeEvent event) {
		ProxiedPlayer player = event.getPlayer();
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		ChangeType state = event.getState();
		switch (state) {
		case ADD:
			break;
		case REMOVE:
			break;
		case SET:
			break;
		
		}
	}
	
	@EventHandler
	public void onPlayerDisconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		LinkHandler.removeWaiting(player);
	}
}
