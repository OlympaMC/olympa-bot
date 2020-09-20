package fr.olympa.bot.discord.message;

import java.io.File;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
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
		User user = member.getUser();
		EmbedBuilder embed = new EmbedBuilder().setTitle(title, titleUrl).setDescription(description);
		//embed.setAuthor(member.getEffectiveName(), user.getAvatarUrl(), user.getEffectiveAvatarUrl());
		embed.setFooter(user.getAsTag() + " | " + member.getId());
		embed.setColor(OlympaBots.getInstance().getDiscord().getColor());
		embed.setThumbnail(user.getAvatarUrl());
		embed.setTimestamp(OffsetDateTime.now());
		return embed;
	}

	public static void sendMessage(DiscordMessage discordMessage, String title, String titleUrl, String description, Member member) {
		if (member.getUser().isBot())
			return;
		EmbedBuilder embed = get(title, titleUrl, description, member);
		String attch = new String();
		int i = 0;
		MessageContent lastMContent = null;
		TextChannel logChannel = discordMessage.getOlympaGuild().getLogChannel();
		Map<MessageAttachement, File> attWithData = new HashMap<>();
		for (MessageContent mContent : discordMessage.getContents()) {
			if (!mContent.hasData())
				if (mContent.isDeleted())
					embed.addField("Suppr" + " (" + Utils.timestampToDateAndHour(Utils.getCurrentTimeInSeconds()) + ")", "❌", true);
				else
					embed.addField("Message", "❌ **nous n'avons aucunes données dessus.**", true);
			else {
				List<MessageAttachement> attachments = mContent.getAttachments();
				if (attachments != null && !attachments.isEmpty())
					attachments.stream().filter(att -> !attWithData.keySet().stream().anyMatch(ma -> ma.getOriginalFileName().equals(att.getOriginalFileName()))).forEach(att -> {
						if (att.getFileName() != null)
							//							logChannel.sendFile(FileHandler.getFile(att.getFileName()))
							//									.append(member.getEffectiveName() + " a un fichier `" + att.getOriginalFileName() + "` dans son message \nLien Original : `" + att.getUrl() + "`").queue();
							attWithData.put(att, FileHandler.getFile(att.getFileName()));
					});
				//					String s = Utils.withOrWithoutS(attachments.size());
				//					attch = "\n\n**Pièce" + s + " jointe" + s + ":** " + attachments.stream().map(a -> "`" + a.getOriginalFileName() + "` " + a.getUrl()).collect(Collectors.joining(", "));
				String editTime = "Original";
				if (i != 0)
					editTime = "Edit n°" + i;
				String content = mContent.getContent();
				if (lastMContent != null && lastMContent.hasData())
					if (content.contains(lastMContent.getContent()))
						content = content.replace(lastMContent.getContent(), "➡️");
				if (content.length() + attch.length() > MessageEmbed.VALUE_MAX_LENGTH) {
					int tooLarge = MessageEmbed.VALUE_MAX_LENGTH - attch.length();
					String tooLargeS = " **" + String.valueOf(tooLarge) + " chars de plus...**";
					tooLarge += tooLargeS.length();
					content = content.substring(0, tooLarge + tooLarge) + tooLargeS + attch;
				}
				embed.addField(editTime + " (" + Utils.timestampToDateAndHour(mContent.getTimestamp(discordMessage)) + ")", content + attch, true);
			}
			lastMContent = mContent;
			i++;
		}
		embed.setTimestamp(Instant.now());
		MessageAction messageAction = null;
		if (discordMessage.isDeleted())
			for (Entry<MessageAttachement, File> e : attWithData.entrySet()) {
				String desc = String.format("Pièce jointe `%s`\nLien original `%s`\n", e.getKey().getOriginalFileName(), e.getKey().getUrl());
				if (messageAction == null)
					messageAction = logChannel.sendFile(e.getValue()).append(desc);
				else
					messageAction = messageAction.addFile(e.getValue()).append(desc);
			}
		if (discordMessage.getLogMessageId() != 0) {
			discordMessage.getLogMsg().queue(logMsg -> logMsg.editMessage(embed.build()).queue());
			logChannel.editMessageById(discordMessage.getLogMessageId(), embed.build()).queue();
			if (messageAction != null)
				messageAction.append("Info sur le msg supprimé : " + discordMessage.getLogJumpUrl() + "\n").queue();
		} else {
			Consumer<Message> succes = logMsg2 -> {
				try {
					DiscordMessage discordMessage2 = CacheDiscordSQL.getDiscordMessage(discordMessage.getGuildId(), discordMessage.getChannelId(), discordMessage.getMessageId()).getValue();
					discordMessage2.setLogMsg(logMsg2);
					CacheDiscordSQL.setDiscordMessage(member.getIdLong(), discordMessage2);
					SqlMessage.updateMessageLogMsgId(discordMessage2);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			};
			if (messageAction != null)
				messageAction.embed(embed.build()).queue(succes);
			else
				logChannel.sendMessage(embed.build()).queue(succes);
		}

	}
}
