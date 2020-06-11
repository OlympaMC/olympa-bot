package fr.olympa.bot.discord.observer;

import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.bot.discord.api.DiscordMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class MessageContent {
	
	private Long time;
	private String content;

	public long getTimestamp(DiscordMessage dm) {
		if (time != null)
			return time + dm.getCreated();
		return dm.getCreated();
	}
	
	public String getContent() {
		return content;
	}

	public List<MessageAttachement> getAttachments() {
		return attachments;
	}

	List<MessageAttachement> attachments;

	public MessageContent() {
	}

	public MessageContent(Message message, DiscordMessage dm) {
		content = message.getContentRaw();
		if (message.isEdited())
			time = message.getTimeEdited().toEpochSecond() - dm.getCreated();
		attachments = message.getAttachments().stream().map(a -> new MessageAttachement(a)).collect(Collectors.toList());
		if (attachments.isEmpty())
			attachments = null;
	}

	public MessageContent(String content, List<Attachment> attachments) {
		this.content = content;
		this.attachments = attachments.stream().map(a -> new MessageAttachement(a)).collect(Collectors.toList());
		if (attachments.isEmpty())
			attachments = null;
	}
	
	public boolean hasData() {
		return content != null;
	}

}
