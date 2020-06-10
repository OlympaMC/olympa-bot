package fr.olympa.bot.discord.observer;

import java.awt.Color;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.OlympaDiscord;
import fr.olympa.bot.discord.api.DiscordIds;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.groups.DiscordGroup;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
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

public class ObserverListener extends ListenerAdapter {
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		if (!ObserverHandler.logEntries || !DiscordUtils.isDefaultGuild(guild) || user.isBot())
			return;
		EmbedBuilder embed = ObverserEmbed.get("✅ Un nouveau joueur est arrivé !", null, member.getAsMention() + " est le **" + DiscordUtils.getMembersSize(guild) + "ème** a rejoindre le discord.", member);
		embed.setColor(Color.GREEN);
		
		long time = user.getTimeCreated().toEpochSecond();
		long duration = Utils.getCurrentTimeInSeconds() - time;
		if (duration < 60 * 12 * 31)
			embed.addField("Nouveau compte", "Crée il y a " + Utils.timestampToDuration(user.getTimeCreated().toEpochSecond()), true);
		else
			embed.addField("Création du compte", "Depuis " + Utils.timestampToDuration(user.getTimeCreated().toEpochSecond()), true);
		
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
		
		Role defaultRole = DiscordGroup.PLAYER.getRole(guild);
		guild.addRoleToMember(member, defaultRole).queue();
	}
	
	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		if (!ObserverHandler.logEntries || !DiscordUtils.isDefaultGuild(guild) || user.isBot())
			return;
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
		if (!ObserverHandler.logRoles || !DiscordUtils.isDefaultGuild(guild) || user.isBot())
			return;
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
		if (!ObserverHandler.logRoles || !DiscordUtils.isDefaultGuild(guild) || user.isBot())
			return;
		if (member.getRoles().isEmpty()) {
			Role defaultRole = DiscordGroup.PLAYER.getRole(guild);
			guild.addRoleToMember(member, defaultRole).queue();
			if (event.getRoles().contains(defaultRole))
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
		if (!ObserverHandler.logUsername || !DiscordUtils.isDefaultGuild(guild) || user.isBot())
			return;
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
		if (!ObserverHandler.logMsgs || !DiscordUtils.isDefaultGuild(guild) || channel.getIdLong() == DiscordIds.getChannelInfo().getIdLong())
			return;
		MessageCache mc = ObserverHandler.getMessageCache(messageId);
		EmbedBuilder embed;
		if (mc == null) {
			embed = new EmbedBuilder();
			embed.setTitle("❌ Message supprimé");
			embed.setDescription("Un message vieux de plus de " + Utils.timestampToDuration(OlympaDiscord.uptime) + " a été supprimé dans " + channel.getAsMention() + ".");
			DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
		} else {
			Member member = mc.getAuthor();
			String originalTxt = mc.getOriginalContent().content;
			if (channel.getIdLong() == DiscordIds.getBotChannel().getIdLong() && originalTxt != null && !originalTxt.isBlank()) {
				List<String> prefixs = Arrays.asList("*", "!", "-");
				if (prefixs.stream().filter(pr -> originalTxt.startsWith(pr)).findFirst().isPresent())
					return;
			}
			if (member.getUser().isBot())
				return;
			embed = ObverserEmbed.get("❌ Message supprimé", null, "Un message de " + member.getAsMention() + " a été supprimé dans " + channel.getAsMention() + ".", member);
			StringBuilder attch = new StringBuilder();
			int i = 0;
			for (MessageContent mContent : mc.getContents()) {
				if (!mContent.hasData())
					embed.addField("Orignal", "❌ *le message a été écrit il y plus de " + Utils.timestampToDuration(OlympaDiscord.uptime) + ", nous n'avons aucunes données dessus.*", true);
				else {
					List<MessageAttachement> attachments = mContent.attachments;
					if (attachments != null && !attachments.isEmpty()) {
						embed.setImage(attachments.get(0).getUrl());
						attch.append("\n\nPièce jointe: " + attachments.stream().map(a -> a.getFileName() + " " + a.getUrl()).collect(Collectors.joining("\n")));
					}
					String editTime = "Original";
					if (i != 0)
						editTime = "Edit n°" + i;
					embed.addField(editTime + " (" + Utils.timestampToDateAndHour(mContent.timestamp) + ")", mContent.content + attch.toString(), true);
				}
				i++;
			}
			embed.addField("Suppr" + " (" + Utils.timestampToDateAndHour(Utils.getCurrentTimeInSeconds()) + ")", "❌", true);
			embed.setTimestamp(Instant.now());
			
			Consumer<? super EmbedBuilder> success = t -> {
				Message logMsg = mc.getLogMsg();
				if (logMsg != null)
					logMsg.editMessage(t.build()).queue();
				else
					DiscordIds.getChannelInfo().sendMessage(t.build()).queue();
			};
			
			// Check ghost tag
			String ghost = mc.getOriginalContent().content.replaceAll("\\s*<@!?\\d{18,}>\\s*", "");
			if (ghost.isEmpty()) {
				EmbedBuilder em = new EmbedBuilder();
				em.setTitle("Je te vois");
				em.setDescription("Les mentions fantômes sont interdites et sont passible de mute.");
				em.setColor(OlympaBots.getInstance().getDiscord().getColor());
				embed.appendDescription("\n😡 Suspicion de ghost tag");
				channel.sendMessage(member.getAsMention()).queue(m -> channel.sendMessage(em.build()).queue(msg -> {
					embed.appendDescription("\nS'y rendre: " + msg.getJumpUrl());
					success.accept(embed);
				}), msg -> {
					success.accept(embed);
				});
			}
			
		}
	}

	@Override
	public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		Message message = event.getMessage();
		TextChannel channel = event.getChannel();
		if (!ObserverHandler.logMsgs || !DiscordUtils.isDefaultGuild(guild) || channel.getIdLong() == DiscordIds.getChannelInfo().getIdLong() || member == null || member.getUser().isBot())
			return;
		MessageCache mc1 = ObserverHandler.getMessageCache(message.getIdLong());
		if (mc1 == null) {
			mc1 = ObserverHandler.addMessageCache(message);
			mc1.setOriginalNotFound();
		} else
			mc1.addEditedMessage(message);
		MessageCache mc = mc1;
		EmbedBuilder embed = ObverserEmbed.get("✍️ Message modifié", message.getJumpUrl(), member.getAsMention() + " a modifié un message dans " + message.getTextChannel().getAsMention() + ".\nS'y rendre: " + message.getJumpUrl(), member);
		OffsetDateTime time = message.getTimeEdited();
		if (time == null)
			time = message.getTimeCreated();
		embed.setTimestamp(time);
		
		StringBuilder attch = new StringBuilder();
		int i = 0;
		for (MessageContent mContent : mc.getContents()) {
			if (!mContent.hasData())
				embed.addField("Orignal", "❌ *le message a été écrit il y plus de " + Utils.timestampToDuration(OlympaDiscord.uptime) + ", nous n'avons aucunes données dessus.*", true);
			else {
				List<MessageAttachement> attachments = mContent.attachments;
				if (attachments != null && !attachments.isEmpty()) {
					embed.setImage(attachments.get(0).getUrl());
					attch.append("\n\nPièce jointe: " + attachments.stream().map(a -> a.getFileName() + " " + a.getUrl()).collect(Collectors.joining("\n")));
				}
				String editTime = "Original";
				if (i != 0)
					editTime = "Edit n°" + i;
				embed.addField(editTime + " (" + Utils.timestampToDateAndHour(mContent.timestamp) + ")", mContent.content + attch.toString(), true);
			}
			i++;
		}
		Message logMsg = mc.getLogMsg();
		if (logMsg != null)
			logMsg.editMessage(embed.build()).queue(msg -> mc.setLogMsg(msg));
		else
			DiscordIds.getChannelInfo().sendMessage(embed.build()).queue(msg -> mc.setLogMsg(msg));
	}
	
	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		if (!ObserverHandler.logVoice || !DiscordUtils.isDefaultGuild(guild) || user.isBot())
			return;
		EmbedBuilder embed = ObverserEmbed.get("✔️ Connecté au vocal", null, member.getAsMention() + " est connecté au salon vocal **" + event.getChannelJoined().getName() + "**.", member);
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Guild guild = event.getGuild();
		Message message = event.getMessage();
		TextChannel channel = message.getTextChannel();
		User author = event.getAuthor();
		if (!ObserverHandler.logMsgs || !DiscordUtils.isDefaultGuild(guild) || Arrays.stream(ObserverHandler.excludeChannelsIds).anyMatch(ex -> channel.getIdLong() == ex))
			return;
		ObserverHandler.addMessageCache(message);
		List<Attachment> attachments = message.getAttachments();
		Member authorMember = event.getMember();
		if (ObserverHandler.logAttachment && !attachments.isEmpty() && authorMember != null) {
			String desc = author.getAsMention() + " dans " + channel.getAsMention() + ":";
			EmbedBuilder embed = ObverserEmbed.get("✍ Pièces jointes", null, desc + "\nS'y rendre: " + message.getJumpUrl(), authorMember);
			embed.setTimestamp(message.getTimeCreated());
			embed.setImage(attachments.get(0).getUrl());
			embed.addField("Pièce jointe: ", attachments.stream().map(a -> a.getFileName() + " " + a.getProxyUrl()).collect(Collectors.joining("\n")), true);
			DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
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
		if (!ObserverHandler.logVoice || !DiscordUtils.isDefaultGuild(guild) || user.isBot())
			return;
		EmbedBuilder embed = ObverserEmbed.get("❌ Déconnecté du vocal", null, member.getAsMention() + " est déconnecté du salon vocal **" + event.getChannelLeft().getName() + "**.", member);
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
	}
	
	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		if (!ObserverHandler.logVoice || !DiscordUtils.isDefaultGuild(guild) || user.isBot())
			return;
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
		if (!ObserverHandler.logUsername || member == null || user.isBot())
			return;
		
		EmbedBuilder embed = ObverserEmbed.get("✏️ Changement de pseudo", null, user.getAsMention() + " a changer de **pseudo Discord**.", member);
		embed.addField("Avant", event.getOldName(), true);
		embed.addField("Après", event.getNewName(), true);
		//		channel.retrieveMessageById(channel.getLatestMessageIdLong()).queue(message -> {
		//			String msg = message.getContentRaw();
		//			System.out.println("DEBUG BOT DISCORD: " + msg);
		//			if (msg.contains("✏️ Changement de pseudo")) {
		//			}
		//		});
		DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
	}
}
