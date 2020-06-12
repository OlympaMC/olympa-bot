package fr.olympa.bot.discord.sql;

import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.textmessage.DiscordMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public class CacheDiscordSQL {
	
	// <discordId, DiscordMember>
	private static Cache<Long, DiscordMember> cacheMembers = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();

	public static DiscordMember getDiscordMember(User user) throws SQLException {
		return getDiscordMember(user.getIdLong());
	}

	public static DiscordMember getDiscordMember(long userDiscordId) throws SQLException {
		DiscordMember discordMember = cacheMembers.asMap().get(userDiscordId);
		if (discordMember == null) {
			discordMember = DiscordSQL.selectMemberByDiscordId(userDiscordId);
			if (discordMember != null)
				setDiscordMember(userDiscordId, discordMember);
		}
		return discordMember;
	}
	
	public static DiscordMember getDiscordMemberByOlympaId(long olympaId) throws SQLException {
		DiscordMember discordMember = cacheMembers.asMap().values().stream().filter(dm -> olympaId != 0 && dm.getOlympaId() == olympaId).findFirst().orElse(null);
		if (discordMember == null) {
			discordMember = DiscordSQL.selectMemberByOlympaId(olympaId);
			if (discordMember != null)
				setDiscordMember(discordMember.getDiscordId(), discordMember);
		}
		return discordMember;
	}

	private static void setDiscordMember(long userDiscordId, DiscordMember discordMember) {
		cacheMembers.put(userDiscordId, discordMember);
	}

	// <discordId, DiscordMember>
	public static Cache<Long, DiscordMessage> cacheMessage = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();

	public static Entry<Long, DiscordMessage> getDiscordMessage(OlympaGuild olympaGuild, Message message) throws SQLException {
		return getDiscordMessage(olympaGuild.getId(), message.getTextChannel().getIdLong(), message.getIdLong());
	}
	
	public static Entry<Long, DiscordMessage> getDiscordMessage(long olympaGuildId, long channelDiscordId, long messageDiscordId) throws SQLException {
		Entry<Long, DiscordMessage> entry = cacheMessage.asMap().entrySet().stream().filter(e -> messageDiscordId != 0 && e.getValue().getMessageId() == messageDiscordId).findFirst().orElse(null);
		if (entry == null) {
			DiscordMessage discordMessage = DiscordSQL.selectMessage(olympaGuildId, channelDiscordId, messageDiscordId);
			if (discordMessage != null) {
				DiscordMember discordMember = getDiscordMemberByOlympaId(discordMessage.getOlympaAuthorId());
				entry = new AbstractMap.SimpleEntry<>(discordMember.getDiscordId(), discordMessage);
				cacheMessage.put(entry.getKey(), entry.getValue());
			}
		}
		return entry;
	}

	public static void setDiscordMessage(long userDiscordId, DiscordMessage discordMessage) {
		cacheMessage.put(userDiscordId, discordMessage);
	}
}
