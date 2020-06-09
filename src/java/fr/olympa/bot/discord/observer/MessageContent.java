package fr.olympa.bot.discord.observer;

import java.util.List;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class MessageContent {

	long timestamp;
	String content;
	List<Attachment> attachments;
	
	public MessageContent() {
	}
	
	public MessageContent(Message message) {
		content = message.getContentRaw();
		if (message.isEdited())
			timestamp = message.getTimeEdited().toEpochSecond();
		else
			timestamp = message.getTimeCreated().toEpochSecond();
		attachments = message.getAttachments();
	}
	
	public MessageContent(String content, List<Attachment> attachments) {
		this.content = content;
		this.attachments = attachments;
	}

	public boolean hasData() {
		return content != null;
	}
}
