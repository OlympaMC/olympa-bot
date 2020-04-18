package fr.olympa.bot.discord.observer;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.User;

public class MessageCache {

	User author;
	String content;
	List<Attachment> attachments = new ArrayList<>();

	public MessageCache(Message message) {
		author = message.getAuthor();
		content = message.getContentRaw();
		attachments = message.getAttachments();
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public User getAuthor() {
		return author;
	}

	public String getContent() {
		return content;
	}
}
