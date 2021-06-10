package fr.olympa.bot.bungee;

import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.ErrorReaction;
import fr.olympa.bot.discord.OlympaDiscord;
import fr.olympa.bot.discord.guild.GuildHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import redis.clients.jedis.JedisPubSub;

public class SpigotReceiveError extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		int index = message.indexOf(':');
		String serverName = message.substring(0, index);
		String stackTrace = message.substring(index + 1);
		sendError(serverName, stackTrace);
	}

	ArrayListMultimap<String, String> queue = ArrayListMultimap.create();

	public void sendErrorsInQueue() {
		for (Entry<String, String> e : queue.entries())
			sendError(e.getKey(), e.getValue());
		queue.clear();
	}

	public static Cache<String, ErrorReaction> cache = CacheBuilder.newBuilder().recordStats().maximumSize(50).build();

	static {
		fr.olympa.api.utils.CacheStats.addCache("ERROR_TRACE", cache);
	}

	public void sendBungeeError(String stackTrace) {
		sendError("bungee", stackTrace);
	}

	public void sendError(String serverName, String stackTrace) {
		OlympaDiscord olympaDiscord = OlympaBots.getInstance().getDiscord();
		if (olympaDiscord == null || olympaDiscord.getJda() == null) {
			if (!queue.containsKey(serverName) || !queue.get(serverName).contains(stackTrace))
				queue.put(serverName, stackTrace);
			return;
		}
		ErrorReaction errorReaction = cache.getIfPresent(stackTrace);
		if (errorReaction != null) {
			errorReaction.addServerError(serverName);
			Message message = errorReaction.getMessage();
			if (message != null)
				message.editMessage(errorReaction.getMessageTitle()).queue();
			else
				LinkSpigotBungee.Provider.link.sendMessage("§cImpossible de modifier une erreur (de &4serverName = %s&c) sur discord, l'instance message est null.", serverName);
			return;
		}
		TextChannel channelStaffDiscord = GuildHandler.getBugsChannel();
		if (channelStaffDiscord == null) {
			LinkSpigotBungee.Provider.link.sendMessage("§cImpossible de print une erreur (de &4serverName = %s&c) sur discord, le bot discord est pas connecté.", serverName);
			return;
		}
		byte[] byteArrray = stackTrace.getBytes(StandardCharsets.UTF_8);
		ErrorReaction reaction = new ErrorReaction(serverName, stackTrace);
		channelStaffDiscord.sendMessage(ErrorReaction.getDefaultTitle(serverName)).addFile(byteArrray, "error.css").queue(msg -> {
			cache.put(stackTrace, reaction);
			reaction.addToMessage(msg);
			reaction.saveToDB();
		});
	}
}
