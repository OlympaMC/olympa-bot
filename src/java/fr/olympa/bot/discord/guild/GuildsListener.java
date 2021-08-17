package fr.olympa.bot.discord.guild;

import java.sql.SQLException;
import java.util.Set;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.bot.discord.invites.InvitesHandler;
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

	Set<Long> allUsers = null;

	@Override
	public void onReady(ReadyEvent event) {
		allUsers = null;
	}

	@Override
	public void onShutdown(ShutdownEvent event) {
		GuildHandler.guilds.forEach(guild -> {
			try {
				GuildSQL.updateGuild(guild);
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
				DiscordMember joinTime = new DiscordMember(member);
				DiscordSQL.addMember(joinTime);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGuildReady(GuildReadyEvent event) {
		LinkSpigotBungee.getInstance().getTask().runTaskAsynchronously(() -> {
			try {
				if (allUsers == null)
					allUsers = DiscordSQL.selectDiscordMembersIds();
				Guild guild = event.getGuild();
				OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
				if (olympaGuild == null) {
					olympaGuild = GuildSQL.addGuild(guild);
					GuildHandler.guilds.add(olympaGuild);
				}
				if (!olympaGuild.getName().equals(guild.getName())) {
					olympaGuild.setName(guild.getName());
					GuildSQL.updateGuild(olympaGuild);
				}

				for (Member member : guild.getMembers())
					//				DiscordMember discordMember = CacheDiscordSQL.getDiscordMemberWtihoutCaaching(member.getUser().getIdLong());
					//				if (discordMember == null)
					//					DiscordSQL.addMember(new DiscordMember(member));
					//				else if (olympaGuild.isOlympaDiscord() && discordMember.getOlympaId() != 0) {
					//					OlympaPlayer olympaPlayer = AccountProviderAPI.getter().get(discordMember.getOlympaId());
					//					if (!member.getEffectiveName().equals(olympaPlayer.getName())) {
					//						System.err.println("DEBUG " + guild.getName() + " membre " + member.getEffectiveName() + " n'a pas le bon pseudo " + olympaPlayer.getName());
					//						member.modifyNickname(olympaPlayer.getName()).reason("MAJ Manuelle").queue(null, ErrorResponseException.ignore(ErrorResponse.MISSING_ACCESS));
					//					}
					//				}
					//			}
					// TEMP
					//				else if (discordMember.getTag() == null) {
					//					discordMember.updateName(member.getUser());
					//				DiscordSQL.updateMember(discordMember);
					//				} else if (discordMember.getJoinTime() == 0) {
					//					discordMember.updateJoinTime(member.getTimeJoined().toEpochSecond());
					//					DiscordSQL.updateMember(discordMember);
					//				}
					if (!allUsers.contains(member.getIdLong())) {
						DiscordSQL.addMember(new DiscordMember(member));
						allUsers.add(member.getIdLong());
					}
				InvitesHandler.init(olympaGuild);
				//TEMP
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
}
