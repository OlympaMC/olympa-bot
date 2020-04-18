package fr.olympa.bot.discord.observer;

import java.awt.Color;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.SwearHandler;
import fr.olympa.bot.discord.api.DiscordIds;
import fr.olympa.bot.discord.api.DiscordUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
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

public class ObserverListener extends ListenerAdapter {

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		if (!ObserverHandler.logEntries || !DiscordUtils.isDefaultGuild(guild) || user.isBot()) {
			return;
		}
		EmbedBuilder embed = ObverserEmbed.get("✅ Un nouveau joueur est arrivé !", null, member.getAsMention() + " est le **" + DiscordUtils.getMembersSize(guild) + "ème** a rejoindre le discord.", member.getUser());
		embed.setColor(Color.GREEN);
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
	}

	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		if (!ObserverHandler.logEntries || !DiscordUtils.isDefaultGuild(guild) || user.isBot()) {
			return;
		}
		String desc = member.getAsMention() + " est resté **" + Utils.timestampToDuration(member.getTimeJoined().toEpochSecond()) + "** sur le discord (nous sommes " + DiscordUtils.getMembersSize(guild) + ").";
		EmbedBuilder embed = ObverserEmbed.get("❌ Un joueur a quitté", null, desc, member.getUser());
		embed.setColor(Color.RED);
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
	}

	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		if (!ObserverHandler.logRoles || !DiscordUtils.isDefaultGuild(guild) || user.isBot()) {
			return;
		}
		String desc = member.getAsMention() + " a désormais le role " + event.getRoles().stream().map(role -> role.getAsMention()).collect(Collectors.joining(", ")) + ".";
		EmbedBuilder embed = ObverserEmbed.get("✅ Ajout d'un role", null, desc, member.getUser());
		embed.setColor(Color.GREEN);
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
	}

	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		if (!ObserverHandler.logRoles || !DiscordUtils.isDefaultGuild(guild) || user.isBot()) {
			return;
		}
		String rolesString = event.getRoles().stream().map(role -> role.getAsMention()).collect(Collectors.joining(", "));
		EmbedBuilder embed = ObverserEmbed.get("❌ Suppression d'un role", null, member.getAsMention() + " n'a désormais plus le role " + rolesString + ".", member.getUser());
		embed.setColor(Color.RED);
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
	}

	@Override
	public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		if (!ObserverHandler.logUsername || !DiscordUtils.isDefaultGuild(guild) || user.isBot()) {
			return;
		}
		EmbedBuilder embed = ObverserEmbed.get("✏️ Changement de pseudo", null, member.getAsMention() + " a changer de **surnom**.", member.getUser());
		embed.addField("Avant", event.getOldNickname(), true);
		embed.addField("Après", event.getNewNickname(), true);
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
	}

	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
		Guild guild = event.getGuild();
		long messageId = event.getMessageIdLong();
		TextChannel channel = event.getChannel();
		if (!ObserverHandler.logMsgs || !DiscordUtils.isDefaultGuild(guild)) {
			return;
		}
		MessageCache message = ObserverHandler.getMessageCache(messageId);
		EmbedBuilder embed;
		if (message != null) {
			User user = message.getAuthor();
			if (user.isBot()) {
				return;
			}
			String msg = message.getContent();
			embed = ObverserEmbed.get("❌ Message supprimé", null, "Un message de " + user.getAsMention() + " a été supprimé dans " + channel.getAsMention() + ".", user);
			embed.addField("Message", msg, true);
			List<Attachment> attachments = message.getAttachments();
			if (!attachments.isEmpty()) {
				embed.addField("Pièce jointe", attachments.stream().map(a -> a.getFileName() + " " + a.getUrl()).collect(Collectors.joining("\n\n")), true);
				embed.setImage(attachments.get(0).getUrl());
			}
		} else {
			embed = new EmbedBuilder();
			embed.setTitle("❌ Message supprimé");
			embed.setDescription("Un vieux message a été supprimé dans " + channel.getAsMention() + ".");
		}
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Guild guild = event.getGuild();
		Message message = event.getMessage();
		TextChannel channel = message.getTextChannel();
		Member member = event.getMember();
		if (!ObserverHandler.logMsgs || !DiscordUtils.isDefaultGuild(guild) || member == null || member.getUser().isBot()) {
			return;
		}
		List<Attachment> attachments = message.getAttachments();
		if (!attachments.isEmpty()) {
			String desc = member.getAsMention() + " dans " + channel.getAsMention() + ":";
			EmbedBuilder embed = ObverserEmbed.get("✍ Pièces jointes", null, desc + "\n\nS'y rendre: " + message.getJumpUrl(), member.getUser());
			embed.setTimestamp(message.getTimeCreated());
			embed.setImage(attachments.get(0).getUrl());
			embed.addField("Pièce jointe", attachments.stream().map(a -> a.getFileName() + " " + a.getUrl()).collect(Collectors.joining("\n\n")), true);
			DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
		}
		for (Pattern regex : SwearHandler.getSwearHandler()) {
			Matcher matcher = regex.matcher(message.getContentDisplay());
			if (matcher.find()) {
				String desc = member.getAsMention() + " dans " + channel.getAsMention() + ": **" + matcher.group() + "**.";
				EmbedBuilder embed = ObverserEmbed.get("💢 Insulte", null, desc + "\n" + message.getJumpUrl(), member.getUser());
				embed.setTimestamp(message.getTimeCreated());
				DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
				break;
			}
		}
		ObserverHandler.addMessageCache(message);

	}

	@Override
	public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		Message message = event.getMessage();
		if (!ObserverHandler.logMsgs || !DiscordUtils.isDefaultGuild(guild) || member == null || member.getUser().isBot()) {
			return;
		}
		MessageCache mc = ObserverHandler.getMessageCache(message.getIdLong());

		EmbedBuilder embed = ObverserEmbed.get("✍️ Message modifié", null, member.getAsMention() + " a modifié un message dans " + message.getTextChannel().getAsMention() + ".\n" + message.getJumpUrl(), member.getUser());
		embed.setTimestamp(message.getTimeCreated());
		String attch = "";
		if (!mc.attachments.isEmpty()) {
			attch = "\n\n" + mc.attachments.stream().map(a -> a.getFileName() + " " + a.getUrl()).collect(Collectors.joining("\n\n"));
		}
		embed.addField("Avant", mc.getContent() + attch, true);
		if (!message.getAttachments().isEmpty()) {
			attch = "\n\n" + message.getAttachments().stream().map(a -> a.getFileName() + " " + a.getUrl()).collect(Collectors.joining("\n\n"));
		}
		embed.addField("Après", message.getContentRaw() + attch, true);
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
		ObserverHandler.addMessageCache(message);
	}

	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		if (!ObserverHandler.logVoice || !DiscordUtils.isDefaultGuild(guild) || user.isBot()) {
			return;
		}
		EmbedBuilder embed = ObverserEmbed.get("✔️ Connecté au vocal", null, member.getAsMention() + " est connecté au salon vocal **" + event.getChannelJoined().getName() + "**.", member.getUser());
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
	}

	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		if (!ObserverHandler.logVoice || !DiscordUtils.isDefaultGuild(guild) || user.isBot()) {
			return;
		}
		EmbedBuilder embed = ObverserEmbed.get("❌ Déconnecté du vocal", null, member.getAsMention() + " est déconnecté du salon vocal **" + event.getChannelLeft().getName() + "**.", member.getUser());
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
	}

	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		if (!ObserverHandler.logVoice || !DiscordUtils.isDefaultGuild(guild)) {
			return;
		}
		EmbedBuilder embed = ObverserEmbed.get("🪑 Changement de salon vocal", null, member.getAsMention() + " s'est déplacé.", member.getUser());
		embed.addField("Avant", "**" + event.getChannelJoined().getName() + "**", true);
		embed.addField("Après", "**" + event.getChannelLeft().getName() + "**", true);
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
	}

	// net.dv8tion.jda.api.events.guild.voice.

	@Override
	public void onUserUpdateName(UserUpdateNameEvent event) {
		User user = event.getUser();
		if (!ObserverHandler.logUsername) {
			return;
		}
		EmbedBuilder embed = ObverserEmbed.get("✏️ Changement de pseudo", null, user.getAsMention() + " a changer de **pseudo Discord**.", user);
		embed.addField("Avant", event.getOldName(), true);
		embed.addField("Après", event.getNewName(), true);
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
	}
}
