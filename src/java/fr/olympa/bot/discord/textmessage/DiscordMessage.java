package fr.olympa.bot.discord.textmessage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.observer.MessageContent;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class DiscordMessage {

	final long olympaGuildId, channelId, messageId, olympaAuthorId, created;
	long logMessageId;
	List<MessageContent> contents;

	public static DiscordMessage createObject(ResultSet resultSet) throws JsonSyntaxException, SQLException {
		return new DiscordMessage(resultSet.getLong("guild_discord_id"),
				resultSet.getLong("channel_discord_id"),
				resultSet.getLong("message_discord_id"),
				resultSet.getLong("author_id"),
				new Gson().fromJson(resultSet.getString("contents"), new TypeToken<List<MessageContent>>() {
				}.getType()),
				resultSet.getTimestamp("created"),
				resultSet.getLong("log_msg_discord_id"));
	}

	public DiscordMessage(Message message) throws SQLException {
		olympaGuildId = GuildHandler.getOlympaGuild(message.getGuild()).getId();
		messageId = message.getIdLong();
		channelId = message.getChannel().getIdLong();
		olympaAuthorId = CacheDiscordSQL.getDiscordMember(message.getAuthor()).getId();
		created = message.getTimeCreated().toEpochSecond();
		contents = new ArrayList<>();
		addEditedMessage(message);
	}

	public DiscordMessage(long olympaGuildId, long channelId, long messageId, long authorId, List<MessageContent> contents, Timestamp created, long logMessageId) {
		this.olympaGuildId = olympaGuildId;
		this.channelId = channelId;
		this.messageId = messageId;
		olympaAuthorId = authorId;
		this.contents = contents;
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

	public long getOlympaAuthorId() {
		return olympaAuthorId;
	}

	public long getCreated() {
		return created;
	}

	public void addEditedMessage(Message message) {
		contents.add(new MessageContent(message, this));
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

	public Message getLogMsg() {
		return getOlympaGuild().getLogChannel().getHistory().getMessageById(logMessageId);
	}

	@Nullable
	public Message getMessage() {
		return getChannel().getHistory().getMessageById(messageId);
	}

	public MessageContent getContent() {
		return contents.get(contents.size() - 1);
	}

	public List<MessageContent> getContents() {
		return contents;
	}

	public MessageContent getOriginalContent() {
		return contents.get(0);
	}

	public void setLogMsg(Message logMsg) {
		logMessageId = logMsg.getIdLong();
	}

	public void setOriginalNotFound() {
		contents.add(0, new MessageContent(false));
	}

	public void setMessageDeleted() {
		contents.add(new MessageContent(true));
	}

	public String getJumpUrl() {
		return "https://discordapp.com/channels/" + getOlympaGuild().getDiscordId() + "/" + channelId + "/" + messageId;
	}
}