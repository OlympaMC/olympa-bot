package fr.olympa.bot.discord.webhook;

import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.bot.discord.api.DiscordUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;

public class WebHookHandler {
	
	private static Guild getGuild() {
		return DiscordUtils.getStaffGuild();
	}
	
	JDA jda;
	
	public WebHookHandler(JDA jda) {
		this.jda = jda;
		
	}

	public TextChannel getChannel() {
		Guild guild = getGuild();
		List<GuildChannel> channels = guild.getChannels().stream().filter(ch -> ch.getName().endsWith("reseaux")).collect(Collectors.toList());
		if (channels.isEmpty()) {
			return null;
		}
		if (channels.size() > 1) {
			System.out.println("[ERROR] They are more than 1 Reseaux channel in guild " + guild.getName());
		}
		return (TextChannel) channels.stream().filter(ch -> ch.getType() == ChannelType.TEXT).findFirst().orElse(null);
	}
	
}
