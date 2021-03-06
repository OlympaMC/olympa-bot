package fr.olympa.bot.discord.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.olympa.api.common.sql.DatabaseConnection;
import fr.olympa.api.common.sql.statement.OlympaStatement;
import fr.olympa.api.common.sql.statement.StatementType;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sanctions.DiscordSanction;

public class DiscordSQL {

	static DatabaseConnection dbConnection;
	static String tableMembers = "discord.members";
	static String tableSanction = "discord.sanctions";

	public static List<DiscordMember> debug() throws SQLException {
		OlympaStatement selectStatement = new OlympaStatement("SELECT * FROM discord.members WHERE discord_tag IS NULL");
		try (PreparedStatement statement = selectStatement.createStatement()) {
			List<DiscordMember> discordMembers = new ArrayList<>();
			ResultSet resultSet = selectStatement.executeQuery(statement);
			while (resultSet.next())
				discordMembers.add(DiscordMember.createObject(resultSet));
			resultSet.close();
			return discordMembers;
		}
	}

	private static OlympaStatement insertSanctionStatement = new OlympaStatement(StatementType.INSERT, tableSanction, "target_id", "author_id", "type", "reason", "expire");

	public static void addSanction(DiscordSanction discordSanction) throws SQLException {
		try (PreparedStatement statement = insertSanctionStatement.createStatement()) {
			int i = 1;
			statement.setLong(i++, discordSanction.getTargetOlympaDiscordId());
			statement.setLong(i++, discordSanction.getAuthorOlympaDiscordId());
			statement.setLong(i++, discordSanction.getType().getId());
			statement.setString(i++, discordSanction.getReason());
			Long expire = discordSanction.getExpire();
			if (expire != null)
				statement.setTimestamp(i, new Timestamp(expire * 1000L));
			else
				statement.setTimestamp(i, null);
			insertSanctionStatement.executeUpdate(statement);
		}
	}

	private static OlympaStatement selectSanctionStatement = new OlympaStatement(StatementType.SELECT, tableSanction, "id", null);

	public static DiscordSanction selectSanction(long id) throws SQLException {
		try (PreparedStatement statement = selectSanctionStatement.createStatement()) {
			DiscordSanction sanction = null;
			statement.setLong(1, id);
			ResultSet resultSet = selectSanctionStatement.executeQuery(statement);
			if (resultSet.next())
				sanction = DiscordSanction.createObject(resultSet);
			resultSet.close();
			return sanction;
		}
	}

	// TODO remove from here
	private static OlympaStatement insertPlayerStatement = new OlympaStatement(StatementType.INSERT, tableMembers, new String[] { "discord_id", "discord_name", "olympa_id" }).returnGeneratedKeys();

	public static DiscordMember addMember(DiscordMember discordMember) throws SQLException {
		try (PreparedStatement statement = insertPlayerStatement.createStatement()) {
			int i = 1;
			statement.setLong(i++, discordMember.getDiscordId());
			statement.setString(i++, discordMember.getName());
			if (discordMember.getOlympaId() != 0)
				statement.setLong(i, discordMember.getOlympaId());
			else
				statement.setObject(i, null);
			insertPlayerStatement.executeUpdate(statement);
			ResultSet resultSet = statement.getGeneratedKeys();
			resultSet.next();
			discordMember.setId(resultSet.getLong("id"));
			resultSet.close();
			return discordMember;
		}
	}

	private static OlympaStatement selectMemberOlympaIdStatement = new OlympaStatement(StatementType.SELECT, tableMembers, "olympa_id", null);

	public static DiscordMember selectMemberByOlympaId(long olympaId) throws SQLException {
		try (PreparedStatement statement = selectMemberOlympaIdStatement.createStatement()) {
			DiscordMember discordMember = null;
			statement.setLong(1, olympaId);
			ResultSet resultSet = selectMemberOlympaIdStatement.executeQuery(statement);
			if (resultSet.next())
				discordMember = DiscordMember.createObject(resultSet);
			resultSet.close();
			return discordMember;
		}
	}

