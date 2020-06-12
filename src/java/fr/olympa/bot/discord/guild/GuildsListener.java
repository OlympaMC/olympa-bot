package fr.olympa.bot.discord.guild;

import java.sql.SQLException;
import java.util.List;

import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.bot.discord.sql.DiscordSQL;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildsListener extends ListenerAdapter {
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		User user = event.getUser();
		DiscordMember discordMembers;
		try {
			discordMembers = CacheDiscordSQL.getDiscordMember(user);
			if (discordMembers == null)
				DiscordSQL.addMember(new DiscordMember(user));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onReady(ReadyEvent event) {
		JDA jda = event.getJDA();
		try {
			List<Long> membersIds = DiscordSQL.selectDiscordMembersIds();
			for (User user : jda.getUsers()) {
				DiscordMember discordMember;
				if (!membersIds.contains(user.getIdLong()))
					discordMember = DiscordSQL.addMember(new DiscordMember(user));
				else
					discordMember = CacheDiscordSQL.getDiscordMember(user);
				List<Guild> guilds = jda.getMutualGuilds(user);
				if (guilds.isEmpty())
					continue;
				OnlineStatus onlineStatus = guilds.get(0).getMember(user).getOnlineStatus();
				if (onlineStatus == OnlineStatus.OFFLINE || onlineStatus == OnlineStatus.UNKNOWN)
					return;
				long lastSeenTime = discordMember.getLastSeenTime();
				discordMember.updateLastSeen();
				if (lastSeenTime == 0 || lastSeenTime > 60 * 10)
					DiscordSQL.updateMember(discordMember);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onShutdown(ShutdownEvent event) {
		GuildHandler.guilds.forEach(guild -> {
			try {
				DiscordSQL.updateGuild(guild);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void onGuildReady(GuildReadyEvent event) {
		try {
			Guild guild = event.getGuild();
			OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
			if (olympaGuild == null) {
				olympaGuild = DiscordSQL.addGuild(guild);
				if (olympaGuild != null)
					GuildHandler.guilds.add(olympaGuild);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
