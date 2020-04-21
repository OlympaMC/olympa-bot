package fr.olympa.bot.discord.observer;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class MessageCache {

	Member author;
	String content;
	List<Attachment> attachments = new ArrayList<>();

	public MessageCache(Message message) {
		author = message.getMember();
		content = message.getContentRaw();
		attachments = message.getAttachments();
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public Member getAuthor() {
		return author;
	}

	public String getContent() {
		return content;
	}
}
