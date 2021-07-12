package fr.olympa.bot.discord.message;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.google.gson.Gson;

import fr.olympa.api.common.sql.SQLClass;
import fr.olympa.api.common.sql.statement.OlympaStatement;
import fr.olympa.api.common.sql.statement.StatementType;

public class SQLMessage extends SQLClass {

	static {
		init("discord", "messages");
	}

	private static OlympaStatement insertMessageStatement = new OlympaStatement(StatementType.INSERT, table, "guild_discord_id", "channel_discord_id", "message_discord_id", "author_id", "contents", "created");

	public static void addMessage(DiscordMessage discordMessage) throws SQLException {
		try (PreparedStatement statement = insertMessageStatement.createStatement()) {
			int i = 1;
			statement.setLong(i++, discordMessage.getGuildId());
			statement.setLong(i++, discordMessage.getChannelId());
			statement.setLong(i++, discordMessage.getMessageId());
			statement.setLong(i++, discordMessage.getOlympaDiscordAuthorId());
			if (!discordMessage.isEmpty()) {
				String contents = new Gson().toJson(discordMessage.getContents());
				//			if (contents.length() > getMaxSizeContents())
				//				updateSizeContents(contents.length());
				statement.setString(i++, contents);
			} else
				statement.setObject(i++, null);
			statement.setTimestamp(i, new Timestamp(discordMessage.getCreated() * 1000L));
			insertMessageStatement.executeUpdate(statement);
		}
	}

	private static OlympaStatement selectMessageStatement = new OlympaStatement(StatementType.SELECT, table, new String[] { "guild_discord_id", "channel_discord_id", "message_discord_id" }, "*");

	public static DiscordMessage selectMessage(long guildId, long channelId, long messageId) throws SQLException {
		try (PreparedStatement statement = selectMessageStatement.createStatement()) {
			DiscordMessage discordMessage = null;
			int i = 1;
			statement.setLong(i++, guildId);
			statement.setLong(i++, channelId);
			statement.setLong(i, messageId);
			ResultSet resultSet = selectMessageStatement.executeQuery(statement);
			if (resultSet.next())
				discordMessage = DiscordMessage.createObject(resultSet);
			resultSet.close();
			return discordMessage;
		}
	}

	private static OlympaStatement updateMessageStatement = new OlympaStatement(StatementType.UPDATE, table, new String[] { "guild_discord_id", "channel_discord_id", "message_discord_id" }, "contents");

	public static void updateMessageContent(DiscordMessage discordMessage) throws SQLException {
		String contents;
		if (!discordMessage.isEmpty())
			contents = discordMessage.getContentsToJson();
		else
			contents = null;
		try (PreparedStatement statement = updateMessageStatement.createStatement()) {
			int i = 1;
			statement.setString(i++, contents);
			statement.setLong(i++, discordMessage.getGuildId());
			statement.setLong(i++, discordMessage.getChannelId());
			statement.setLong(i, discordMessage.getMessageId());
			updateMessageStatement.executeUpdate(statement);
		}
	}

	private static OlympaStatement updateMessageStatement2 = new OlympaStatement(StatementType.UPDATE, table, new String[] { "guild_discord_id", "channel_discord_id", "message_discord_id" }, "log_msg_discord_id");

	public static void updateMessageLogMsgId(DiscordMessage discordMessage) throws SQLException {
		try (PreparedStatement statement = updateMessageStatement2.createStatement()) {
			int i = 1;
			statement.setLong(i++, discordMessage.getLogMessageId());
			statement.setLong(i++, discordMessage.getGuildId());
			statement.setLong(i++, discordMessage.getChannelId());
			statement.setLong(i, discordMessage.getMessageId());
			updateMessageStatement2.executeUpdate(statement);
		}
	}

	private static OlympaStatement purgeStatement = new OlympaStatement("DELETE FROM " + table + " WHERE created < now() - INTERVAL 62 DAY;");

	public static int purge() throws SQLException {
		try (PreparedStatement statement = purgeStatement.createStatement()) {
			return purgeStatement.executeUpdate(statement);
		}
	}
}
