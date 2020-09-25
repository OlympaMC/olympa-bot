package fr.olympa.bot.discord.message.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.match.MatcherPattern;
import fr.olympa.bot.OlympaBots;

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

	public static String addFile(String fileURL, String fileName) throws IOException {
		return addFile(new URL(fileURL), fileName);
	}

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

	public static String addFile(URL fileURL, String fileName) throws IOException {
		String[] fileNameAndExt = fileName.split("\\.");
		String newName = fileNameAndExt[0];
		String ext = fileNameAndExt.length > 1 ? "." + fileNameAndExt[1] : new String();
		File attFile;
		int i = 0;
		do {
			if (i == 1)
				newName += " (" + i + ")";
			else if (i > 1)
				if (new MatcherPattern("\\(\\d\\)$").contains(newName))
					newName = newName.replace("\\(\\d\\)$", "(" + i + ")");
				else
					newName += " (" + i + ")";
			attFile = new File(getFolder(), newName + ext);
			i++;
		} while (attFile.exists() && i < 1000);
		InputStream in = fileURL.openStream();
		Files.copy(in, Paths.get(attFile.getPath()), StandardCopyOption.REPLACE_EXISTING);
		return newName + ext;
	}
}
