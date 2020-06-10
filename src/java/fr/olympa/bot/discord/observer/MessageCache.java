package fr.olympa.bot.discord.observer;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public class MessageCache {
	
	Member author;
	List<MessageContent> content = new ArrayList<>();
	Message logMsg = null;
	
	public MessageCache(Message message) {
		author = message.getMember();
		addEditedMessage(message);
	}
	
	public void addEditedMessage(Message message) {
		content.add(new MessageContent(message));
	}

	public Member getAuthor() {
		return author;
	}

	public MessageContent getContent() {
		return content.get(content.size() - 1);
	}

	public List<MessageContent> getContents() {
		return content;
	}
	
	public Message getLogMsg() {
		return logMsg;
	}
	
	public MessageContent getOriginalContent() {
		return content.get(0);
	}
	
	public void setLogMsg(Message logMsg) {
		this.logMsg = logMsg;
	}

	public void setOriginalNotFound() {
		content.add(0, new MessageContent());
	}

	public void setLastDeleted() {
		content.add(new MessageContent());
	}
}
