package fr.olympa.bot.discord.sql;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.bot.discord.api.DiscordMember;
import fr.olympa.bot.discord.api.DiscordMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public class CacheDiscordSQL {

	public static Cache<Long, DiscordMember> cacheMembers = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();

	public static DiscordMember getDiscordMember(User user) throws SQLException {
		long userId = user.getIdLong();
		DiscordMember discordMember = cacheMembers.asMap().get(userId);
		if (discordMember == null) {
			discordMember = DiscordSQL.selectMemberByDiscordId(userId);
			if (discordMember != null)
				cacheMembers.put(userId, discordMember);
		}
		return discordMember;
	}

	public static Cache<Long, DiscordMessage> cacheMessage = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();
	
	public static DiscordMessage getDiscordMessage(Message message) throws SQLException {
		long userId = message.getAuthor().getIdLong();
		DiscordMessage discordMember = cacheMessage.asMap().get(userId);
		if (discordMember == null) {
			discordMember = DiscordSQL.selectMessage(message);
			if (discordMember != null)
				cacheMessage.put(userId, discordMember);
		}
		return discordMember;
	}

	public static DiscordMessage getDiscordMessage(long guildId, long channelId, long messageId) throws SQLException {
		DiscordMessage discordMember = cacheMessage.asMap().values().stream().filter(dm -> messageId != 0 && dm.getMessageId() == messageId).findFirst().orElse(null);
		if (discordMember == null) {
			discordMember = DiscordSQL.selectMessage(guildId, channelId, messageId);
			if (discordMember != null)
				cacheMessage.put(discordMember.getAuthorId(), discordMember);
		}
		return discordMember;
	}
}
