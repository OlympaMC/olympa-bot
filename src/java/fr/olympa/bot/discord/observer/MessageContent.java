package fr.olympa.bot.discord.observer;

import java.util.List;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class MessageContent {

	long timestamp;
	String content;
	List<MessageAttachement> attachments;
	
	public MessageContent() {
	}
	
	public MessageContent(Message message) {
		content = message.getContentRaw();
		if (message.isEdited())
			timestamp = message.getTimeEdited().toEpochSecond();
		else
			timestamp = message.getTimeCreated().toEpochSecond();
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
