package fr.olympa.bot.discord.invites;

import java.sql.SQLException;

import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class InvitesListener extends ListenerAdapter {

	@Override
	public void onGuildInviteCreate(GuildInviteCreateEvent event) {
		Invite invite = event.getInvite();
		DiscordInvite discordInvite = new DiscordInvite(invite);
		try {
			discordInvite.createNew();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//		invite.get
	}

	@Override
	public void onGuildInviteDelete(GuildInviteDeleteEvent event) {

	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		Member member = event.getMember();
		event.getGuild().retrieveInvites().queue(invites -> {
			for (Invite inv : invites) {

			}
		});
	}
}
