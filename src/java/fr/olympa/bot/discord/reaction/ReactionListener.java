package fr.olympa.bot.discord.reaction;

import java.util.concurrent.ConcurrentMap;

import fr.olympa.bot.OlympaBots;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmoteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionListener extends ListenerAdapter {

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		long messageId = event.getMessageIdLong();
		OlympaBots.getInstance().getProxy().getScheduler().runAsync(OlympaBots.getInstance(), () -> {
			Message message = event.getTextChannel().retrieveMessageById(messageId).complete();
			User user = event.getUser();
			MessageReaction react = event.getReaction();
			ReactionDiscord reaction = AwaitReaction.get(messageId);
			if (reaction == null || user.isBot())
				return;
			long nb = 0;
			if (!reaction.canMultiple())
				nb = message.getReactions().stream().filter(r -> !r.retrieveUsers().complete().contains(user)).count();
			if (!reaction.canInteract(user) || nb > 1 || !reaction.onReactAdd(message, event.getChannel(), user, react, reaction.getData(react))) {
				react.removeReaction(user).queue();
				return;
			}
		});
	}

	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		long messageId = event.getMessageIdLong();
		User user = event.getUser();
		ReactionDiscord reaction = AwaitReaction.get(messageId);
		if (reaction == null || user.isBot())
			return;
		reaction.onReactRemove(messageId, event.getChannel(), event.getReaction(), user);
	}

	@Override
	public void onMessageReactionRemoveAll(MessageReactionRemoveAllEvent event) {
		long messageId = event.getMessageIdLong();
		ReactionDiscord reaction = AwaitReaction.get(messageId);
		if (reaction == null)
			return;
		reaction.onReactModClearAll(messageId, event.getChannel());
	}

	@Override
	public void onMessageReactionRemoveEmote(MessageReactionRemoveEmoteEvent event) {
		long messageId = event.getMessageIdLong();
		ReactionDiscord reaction = AwaitReaction.get(messageId);
		if (reaction == null)
			return;
		reaction.onReactModDeleteOne(messageId, event.getChannel());
	}

	@Override
	public void onStatusChange(StatusChangeEvent event) {
		if (!event.getNewStatus().equals(Status.SHUTTING_DOWN))
			return;
		ConcurrentMap<Long, ReactionDiscord> reactions = AwaitReaction.getAll();
		if (reactions == null || reactions.isEmpty())
			return;
		reactions.entrySet().forEach(entry -> entry.getValue().onBotStop(entry.getKey()));
	}
}
