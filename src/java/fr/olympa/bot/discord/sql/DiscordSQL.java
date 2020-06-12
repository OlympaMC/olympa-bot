package fr.olympa.bot.discord.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.api.sql.OlympaStatement.StatementType;
import fr.olympa.api.utils.Utils;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.reaction.ReactionDiscord;
import fr.olympa.bot.discord.textmessage.DiscordMessage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

public class DiscordSQL {
	
	static DbConnection dbConnection;
	static String tableGuild = "discord.guilds";
	static String tableMembers = "discord.members";
	static String tableMessages = "discord.messages";
	static String tableReaction = "discord.reactions";

	private static OlympaStatement insertReactionStatement = new OlympaStatement(StatementType.INSERT, tableReaction, new String[] { "message_id", "allowed_users_ids", "data", "can_multiple", "guild_id" });

	public static OlympaGuild addReaction(ReactionDiscord reaction) throws SQLException {
		PreparedStatement statement = insertReactionStatement.getStatement();
		OlympaGuild olympaGuild = null;
		int i = 1;
		statement.setLong(i++, reaction.getMessageId());
		statement.setString(i++, new Gson().toJson(reaction.getCanReactUserIds()));
		statement.setString(i++, new Gson().toJson(reaction.getDatas()));
		statement.setInt(i++, reaction.canMultiple() ? 1 : 0);
		statement.setLong(i++, reaction.getOlympaGuildId());
		statement.executeUpdate();
		ResultSet resultSet = statement.getGeneratedKeys();
		resultSet.next();
		olympaGuild = OlympaGuild.createObject(resultSet);
		resultSet.close();
		statement.close();
		return olympaGuild;
	}

	private static OlympaStatement selectReactionStatement = new OlympaStatement(StatementType.SELECT, tableReaction, "message_id", null);
	
