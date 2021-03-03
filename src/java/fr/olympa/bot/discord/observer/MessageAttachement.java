package fr.olympa.bot.discord.observer;

import net.dv8tion.jda.api.entities.Message.Attachment;

public class MessageAttachement {

	public String getOriginalFileName() {
		return originalfileName;
	}

	public String getFileName() {
		return fileName;
	}

	public String getUrl() {
		return url;
	}

	public String getProxyUrl() {
		return proxyUrl;
	}

	String originalfileName;
	String fileName;
	String url;
	String proxyUrl;

	public MessageAttachement(Attachment attachement, String newName) {
		fileName = newName;
		originalfileName = attachement.getFileName();
		url = attachement.getUrl();
		proxyUrl = attachement.getProxyUrl();
	}

	public MessageAttachement(Attachment attachement) {
		originalfileName = attachement.getFileName();
		url = attachement.getUrl();
		proxyUrl = attachement.getProxyUrl();
	}

}
