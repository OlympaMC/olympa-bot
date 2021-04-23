package fr.olympa.bot.discord;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

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
	List<Message> allMessages = new ArrayList<>();

	public ErrorReaction(SimpleEntry<String, String> entry, Message message) {
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
		serverName = entry.getKey();
		stackTrace = entry.getValue();
		allMessages.add(message);
	}

	public ErrorReaction() {}

	public void addMessage(Message message) {
		allMessages.add(message);
	}

	public void removeMessages() {
		message.getTextChannel().deleteMessages(allMessages);
	}

	@Override
	public boolean onReactAdd(Message message, MessageChannel messageChannel, User user, MessageReaction messageReaction, String data) {
		switch (data) {
		case "forMe":
			return true;
		case "fix":
			return true;
		case "delete":
			removeMessages();
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

	@Override
	public void onBotStop(long messageId) {

	}

	@Override
	public void onReactModClearAll(long messageId, MessageChannel messageChannel) {

	}

	@Override
	public void onReactModDeleteOne(long messageId, MessageChannel messageChannel) {

	}
}
