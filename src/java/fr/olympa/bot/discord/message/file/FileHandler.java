package fr.olympa.bot.discord.message.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.match.MatcherPattern;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.message.JumpURL;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class FileHandler {

	public static File getFolder() {
		return new File(OlympaBots.getInstance().getDataFolder(), "discordAttachment");
	}

	public static void createFolder() {
		File folder = getFolder();
		if (!folder.exists())
			folder.mkdirs();
	}

	public static File getFile(String fileName) {
		return new File(getFolder(), fileName);
	}

	@Deprecated
	public static String addFile(String fileURL, String fileName) throws IOException {
		return addFile(new URL(fileURL), fileName);
	}

	@Deprecated
	public static String tryAddFile(String primarURL, String secondURL, String fileName) throws IOException {
		String file = null;
		try {
			file = addFile(new URL(primarURL), fileName);
		} catch (IOException e) {
			LinkSpigotBungee.Provider.link.sendMessage("Impossible de télécharger le fichier %s sur les serveurs discords, 2ème essai avec le lien original...", fileName);
			e.printStackTrace();
			file = addFile(new URL(secondURL), fileName);
		}
		return file;
	}

	public static String addFile(Attachment att, Message message) {
		String[] fileNameAndExt = att.getFileName().split("\\.");
		StringBuilder sb = new StringBuilder(fileNameAndExt[0]);
		String ext = fileNameAndExt.length > 1 ? "." + fileNameAndExt[1] : "";
		File attFile;
		int i = 0;
		do {
			if (i == 1)
				sb.append(" (" + i + ")");
			else if (i > 1)
				if (MatcherPattern.of("\\(\\d\\)$").contains(sb.toString()))
					sb = new StringBuilder(sb.toString().replace("\\(\\d\\)$", "(" + i + ")"));
				else
					sb.append(" (" + i + ")");
			attFile = new File(getFolder(), sb.toString() + ext);
			i++;
		} while (attFile.exists() && i < 1000);

		File finalFile = attFile;
		//		att.downloadToFile().complete(finalFile);
		Path path = Paths.get(finalFile.getPath());
		att.retrieveInputStream().whenComplete((in, err) -> {
			try {
				Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		try {
			Files.setAttribute(path, "user:Auteur", message.getAuthor());
			Files.setAttribute(path, "user:Serveur Discord", message.getGuild().getName() + "|" + message.getGuild().getIdLong());
			Files.setAttribute(path, "user:Serveur Owner", message.getGuild().getOwner().getUser().getAsTag());
			Files.setAttribute(path, "user:Channel", message.getChannel().getName());
			Files.setAttribute(path, "user:Channel Id", message.getChannel().getId());
			Files.setAttribute(path, "user:Link", new JumpURL(message).get());
			Files.setAttribute(path, "user:Message text", message.getContentDisplay());
			Files.setAttribute(path, "user:Original File Name", att.getFileName());
			Files.setAttribute(path, "user:Original URL", att.getUrl());
			Files.setAttribute(path, "user:Discord Proxy URL", att.getProxyUrl());
			Files.setAttribute(path, "user:Time", att.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString() + ext;
	}

	@Deprecated
	public static String addFile(URL fileURL, String fileName) throws IOException {
		String[] fileNameAndExt = fileName.split("\\.");
		StringBuilder sb = new StringBuilder(fileNameAndExt[0]);
		String ext = fileNameAndExt.length > 1 ? "." + fileNameAndExt[1] : new String();
		File attFile;
		int i = 0;
		do {
			if (i == 1)
				sb.append(" (" + i + ")");
			else if (i > 1)
				if (MatcherPattern.of("\\(\\d\\)$").contains(sb.toString()))
					sb = new StringBuilder(sb.toString().replace("\\(\\d\\)$", "(" + i + ")"));
				else
					sb.append(" (" + i + ")");
			attFile = new File(getFolder(), sb.toString() + ext);
			i++;
		} while (attFile.exists() && i < 1000);
		InputStream in = fileURL.openStream();
		Files.copy(in, Paths.get(attFile.getPath()), StandardCopyOption.REPLACE_EXISTING);
		return sb.toString() + ext;
	}

}
