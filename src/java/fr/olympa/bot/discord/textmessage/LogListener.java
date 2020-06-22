package fr.olympa.bot.discord.textmessage;

import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.groups.DiscordGroup;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class LogListener extends ListenerAdapter {

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		if (olympaGuild.getType() == DiscordGuildType.PUBLIC) {
			Role defaultRole = DiscordGroup.PLAYER.getRole(guild);
			guild.addRoleToMember(member, defaultRole).queue();
		}
		if (!olympaGuild.isLogEntries())
			return;
		EmbedBuilder embed = SendLogs.get("✅ Un nouveau joueur est arrivé !", null, member.getAsMention() + " est le **" + DiscordUtils.getMembersSize(guild) + "ème** a rejoindre le discord.", member);
		embed.setColor(Color.GREEN);
		long time = user.getTimeCreated().toEpochSecond();
		long duration = Utils.getCurrentTimeInSeconds() - time;
		String t = Utils.timestampToDuration(user.getTimeCreated().toEpochSecond());
		if (duration < 60 * 12 * 31)
			embed.addField("Nouveau compte", "Crée il y a `" + t + "`", true);
		else {
			String date = user.getTimeCreated().format(DateTimeFormatter.ISO_LOCAL_DATE);
			embed.addField("Création du compte", "Crée le " + date + " (" + t + ")", true);
		}
		olympaGuild.getLogChannel().sendMessage(embed.build()).queue();
	}

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = event.getUser();
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		if (!olympaGuild.isLogEntries())
			return;
		String time = Utils.timestampToDuration(member.getTimeJoined().toEpochSecond());
		String name = " (" + member.getEffectiveName() + ")";
		if (member.getEffectiveName().equals(user.getName()))
			name = new String();
		String desc = "`" + user.getAsTag() + "`" + name + " est resté `" + time + "` Nous sommes `" + DiscordUtils.getMembersSize(guild) + "`.";
		EmbedBuilder embed = SendLogs.get("❌ Un joueur a quitté", null, desc, member);
		embed.setColor(Color.RED);
		olympaGuild.getLogChannel().sendMessage(embed.build()).queue();
	}

	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		if (user.isBot() || Utils.getCurrentTimeInSeconds() - member.getTimeJoined().toEpochSecond() < 5 && member.getRoles().isEmpty())
			return;
		List<Role> addedRoles = event.getRoles();
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		if (!olympaGuild.isLogRoles())
			return;
		String rolesString = addedRoles.stream().map(role -> role.getAsMention()).collect(Collectors.joining(", "));
		String s = Utils.withOrWithoutS(addedRoles.size());
		EmbedBuilder embed = SendLogs.get("✅ Ajout d'un role", null, member.getAsMention() + " a désormais le" + s + " role" + s + " " + rolesString + ".", member);
		embed.setColor(Color.GREEN);
		olympaGuild.getLogChannel().sendMessage(embed.build()).queue();
	}

	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		if (user.isBot())
			return;
		List<Role> removedRoles = event.getRoles();
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		if (olympaGuild.getType() == DiscordGuildType.PUBLIC)
			if (member.getRoles().isEmpty()) {
				Role defaultRole = DiscordGroup.PLAYER.getRole(guild);
				guild.addRoleToMember(member, defaultRole).queue();
				if (removedRoles.contains(defaultRole))
					return;
			}
		if (!olympaGuild.isLogRoles())
			return;
		String rolesString = removedRoles.stream().map(role -> role.getAsMention()).collect(Collectors.joining(", "));
		String s = Utils.withOrWithoutS(removedRoles.size());
		EmbedBuilder embed = SendLogs.get("❌ Suppression d'un role", null, member.getAsMention() + " n'a désormais plus le" + s + " role" + s + " " + rolesString + ".", member);
		embed.setColor(Color.RED);
		olympaGuild.getLogChannel().sendMessage(embed.build()).queue();
	}
	
	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		if (!olympaGuild.isLogVoice() || user.isBot())
			return;
		EmbedBuilder embed = SendLogs.get("✅ Connecté au vocal", null, member.getAsMention() + " est connecté au salon vocal `" + event.getChannelJoined().getName() + "`.", member);
		olympaGuild.getLogChannel().sendMessage(embed.build()).queue();
	}

	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		if (!olympaGuild.isLogVoice() || user.isBot())
			return;
		EmbedBuilder embed = SendLogs.get("❌ Déconnecté du vocal", null, member.getAsMention() + " est déconnecté du salon vocal `" + event.getChannelLeft().getName() + "`.", member);
		olympaGuild.getLogChannel().sendMessage(embed.build()).queue();
	}

	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		if (!olympaGuild.isLogVoice() || user.isBot())
			return;
		EmbedBuilder embed = SendLogs.get("🪑 Changement de salon vocal", null, member.getAsMention() + " s'est déplacé.", member);
		embed.addField("Avant", "`" + event.getChannelLeft().getName() + "`", true);
		embed.addField("Apres", "`" + event.getChannelJoined().getName() + "`", true);
		olympaGuild.getLogChannel().sendMessage(embed.build()).queue();
	}

	@Override
	public void onUserUpdateName(UserUpdateNameEvent event) {
		User user = event.getUser();
		for (OlympaGuild olympaGuild : GuildHandler.guilds) {
			if (!olympaGuild.isLogUsername() || user.isBot())
				return;
			Member member = olympaGuild.getGuild().getMember(user);
			if (member == null || !member.getEffectiveName().equals(event.getNewName()))
				return;
			EmbedBuilder embed = SendLogs.get("✏️ Changement de pseudo", null, user.getAsMention() + " a changer de **pseudo Discord**.", member);
			embed.addField("Avant", "`" + event.getOldName() + "`", true);
			embed.addField("Apres", "`" + event.getNewName() + "`", true);
			olympaGuild.getLogChannel().sendMessage(embed.build()).queue();
		}
	}

	@Override
	public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
		Guild guild = event.getGuild();
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		Member member = event.getMember();
		User user = event.getUser();
		if (!olympaGuild.isLogUsername() || user.isBot())
			return;
		EmbedBuilder embed = SendLogs.get("✏️ Changement de surnom", null, member.getAsMention() + " a changer de **surnom**.", member);
		embed.addField("Avant", "`" + event.getOldNickname() + "`", true);
		embed.addField("Apres", "`" + event.getNewNickname() + "`", true);
		olympaGuild.getLogChannel().sendMessage(embed.build()).queue();
	}

}
