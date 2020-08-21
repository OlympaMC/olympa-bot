package fr.olympa.bot.discord.reaction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import fr.olympa.bot.discord.sql.DiscordSQL;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

public abstract class ReactionDiscord {

	private Map<String, String> datas;

	private List<Long> canReactUserIds;

	private boolean canMultiple = false;
	private long messageId, olympaGuildId;

	public ReactionDiscord() {
	}

	public ReactionDiscord(Map<String, String> datas, long messageId, long olympaGuildId, long... canReactUserIds) {
		this.datas = datas;
		this.messageId = messageId;
		this.olympaGuildId = olympaGuildId;
		this.canReactUserIds = Arrays.stream(canReactUserIds).boxed().collect(Collectors.toList());
		try {
			DiscordSQL.addReaction(this);
		} catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException | SQLException e) {
			System.out.println("DEBUG pour fix les réactions insertDB: ");
			e.printStackTrace();
		}
	}

	public boolean canInteract(User user) {
		if (canReactUserIds.isEmpty())
			return true;
		return canReactUserIds.stream().anyMatch(id -> user.getIdLong() == id);
	}

	public boolean canMultiple() {
		return canMultiple;
	}

	public List<Long> getCanReactUserIds() {
		return canReactUserIds;
	}

	public String getData(MessageReaction messageReaction) {
		String emoji = messageReaction.getReactionEmote().getEmoji();
		if (emoji != null)
			return datas.get(emoji);
		return null;
	}

	public Map<String, String> getDatas() {
		return datas;
	}

	public Set<String> getEmojis() {
		return datas.keySet();
	}

	public long getMessageId() {
		return messageId;
	}

	public long getOlympaGuildId() {
		return olympaGuildId;
	}

	public abstract void onBotStop(long messageId);

	public abstract boolean onReactAdd(long messageId, MessageChannel messageChannel, User user, MessageReaction messageReaction, String data);

	public abstract void onReactModClearAll(long messageId, MessageChannel messageChannel);

	public abstract void onReactModDeleteOne(long messageId, MessageChannel messageChannel);

	public abstract void onReactRemove(long messageId, MessageChannel messageChannel, MessageReaction messageReaction, User user);

	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}

	public void setOlympaGuildId(long olympaGuildId) {
		this.olympaGuildId = olympaGuildId;
	}

	public void createObject(ResultSet resultSet) throws JsonSyntaxException, SQLException {
		datas = new Gson().fromJson(resultSet.getString("data"), new TypeToken<Map<String, String>>() {
		}.getType());
		canReactUserIds = new Gson().fromJson(resultSet.getString("allowed_users_ids"), new TypeToken<List<Long>>() {
		}.getType());
		canMultiple = resultSet.getInt("can_multiple") == 1;
		messageId = resultSet.getLong("message_id");
		olympaGuildId = resultSet.getInt("guild_id");
	}
}
