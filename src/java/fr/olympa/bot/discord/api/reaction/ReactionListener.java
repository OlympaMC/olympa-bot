package fr.olympa.bot.discord.api.reaction;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.groups.DiscordGroup;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmoteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class ReactionListener extends ListenerAdapter {

	static final List<String> BANNED_EMOJI = Arrays.asList("⚠️");

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		long messageId = event.getMessageIdLong();
		ChannelType channelType = event.getChannelType();
		User user = event.getUser();
		@Nullable
		Member member = event.getMember();
		if (user.isBot())
			return;
		MessageReaction react = event.getReaction();
		ReactionEmote reactEmote = react.getReactionEmote();
		if (reactEmote.isEmoji()) {
			if (channelType.isGuild() && BANNED_EMOJI.contains(react.getReactionEmote().getEmoji()) && !DiscordPermission.ADMIN.hasPermission(member)) {
				react.removeReaction(user).queue(null, ErrorResponseException.ignore(ErrorResponse.MISSING_ACCESS));
				return;
			}
		} else if (reactEmote.isEmote()) {
			if (channelType.isGuild() && !DiscordGroup.isStaff(event.getMember()) && reactEmote.getEmote().isManaged()) {
				react.removeReaction(user).queue(null, ErrorResponseException.ignore(ErrorResponse.MISSING_ACCESS));
				return;
			}
		} else
			return;
		ReactionDiscord reaction = AwaitReaction.get(messageId);
		if (reaction == null)
			return;
		if (!reaction.canInteract(user) || !reaction.hasReactionEmoji(react.getReactionEmote().getName())) {
			if (channelType.isGuild())
				react.removeReaction(user).queue(null, ErrorResponseException.ignore(ErrorResponse.MISSING_ACCESS));
			return;
		}
		LinkSpigotBungee.Provider.link.getTask().runTaskAsynchronously(() -> {
			Message message = event.getTextChannel().retrieveMessageById(messageId).complete();
			long nb = 0;
			if (!reaction.canMultiple())
				nb = message.getReactions().stream().filter(r -> r.retrieveUsers().complete().contains(user)).count();
			if (nb > 1 || !reaction.onReactAdd(message, event.getChannel(), user, react, reaction.getReactionsEmojis(react))) {
				react.removeReaction(user).queue(null, ErrorResponseException.ignore(ErrorResponse.MISSING_ACCESS));
				return;
			}
		});

	}

	@Override
	public void onMessageDelete(MessageDeleteEvent event) {
		long messageId = event.getMessageIdLong();
		ReactionDiscord reaction = AwaitReaction.get(messageId);
		if (reaction == null)
			return;
		reaction.removeFromCache();
		reaction.removeFromDB();
	}

	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		long messageId = event.getMessageIdLong();
		User user = event.getUser();
		MessageReaction react = event.getReaction();
		ReactionDiscord reaction = AwaitReaction.get(messageId);
		if (reaction == null || user.isBot())
			return;
		event.getTextChannel().retrieveMessageById(messageId).queue(message -> reaction.onReactRemove(message, event.getChannel(), user, event.getReaction(), reaction.getReactionsEmojis(react)));
	}

	@Override
	public void onMessageReactionRemoveAll(MessageReactionRemoveAllEvent event) {
		long messageId = event.getMessageIdLong();
		ReactionDiscord reaction = AwaitReaction.get(messageId);
		if (reaction == null)
			return;
		reaction.onReactModClearAll(messageId, event.getChannel());
		if (reaction.isRemoveWhenModClearAll())
			reaction.remove(event.getChannel());
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
