package fr.olympa.bot.discord.reaction;

import java.util.concurrent.ConcurrentMap;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.bot.discord.guild.GuildHandler;
import net.dv8tion.jda.api.entities.Message;

public class AwaitReaction {

	public static Cache<Long, ReactionDiscord> reactions = CacheBuilder.newBuilder()/*.expireAfterWrite(12, TimeUnit.HOURS)*/.build();

	public static void addReaction(Message message, ReactionDiscord reaction) {
		AwaitReaction.reactions.put(message.getIdLong(), reaction);
		for (String emoji : reaction.getEmojis())
			message.addReaction(emoji).queue();
		reaction.setMessageId(message.getIdLong());
		reaction.setOlympaGuildId(GuildHandler.getOlympaGuild(message.getGuild()).getId());
	}

	public static ReactionDiscord get(long messegId) {
		return reactions.asMap().get(messegId);
	}

	public static ReactionDiscord get(Message message) {
		return get(message.getIdLong());
	}

	public static ConcurrentMap<Long, ReactionDiscord> getAll() {
		return reactions.asMap();
	}

	public static void removeReaction(long messageId) {
		AwaitReaction.reactions.invalidate(messageId);
	}
}
