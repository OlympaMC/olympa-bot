package fr.olympa.bot.bungee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.PatternSyntaxException;

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
				String out = sb.toString().replaceAll("(\\[\\]|)", "").replace("[0m", "").replace("[32m", "**").replace("[36m", "**").replace("[36m", "**").replace("[0;36m", "**").replace("[31m", "**").replace("[0;31m", "**");
				channel.sendMessage(member.getAsMention() + " " + out).queue();
				OlympaBungee.getInstance().sendMessage("§c[§4OUT§c] §c" + out);
				p.waitFor();
				p.destroy();
			} catch (IOException | InterruptedException | PatternSyntaxException e) {
				channel.sendMessage(member.getAsMention() + " " + e.getMessage()).queue();
				e.printStackTrace();
			}
		}).start();
	}
}
