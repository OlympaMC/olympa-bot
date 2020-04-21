package fr.olympa.bot.discord.observer;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.discord.api.DiscordIds;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.groups.DiscordGroup;
import fr.olympa.core.bungee.utils.BungeeConfigUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.Role;
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
		EmbedBuilder embed = ObverserEmbed.get("✅ Un nouveau joueur est arrivé !", null, member.getAsMention() + " est le **" + DiscordUtils.getMembersSize(guild) + "ème** a rejoindre le discord.", member);
		embed.setColor(Color.GREEN);

		long time = user.getTimeCreated().toEpochSecond();
		long duration = Utils.getCurrentTimeInSeconds() - time;
		if (duration < 60 * 12 * 31) {
			embed.addField("Nouveau compte", "Crée il y a " + Utils.timestampToDuration(user.getTimeCreated().toEpochSecond()), true);
		}
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();

		Role defaultRole = DiscordGroup.PLAYER.getRole(guild);
		guild.addRoleToMember(member, defaultRole).queue();
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
		EmbedBuilder embed = ObverserEmbed.get("❌ Un joueur a quitté", null, desc, member);
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
		EmbedBuilder embed = ObverserEmbed.get("✅ Ajout d'un role", null, desc, member);
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
		EmbedBuilder embed = ObverserEmbed.get("❌ Suppression d'un role", null, member.getAsMention() + " n'a désormais plus le role " + rolesString + ".", member);
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
		EmbedBuilder embed = ObverserEmbed.get("✏️ Changement de surnom", null, member.getAsMention() + " a changer de **surnom**.", member);
		embed.addField("Avant", event.getOldNickname(), true);
		embed.addField("Après", event.getNewNickname(), true);
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
	}

	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
		Guild guild = event.getGuild();
		long messageId = event.getMessageIdLong();
		TextChannel channel = event.getChannel();
		if (!ObserverHandler.logMsgs || !DiscordUtils.isDefaultGuild(guild) || channel.getIdLong() == DiscordIds.getChannelInfo().getIdLong()) {
			return;
		}
		MessageCache message = ObserverHandler.getMessageCache(messageId);
		EmbedBuilder embed;
		if (message != null) {
			Member member = message.getAuthor();
			if (member.getUser().isBot()) {
				return;
			}
			String msg = message.getContent();
			embed = ObverserEmbed.get("❌ Message supprimé", null, "Un message de " + member.getAsMention() + " a été supprimé dans " + channel.getAsMention() + ".", member);
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
		if (!ObserverHandler.logMsgs || !DiscordUtils.isDefaultGuild(guild) || channel.getIdLong() == DiscordIds.getChannelInfo().getIdLong() || member == null || member.getUser().isBot()) {
			return;
		}
		ObserverHandler.addMessageCache(message);
		List<Attachment> attachments = message.getAttachments();
		if (ObserverHandler.logAttachment && !attachments.isEmpty()) {
			String desc = member.getAsMention() + " dans " + channel.getAsMention() + ":";
			EmbedBuilder embed = ObverserEmbed.get("✍ Pièces jointes", null, desc + "\n\nS'y rendre: " + message.getJumpUrl(), member);
			embed.setTimestamp(message.getTimeCreated());
			embed.setImage(attachments.get(0).getUrl());
			embed.addField("Pièce jointe", attachments.stream().map(a -> a.getFileName() + " " + a.getUrl()).collect(Collectors.joining("\n\n")), true);
			DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
		}
		System.out.println("test " + BungeeConfigUtils.getDefaultConfig().getStringList("chat.insult"));
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
	public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		Message message = event.getMessage();
		TextChannel channel = event.getChannel();
		if (!ObserverHandler.logMsgs || !DiscordUtils.isDefaultGuild(guild) || channel.getIdLong() == DiscordIds.getChannelInfo().getIdLong() || member == null || member.getUser().isBot()) {
			return;
		}
		MessageCache mc = ObserverHandler.getMessageCache(message.getIdLong());

		EmbedBuilder embed = ObverserEmbed.get("✍️ Message modifié", null, member.getAsMention() + " a modifié un message dans " + message.getTextChannel().getAsMention() + ".\n" + message.getJumpUrl(), member);
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
		EmbedBuilder embed = ObverserEmbed.get("✔️ Connecté au vocal", null, member.getAsMention() + " est connecté au salon vocal **" + event.getChannelJoined().getName() + "**.", member);
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
		EmbedBuilder embed = ObverserEmbed.get("❌ Déconnecté du vocal", null, member.getAsMention() + " est déconnecté du salon vocal **" + event.getChannelLeft().getName() + "**.", member);
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
	}

	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		if (!ObserverHandler.logVoice || !DiscordUtils.isDefaultGuild(guild) || user.isBot()) {
			return;
		}
		EmbedBuilder embed = ObverserEmbed.get("🪑 Changement de salon vocal", null, member.getAsMention() + " s'est déplacé.", member);
		embed.addField("Avant", "**" + event.getChannelJoined().getName() + "**", true);
		embed.addField("Après", "**" + event.getChannelLeft().getName() + "**", true);
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
	}

	// net.dv8tion.jda.api.events.guild.voice.

	@Override
	public void onUserUpdateName(UserUpdateNameEvent event) {
		User user = event.getUser();
		TextChannel channel = DiscordIds.getChannelInfo();
		Member member = channel.getGuild().getMember(user);
		if (!ObserverHandler.logUsername || member == null || user.isBot()) {
			return;
		}

		EmbedBuilder embed = ObverserEmbed.get("✏️ Changement de pseudo", null, user.getAsMention() + " a changer de **pseudo Discord**.", member);
		embed.addField("Avant", event.getOldName(), true);
		embed.addField("Après", event.getNewName(), true);
		channel.retrieveMessageById(channel.getLatestMessageIdLong()).queue(message -> {
			String msg = message.getContentRaw();
			System.out.println("DEBUG BOT DISCORD: " + msg);
			if (msg.contains("✏️ Changement de pseudo")) {
			}
		});
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
	}
}