	private static OlympaStatement selectMemberDiscordIdStatement = new OlympaStatement(StatementType.SELECT, tableMembers, "discord_id", null);

	public static DiscordMember selectMemberByDiscordId(long discordId) throws SQLException {
		try (PreparedStatement statement = selectMemberDiscordIdStatement.createStatement()) {
			DiscordMember discordMember = null;
			statement.setLong(1, discordId);
			ResultSet resultSet = selectMemberDiscordIdStatement.executeQuery(statement);
			if (resultSet.next())
				discordMember = DiscordMember.createObject(resultSet);
			resultSet.close();
			return discordMember;
		}
	}

	private static OlympaStatement selectMemberOlympaDiscordIdStatement = new OlympaStatement(StatementType.SELECT, tableMembers, "id", null);

	public static DiscordMember selectMemberByDiscordOlympaId(long discordId) throws SQLException {
		try (PreparedStatement statement = selectMemberOlympaDiscordIdStatement.createStatement()) {
			DiscordMember discordMember = null;
			statement.setLong(1, discordId);
			ResultSet resultSet = selectMemberOlympaDiscordIdStatement.executeQuery(statement);
			if (resultSet.next())
				discordMember = DiscordMember.createObject(resultSet);
			resultSet.close();
			return discordMember;
		}
	}

	//	private static OlympaStatement updateMemberStatement = new OlympaStatement(StatementType.UPDATE, tableMembers, "id",
	//			new String[] { "discord_name", "discord_tag", "olympa_id", "xp", "last_seen", "join_date", "leave_date", "old_names", "permissions" });
	//
	//	public static void updateMember(DiscordMember discordMember) throws SQLException {
	//		try (PreparedStatement statement = updateMemberStatement.createStatement()) {
	//			int i = 1;
	//			statement.setString(i++, discordMember.getName());
	//			statement.setString(i++, discordMember.getTag());
	//			if (discordMember.getOlympaId() != 0)
	//				statement.setLong(i++, discordMember.getOlympaId());
	//			else
	//				statement.setObject(i++, null);
	//			statement.setDouble(i++, discordMember.getXp());
	//			if (discordMember.getLastSeen() != -1)
	//				statement.setTimestamp(i++, new Timestamp(discordMember.getLastSeen() * 1000L));
	//			else
	//				statement.setObject(i++, null);
	//			if (discordMember.getJoinTime() != 0)
	//				statement.setDate(i++, new Date(discordMember.getJoinTime() * 1000L));
	//			else
	//				statement.setObject(i++, null);
	//			if (discordMember.getLeaveTime() != 0)
	//				statement.setDate(i++, new Date(discordMember.getLeaveTime() * 1000L));
	//			else
	//				statement.setObject(i++, null);
	//			if (!discordMember.getOldNames().isEmpty())
	//				statement.setString(i++, new Gson().toJson(discordMember.getOldNames()));
	//			else
	//				statement.setObject(i++, null);
	//			if (!discordMember.getPermissions().isEmpty())
	//				statement.setString(i++, new Gson().toJson(discordMember.getPermissions()));
	//			else
	//				statement.setObject(i++, null);
	//			statement.setLong(i, discordMember.getId());
	//			updateMemberStatement.executeUpdate(statement);
	//		}
	//	}

	private static OlympaStatement selectDiscordMembersIdsStatement = new OlympaStatement(StatementType.SELECT, tableMembers, (String[]) null, "discord_id");

	public static Set<Long> selectDiscordMembersIds() throws SQLException {
		try (PreparedStatement statement = selectDiscordMembersIdsStatement.createStatement()) {
			Set<Long> membersIds = new HashSet<>();
			ResultSet resultSet = selectDiscordMembersIdsStatement.executeQuery(statement);
			while (resultSet.next())
				membersIds.add(resultSet.getLong(1));
			resultSet.close();
			return membersIds;
		}
	}
	// TODO remove to here
	// https://stackoverflow.com/questions/14096429/how-to-delete-a-mysql-record-after-a-certain-time
}
