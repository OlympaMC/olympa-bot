package fr.olympa.bot.discord.api;

import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.OlympaDiscord;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class DiscordUtils {

	public static void allow(GuildChannel channel, IPermissionHolder role, Consumer<Void> success, Permission... perms) {
		if (role == null)
			role = channel.getGuild().getPublicRole();
		channel.getManager().putPermissionOverride(role, Arrays.asList(perms), null).queue(r -> {
			if (success != null)
				success.accept(r);
		});
	}

	public static boolean isReal(Member member) {
		return isReal(member.getUser());
	}

	public static boolean isReal(User user) {
		return !user.isBot() && !user.isSystem();
	}

	public static void allow(GuildChannel channel, IPermissionHolder role, Permission... perms) {
		allow(channel, role, null, perms);
	}

	public static void allow(GuildChannel channel, Permission... perms) {
		allow(channel, null, null, perms);
	}

	public static boolean compareGuild(Guild guild1, Guild guild2) {
		return guild1 != null && guild2 != null && guild1.getIdLong() == guild2.getIdLong();
	}

	public static ScheduledFuture<?> deleteTempMessage(Message message) {
		try {
			return message.delete().queueAfter(OlympaDiscord.getTimeToDelete(), TimeUnit.SECONDS, null, ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
		} catch (Exception e) {
			return null;
		}
	}

	public static void deny(GuildChannel channel, IPermissionHolder role, Consumer<Void> success, Permission... perms) {
		if (role == null)
			role = channel.getGuild().getPublicRole();
		channel.getManager().putPermissionOverride(role, null, Arrays.asList(perms)).queue(r -> {
			if (success != null)
				success.accept(r);
		});
	}

	public static void deny(GuildChannel channel, IPermissionHolder role, Permission... perms) {
		deny(channel, role, null, perms);
	}

	public static void deny(GuildChannel channel, Permission... perms) {
		deny(channel, null, null, perms);
	}

	public static long getMembersSize(Guild guild) {
		return guild.getMembers().stream().filter(mem -> !mem.getUser().isBot()).count();
	}

	public static boolean isMe(Member member) {
		return OlympaBots.getInstance().getDiscord().getJda().getSelfUser().getIdLong() == member.getIdLong();
	}

	public static boolean isMe(User user) {
		return OlympaBots.getInstance().getDiscord().getJda().getSelfUser().getIdLong() == user.getIdLong();
	}

	public static boolean isStaffGuild(Guild guild) {
		Guild staffGuild = GuildHandler.getOlympaGuild(DiscordGuildType.PUBLIC).getGuild();
		return compareGuild(staffGuild, guild);
	}

	public static void sendTempMessage(MessageAction message) {
		message.queue(msgSend -> deleteTempMessage(msgSend));
	}

	public static void sendTempMessage(MessageChannel channel, Member member, String msg) {
		sendTempMessage(channel, member.getAsMention() + " ➤ " + msg);
	}

	public static void sendTempMessage(MessageChannel channel, String msg) {
		sendTempMessage(channel.sendMessage(msg));
	}

	public static String getMemberFullNames(Member member) {
		if (member == null)
			return "membre inconnu";
		User user = member.getUser();
		if (member.getNickname() != null)
			return "`" + member.getNickname() + "`" + "(`" + user.getAsTag() + "`)";
		else
			return "`" + user.getAsTag() + "`";
	}

	public static String getMemberMentionNameFull(Member member) {
		return getMemberMentionNameFull(member.getUser());
	}

	public static String getMemberMentionNameFull(User user) {
		return user.getAsMention() + "(`" + user.getAsTag() + "`)";
	}
}
