package fr.olympa.bot.discord.servers;

import org.apache.commons.collections4.map.LinkedMap;

import fr.olympa.bot.discord.api.reaction.ReactionDiscord;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

public class RefreshServersReaction extends ReactionDiscord {

	public RefreshServersReaction(LinkedMap<String, String> map, User... canReactUsers) {
		super(map, true, canReactUsers);
	}

	public RefreshServersReaction() {
		super();
	}

	@Override
	public void onReactModDeleteOne(long messageId, MessageChannel messageChannel) {}

	@Override
	public void onReactModClearAll(long messageId, MessageChannel messageChannel) {}

	@Override
	public boolean onReactAdd(Message message, MessageChannel messageChannel, User user, MessageReaction messageReaction, String reactionsEmojis) {
		if ("refresh".equalsIgnoreCase(reactionsEmojis))
			message.editMessageEmbeds(ServersCommand.getEmbeds()).queue();
		return false;
	}

	@Override
	public void onBotStop(long messageId) {}

	@Override
	public void onReactRemove(Message message, MessageChannel channel, User user, MessageReaction reaction, String reactionsEmojis) {}
}
