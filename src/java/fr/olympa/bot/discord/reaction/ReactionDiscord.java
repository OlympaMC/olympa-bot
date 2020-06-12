package fr.olympa.bot.discord.reaction;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

public abstract class ReactionDiscord {

	public long getOlympaGuildId() {
		return olympaGuildId;
	}

	public void setOlympaGuildId(long olympaGuildId) {
		this.olympaGuildId = olympaGuildId;
	}

	private Map<String, String> datas;
	private List<Long> canReactUserIds;
	private boolean canMultiple = false;
	private long messageId, olympaGuildId;

	public long getMessageId() {
		return messageId;
	}
	
	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}
	
	public ReactionDiscord(Map<String, String> datas, long messageId, long olympaGuildId, long... canReactUserIds) {
		this.messageId = messageId;
		this.olympaGuildId = olympaGuildId;
		this.datas = datas;
		this.canReactUserIds = Arrays.stream(canReactUserIds).boxed().collect(Collectors.toList());
	}
	
	public Map<String, String> getDatas() {
		return datas;
	}
	
	public List<Long> getCanReactUserIds() {
		return canReactUserIds;
	}
	
	public boolean canMultiple() {
		return canMultiple;
	}
	
	public ReactionDiscord(String datas, String canReactUserIds, boolean canMultiple, long messageId, long olympaGuildId) {
		this.datas = new Gson().fromJson(datas, new TypeToken<Map<String, String>>() {
		}.getType());
		this.canReactUserIds = new Gson().fromJson(canReactUserIds, new TypeToken<List<Long>>() {
		}.getType());
		this.canMultiple = canMultiple;
		this.messageId = messageId;
		this.olympaGuildId = olympaGuildId;
	}

	public boolean canInteract(User user) {
		if (canReactUserIds.isEmpty())
			return true;
		return canReactUserIds.stream().anyMatch(id -> user.getIdLong() == id);
	}
	
	public String getData(MessageReaction messageReaction) {
		String emoji = messageReaction.getReactionEmote().getEmoji();
		if (emoji != null)
			return datas.get(emoji);
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
