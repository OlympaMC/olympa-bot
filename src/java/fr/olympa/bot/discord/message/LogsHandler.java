package fr.olympa.bot.discord.message;

import java.io.File;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.message.file.FileHandler;
import fr.olympa.bot.discord.observer.MessageAttachement;
import fr.olympa.bot.discord.observer.MessageContent;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class LogsHandler {

	public static EmbedBuilder get(String title, String titleUrl, String description, Member member) {
		return get(title, titleUrl, description, member.getUser());
	}

	public static EmbedBuilder get(String title, String titleUrl, String description, User user) {
		EmbedBuilder embed = new EmbedBuilder().setTitle(title, titleUrl).setDescription(description);
		if (user != null) {
			embed.setFooter(user.getAsTag() + " | " + user.getId());
			embed.setThumbnail(user.getAvatarUrl());
		}
		embed.setColor(OlympaBots.getInstance().getDiscord().getColor());
		embed.setTimestamp(OffsetDateTime.now());
		return embed;
	}

	public static EmbedBuilder get(String title, String titleUrl, String description, User user, User author) {
		EmbedBuilder embed = get(title, titleUrl, description, user);
		if (author != null)
			embed.setAuthor(author.getAsTag(), author.getAvatarUrl(), author.getEffectiveAvatarUrl());
		return embed;
	}

	public static void sendMessage(DiscordMessage discordMessage, String title, String titleUrl, String description, Member member, User author) {
		if (member.getUser().isBot() || !DiscordUtils.isReal(member.getUser()) || discordMessage.isEmpty())
			return;
		EmbedBuilder embed = get(title, titleUrl, description, member.getUser(), author);
		int i = 0;
		MessageContent lastMContent = null;
		TextChannel logChannel = discordMessage.getOlympaGuild().getLogChannel();
		Map<MessageAttachement, File> attWithData = new HashMap<>();
		List<MessageContent> contents = discordMessage.getContents();
		int totalEdits = contents.size();
		if (totalEdits > 24) {
			MessageContent original = contents.get(0);
			contents = contents.subList(contents.size() - 23, contents.size());
			contents.add(original);
			embed.appendDescription("\nIl a eu trop d'éditions, seule l'original et les 24 dernières éditions peuvent être afficher (un dev peux accéder à l'historique complet).");
		}
		for (MessageContent mContent : contents) {
			if (discordMessage.getContents().indexOf(mContent) == 0 && mContent.getAttachments() != null) {
				String s = Utils.withOrWithoutS(mContent.getAttachments().size());
				embed.appendDescription("\n\n**Pièce" + s + " jointe" + s + ":** " + mContent.getAttachments().stream().map(a -> "`" + a.getOriginalFileName() + "` " + a.getUrl()).collect(Collectors.joining(", ")));
				List<String> exts = Arrays.asList(".jpg", ".jpe", ".bmp", ".gif", ".png");
				MessageAttachement image = mContent.getAttachments().stream().filter(att -> exts.stream().anyMatch(e -> att.getFileName().toLowerCase().endsWith(e))).findFirst().orElse(null);
				if (image != null)
					embed.setImage(image.getProxyUrl());
			}
			if (!mContent.hasData())
				if (mContent.isDeleted())
					embed.addField("Suppr" + " (" + Utils.timestampToDateAndHour(Utils.getCurrentTimeInSeconds()) + ")", "❌", true);
				else
					embed.addField("Message", "❌ **nous n'avons aucunes données dessus.**", true);
			else {
				List<MessageAttachement> attachments = mContent.getAttachments();
				if (attachments != null && !attachments.isEmpty())
					attachments.stream().filter(att -> attWithData.keySet().stream().noneMatch(ma -> ma.getOriginalFileName().equals(att.getOriginalFileName()))).forEach(att -> {
						if (att.getFileName() != null)
							attWithData.put(att, FileHandler.getFile(att.getFileName()));
					});
				String editTime;
				if (i == 0)
					editTime = "Original";
				else {

					if (totalEdits > 24)
						editTime = "Edit n°" + (totalEdits - 23 + i);
					else
						editTime = "Edit n°" + i;
					if (lastMContent != null && mContent.getTime() != null)
						editTime += " (" + Utils.timeToDuration(mContent.getTime(lastMContent)) + " après)";
				}
				String content = mContent.getContent();
				if (content != null && !content.isBlank()) {
					if (lastMContent != null && lastMContent.hasData() && lastMContent.getContent() != null)
						if (lastMContent.getContent().contains(content))
							content = content.replace(lastMContent.getContent(), "➡️");
					if (content.length() > MessageEmbed.VALUE_MAX_LENGTH) {
						int tooLarge = content.length() - MessageEmbed.VALUE_MAX_LENGTH;
						String tooLargeS = " **" + tooLarge + " chars de plus...**";
						tooLarge += tooLargeS.length();
						content = content.substring(0, content.length() - tooLarge) + tooLargeS;
					}
				} else
					content = "`Message vide`";
				EmbedBuilder em2 = new EmbedBuilder(embed).addField(editTime, content, false);
				if (em2.isValidLength())
					embed.addField(editTime, content, false);
				else {
					embed.addField(editTime, "`Erreur > Trop de charatères dans l'Embed.`", true);
					break;
				}
			}
			lastMContent = mContent;
			i++;
		}
		embed.setTimestamp(Instant.ofEpochMilli(discordMessage.getCreated() * 1000L));
		MessageAction messageAction = null;
		if (discordMessage.isDeleted())
			for (Entry<MessageAttachement, File> e : attWithData.entrySet()) {
				String desc = String.format("Pièce jointe `%s`%nLien original `%s`%n", e.getKey().getOriginalFileName(), e.getKey().getUrl());
				if (messageAction == null)
					messageAction = logChannel.sendFile(e.getValue()).append(desc);
				else
					messageAction = messageAction.addFile(e.getValue()).append(desc);
			}
		if (discordMessage.getLogMessageId() != 0) {
			discordMessage.getLogMsg().queue(logMsg -> logMsg.editMessageEmbeds(embed.build()).queue());
			logChannel.editMessageEmbedsById(discordMessage.getLogMessageId(), embed.build()).queue();
			if (messageAction != null)
				messageAction.append("Info sur le msg supprimé : " + discordMessage.getLogJumpUrl() + "\n").queue();
		} else {
			CacheDiscordSQL.setDiscordMessage(member.getIdLong(), discordMessage);
			Consumer<Message> succes = logMsg2 -> {
				try {
					DiscordMessage discordMessage2 = CacheDiscordSQL.getDiscordMessage(discordMessage.getGuildId(), discordMessage.getChannelId(), discordMessage.getMessageId()).getValue();
					discordMessage2.setLogMsg(logMsg2);
					SQLMessage.updateMessageLogMsgId(discordMessage2);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			};
			if (messageAction != null)
				messageAction.embed(embed.build()).queue(succes);
			else
				logChannel.sendMessageEmbeds(embed.build()).queue(succes);
		}

	}
}
