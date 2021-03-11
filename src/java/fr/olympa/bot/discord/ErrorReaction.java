package fr.olympa.bot.discord;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;

import org.apache.commons.collections4.map.LinkedMap;

import fr.olympa.bot.discord.api.reaction.ReactionDiscord;
import fr.olympa.bot.discord.guild.GuildHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

public class ErrorReaction extends ReactionDiscord {

	String serverName;
	String stackTrace;

	public ErrorReaction(SimpleEntry<String, String> entry, Message msg) {
		super((LinkedMap<String, String>) Map.of("☝️", "forMe", "📌", "pin", "👍", "fix", "❌", "delete"), msg.getIdLong(),
				GuildHandler.getOlympaGuildByDiscordId(msg.getGuild().getIdLong()).getId());
		serverName = entry.getKey();
		stackTrace = entry.getValue();
	}

	public ErrorReaction() {}

	@Override
	public void onBotStop(long messageId) {

	}

	@Override
	public boolean onReactAdd(Message message, MessageChannel messageChannel, User user, MessageReaction messageReaction, String data) {
		switch (data) {
		case "forMe":
			break;
		case "fix":
			break;
		case "delete":
			message.delete().queue();
			break;
		case "pin":
			message.pin().queue();
			break;
		}
		return false;
	}

	@Override
	public void onReactModClearAll(long messageId, MessageChannel messageChannel) {

	}

	@Override
	public void onReactModDeleteOne(long messageId, MessageChannel messageChannel) {

	}

	@Override
	public void onReactRemove(Message message, MessageChannel channel, User user, MessageReaction reaction, String reactionsEmojis) {
		switch (reactionsEmojis) {
		case "forMe":
			break;
		case "fix":
			break;
		case "pin":
			message.unpin().queue();
			break;
		}

	}

}
