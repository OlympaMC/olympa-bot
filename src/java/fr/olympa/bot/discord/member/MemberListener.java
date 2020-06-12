package fr.olympa.bot.discord.member;

import java.sql.SQLException;

import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.bot.discord.sql.DiscordSQL;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivityOrderEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MemberListener extends ListenerAdapter {

	@Override
	public void onUserUpdateActivityOrder(UserUpdateActivityOrderEvent event) {
		User user = event.getEntity();
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			long lastSeenTime = discordMember.getLastSeenTime();
			discordMember.updateLastSeen();
			if (lastSeenTime == 0 || lastSeenTime > 60 * 10)
				DiscordSQL.updateMember(discordMember);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event) {
		User user = event.getEntity();
		if (event.getNewOnlineStatus() == OnlineStatus.UNKNOWN)
			return;
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			long lastSeenTime = discordMember.getLastSeenTime();
			discordMember.updateLastSeen();
			if (lastSeenTime == 0 || lastSeenTime > 60 * 10)
				DiscordSQL.updateMember(discordMember);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
