package fr.olympa.bot.discord.servers;

import java.util.Map;

import fr.olympa.bot.discord.reaction.ReactionDiscord;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

public class RefreshServersReaction extends ReactionDiscord {

	public RefreshServersReaction(Map<String, String> map, Message msg, long guildOlympaId, long... canReactUserIds) {
		super(map, msg.getIdLong(), guildOlympaId, canReactUserIds);
	}

	public RefreshServersReaction() {
		super();
	}

	@Override
	public void onReactRemove(long messageId, MessageChannel messageChannel, MessageReaction messageReaction, User user) {
	}

	@Override
	public void onReactModDeleteOne(long messageId, MessageChannel messageChannel) {
	}

	@Override
	public void onReactModClearAll(long messageId, MessageChannel messageChannel) {
	}

	@Override
	public boolean onReactAdd(Message message, MessageChannel messageChannel, User user, MessageReaction messageReaction, String data) {
		if ("refresh".equalsIgnoreCase(data))
			message.editMessage(ServersCommand.getEmbed()).queue();
		return false;
	}

	@Override
	public void onBotStop(long messageId) {
	}
}
