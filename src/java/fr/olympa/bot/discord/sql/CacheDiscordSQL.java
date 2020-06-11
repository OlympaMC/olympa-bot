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
		return getDiscordMember(user.getIdLong());
	}
	
	public static DiscordMember getDiscordMember(long userDiscordId) throws SQLException {
		DiscordMember discordMember = cacheMembers.asMap().get(userDiscordId);
		if (discordMember == null) {
			discordMember = DiscordSQL.selectMemberByDiscordId(userDiscordId);
			if (discordMember != null)
				cacheMembers.put(userDiscordId, discordMember);
		}
		return discordMember;
	}

	public static DiscordMember getDiscordMemberByOlympaId(long olympaDiscordId) throws SQLException {
		DiscordMember discordMember = cacheMembers.asMap().values().stream().filter(dm -> olympaDiscordId != 0 && dm.getOlympaId() == olympaDiscordId).findFirst().orElse(null);
		if (discordMember == null) {
			discordMember = DiscordSQL.selectMemberByOlympaId(olympaDiscordId);
			if (discordMember != null)
				cacheMembers.put(discordMember.getDiscordId(), discordMember);
		}
		return discordMember;
	}

	public static Cache<Long, DiscordMessage> cacheMessage = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();
	
	public static DiscordMessage getDiscordMessage(Message message) throws SQLException {
		long userDiscordId = message.getAuthor().getIdLong();
		DiscordMessage discordMessage = cacheMessage.asMap().get(userDiscordId);
		if (discordMessage == null) {
			discordMessage = DiscordSQL.selectMessage(message);
			if (discordMessage != null)
				cacheMessage.put(userDiscordId, discordMessage);
		}
		return discordMessage;
	}

	public static DiscordMessage getDiscordMessage(long guildId, long channelId, long messageId) throws SQLException {
		DiscordMessage discordMessage = cacheMessage.asMap().values().stream().filter(dm -> messageId != 0 && dm.getMessageId() == messageId).findFirst().orElse(null);
		if (discordMessage == null) {
			discordMessage = DiscordSQL.selectMessage(guildId, channelId, messageId);
			if (discordMessage != null) {
				DiscordMember discordMember = getDiscordMember(discordMessage.getAuthorId());
				cacheMessage.put(discordMember.getDiscordId(), discordMessage);
			}
		}
		return discordMessage;
	}
}