	public static ReactionDiscord selectReaction(long messageId) throws SQLException {
		PreparedStatement statement = selectReactionStatement.getStatement();
		ReactionDiscord reaction = null;
		int i = 1;
		statement.setLong(i++, messageId);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next())
			reaction = new ReactionDiscord(resultSet.getString("data"), resultSet.getString("allowed_users_ids"), resultSet.getInt("can_multiple") == 1, resultSet.getLong("message_id"), resultSet.getLong("guild_id")) {
				@Override
				public void onReactRemove(long messageId, MessageChannel messageChannel, MessageReaction messageReaction, User user) {
				}

				@Override
				public void onReactModDeleteOne(long messageId, MessageChannel messageChannel) {
				}

				@Override
				public void onReactModClearAll(long messageId, MessageChannel messageChannel) {
				}

				@Override
				public boolean onReactAdd(long messageId, MessageChannel messageChannel, MessageReaction messageReaction, User user) {
					return false;
				}

				@Override
				public void onBotStop(long messageId) {
				}
			};
		resultSet.close();
		return reaction;
	}
	
	private static OlympaStatement insertGuildStatement = new OlympaStatement(StatementType.INSERT, tableGuild, new String[] { "guild_id", "guild_name" });

	public static OlympaGuild addGuild(Guild guild) throws SQLException {
		PreparedStatement statement = insertGuildStatement.getStatement();
		OlympaGuild olympaGuild = null;
		int i = 1;
		statement.setLong(i++, guild.getIdLong());
		statement.setString(i++, guild.getName());
		statement.executeUpdate();
		ResultSet resultSet = statement.getGeneratedKeys();
		resultSet.next();
		olympaGuild = OlympaGuild.createObject(resultSet);
		resultSet.close();
		statement.close();
		return olympaGuild;
	}
	
	private static OlympaStatement selectGuildIdStatement = new OlympaStatement(StatementType.SELECT, tableGuild, "id", null);
	
	public static OlympaGuild selectGuildById(long id) throws SQLException {
		PreparedStatement statement = selectGuildIdStatement.getStatement();
		OlympaGuild olympaGuild = null;
		int i = 1;
		statement.setLong(i++, id);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next())
			olympaGuild = OlympaGuild.createObject(resultSet);
		resultSet.close();
		return olympaGuild;
	}

	private static OlympaStatement selectGuildsIdStatement = new OlympaStatement(StatementType.SELECT, tableGuild, (String[]) null, (String[]) null);
	
	public static List<OlympaGuild> selectGuilds() throws SQLException {
		PreparedStatement statement = selectGuildsIdStatement.getStatement();
		List<OlympaGuild> olympaGuilds = new ArrayList<>();
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next())
			olympaGuilds.add(OlympaGuild.createObject(resultSet));
		resultSet.close();
		return olympaGuilds;
	}
	
	private static OlympaStatement updateGuildStatement = new OlympaStatement(StatementType.UPDATE, tableGuild, "id", new String[] {
			"guild_name", "log_voice", "log_msg", "log_username", "log_attachment", "log_roles", "log_entries", "log_channel_id", "exclude_channels_ids", "guild_type" });
	
	public static void updateGuild(OlympaGuild olympaGuild) throws SQLException {
		PreparedStatement statement = updateGuildStatement.getStatement();
		int i = 1;
		statement.setString(i++, olympaGuild.getName());
		statement.setLong(i++, Utils.booleanToBinary(olympaGuild.isLogVoice()));
		statement.setLong(i++, Utils.booleanToBinary(olympaGuild.isLogMsg()));
		statement.setLong(i++, Utils.booleanToBinary(olympaGuild.isLogUsername()));
		statement.setLong(i++, Utils.booleanToBinary(olympaGuild.isLogAttachment()));
		statement.setLong(i++, Utils.booleanToBinary(olympaGuild.isLogRoles()));
		statement.setLong(i++, Utils.booleanToBinary(olympaGuild.isLogEntries()));
		if (olympaGuild.getLogChannelId() != 0)
			statement.setLong(i++, olympaGuild.getLogChannelId());
		else
			statement.setObject(i++, null);
		if (!olympaGuild.getExcludeChannelsIds().isEmpty())
			statement.setString(i++, new Gson().toJson(olympaGuild.getExcludeChannelsIds()));
		else
			statement.setObject(i++, null);
		statement.setInt(i++, olympaGuild.getType().ordinal());
		statement.setLong(i++, olympaGuild.getId());
		statement.executeUpdate();
		statement.close();
	}

	private static OlympaStatement insertPlayerStatement = new OlympaStatement(StatementType.INSERT, tableMembers, new String[] { "discord_id", "discord_name", "olympa_id" });
	
	public static DiscordMember addMember(DiscordMember discordMember) throws SQLException {
		PreparedStatement statement = insertPlayerStatement.getStatement();
		int i = 1;
		statement.setLong(i++, discordMember.getDiscordId());
		statement.setString(i++, discordMember.getName());
		if (discordMember.getOlympaId() != 0)
			statement.setLong(i++, discordMember.getOlympaId());
		else
			statement.setObject(i++, null);
		statement.executeUpdate();
		ResultSet resultSet = statement.getGeneratedKeys();
		resultSet.next();
		discordMember.setId(resultSet.getLong("id"));
		resultSet.close();
		statement.close();
		return discordMember;
	}
	
	private static OlympaStatement selectMemberOlympaIdStatement = new OlympaStatement(StatementType.SELECT, tableMembers, "olympa_id", null);
	
	public static DiscordMember selectMemberByOlympaId(long olympaId) throws SQLException {
		PreparedStatement statement = selectMemberOlympaIdStatement.getStatement();
		DiscordMember discordMember = null;
		int i = 1;
		statement.setLong(i++, olympaId);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next())
			discordMember = DiscordMember.createObject(resultSet);
		resultSet.close();
		return discordMember;
	}
	
	private static OlympaStatement selectMemberDiscordIdStatement = new OlympaStatement(StatementType.SELECT, tableMembers, "discord_id", null);
	
	public static DiscordMember selectMemberByDiscordId(long discordId) throws SQLException {
		PreparedStatement statement = selectMemberDiscordIdStatement.getStatement();
		DiscordMember discordMember = null;
		int i = 1;
		statement.setLong(i++, discordId);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next())
			discordMember = DiscordMember.createObject(resultSet);
		resultSet.close();
		return discordMember;
	}
	
	private static OlympaStatement updateMemberStatement = new OlympaStatement(StatementType.UPDATE, tableMembers, "id", new String[] { "discord_name", "olympa_id", "xp", "last_seen" });
	
	public static void updateMember(DiscordMember discordMember) throws SQLException {
		PreparedStatement statement = updateMemberStatement.getStatement();
		int i = 1;
		statement.setString(i++, discordMember.getName());
		if (discordMember.getOlympaId() != 0)
			statement.setLong(i++, discordMember.getOlympaId());
		else
			statement.setObject(i++, null);
		statement.setDouble(i++, discordMember.getXp());
		if (discordMember.getLastSeen() != 0)
			statement.setTimestamp(i++, new Timestamp(discordMember.getLastSeen() * 1000L));
		else
			statement.setObject(i++, null);
		statement.setLong(i++, discordMember.getId());
		statement.executeUpdate();
		statement.close();
	}
	
	private static OlympaStatement insertMessageStatement = new OlympaStatement(StatementType.INSERT, tableMessages, "guild_discord_id", "channel_discord_id", "message_discord_id", "author_id", "contents", "created");
	
	public static void addMessage(DiscordMessage discordMessage) throws SQLException {
		PreparedStatement statement = insertMessageStatement.getStatement();
		int i = 1;
		statement.setLong(i++, discordMessage.getGuildId());
		statement.setLong(i++, discordMessage.getChannelId());
		statement.setLong(i++, discordMessage.getMessageId());
		statement.setLong(i++, discordMessage.getOlympaAuthorId());
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
	
	public static void updateMessage(DiscordMessage discordMessage) throws SQLException {
		PreparedStatement statement = updateMessageStatement.getStatement();
		int i = 1;
		statement.setString(i++, new Gson().toJson(discordMessage.getContents()));
		statement.setLong(i++, discordMessage.getGuildId());
		statement.setLong(i++, discordMessage.getChannelId());
		statement.setLong(i++, discordMessage.getMessageId());
		if (discordMessage.getLogMessageId() != 0)
			statement.setLong(i++, discordMessage.getLogMessageId());
		else
			statement.setObject(i++, null);
		statement.executeUpdate();
		statement.close();
	}
	
	private static OlympaStatement selectDiscordMembersIdsStatement = new OlympaStatement(StatementType.SELECT, tableMembers, (String[]) null, "discord_id");
	
	public static List<Long> selectDiscordMembersIds() throws SQLException {
		PreparedStatement statement = selectDiscordMembersIdsStatement.getStatement();
		List<Long> membersIds = new ArrayList<>();
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next())
			membersIds.add(resultSet.getLong(1));
		resultSet.close();
		return membersIds;
	}

	// https://stackoverflow.com/questions/14096429/how-to-delete-a-mysql-record-after-a-certain-time
}
