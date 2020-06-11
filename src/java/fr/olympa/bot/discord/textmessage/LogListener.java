package fr.olympa.bot.discord.textmessage;

import java.awt.Color;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordMessage;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.groups.DiscordGroup;
import fr.olympa.bot.discord.guild.GuildsHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.bot.discord.observer.MessageContent;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import net.dv8tion.jda.api.EmbedBuilder;
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
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class LogListener extends ListenerAdapter {
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		OlympaGuild olympaGuild = GuildsHandler.getOlympaGuild(guild);
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
			String date = user.getTimeCreated().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG));
			embed.addField("Création du compte", "Crée le " + date + "(" + t + ")", true);
		}
		olympaGuild.getLogChannel().sendMessage(embed.build()).queue();
	}
	
	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		OlympaGuild olympaGuild = GuildsHandler.getOlympaGuild(guild);
		if (!olympaGuild.isLogEntries())
			return;
		String time = Utils.timestampToDuration(member.getTimeJoined().toEpochSecond());
		String desc = member.getAsMention() + " est resté `" + time + "` Nous sommes `" + DiscordUtils.getMembersSize(guild) + "`.";
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
		OlympaGuild olympaGuild = GuildsHandler.getOlympaGuild(guild);
		if (!olympaGuild.isLogRoles())
			return;
		String rolesString = addedRoles.stream().map(role -> role.getAsMention()).collect(Collectors.joining(", "));
		String s = Utils.withOrWithoutS(addedRoles.size());
		EmbedBuilder embed = SendLogs.get("✅ Ajout d'un role", null, member.getAsMention() + " a désormais le" + s + " role" + s + " `" + rolesString + "`.", member);
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
		OlympaGuild olympaGuild = GuildsHandler.getOlympaGuild(guild);
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
		EmbedBuilder embed = SendLogs.get("❌ Suppression d'un role", null, member.getAsMention() + " n'a désormais plus le" + s + " role" + s + " `" + rolesString + "`.", member);
		embed.setColor(Color.RED);
		olympaGuild.getLogChannel().sendMessage(embed.build()).queue();
	}
	
	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
		Guild guild = event.getGuild();
		long messageId = event.getMessageIdLong();
		TextChannel channel = event.getChannel();
		OlympaGuild olympaGuild = GuildsHandler.getOlympaGuild(guild);
		if (!olympaGuild.isLogMsg() || olympaGuild.getExcludeChannelsIds().stream().anyMatch(ex -> channel.getIdLong() == ex))
			return;
		try {
			DiscordMessage discordMessage = CacheDiscordSQL.getDiscordMessage(guild.getIdLong(), channel.getIdLong(), messageId);
			if (discordMessage == null)
				return;
			discordMessage.setMessageDeleted();
			Member member = discordMessage.getAuthor();
			if (member == null || member.getUser().isBot())
				return;
			StringJoiner sj = new StringJoiner(".\n");
			sj.add(member.getAsMention() + " a supprimé un message dans " + channel.getAsMention());
			sj.add("S'y rendre: " + discordMessage.getJumpUrl());

			// Check ghost tag
			MessageContent originalContent = discordMessage.getOriginalContent();
			if (originalContent != null && originalContent.getContent() != null) {
				String ghost = originalContent.getContent().replaceAll("\\s*<@!?\\d{18,}>\\s*", "");
				if (ghost.isEmpty()) {
					EmbedBuilder embed2 = new EmbedBuilder();
					embed2.setTitle("Je te vois");
					embed2.setDescription("Les mentions fantômes sont interdites et sont passible de mute.");
					embed2.setColor(OlympaBots.getInstance().getDiscord().getColor());
					channel.sendMessage(member.getAsMention()).queue(m -> channel.sendMessage(embed2.build()).queue(msg -> {
						sj.add("😡 Suspicion de ghost tag");
						sj.add("S'y rendre: " + msg.getJumpUrl() + ".");
						SendLogs.sendMessageLog(discordMessage, "❌ Message supprimé", discordMessage.getJumpUrl(), sj.toString(), member);
					}));
					return;
				}
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
		OlympaGuild olympaGuild = GuildsHandler.getOlympaGuild(guild);
		if (!olympaGuild.isLogMsg() || olympaGuild.getExcludeChannelsIds().stream().anyMatch(ex -> channel.getIdLong() == ex) || user.isBot())
			return;
		DiscordMessage discordMessage;
		try {
			discordMessage = CacheDiscordSQL.getDiscordMessage(message);
			if (discordMessage == null)
				return;
			StringJoiner sj = new StringJoiner(".\n");
			sj.add(member.getAsMention() + " a modifié un message dans " + channel.getAsMention());
			sj.add("S'y rendre: " + message.getJumpUrl());
			SendLogs.sendMessageLog(discordMessage, "✍️ Message modifié", message.getJumpUrl(), sj.toString(), member);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		OlympaGuild olympaGuild = GuildsHandler.getOlympaGuild(guild);
		if (!olympaGuild.isLogVoice() || user.isBot())
			return;
		EmbedBuilder embed = SendLogs.get("✅ Connecté au vocal", null, member.getAsMention() + " est connecté au salon vocal `" + event.getChannelJoined().getName() + "`.", member);
		olympaGuild.getLogChannel().sendMessage(embed.build()).queue();
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Guild guild = event.getGuild();
		Message message = event.getMessage();
		User user = event.getAuthor();
		TextChannel channel = message.getTextChannel();
		OlympaGuild olympaGuild = GuildsHandler.getOlympaGuild(guild);
		if (!olympaGuild.isLogMsg() || olympaGuild.getExcludeChannelsIds().stream().anyMatch(ex -> channel.getIdLong() == ex) || user.isBot())
			return;
		DiscordMessage discordMessage;
		try {
			discordMessage = CacheDiscordSQL.getDiscordMessage(message);
			if (discordMessage == null)
				return;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// System.out.println("test1 " + OlympaBungee.getInstance()); null
		//		for (Pattern regex : new SwearHandler(BungeeConfigUtils.getDefaultConfig().getStringList("chat.insult")).getRegexSwear()) {
		//			Matcher matcher = regex.matcher(message.getContentDisplay());
		//			if (matcher.find()) {
		//				String desc = member.getAsMention() + " dans " + channel.getAsMention() + ": **" + matcher.group() + "**.";
		//				EmbedBuilder embed = ObverserEmbed.get("💢 Insulte", null, desc + "\n" + message.getJumpUrl(), member.getUser());
		//				embed.setTimestamp(message.getTimeCreated());
		//				DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
		//				break;
		//			}
		//		}
	}
	
	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		OlympaGuild olympaGuild = GuildsHandler.getOlympaGuild(guild);
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
		OlympaGuild olympaGuild = GuildsHandler.getOlympaGuild(guild);
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
		for (OlympaGuild olympaGuild : GuildsHandler.guilds) {
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
		OlympaGuild olympaGuild = GuildsHandler.getOlympaGuild(guild);
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
