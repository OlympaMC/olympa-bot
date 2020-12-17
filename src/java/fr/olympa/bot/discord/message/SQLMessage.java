package fr.olympa.bot.discord.message;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.google.gson.Gson;

import fr.olympa.api.sql.SQLClass;
import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.api.sql.statement.StatementType;

public class SQLMessage extends SQLClass {

	static {
		init("discord", "messages");
	}

	private static OlympaStatement insertMessageStatement = new OlympaStatement(StatementType.INSERT, table, "guild_discord_id", "channel_discord_id", "message_discord_id", "author_id", "contents", "created");

	public static void addMessage(DiscordMessage discordMessage) throws SQLException {
		PreparedStatement statement = insertMessageStatement.getStatement();
		int i = 1;
		statement.setLong(i++, discordMessage.getGuildId());
		statement.setLong(i++, discordMessage.getChannelId());
		statement.setLong(i++, discordMessage.getMessageId());
		statement.setLong(i++, discordMessage.getOlympaDiscordAuthorId());
		if (!discordMessage.isEmpty()) {
			String contents = new Gson().toJson(discordMessage.getContents());
			if (contents.length() > getMaxSizeContents())
				updateSizeContents(contents.length());
			statement.setString(i++, contents);
		} else
			statement.setObject(i++, null);
		statement.setTimestamp(i, new Timestamp(discordMessage.getCreated() * 1000L));
		insertMessageStatement.executeUpdate();
		statement.close();
	}

	private static OlympaStatement selectMessageStatement = new OlympaStatement(StatementType.SELECT, table, new String[] { "guild_discord_id", "channel_discord_id", "message_discord_id" }, "*");

	public static DiscordMessage selectMessage(long guildId, long channelId, long messageId) throws SQLException {
		PreparedStatement statement = selectMessageStatement.getStatement();
		DiscordMessage discordMessage = null;
		int i = 1;
		statement.setLong(i++, guildId);
		statement.setLong(i++, channelId);
		statement.setLong(i, messageId);
		ResultSet resultSet = selectMessageStatement.executeQuery();
		if (resultSet.next())
			discordMessage = DiscordMessage.createObject(resultSet);
		resultSet.close();
		return discordMessage;
	}

	private static OlympaStatement selectMaxSizeContentsStatement = new OlympaStatement(StatementType.SELECT, table, null, "MAX(LENGTH(contents))");
	private static long maxSizeContents = -1;

	private static long getMaxSizeContents() throws SQLException {
		if (maxSizeContents == -1) {
			ResultSet resultSet = selectMaxSizeContentsStatement.executeQuery();
			if (resultSet.next())
				maxSizeContents = resultSet.getLong(1);
			resultSet.close();
		}
		return maxSizeContents;
	}

	private static OlympaStatement updateMessageStatement = new OlympaStatement(StatementType.UPDATE, table, new String[] { "guild_discord_id", "channel_discord_id", "message_discord_id" }, "contents");

	public static void updateMessageContent(DiscordMessage discordMessage) throws SQLException {
		String contents;
		if (!discordMessage.isEmpty()) {
			contents = new Gson().toJson(discordMessage.getContents());
			if (contents.length() > getMaxSizeContents())
				updateSizeContents(contents.length());
		} else
			contents = null;
		PreparedStatement statement = updateMessageStatement.getStatement();
		int i = 1;
		statement.setString(i++, contents);
		statement.setLong(i++, discordMessage.getGuildId());
		statement.setLong(i++, discordMessage.getChannelId());
		statement.setLong(i, discordMessage.getMessageId());
		updateMessageStatement.executeUpdate();
		statement.close();
	}

	private static OlympaStatement updateMessageStatement2 = new OlympaStatement(StatementType.UPDATE, table, new String[] { "guild_discord_id", "channel_discord_id", "message_discord_id" }, "log_msg_discord_id");

	public static void updateMessageLogMsgId(DiscordMessage discordMessage) throws SQLException {
		PreparedStatement statement = updateMessageStatement2.getStatement();
		int i = 1;
		statement.setLong(i++, discordMessage.getLogMessageId());
		statement.setLong(i++, discordMessage.getGuildId());
		statement.setLong(i++, discordMessage.getChannelId());
		statement.setLong(i, discordMessage.getMessageId());
		updateMessageStatement2.executeUpdate();
		statement.close();
	}

	private static OlympaStatement updateSizeContentsStatement = new OlympaStatement("ALTER TABLE " + table + " CHANGE COLUMN `contents` `contents` VARCHAR(?);");

	private static void updateSizeContents(long size) throws SQLException {
		PreparedStatement statement = updateSizeContentsStatement.getStatement();
		int i = 1;
		statement.setLong(i, size);
		updateSizeContentsStatement.executeUpdate();
		statement.close();
	}

	private static OlympaStatement purgeStatement = new OlympaStatement("DELETE FROM " + table + " WHERE CHAR_LENGTH(contents) > 1000 OR created < now() - INTERVAL 62 DAY;");

	public static int purge() throws SQLException {
		PreparedStatement statement = purgeStatement.getStatement();
		int rows = purgeStatement.executeUpdate();
		statement.close();
		maxSizeContents = -1;
		updateSizeContents(getMaxSizeContents());
		return rows;
	}
}
