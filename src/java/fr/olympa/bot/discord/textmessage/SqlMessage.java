package fr.olympa.bot.discord.textmessage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.google.gson.Gson;

import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.api.sql.StatementType;

public class SqlMessage {
	static String tableMessages = "discord.messages";

	private static OlympaStatement insertMessageStatement = new OlympaStatement(StatementType.INSERT, tableMessages, "guild_discord_id", "channel_discord_id", "message_discord_id", "author_id", "contents", "created");

	public static void addMessage(DiscordMessage discordMessage) throws SQLException {
		PreparedStatement statement = insertMessageStatement.getStatement();
		int i = 1;
		statement.setLong(i++, discordMessage.getGuildId());
		statement.setLong(i++, discordMessage.getChannelId());
		statement.setLong(i++, discordMessage.getMessageId());
		statement.setLong(i++, discordMessage.getOlympaDiscordAuthorId());
		statement.setString(i++, new Gson().toJson(discordMessage.getContents()));
		statement.setTimestamp(i++, new Timestamp(discordMessage.getCreated() * 1000L));
		statement.executeUpdate();
		statement.close();
	}

	private static OlympaStatement selectMessageStatement = new OlympaStatement(StatementType.SELECT, tableMessages, new String[] { "guild_discord_id", "channel_discord_id", "message_discord_id" }, "*");

	public static DiscordMessage selectMessage(long guildId, long channelId, long messageId) throws SQLException {
		PreparedStatement statement = selectMessageStatement.getStatement();
		DiscordMessage discordMessage = null;
		int i = 1;
		statement.setLong(i++, guildId);
		statement.setLong(i++, channelId);
		statement.setLong(i++, messageId);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next())
			discordMessage = DiscordMessage.createObject(resultSet);
		resultSet.close();
		return discordMessage;
	}

	private static OlympaStatement updateMessageStatement = new OlympaStatement(StatementType.UPDATE, tableMessages, new String[] { "guild_discord_id", "channel_discord_id", "message_discord_id" }, "contents");

	public static void updateMessageContent(DiscordMessage discordMessage) throws SQLException {
		PreparedStatement statement = updateMessageStatement.getStatement();
		int i = 1;
		statement.setString(i++, new Gson().toJson(discordMessage.getContents()));
		statement.setLong(i++, discordMessage.getGuildId());
		statement.setLong(i++, discordMessage.getChannelId());
		statement.setLong(i++, discordMessage.getMessageId());
		statement.executeUpdate();
		statement.close();
	}

	private static OlympaStatement updateMessageStatement2 = new OlympaStatement(StatementType.UPDATE, tableMessages, new String[] { "guild_discord_id", "channel_discord_id", "message_discord_id" }, "log_msg_discord_id");

	public static void updateMessageLogMsgId(DiscordMessage discordMessage) throws SQLException {
		PreparedStatement statement = updateMessageStatement2.getStatement();
		int i = 1;
		statement.setLong(i++, discordMessage.getLogMessageId());
		statement.setLong(i++, discordMessage.getGuildId());
		statement.setLong(i++, discordMessage.getChannelId());
		statement.setLong(i++, discordMessage.getMessageId());
		statement.executeUpdate();
		statement.close();
	}
}
