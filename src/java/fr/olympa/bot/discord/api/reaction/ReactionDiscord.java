package fr.olympa.bot.discord.api.reaction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.collections4.map.LinkedMap;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.discord.guild.GuildHandler;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;

public abstract class ReactionDiscord {

	public Map<Object, Object> data = new HashMap<>();
	public LinkedMap<String, String> reactionsEmojis;

	private List<Long> canReactUserIds;

	private boolean canMultiple;
	private boolean removeWhenModClearAll = true;
	@Nullable
	private long messageId;
	@Nullable
	private long olympaGuildId;
	@Nullable
	protected Message message;
	protected long time;

	protected ReactionDiscord() {}

	protected ReactionDiscord(LinkedMap<String, String> reactionsEmojis, boolean canMultiple, IMentionable... canReactUsers) {
		this(reactionsEmojis, canMultiple);
		canReactUserIds = Arrays.stream(canReactUsers).mapToLong(IMentionable::getIdLong).boxed().toList();
	}

	protected ReactionDiscord(LinkedMap<String, String> reactionsEmojis, boolean canMultiple) {
		this.canMultiple = canMultiple;
		this.reactionsEmojis = reactionsEmojis;
		time = Utils.getCurrentTimeInSeconds();
	}

	/**
	 *
	 * @param canMultiple
	 * @param emojisAndName emoji, name, emoji, name ...
	 */
	protected ReactionDiscord(boolean canMultiple, String... emojisAndName) {
		this.canMultiple = canMultiple;
		reactionsEmojis = new LinkedMap<>();
		if (emojisAndName != null && emojisAndName.length != 0)
			for (int i = 0; emojisAndName.length > i + 1; i++)
				reactionsEmojis.put(emojisAndName[i], emojisAndName[++i]);
	}

	public boolean canInteract(User user) {
		if (canReactUserIds == null || canReactUserIds.isEmpty())
			return true;
		return canReactUserIds.stream().anyMatch(id -> user.getIdLong() == id);
	}

	public long getTime() {
		return time;
	}

	public boolean setMutiple(boolean canMultiple) {
		return this.canMultiple = canMultiple;
	}

	public boolean enableMutiple() {
		return canMultiple = true;
	}

	public boolean disableMutiple() {
		return canMultiple = false;
	}

	public boolean disableWhenModClearAll() {
		return removeWhenModClearAll = true;
	}

	public boolean canMultiple() {
		return canMultiple;
	}

	public List<Long> getCanReactUserIds() {
		return canReactUserIds;
	}

	public String getReactionsEmojis(MessageReaction messageReaction) {
		ReactionEmote emote = messageReaction.getReactionEmote();
		if (emote.isEmote() || reactionsEmojis == null)
			return null;
		String emoji = messageReaction.getReactionEmote().getEmoji();
		return reactionsEmojis.get(emoji);
	}

	public boolean hasReactionEmoji(String reactionsEmojisKey) {
		return reactionsEmojis != null && reactionsEmojis.keySet().contains(reactionsEmojisKey);
	}

	public Map<Object, Object> getData() {
		return data;
	}

	public Object putData(Object key, Object value) {
		return data.put(key, value);
	}

	public Object removeData(Object key) {
		return data.remove(key);
	}

	public Object getData(Object key) {
		return data.get(key);
	}

	public Map<String, String> getEmojisData() {
		return reactionsEmojis;
	}

	public List<String> getEmojis() {
		return reactionsEmojis == null ? null : reactionsEmojis.asList();
	}

	public long getMessageId() {
		return messageId;
	}
	//
	//	public Message getMessage() {
	//		return message;
	//	}

	public boolean isGuildReaction() {
		return olympaGuildId != 0;
	}

	public long getOlympaGuildId() {
		return olympaGuildId;
	}

	public abstract void onBotStop(long messageId);

	public abstract boolean onReactAdd(Message message, MessageChannel messageChannel, User user, MessageReaction messageReaction, String data);

	public abstract void onReactModClearAll(long messageId, MessageChannel messageChannel);

	public abstract void onReactModDeleteOne(long messageId, MessageChannel messageChannel);

	public abstract void onReactRemove(Message message, MessageChannel channel, User user, MessageReaction reaction, String data);

	//	public void setMessageId(long messageId) {
	//		this.messageId = messageId;
	//	}

	//	public void setOlympaGuildId(long olympaGuildId) {
	//		this.olympaGuildId = olympaGuildId;
	//	}

	public void createObject(ResultSet resultSet) throws JsonSyntaxException, SQLException {
		reactionsEmojis = new Gson().fromJson(resultSet.getString("emojis"), new TypeToken<LinkedMap<String, String>>() {}.getType());
		String allowed = resultSet.getString("allowed_users_ids");
		if (allowed != null && !allowed.isEmpty())
			canReactUserIds = new Gson().fromJson(resultSet.getString("allowed_users_ids"), new TypeToken<List<Long>>() {}.getType());
		else
			canReactUserIds = new ArrayList<>();
		canMultiple = resultSet.getInt("can_multiple") == 1;
		removeWhenModClearAll = resultSet.getInt("remove_when_modclearall") == 1;
		messageId = resultSet.getLong("message_id");
		olympaGuildId = resultSet.getLong("guild_id");
		time = resultSet.getTimestamp("date").getTime() / 1000L;
		if (resultSet.getString("data") != null)
			data = new Gson().fromJson(resultSet.getString("data"), new TypeToken<Map<Object, Object>>() {}.getType());
	}

	public boolean isRemoveWhenModClearAll() {
		return removeWhenModClearAll;
	}

	public void addToMessage(Message message) {
		AwaitReaction.reactions.put(message.getIdLong(), this);
		addReaction(message, getEmojis());
		messageId = message.getIdLong();
		this.message = message;
		if (message.isFromGuild())
			olympaGuildId = GuildHandler.getOlympaGuild(message.getGuild()).getId();
	}

	private void addReaction(Message message, List<String> emojis) {
		emojis.stream().forEach(emoji -> message.addReaction(emoji).queue());
	}

	public Boolean removeFromDB() {
		try {
			return ReactionSQL.removeReaction(this);
		} catch (SecurityException | SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void removeFromCache() {
		AwaitReaction.reactions.invalidate(messageId);
	}

	public Boolean remove(MessageChannel messageChannel) {
		removeFromCache();
		messageChannel.retrieveMessageById(messageId).queue(message -> message.clearReactions().queue(null, ErrorResponseException.ignore(ErrorResponse.MISSING_ACCESS)));
		return removeFromDB();
	}

	public void saveToDB() {
		try {
			ReactionSQL.addReaction(this);
		} catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException | SQLException e) {
			e.printStackTrace();
		}
	}
}
