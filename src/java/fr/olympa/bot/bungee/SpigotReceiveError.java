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

	//	public static Cache<Entry<String, String>, List<Message>> cache = CacheBuilder.newBuilder().maximumSize(50).build();
	public static Cache<String, ErrorReaction> cache = CacheBuilder.newBuilder().recordStats().maximumSize(50).build();

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
			message.editMessage(errorReaction.getMessageTitle()).queue(null, x -> {
				cache.invalidate(stackTrace);
				sendError(serverName, stackTrace);
			});
			return;
		}
		TextChannel channelStaffDiscord = GuildHandler.getBugsChannel();
		if (channelStaffDiscord == null) {
			LinkSpigotBungee.Provider.link.sendMessage("&cImpossible de print une erreur (de &4serverName = %s&c) sur discord, le bot discord est pas connecté.", serverName);
			return;
		}
		byte[] byteArrray = stackTrace.getBytes(StandardCharsets.UTF_8);
		ErrorReaction reaction = new ErrorReaction(serverName, stackTrace);
		channelStaffDiscord.sendMessage(ErrorReaction.getDefaultTitle(serverName)).addFile(byteArrray, "error.css").queue(msg -> {
			cache.put(stackTrace, reaction);
			reaction.addToMessage(msg);
			reaction.saveToDB();
		});
		//		SimpleEntry<String, String> entry = new AbstractMap.SimpleEntry<>(serverName, stackTrace);
		//		List<Message> messages = cache.getIfPresent(entry);
		//		if (messages != null) {
		//			Message message = messages.get(0);
		//			String content = message.getContentRaw();
		//			int xIndex = content.lastIndexOf('x');
		//			int times = Integer.parseInt(content.substring(xIndex + 1));
		//			times++;
		//			message.editMessage(content.substring(0, xIndex + 1) + times).queue(null, x -> {
		//				cache.invalidate(entry);
		//				sendError(serverName, stackTrace);
		//			});
		//			return;
		//		}
		//		List<String> strings = new ArrayList<>(2);
		//		int maxSize = 2000 - 50;
		//		if (stackTrace.length() < maxSize)
		//			strings.add(stackTrace);
		//		else {

		//			StringTokenizer tok = new StringTokenizer(stackTrace, "\n");
		//			StringBuilder output = new StringBuilder(stackTrace.length());
		//			int lineLen = 0;
		//			while (tok.hasMoreTokens()) {
		//				String word = tok.nextToken() + "\n";
		//				if (lineLen + word.length() > maxSize) {
		//					strings.add(output.toString());
		//					output = new StringBuilder(stackTrace.length());
		//					lineLen = 0;
		//				}
		//				output.append(word);
		//				lineLen += word.length();
		//			}
		//			strings.add(output.toString());
		//			List<Message> msgs = new ArrayList<>();
		//			cache.put(entry, msgs);
		//			channelStaffDiscord.sendMessage("**Erreur sur " + serverName + "**").append("```css\n" + strings.get(0) + "```\nx1").queue(msg -> {
		//				msgs.add(msg);
		//				ErrorReaction reaction = new ErrorReaction(entry, msg);
		//				reaction.addToMessage(msg);
		//				reaction.saveToDB();
		//				for (int i = 1; i < strings.size(); i++)
		//					channelStaffDiscord.sendMessage("```css\n" + strings.get(i) + "```").queue(m -> {
		//						msgs.add(m);
		//						reaction.addMessage(m);
		//					});
		//			});

		//		}
	}

	//	public void sendErrorFlushInfo() {
	//		TextChannel channelStaffDiscord = GuildHandler.getBugsChannel();
	//		if (channelStaffDiscord != null)
	//			channelStaffDiscord.sendMessage("__Le bot a redémarré ~ vidage du cache__").queue();
	//	}
}
