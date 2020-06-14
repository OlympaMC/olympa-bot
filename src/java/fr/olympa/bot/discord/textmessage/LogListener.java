package fr.olympa.bot.discord.textmessage;

import java.awt.Color;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.groups.DiscordGroup;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.bot.discord.observer.MessageContent;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
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
	public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
		Guild guild = event.getGuild();
		long messageId = event.getMessageIdLong();
		TextChannel channel = event.getChannel();
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		if (!olympaGuild.isLogMsg() || olympaGuild.getExcludeChannelsIds().stream().anyMatch(ex -> channel.getIdLong() == ex))
			return;
		try {
			Entry<Long, DiscordMessage> entry = CacheDiscordSQL.getDiscordMessage(olympaGuild.getId(), channel.getIdLong(), messageId);
			if (entry == null)
				return;
			DiscordMessage discordMessage = entry.getValue();
			Member member = discordMessage.getGuild().getMemberById(entry.getKey());
			if (member == null || member.getUser().isBot())
				return;
			StringJoiner sj = new StringJoiner(".\n");
			sj.add(member.getAsMention() + " a supprimé un message dans " + channel.getAsMention());
			sj.add("S'y rendre: " + discordMessage.getJumpUrl());
			
			// Check ghost tag
			MessageContent originalContent = discordMessage.getOriginalContent();
			if (originalContent != null && originalContent.getContent() != null) {
				Matcher matcher = Pattern.compile("<@!?(\\d{18,})>").matcher(originalContent.getContent());
				boolean canSee = false;

				while (matcher.find()) {
					String userId = matcher.group(1);
					canSee = guild.getMemberById(userId).getPermissions(channel).contains(Permission.MESSAGE_READ);
					if (canSee)
						break;
				}
				if (!canSee || !originalContent.getContent().replace(matcher.group(), "").isBlank())
					return;
				EmbedBuilder embed = new EmbedBuilder();
				embed.setTitle("Je te vois");
				embed.setDescription("Les mentions fantômes sont interdites et sont passible de mute.");
				embed.setColor(OlympaBots.getInstance().getDiscord().getColor());
				channel.sendMessage(member.getAsMention()).queue(m -> channel.sendMessage(embed.build()).queue(msg -> {
					sj.add("😡 Suspicion de ghost tag");
					sj.add("S'y rendre: " + msg.getJumpUrl() + ".");
					SendLogs.sendMessageLog(discordMessage, "❌ Message supprimé", discordMessage.getJumpUrl(), sj.toString(), member);
				}));
				return;
				
			}
			SendLogs.sendMessageLog(discordMessage, "❌ Message supprimé", discordMessage.getJumpUrl(), sj.toString(), member);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = event.getAuthor();
		Message message = event.getMessage();
		TextChannel channel = event.getChannel();
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		if (!olympaGuild.isLogMsg() || olympaGuild.getExcludeChannelsIds().stream().anyMatch(ex -> channel.getIdLong() == ex) || user.isBot())
			return;
		try {
			Entry<Long, DiscordMessage> entry = CacheDiscordSQL.getDiscordMessage(olympaGuild, message);
			if (entry == null)
				return;
			DiscordMessage discordMessage = entry.getValue();
			StringJoiner sj = new StringJoiner(".\n");
			sj.add(member.getAsMention() + " a modifié un message dans " + channel.getAsMention());
			sj.add("S'y rendre: " + message.getJumpUrl());
			SendLogs.sendMessageLog(discordMessage, "✍️ Message modifié", message.getJumpUrl(), sj.toString(), member);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Guild guild = event.getGuild();
		Message message = event.getMessage();
		Member member = event.getMember();
		User user = event.getAuthor();
		message.getEmotesBag();
		TextChannel channel = message.getTextChannel();
		OlympaGuild olympaGuild = GuildsHandler.getOlympaGuild(guild);
		if (!olympaGuild.isLogMsg() || olympaGuild.getExcludeChannelsIds().stream().anyMatch(ex -> channel.getIdLong() == ex) || user.isBot())
			return;
		try {
			Entry<Long, DiscordMessage> entry = CacheDiscordSQL.getDiscordMessage(olympaGuild, message);
			if (entry == null)
				return;
			DiscordMessage discordMessage = entry.getValue();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (member.isFake())
			return;
	}*/

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
