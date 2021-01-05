package fr.olympa.bot.discord.invites;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import net.dv8tion.jda.api.entities.Invite;

public class InvitesHandler {

	private static Cache<String, DiscordInvite> cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).removalListener(notification -> {
		try {
			((DiscordInvite) notification.getValue()).update();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}).build();

	public static DiscordInvite get(String code) {
		DiscordInvite discordInvite = cache.getIfPresent(code);
		if (discordInvite == null)
			try {
				discordInvite = DiscordInvite.getByCode(code);
				if (discordInvite != null)
					addInvite(discordInvite);
			} catch (IllegalAccessException | SQLException e) {
				e.printStackTrace();
			}
		return discordInvite;
	}

	public static DiscordInvite getWithoutCaching(String code) {
		DiscordInvite discordInvite = cache.getIfPresent(code);
		if (discordInvite == null)
			try {
				discordInvite = DiscordInvite.getByCode(code);
			} catch (IllegalAccessException | SQLException e) {
				e.printStackTrace();
			}
		return discordInvite;
	}

	public static boolean exist(String code) {
		return get(code) != null;
	}

	public static void addInvite(DiscordInvite discordInvite) {
		cache.put(discordInvite.getCode(), discordInvite);
	}

	public static void init() {
		GuildHandler.getOlympaGuild(DiscordGuildType.PUBLIC).getGuild().retrieveInvites().queue(ivs -> {
			try {
				for (Invite iv : ivs) {
					DiscordInvite discordInvite = getWithoutCaching(iv.getCode());
					if (discordInvite == null)
						addInvite(new DiscordInvite(iv).createNew());
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

}
