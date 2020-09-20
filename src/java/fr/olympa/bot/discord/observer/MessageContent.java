package fr.olympa.bot.discord.observer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.olympa.bot.discord.message.DiscordMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class MessageContent {

	private Long time;
	private String content;
	private Boolean deleted;
	List<MessageAttachement> attachments;

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

	public Long getTime() {
		return time;
	}

	public boolean isDeleted() {
		return deleted == true;
	}

	public MessageContent(boolean deleted) {
		this.deleted = deleted;
	}

	public MessageContent(Message message, DiscordMessage dm, Map<Attachment, String> attachments) {
		content = message.getContentRaw();
		if (message.isEdited())
			time = message.getTimeEdited().toEpochSecond() - dm.getCreated();
		this.attachments = attachments.entrySet().stream().map(e -> new MessageAttachement(e.getKey(), e.getValue())).collect(Collectors.toList());
		if (attachments.isEmpty())
			attachments = null;
	}

	public MessageContent(Message message, DiscordMessage dm) {
		content = message.getContentRaw();
		if (message.isEdited())
			time = message.getTimeEdited().toEpochSecond() - dm.getCreated();
		attachments = message.getAttachments().stream().map(e -> new MessageAttachement(e)).collect(Collectors.toList());
		if (attachments.isEmpty())
			attachments = null;
	}

	public MessageContent(String content, Map<Attachment, String> attachments) {
		this.content = content;
		this.attachments = attachments.entrySet().stream().map(e -> new MessageAttachement(e.getKey(), e.getValue())).collect(Collectors.toList());
		if (attachments.isEmpty())
			attachments = null;
	}

	public boolean hasData() {
		return content != null || attachments != null;
	}

}
