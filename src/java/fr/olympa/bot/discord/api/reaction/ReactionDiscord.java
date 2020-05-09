package fr.olympa.bot.discord.api.reaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

public abstract class ReactionDiscord {

	List<Long> canReactUserIds = new ArrayList<>();
	Map<String, String> datas;
	boolean canMultiple = false;

	public ReactionDiscord(Map<String, String> datas, long... canReactUserIds) {
		this.datas = datas;
		this.canReactUserIds = Arrays.stream(canReactUserIds).boxed().collect(Collectors.toList());
	}

	public boolean canInteract(User user) {
		if (canReactUserIds.isEmpty()) {
			return true;
		}
		return canReactUserIds.stream().anyMatch(id -> user.getIdLong() == id);
	}

	public String getData(MessageReaction messageReaction) {
		String emoji = messageReaction.getReactionEmote().getEmoji();
		if (emoji != null) {
			return datas.get(emoji);
		}
		return null;
	}

	public Set<String> getEmojis() {
		return datas.keySet();
	}

	public abstract void onBotStop(long messageId);

	public abstract boolean onReactAdd(long messageId, MessageChannel messageChannel, MessageReaction messageReaction, User user);

	public abstract void onReactModClearAll(long messageId, MessageChannel messageChannel);

	public abstract void onReactModDeleteOne(long messageId, MessageChannel messageChannel);

	public abstract void onReactRemove(long messageId, MessageChannel messageChannel, MessageReaction messageReaction, User user);
}
