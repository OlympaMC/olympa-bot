package fr.olympa.bot.discord.invites;

import java.awt.Color;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.stream.Collectors;

import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
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
		//		InvitesHandler.addInvite(discordInvite);
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
		discordInvite.update();
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		Member member = event.getMember();
		Guild guild = event.getGuild();
		OlympaGuild opGuild = GuildHandler.getOlympaGuild(guild);
		try {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(member);
			User user = member.getUser();
			OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
			EmbedBuilder embed = new EmbedBuilder();
			embed.setDescription("`" + member.getUser().getAsTag() + "` est le **" + DiscordUtils.getMembersSize(guild) + "**ème a rejoindre.");
			embed.setThumbnail(member.getUser().getAvatarUrl());
			embed.setColor(Color.GREEN);
			long time = user.getTimeCreated().toEpochSecond();
			long duration = Utils.getCurrentTimeInSeconds() - time;
			String sDuration = Utils.timestampToDuration(user.getTimeCreated().toEpochSecond());
			if (duration < 60 * 12 * 31)
				embed.addField("Nouveau compte", "Créé il y a `" + sDuration + "`", true);
			else {
				String date = user.getTimeCreated().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(Locale.FRANCE));
				embed.addField("Création du compte", date + " (" + sDuration + ")", true);
			}
			InvitesHandler.detectNewInvite(opGuild, inviters -> {
				if (olympaGuild.isLogEntries()) {
					if (!inviters.isEmpty())
						embed.addField("Invité par ", ColorUtils.join(inviters.stream().map(inviter -> {
							return inviter.getAsMention() + " (" + inviter.getAsTag() + ")";
						}).collect(Collectors.toList()).iterator(), "ou"), true);
					olympaGuild.getLogChannel().sendMessageEmbeds(embed.build()).append(member.getAsMention()).queue();
				}
			}, discordMember, (memberWhoInviteScore, authorMember) -> {
				if (opGuild.isSendingWelcomeMessage()) {
					TextChannel defaultChannel = guild.getDefaultChannel();
					if (defaultChannel != null)
						if (memberWhoInviteScore != null && authorMember != null)
							defaultChannel.sendMessage(String.format("%s nous a rejoint suite à l'invitation de `%s`, qui comptabilise maintenant **%d** invitation%s.",
									member.getUser().getAsMention(), authorMember.getAsTag(), memberWhoInviteScore.getRealUses(), Utils.withOrWithoutS(memberWhoInviteScore.getRealUses())))
									.queue();
						else
							defaultChannel.sendMessage(String.format("%s nous a rejoint.", member.getUser().getAsMention())).queue();
				}
			});
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
			dm = CacheDiscordSQL.getDiscordMember(member);
			OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
			String time = Utils.timestampToDuration(member.getTimeJoined().toEpochSecond());
			StringBuilder name = new StringBuilder();
			name.append("`" + user.getAsTag() + "`");
			name.append("(");
			if (!member.getEffectiveName().equals(user.getName()))
				name.append(member.getEffectiveName() + "|");
			name.append(member.getId() + ")");
			String desc = name + " nous a quitté après être resté  `" + time + "` avec nous.";
			EmbedBuilder embed = new EmbedBuilder();
			embed.setDescription(desc);
			embed.setColor(Color.RED);
			InvitesHandler.addUsesLeaver(dm, opGuild, invites -> {
				if (olympaGuild.isLogEntries()) {
					if (!invites.isEmpty())
						embed.addField("Invité par ", ColorUtils.join(invites.stream().map(di -> {
							try {
								return di.getAuthor().getAsMention() + " (" + di.getAuthor().getAsTag() + ")";
							} catch (SQLException e) {
								e.printStackTrace();
							}
							return "null";
						}).collect(Collectors.toList()).iterator(), "ou"), true);
					olympaGuild.getLogChannel().sendMessageEmbeds(embed.build()).queue();
				}
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
