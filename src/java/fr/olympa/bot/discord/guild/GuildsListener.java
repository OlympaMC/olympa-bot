package fr.olympa.bot.discord.guild;

import java.sql.SQLException;
import java.util.List;

import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.bot.discord.sql.DiscordSQL;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildsListener extends ListenerAdapter {

	List<Long> allUsers = null;

	@Override
	public void onReady(ReadyEvent event) {
		allUsers = null;
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
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		Member member = event.getMember();
		User user = event.getUser();
		DiscordMember discordMembers;
		try {
			discordMembers = CacheDiscordSQL.getDiscordMember(user);
			if (discordMembers == null) {
				DiscordMember joinTime = new DiscordMember(user);
				joinTime.updateJoinTime(member.getTimeJoined().toEpochSecond());
				DiscordSQL.addMember(joinTime);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGuildReady(GuildReadyEvent event) {
		try {
			if (allUsers == null)
				allUsers = DiscordSQL.selectDiscordMembersIds();
			Guild guild = event.getGuild();
			OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
			if (olympaGuild == null) {
				olympaGuild = DiscordSQL.addGuild(guild);
				if (olympaGuild != null)
					GuildHandler.guilds.add(olympaGuild);
			}
			if (!olympaGuild.getName().equals(guild.getName())) {
				olympaGuild.setName(guild.getName());
				DiscordSQL.updateGuild(olympaGuild);
			}
			for (Member membre : guild.getMembers()) {
				DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(membre.getUser());
				if (discordMember == null)
					DiscordSQL.addMember(new DiscordMember(membre));
				else if (discordMember.getTag() == null) {
					discordMember.updateName(membre.getUser());
					DiscordSQL.updateMember(discordMember);
				}
			}
			//				if (!allUsers.contains(membre.getIdLong())) {
			//			DiscordSQL.addMember(new DiscordMember(membre));
			//			allUsers.add(membre.getIdLong());
			//				}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
