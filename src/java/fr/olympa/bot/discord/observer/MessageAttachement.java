package fr.olympa.bot.discord.observer;

import net.dv8tion.jda.api.entities.Message.Attachment;

public class MessageAttachement {

	public String getFileName() {
		return fileName;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getProxyUrl() {
		return proxyUrl;
	}
	
	String fileName;
	String url;
	String proxyUrl;
	
	public MessageAttachement(Attachment attachement) {
		fileName = attachement.getFileName();
		url = attachement.getUrl();
		proxyUrl = attachement.getProxyUrl();
	}
	
}
