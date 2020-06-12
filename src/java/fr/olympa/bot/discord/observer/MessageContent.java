package fr.olympa.bot.discord.observer;

import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.bot.discord.textmessage.DiscordMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class MessageContent {

	private Long time;
	private String content;
	private Boolean deleteOrNoData;
	
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

	public boolean isDeleteOrNoData() {
		return deleteOrNoData;
	}

	List<MessageAttachement> attachments;
	
	public MessageContent(boolean deleteOrNoData) {
		this.deleteOrNoData = deleteOrNoData;
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
