package fr.olympa.bot.bungee;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import fr.olympa.core.bungee.OlympaBungee;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class ServerHandler {
	
	public static void action(String action, String serverName, Message message) {
		new Thread((Runnable) () -> {
			MessageChannel channel = message.getChannel();
			Member member = message.getMember();
			try {
				String s;
				Process p;
				p = Runtime.getRuntime().exec("mc " + action + " " + serverName);
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				StringBuilder sb = new StringBuilder();
				while ((s = br.readLine()) != null)
					sb.append(s);
				String out = sb.toString().replace("0;", "").replace("", "")
						.replace("[0m", "§f")
						.replace("[1m", "§l")
						.replace("[4m", "§n")
						.replace("[32m", "§3")
						.replace("[36m", "§b")
						.replace("[49m", "§f")
						.replace("[30m", "§0")
						.replace("[31m", "§4")
						.replace("[32m", "§2")
						.replace("[33m", "§6")
						.replace("[34m", "§1")
						.replace("[35m", "§5")
						.replace("[36m", "§3")
						.replace("[37m", "§7")
						.replace("[90m", "§8")
						.replace("[91m", "§c")
						.replace("[92m", "§a")
						.replace("[91m", "§e")
						.replace("[91m", "§9")
						.replace("[91m", "§d")
						.replace("[91m", "§b")
						.replace("[97m", "§f");
				channel.sendMessage(member.getAsMention() + " " + out).queue();
				OlympaBungee.getInstance().sendMessage("§c[§4OUT§c] §c" + out);
				p.waitFor();
				p.destroy();
			} catch (Exception e) {
				channel.sendMessage(member.getAsMention() + " **ERREUR**: " + e.getMessage()).queue();
				e.printStackTrace();
			}
		}).start();
	}
}
