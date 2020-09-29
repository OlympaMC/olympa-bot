package fr.olympa.bot.bungee;

import fr.olympa.bot.discord.message.SwearDiscord;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.api.config.BungeeConfigReloadEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class DiscordListener implements Listener {

	@EventHandler
	private void onBungeeConfigReload(BungeeConfigReloadEvent event) {
		String fileName = event.getFileName();
		if (!fileName.equals(OlympaBungee.getInstance().getDefaultConfig().getFileName()))
			return;
		SwearDiscord.updatedConfig();
	}

}
