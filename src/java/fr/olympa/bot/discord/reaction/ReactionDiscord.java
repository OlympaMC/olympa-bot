package fr.olympa.bot.discord.reaction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.LinkedMap;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import fr.olympa.bot.discord.guild.GuildHandler;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.User;

public abstract class ReactionDiscord {

	public boolean isRemoveWhenModClearAll() {
		return removeWhenModClearAll;
	}

	public void addToMessage(Message message) {
		AwaitReaction.reactions.put(message.getIdLong(), this);

		addReaction(message, getEmojis());
		setMessageId(message.getIdLong());
		setOlympaGuildId(GuildHandler.getOlympaGuild(message.getGuild()).getId());
	}

	private void addReaction(Message message, List<String> emojis) {
		emojis.forEach(emoji -> message.addReaction(emoji).queue());
	}

	public Boolean removeFromDB() {
		try {
			return ReactionSQL.removeReaction(this);
		} catch (SecurityException | SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Boolean remove(MessageChannel messageChannel) {
		AwaitReaction.reactions.invalidate(messageId);
		messageChannel.retrieveMessageById(messageId).queue(message -> message.clearReactions().queue());
		return removeFromDB();
	}

	public void saveToDB() {
		try {
			ReactionSQL.addReaction(this);
		} catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException | SQLException e) {
			e.printStackTrace();
		}
	}

	public LinkedMap<String, String> reactionsEmojis;

	private List<Long> canReactUserIds;

	private boolean canMultiple = false;
	private boolean removeWhenModClearAll = true;
	private long messageId, olympaGuildId;

	public ReactionDiscord() {
	}

	public ReactionDiscord(LinkedMap<String, String> reactionsEmojis, long messageId, long olympaGuildId, IMentionable... canReactUsers) {
		this.reactionsEmojis = reactionsEmojis;
		this.messageId = messageId;
		this.olympaGuildId = olympaGuildId;
		canReactUserIds = Arrays.stream(canReactUsers).mapToLong(IMentionable::getIdLong).boxed().collect(Collectors.toList());
	}

	public ReactionDiscord(LinkedMap<String, String> reactionsEmojis, long messageId, long olympaGuildId) {
		this.reactionsEmojis = reactionsEmojis;
		this.messageId = messageId;
		this.olympaGuildId = olympaGuildId;
	}

	public boolean canInteract(User user) {
		if (canReactUserIds == null || canReactUserIds.isEmpty())
			return true;
		return canReactUserIds.stream().anyMatch(id -> user.getIdLong() == id);
	}

	public boolean canMultiple() {
		return canMultiple;
	}

	public List<Long> getCanReactUserIds() {
		return canReactUserIds;
	}

	public String getReactionsEmojis(MessageReaction messageReaction) {
		ReactionEmote emote = messageReaction.getReactionEmote();
		if (emote.isEmote())
			return null;
		String emoji = messageReaction.getReactionEmote().getEmoji();
		if (emoji != null)
			return reactionsEmojis.get(emoji);
		return null;
	}

	public boolean hasReactionEmoji(String reactionsEmojisKey) {
		return reactionsEmojis.keySet().contains(reactionsEmojisKey);
	}

	public Map<String, String> getDatas() {
		return reactionsEmojis;
	}

	public List<String> getEmojis() {
		if (reactionsEmojis.firstKey().equals("question"))
			return reactionsEmojis.asList().subList(1, reactionsEmojis.size());
		return reactionsEmojis.asList();
	}

	public long getMessageId() {
		return messageId;
	}

	public long getOlympaGuildId() {
		return olympaGuildId;
	}

	public abstract void onBotStop(long messageId);

	public abstract boolean onReactAdd(Message message, MessageChannel messageChannel, User user, MessageReaction messageReaction, String data);

	public abstract void onReactModClearAll(long messageId, MessageChannel messageChannel);

	public abstract void onReactModDeleteOne(long messageId, MessageChannel messageChannel);

	public abstract void onReactRemove(Message message, MessageChannel channel, User user, MessageReaction reaction, String reactionsEmojis);

	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}

	public void setOlympaGuildId(long olympaGuildId) {
		this.olympaGuildId = olympaGuildId;
	}

	public void createObject(ResultSet resultSet) throws JsonSyntaxException, SQLException {
		reactionsEmojis = new Gson().fromJson(resultSet.getString("data"), new TypeToken<LinkedMap<String, String>>() {
		}.getType());
		String allowed = resultSet.getString("allowed_users_ids");
		if (allowed != null && !allowed.isEmpty())
			canReactUserIds = new Gson().fromJson(resultSet.getString("allowed_users_ids"), new TypeToken<List<Long>>() {
			}.getType());
		else
			canReactUserIds = new ArrayList<>();
		canMultiple = resultSet.getInt("can_multiple") == 1;
		removeWhenModClearAll = resultSet.getInt("remove_when_modclearall") == 1;
		messageId = resultSet.getLong("message_id");
		olympaGuildId = resultSet.getLong("guild_id");
	}

}
