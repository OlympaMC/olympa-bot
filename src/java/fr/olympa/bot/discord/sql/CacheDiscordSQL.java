package fr.olympa.bot.discord.sql;

import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.message.DiscordMessage;
import fr.olympa.bot.discord.message.SQLMessage;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public class CacheDiscordSQL {

	public static void debug() {
		LinkSpigotBungee<?> link = LinkSpigotBungee.getInstance();
		link.getTask().scheduleSyncRepeatingTask(() -> link.sendMessage("§dNombre de DiscordMember en cache §5" + cacheMembers.size()), 29, 60, TimeUnit.MINUTES);
	}

	// <discordId, DiscordMember>
	public static Cache<Long, DiscordMember> cacheMembers = CacheBuilder.newBuilder().recordStats().maximumSize(500).removalListener(notification -> {
		if (notification.getCause() == RemovalCause.REPLACED)
			return;
		DiscordMember member = (DiscordMember) notification.getValue();
		if (member.cacheNeedToBeSave())
			member.saveCacheToDb();
	}).build();

	public static void saveToDbIfNeeded() {
		cacheMembers.asMap().forEach((id, discordMember) -> {
			if (discordMember.cacheNeedToBeSave())
				discordMember.saveCacheToDb();
		});
	}

	public static void update(DiscordMember discordMember) {
		cacheMembers.put(discordMember.getDiscordId(), discordMember);
	}

	public static void updateNameOrTagIfNeeded(User user, DiscordMember discordMember) throws SQLException {
		if (user == null)
			user = OlympaBots.getInstance().getDiscord().getJda().getUserById(discordMember.getDiscordId());
		if (user != null)
			discordMember.updateName(user);
	}

	public static DiscordMember getDiscordMemberAndCreateIfNotExist(User user) throws SQLException {
		DiscordMember discordMember = getDiscordMember(user);
		if (discordMember == null) {
			discordMember = new DiscordMember(user);
			//			discordMember.insert();
			DiscordSQL.addMember(discordMember);
			setDiscordMember(user.getIdLong(), discordMember);
		}
		return discordMember;
	}

	public static DiscordMember getDiscordMemberWithoutCaching(User user) throws SQLException {
		DiscordMember discordMember = cacheMembers.asMap().get(user.getIdLong());
		if (discordMember == null)
			discordMember = DiscordSQL.selectMemberByDiscordId(user.getIdLong());
		if (discordMember != null)
			updateNameOrTagIfNeeded(user, discordMember);
		return discordMember;
	}

	public static DiscordMember getDiscordMember(Member member) throws SQLException {
		return getDiscordMember(member.getUser());
	}

	public static DiscordMember getDiscordMember(User user) throws SQLException {
		DiscordMember discordMember = cacheMembers.asMap().get(user.getIdLong());
		if (discordMember == null) {
			discordMember = DiscordSQL.selectMemberByDiscordId(user.getIdLong());
			if (discordMember != null) {
				updateNameOrTagIfNeeded(user, discordMember);
				setDiscordMember(user.getIdLong(), discordMember);
			}
		}
		return discordMember;
	}

	public static DiscordMember getDiscordMemberByDiscordOlympaId(long discordOlympaId) throws SQLException {
		DiscordMember discordMember = cacheMembers.asMap().values().stream().filter(dm -> dm.getId() == discordOlympaId).findFirst().orElse(null);
		if (discordMember == null) {
			discordMember = DiscordSQL.selectMemberByDiscordOlympaId(discordOlympaId);
			if (discordMember != null) {
				updateNameOrTagIfNeeded(null, discordMember);
				setDiscordMember(discordMember.getDiscordId(), discordMember);
			}
		}
		return discordMember;
	}

	public static DiscordMember getDiscordMemberByOlympaId(long olympaId) throws SQLException {
		DiscordMember discordMember = cacheMembers.asMap().values().stream().filter(dm -> dm.getOlympaId() == olympaId).findFirst().orElse(null);
		if (discordMember == null) {
			discordMember = DiscordSQL.selectMemberByOlympaId(olympaId);
			if (discordMember != null) {
				updateNameOrTagIfNeeded(null, discordMember);
				setDiscordMember(discordMember.getDiscordId(), discordMember);
			}
		}
		return discordMember;
	}

	private static void setDiscordMember(long userDiscordId, DiscordMember discordMember) {
		cacheMembers.put(userDiscordId, discordMember);
	}

	// <discordId, DiscordMember>
	private static Cache<Long, DiscordMessage> cacheMessage = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();

	public static Entry<Long, DiscordMessage> getDiscordMessage(OlympaGuild olympaGuild, Message message) throws SQLException {
		return getDiscordMessage(olympaGuild.getId(), message.getTextChannel().getIdLong(), message.getIdLong());
	}

	public static Entry<Long, DiscordMessage> getDiscordMessage(long olympaGuildId, long channelDiscordId, long messageDiscordId) throws SQLException {
		Entry<Long, DiscordMessage> entry = cacheMessage.asMap().entrySet().stream().filter(e -> e.getValue().getMessageId() == messageDiscordId).findFirst().orElse(null);
		if (entry == null) {
			DiscordMessage discordMessage = SQLMessage.selectMessage(olympaGuildId, channelDiscordId, messageDiscordId);
			if (discordMessage != null) {
				DiscordMember discordMember = getDiscordMemberByDiscordOlympaId(discordMessage.getOlympaDiscordAuthorId());
				entry = new AbstractMap.SimpleEntry<>(discordMember.getDiscordId(), discordMessage);
				if (entry != null)
					cacheMessage.put(entry.getKey(), entry.getValue());
			}
		}
		return entry;
	}

	public static void setDiscordMessage(long userDiscordId, DiscordMessage discordMessage) {
		cacheMessage.put(userDiscordId, discordMessage);
	}

}
