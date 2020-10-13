package fr.olympa.bot.bungee;

import fr.olympa.bot.OlympaBots;
import redis.clients.jedis.JedisPubSub;

public class SpigotReceiveError extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		int index = message.indexOf(':');
		String serverName = message.substring(0, index);
		String stackTrace = message.substring(index + 1);
		OlympaBots.getInstance().bungeeListener.sendError(serverName, stackTrace);
	}

}
