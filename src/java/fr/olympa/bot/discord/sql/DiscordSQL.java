package fr.olympa.bot.discord.sql;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.api.sql.statement.StatementType;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sanctions.DiscordSanction;

public class DiscordSQL {

	static DbConnection dbConnection;
	static String tableMembers = "discord.members";
	static String tableSanction = "discord.sanctions";

	private static OlympaStatement insertSanctionStatement = new OlympaStatement(StatementType.INSERT, tableSanction, "target_id", "author_id", "type", "reason", "expire");

	public static void addSanction(DiscordSanction discordSanction) throws SQLException {
		PreparedStatement statement = insertSanctionStatement.getStatement();
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
		statement.executeUpdate();
		statement.close();
	}

	private static OlympaStatement selectSanctionStatement = new OlympaStatement(StatementType.SELECT, tableSanction, "id", null);

	public static DiscordSanction selectSanction(long id) throws SQLException {
		PreparedStatement statement = selectSanctionStatement.getStatement();
		DiscordSanction sanction = null;
		int i = 1;
		statement.setLong(i++, id);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next())
			sanction = DiscordSanction.createObject(resultSet);
		resultSet.close();
		return sanction;
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
		statement.setLong(1, discordId);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next())
			discordMember = DiscordMember.createObject(resultSet);
		resultSet.close();
		return discordMember;
	}

	private static OlympaStatement selectMemberOlympaDiscordIdStatement = new OlympaStatement(StatementType.SELECT, tableMembers, "id", null);

	public static DiscordMember selectMemberByDiscordOlympaId(long discordId) throws SQLException {
		PreparedStatement statement = selectMemberOlympaDiscordIdStatement.getStatement();
		DiscordMember discordMember = null;
		statement.setLong(1, discordId);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next())
			discordMember = DiscordMember.createObject(resultSet);
		resultSet.close();
		return discordMember;
	}

	private static OlympaStatement updateMemberStatement = new OlympaStatement(StatementType.UPDATE, tableMembers, "id",
			new String[] { "discord_name", "discord_tag", "olympa_id", "xp", "last_seen", "join_date", "leave_date", "old_names", "permissions" });

	public static void updateMember(DiscordMember discordMember) throws SQLException {
		PreparedStatement statement = updateMemberStatement.getStatement();
		int i = 1;
		statement.setString(i++, discordMember.getName());
		statement.setString(i++, discordMember.getTag());
		if (discordMember.getOlympaId() != 0)
			statement.setLong(i++, discordMember.getOlympaId());
		else
			statement.setObject(i++, null);
		statement.setDouble(i++, discordMember.getXp());
		if (discordMember.getLastSeen() != -1)
			statement.setTimestamp(i++, new Timestamp(discordMember.getLastSeen() * 1000L));
		else
			statement.setObject(i++, null);
		if (discordMember.getJoinTime() != 0)
			statement.setDate(i++, new Date(discordMember.getJoinTime() * 1000L));
		else
			statement.setObject(i++, null);
		if (discordMember.getLeaveTime() != 0)
			statement.setDate(i++, new Date(discordMember.getLeaveTime() * 1000L));
		else
			statement.setObject(i++, null);
		if (!discordMember.getOldNames().isEmpty())
			statement.setString(i++, new Gson().toJson(discordMember.getOldNames()));
		else
			statement.setObject(i++, null);
		if (!discordMember.getPermissions().isEmpty())
			statement.setString(i++, new Gson().toJson(discordMember.getPermissions()));
		else
			statement.setObject(i++, null);
		statement.setLong(i, discordMember.getId());
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
