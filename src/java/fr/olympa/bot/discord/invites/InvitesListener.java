package fr.olympa.bot.discord.invites;

import java.awt.Color;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.message.LogsHandler;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class InvitesListener extends ListenerAdapter {

	@Override
	public void onGuildInviteCreate(GuildInviteCreateEvent event) {
		Invite invite = event.getInvite();
		DiscordInvite discordInvite = new DiscordInvite(invite);
		InvitesHandler.addInvite(discordInvite);
		try {
			discordInvite.createNew();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGuildInviteDelete(GuildInviteDeleteEvent event) {
		DiscordInvite discordInvite = InvitesHandler.get(event.getCode()).expand();
		if (discordInvite == null)
			return;
		discordInvite.delete();
		try {
			discordInvite.update();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		Member member = event.getMember();
		Guild guild = event.getGuild();
		OlympaGuild opGuild = GuildHandler.getOlympaGuild(guild);
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(member.getIdLong());
			User user = member.getUser();
			OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
			if (!olympaGuild.isLogEntries())
				return;
			EmbedBuilder embed = LogsHandler.get("✅ Un nouveau joueur est arrivé !", null, member.getAsMention() + " est le **" + DiscordUtils.getMembersSize(guild) + "ème** a rejoindre le discord.", member);
			embed.setColor(Color.GREEN);
			long time = user.getTimeCreated().toEpochSecond();
			long duration = Utils.getCurrentTimeInSeconds() - time;
			String t = Utils.timestampToDuration(user.getTimeCreated().toEpochSecond());
			if (duration < 60 * 12 * 31)
				embed.addField("Nouveau compte", "Créé il y a `" + t + "`", true);
			else {
				String date = user.getTimeCreated().format(DateTimeFormatter.ISO_LOCAL_DATE);
				embed.addField("Création du compte", "Créé le " + date + " (" + t + ")", true);
			}

			InvitesHandler.detectNewInvite(opGuild, inviters -> {
				if (!inviters.isEmpty())
					embed.addField("Invité par ", ColorUtils.join(inviters.stream().map(inviter -> {
						return inviter.getAsMention() + " (" + inviter.getAsTag() + ")";
					}).collect(Collectors.toList()).iterator(), "ou"), true);
				olympaGuild.getLogChannel().sendMessage(embed.build()).queue();
			}, discordMember);
			InvitesHandler.removeLeaverUser(discordMember, opGuild);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		Member member = event.getMember();
		OlympaGuild opGuild = GuildHandler.getOlympaGuild(event.getGuild());
		DiscordMember dm;
		try {
			dm = CacheDiscordSQL.getDiscordMember(member.getIdLong());
			OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
			if (!olympaGuild.isLogEntries())
				return;
			String time = Utils.timestampToDuration(member.getTimeJoined().toEpochSecond());
			String name;
			if (member.getEffectiveName().equals(user.getName()))
				name = "";
			else
				name = " (" + member.getEffectiveName() + ")";
			String desc = "`" + user.getAsTag() + "`" + name + " est resté `" + time + "` Nous sommes `" + DiscordUtils.getMembersSize(guild) + "`.";
			EmbedBuilder embed = LogsHandler.get("❌ Un joueur a quitté", null, desc, member);
			embed.setColor(Color.RED);
			InvitesHandler.addUsesLeaver(dm, opGuild, invites -> {
				if (!invites.isEmpty())
					embed.addField("Invité par ", ColorUtils.join(invites.stream().map(di -> {
						try {
							return di.getAuthor().getAsTag();
						} catch (SQLException e) {
							e.printStackTrace();
						}
						return "null";
					}).collect(Collectors.toList()).iterator(), "ou"), true);
				olympaGuild.getLogChannel().sendMessage(embed.build()).queue();
			});

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
