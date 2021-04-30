package fr.olympa.bot.discord;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.LinkedMap;

import fr.olympa.bot.discord.api.reaction.ReactionDiscord;
import fr.olympa.bot.discord.commands.ClearCommand;
import fr.olympa.bot.discord.guild.GuildHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

public class ErrorReaction extends ReactionDiscord {

	String serverName;
	String stackTrace;
	Message message;
	Map<String, Integer> exceptionByServers = new HashMap<>();

	public ErrorReaction(String serverName, String stackTrace, Message message) {
		super(new LinkedMap<String, String>() {
			private static final long serialVersionUID = -3385687263702744975L;
			{
				put("☝️", "forMe");
				put("📌", "pin");
				put("👍", "fix");
				put("❌", "delete");
				put("🗑️", "clear_channel");
			}
		}, message.getIdLong(), GuildHandler.getOlympaGuildByDiscordId(message.getGuild().getIdLong()).getId());
		this.message = message;
		this.serverName = serverName;
		this.stackTrace = stackTrace;
		exceptionByServers.put(serverName, 1);
	}

	public ErrorReaction() {}

	@Override
	public boolean onReactAdd(Message message, MessageChannel messageChannel, User user, MessageReaction messageReaction, String data) {
		switch (data) {
		case "forMe":
			return true;
		case "fix":
			return true;
		case "delete":
			message.delete().queue();
			break;
		case "clear_channel":
			return !ClearCommand.clearAllMessage(user, message);
		case "pin":
			message.pin().queue();
			return true;
		}
		return false;
	}

	@Override
	public void onReactRemove(Message message, MessageChannel channel, User user, MessageReaction reaction, String reactionsEmojis) {
		switch (reactionsEmojis) {
		case "forMe":
			break;
		case "fix":
			break;
		case "clear_channel":
			ClearCommand.clearAllMessage(user, message);
		case "pin":
			message.unpin().queue();
			break;
		}
	}

	public void addServerError(String serverName) {
		int i = exceptionByServers.get(serverName);
		exceptionByServers.put(serverName, ++i);
	}

	public static String getDefaultTitle(String serverName) {
		return "**Erreur sur " + serverName + "**";
	}

	public String getMessageTitle() {
		return "**Erreur** " + exceptionByServers.entrySet().stream().map(entry -> "x" + entry.getValue() + " sur **" + entry.getKey() + "**").collect(Collectors.joining(", "));
	}

	public String getServerName() {
		return serverName;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public Message getMessage() {
		return message;
	}

	@Override
	public void onBotStop(long messageId) {}

	@Override
	public void onReactModClearAll(long messageId, MessageChannel messageChannel) {}

	@Override
	public void onReactModDeleteOne(long messageId, MessageChannel messageChannel) {}

}
