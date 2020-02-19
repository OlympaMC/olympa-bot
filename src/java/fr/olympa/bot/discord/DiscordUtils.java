package fr.olympa.bot.discord;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class DiscordUtils {

	public static boolean compareGuild(Guild guild1, Guild guild2) {
		return guild1 != null && guild2 != null && guild1.getIdLong() == guild2.getIdLong();
	}

	public static ScheduledFuture<?> deleteTempMessage(Message message) {
		try {
			return message.delete().queueAfter(OlympaDiscord.timeToDelete, TimeUnit.SECONDS);
		} catch (Exception e) {
			return null;
		}
	}

	public static Guild getDefaultGuild() {
		return OlympaDiscord.getJda().getGuildById(544593846831415307L);
	}

	public static Member getMember(User user) {
		Guild guild = getDefaultGuild();
		Member member = null;
		if (guild != null) {
			member = guild.getMember(user);
		}
		return member;
	}

	public static Guild getStaffGuild() {
		return OlympaDiscord.getJda().getGuildById(541605430397370398L);
	}
	
	public static boolean isDefaultGuild(Guild guild) {
		Guild defaultGuild = getDefaultGuild();
		return compareGuild(defaultGuild, guild);
	}
	
	public static boolean isStaffGuild(Guild guild) {
		Guild staffGuild = getStaffGuild();
		return compareGuild(staffGuild, guild);
	}

	public static void sendTempMessage(MessageAction message) {
		message.queue(msgSend -> deleteTempMessage(msgSend));
	}
	
	public static void sendTempMessage(MessageChannel channel, String msg) {
		sendTempMessage(channel.sendMessage(msg));
	}
}
