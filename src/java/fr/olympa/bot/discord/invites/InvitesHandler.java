package fr.olympa.bot.discord.invites;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class InvitesHandler {

	private static Cache<String, DiscordInvite> cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).removalListener(notification -> {
		//		try {
		//			DiscordSQL.updateMember((DiscordMember) notification.getValue());
		//		} catch (SQLException e) {
		//			e.printStackTrace();
		//		}
	}).build();

	private static void addDiscordMember(DiscordInvite discordInvite) {
		cache.put(discordInvite.getCode(), discordInvite);
	}
}
