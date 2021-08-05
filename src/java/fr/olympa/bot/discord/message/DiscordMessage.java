package fr.olympa.bot.discord.message;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.observer.MessageContent;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;

public class DiscordMessage {

	final long olympaGuildId, channelId, messageId, olympaDiscordAuthorId, created;
	long logMessageId;
	List<MessageContent> contents;

	public static DiscordMessage createObject(ResultSet resultSet) throws JsonSyntaxException, SQLException {
		return new DiscordMessage(resultSet.getLong("guild_discord_id"),
				resultSet.getLong("channel_discord_id"),
				resultSet.getLong("message_discord_id"),
				resultSet.getLong("author_id"),
				resultSet.getString("contents"),
				resultSet.getTimestamp("created"),
				resultSet.getLong("log_msg_discord_id"));
	}

	public DiscordMessage(Message message) throws SQLException {
		olympaGuildId = GuildHandler.getOlympaGuild(message.getGuild()).getId();
		messageId = message.getIdLong();
		channelId = message.getChannel().getIdLong();
		olympaDiscordAuthorId = CacheDiscordSQL.getDiscordMember(message.getAuthor()).getId();
		created = message.getTimeCreated().toEpochSecond();
		if (DiscordUtils.isReal(message.getAuthor()))
			addEditedMessage(message);
	}

	public DiscordMessage(Message message, Map<Attachment, String> map) throws SQLException {
		olympaGuildId = GuildHandler.getOlympaGuild(message.getGuild()).getId();
		messageId = message.getIdLong();
		channelId = message.getChannel().getIdLong();
		olympaDiscordAuthorId = CacheDiscordSQL.getDiscordMember(message.getAuthor()).getId();
		created = message.getTimeCreated().toEpochSecond();
		if (DiscordUtils.isReal(message.getAuthor()))
			addEditedMessage(message, map);
	}

	public DiscordMessage(long olympaGuildId, long channelId, long messageId, long olympaDiscordAuthorId, String contents, Timestamp created, long logMessageId) {
		this.olympaGuildId = olympaGuildId;
		this.channelId = channelId;
		this.messageId = messageId;
		this.olympaDiscordAuthorId = olympaDiscordAuthorId;
		if (contents != null && !contents.isBlank())
			this.contents = new Gson().fromJson(contents, new TypeToken<List<MessageContent>>() {}.getType());
		this.created = created.getTime() / 1000L;
		this.logMessageId = logMessageId;
	}

	public long getGuildId() {
		return olympaGuildId;
	}

	public long getChannelId() {
		return channelId;
	}

	public long getMessageId() {
		return messageId;
	}

	public long getLogMessageId() {
		return logMessageId;
	}

	public String getLogJumpUrl() {
		OlympaGuild opGuild = getOlympaGuild();
		return new DiscordURL(opGuild.getDiscordId(), opGuild.getLogChannel().getIdLong(), logMessageId).getJumpLabel();
	}

	public long getOlympaDiscordAuthorId() {
		return olympaDiscordAuthorId;
	}

	public long getCreated() {
		return created;
	}

	public void addEditedMessage(Message message, Map<Attachment, String> attachments) {
		getContents().add(new MessageContent(message, this, attachments));
		while (isFullContents())
			OlympaBots.getInstance().sendMessage("[DISCORD BOT] Message edition of %s is too long for saving in db. data lost : %s", "?", new Gson().toJson(contents.remove(contents.size() / 2)));
	}

	public void addEditedMessage(Message message) {
		getContents().add(new MessageContent(message, this, Collections.emptyMap()));
		while (isFullContents())
			OlympaBots.getInstance().sendMessage("[DISCORD BOT] Message edition of %s is too long for saving in db. data lost : %s", "?", new Gson().toJson(contents.remove(contents.size() / 2)));
	}

	public OlympaGuild getOlympaGuild() {
		return GuildHandler.getOlympaGuildByOlympaId(olympaGuildId);
	}

	public Guild getGuild() {
		return getOlympaGuild().getGuild();
	}

	public TextChannel getChannel() {
		return getGuild().getTextChannelById(channelId);
	}

	public RestAction<Message> getLogMsg() {
		return getOlympaGuild().getLogChannel().retrieveMessageById(logMessageId);
	}

	@Nullable
	public Message getMessage() {
		return getChannel().getHistory().getMessageById(messageId);
	}

	public MessageContent getContent() {
		return contents != null && !contents.isEmpty() ? contents.get(contents.size() - 1) : null;
	}

	public List<MessageContent> getContents() {
		if (contents == null)
			contents = new ArrayList<>();
		return contents;
	}

	public String getContentsToJson() {
		return new Gson().toJson(getContents());
	}

	public boolean isFullContents() {
		return getContentsToJson().length() > 65_000;
	}

	public boolean isDeleted() {
		return contents != null && !contents.isEmpty() && contents.get(contents.size() - 1).isDeleted();
	}

	public boolean isEmpty() {
		return contents == null || contents.isEmpty() || contents.size() == 1 && contents.get(0).isEmpty();
	}

	public MessageContent getOriginalContent() {
		if (contents == null || contents.isEmpty())
			return null;
		return contents.get(0);
	}

	public void setLogMsg(Message logMsg) {
		logMessageId = logMsg.getIdLong();
	}

	public void setOriginalNotFound() {
		getContents().add(0, new MessageContent(false));
	}

	public void setMessageDeleted() {
		getContents().add(new MessageContent(true));
	}

	public String getJumpUrlBrut() {
		return new DiscordURL(getOlympaGuild().getDiscordId(), channelId, messageId).get();
	}

	public String getJumpUrl() {
		return new DiscordURL(getOlympaGuild().getDiscordId(), channelId, messageId).getJumpLabel();
	}
}