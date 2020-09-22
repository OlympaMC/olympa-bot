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
	private Boolean deleted = null;
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

	public Long getTime(MessageContent before) {
		return before != null && before.time != null && before.time != 0 ? time - before.time : time;
	}

	public boolean isDeleted() {
		return deleted != null && deleted;
	}

	public boolean isEmpty() {
		return content == null && deleted == null && attachments == null;
	}

	public MessageContent(boolean deleted) {
		this.deleted = deleted;
	}

	public MessageContent(Message message, DiscordMessage dm, Map<Attachment, String> attachments) {
		if (!message.getContentRaw().isBlank())
			content = message.getContentRaw();
		if (message.isEdited())
			time = message.getTimeEdited().toEpochSecond() - dm.getCreated();
		if (!attachments.isEmpty())
			this.attachments = attachments.entrySet().stream().map(e -> new MessageAttachement(e.getKey(), e.getValue())).collect(Collectors.toList());
	}

	public MessageContent(Message message, DiscordMessage dm) {
		content = message.getContentRaw();
		if (message.isEdited())
			time = message.getTimeEdited().toEpochSecond() - dm.getCreated();
		if (!message.getAttachments().isEmpty())
			attachments = message.getAttachments().stream().map(MessageAttachement::new).collect(Collectors.toList());
	}

	public MessageContent(String content, Map<Attachment, String> attachments) {
		this.content = content;
		if (!attachments.isEmpty())
			this.attachments = attachments.entrySet().stream().map(e -> new MessageAttachement(e.getKey(), e.getValue())).collect(Collectors.toList());
	}

	public boolean hasData() {
		return content != null || attachments != null;
	}

}
