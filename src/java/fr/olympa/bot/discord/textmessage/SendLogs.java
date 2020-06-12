package fr.olympa.bot.discord.textmessage;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.observer.MessageAttachement;
import fr.olympa.bot.discord.observer.MessageContent;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public class SendLogs {

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
	
	public static void sendMessageLog(DiscordMessage discordMessage, String title, String titleUrl, String description, Member member) {
		if (member.getUser().isBot())
			return;
		EmbedBuilder embed = get(title, titleUrl, description, member);
		String attch = new String();
		int i = 0;
		MessageContent lastMContent = null;
		for (MessageContent mContent : discordMessage.getContents()) {
			if (!mContent.hasData())
				if (mContent.isDeleteOrNoData())
					embed.addField("Suppr" + " (" + Utils.timestampToDateAndHour(Utils.getCurrentTimeInSeconds()) + ")", "❌", true);
				else
					embed.addField("Message", "❌ **nous n'avons aucunes données dessus.**", true);
			else {
				List<MessageAttachement> attachments = mContent.getAttachments();
				if (attachments != null && !attachments.isEmpty()) {
					embed.setImage(attachments.get(0).getUrl());
					String s = Utils.withOrWithoutS(attachments.size());
					attch = "\n\n**Pièce" + s + " jointe" + s + ":** " + attachments.stream().map(a -> "`" + a.getFileName() + "` " + a.getUrl()).collect(Collectors.joining(", "));
				}
				String editTime = "Original";
				if (i != 0)
					editTime = "Edit n°" + i;
				String content = mContent.getContent();
				if (lastMContent != null && lastMContent.hasData())
					if (content.contains(lastMContent.getContent()))
						content = content.replace(lastMContent.getContent(), "➡️");
				embed.addField(editTime + " (" + Utils.timestampToDateAndHour(mContent.getTimestamp(discordMessage)) + ")", content + attch, true);
			}
			lastMContent = mContent;
			i++;
		}
		embed.setTimestamp(Instant.now());
		Message logMsg = discordMessage.getLogMsg();
		if (logMsg != null)
			logMsg.editMessage(embed.build()).queue();
		else
			discordMessage.getOlympaGuild().getLogChannel().sendMessage(embed.build()).queue(logMsg2 -> {
				discordMessage.setLogMsg(logMsg2);
				CacheDiscordSQL.setDiscordMessage(member.getIdLong(), discordMessage);
			});
	}
}
