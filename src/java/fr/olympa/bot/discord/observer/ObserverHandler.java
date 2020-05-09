package fr.olympa.bot.discord.observer;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.dv8tion.jda.api.entities.Message;

public class ObserverHandler {

	public static boolean logVoice = false;
	public static boolean logMsgs = true;
	public static boolean logUsername = false;
	public static boolean logAttachment = true;
	public static boolean logEntries = true;
	public static boolean logRoles = true;

	public static Cache<Long, MessageCache> messageCache = CacheBuilder.newBuilder().expireAfterWrite(12, TimeUnit.HOURS).build();

	public static void addMessageCache(Message message) {
		messageCache.put(message.getIdLong(), new MessageCache(message));

	}

	public static MessageCache getMessageCache(long idLong) {
		return messageCache.asMap().get(idLong);
	}
}
